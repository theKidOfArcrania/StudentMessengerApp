package messenger;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

//Chat Room header:
//0-7: mod index. (MUST acquire lock to modify)
//8-11: next user index (MUST acquire lock to modify)
//12-19: the salt used for the password.
//20-51: the hash-code for the password + salt + pepper
public class ChatRoom implements Closeable {

	public enum State {
		Typing(Message.FLAG_STOP_TYPING), Activity(Message.FLAG_INACTIVE);
		final byte flagStart;

		State(byte flagStart) {
			this.flagStart = flagStart;
		}

	}

	private static final Object LOCK = new Object();

	private static final int HEADER_MOD_LEN = 8;
	private static final int HEADER_USER_LEN = 4;
	private static final int HEADER_SALT_LEN = 8;
	private static final int HEADER_HASH_LEN = 32;
	private static final int HEADER_LEN = HEADER_MOD_LEN + HEADER_USER_LEN + HEADER_SALT_LEN + HEADER_HASH_LEN; // byte-size of header.
	private static final int BUFFER_ALLOC = 1024; // 1K data load
	private static final char[] PUBLIC_LOCK = "THIS is A LONG PASSWORD THAT HAS DIFFERENT CHARACTERS: !@#$%^&*()_+|qwertyuioipadsfghkl;'vzcxvnm.fajkdei31874654681+".toCharArray();

	public static byte[] generateChatKeyCode(MessageDigest digest, char[] passwordLock) {
		byte[] keyCode = new byte[16];
		digest.update(charToByte(PUBLIC_LOCK));
		digest.update(passwordLock == null ? new byte[0] : charToByte(passwordLock));
		System.arraycopy(digest.digest(), 0, keyCode, 0, keyCode.length);
		return keyCode;
	}

	public static boolean verifyPassword(FileChannel channel, ByteBuffer buffer, char[] passwordLock) throws IOException {

		try {
			// Initialize digest
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			// Generate key code
			byte[] keyCode = generateChatKeyCode(digest, passwordLock);

			// verify password, with the pepper added
			byte[] salt = new byte[HEADER_SALT_LEN];
			byte[] checkSum = new byte[HEADER_HASH_LEN];

			channel.position(HEADER_MOD_LEN + HEADER_USER_LEN);
			buffer.rewind().limit(HEADER_SALT_LEN + HEADER_HASH_LEN);
			readFully(channel, buffer, HEADER_SALT_LEN + HEADER_HASH_LEN, true);

			buffer.get(salt).get(checkSum);

			for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
				digest.reset();
				digest.update(keyCode);
				digest.update(salt);
				digest.update((byte) i); // add pepper

				byte[] verifyPass = digest.digest();

				if (verifyPass.length != checkSum.length) {
					int lengthVerify = Math.min(verifyPass.length, checkSum.length);
					System.err.println("Unexpected verify-check-sum length: " + checkSum.length);
					for (int j = 0; j < lengthVerify; j++) {
						if (verifyPass[j] != checkSum[j]) {
							return false;
						}
					}
					return true;
				} else if (Arrays.equals(verifyPass, checkSum)) {
					return true;
				}
			}
			return false;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new IOException("Unable to initialize cipher.");
		}
	}

	private static byte[] charToByte(char[] in) {
		byte[] out = new byte[in.length];

		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) (in[i] ^ in[i] >> 8 ^ in[i] >> 4 ^ in[i] >> 12);
		}

		return out;
	}

	private static ByteBuffer ensureCap(ByteBuffer buffer, int minCap) {
		// overflow-conscious code
		if (minCap < 0) {
			return buffer;
		}
		if (minCap - buffer.capacity() > 0) {
			return grow(buffer, minCap);
		}

		return buffer;
	}

	private static ByteBuffer grow(ByteBuffer buffer, int minCap) {
		int cap;

		// overflow-conscious code.
		int oldCap = buffer.capacity();
		cap = oldCap + (oldCap >> 1);

		if (minCap - cap > 0) {
			cap = minCap;
		}

		return ByteBuffer.allocateDirect(cap);
	}

	private static void readFully(FileChannel channel, ByteBuffer small, int bytes, boolean doLock) throws IOException {
		synchronized (LOCK) {
			ByteBuffer buffer = ensureCap(small, bytes);
			buffer.rewind().limit(bytes);
			FileLock lock = null;
			try {
				if (doLock) {
					lock = channel.lock(channel.position(), bytes, true);
				}
				int bytesRead = channel.read(buffer);
				if (bytes != bytesRead) {
					throw new EOFException("End of file unexpected.");
				}
			} finally {
				if (lock != null && lock.isValid()) {
					lock.close();
				}
			}
			buffer.flip();
		}
	}

	private boolean connected = false;

	private boolean closed = false;
	private long lastReadIndex = 0;
	private long mod = 0;
	private final Path pathChat;

	private final int userID;
	private final HashMap<Integer, UserStats> userProfiles = new HashMap<>();
	private final String chatName;

	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_ALLOC);
	private final FileChannel channel;

	private final ArrayList<Message> cache = new ArrayList<>();

	private final Cipher coder;

	private final SecretKey chatKey;

	/**
	 * This contructor is called internally by ChatList. DO NOT use this constructor.
	 *
	 * @throws PasswordInvalidException if the password lock is invalid.
	 */
	@SuppressWarnings({ "unused", "javadoc" })
	ChatRoom(Path pathChat, String chatName, char[] passwordLock) throws IOException, PasswordInvalidException {
		this.chatName = chatName;
		this.pathChat = pathChat;

		MessageDigest digest;
		byte[] keyCode;

		try {
			// Initialize coder cipher.
			coder = Cipher.getInstance("AES/CBC/PKCS5Padding");
			digest = MessageDigest.getInstance("SHA-256");
			keyCode = generateChatKeyCode(digest, passwordLock);
			chatKey = new SecretKeySpec(keyCode, "AES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
			throw new IOException("Unable to initialize cipher.");
		}

		channel = FileChannel.open(pathChat, READ, WRITE, CREATE);
		Files.setAttribute(pathChat, "dos:hidden", true);
		channel.position(0);

		// Initialize buffer array.
		buffer = ByteBuffer.allocateDirect(BUFFER_ALLOC);

		// If this file was newly created, initialize the header.
		if (channel.size() == 0) {
			// Initialize salt and pepper for password
			SecureRandom srandom;
			try {
				srandom = SecureRandom.getInstanceStrong();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				throw new IOException("Unable to create chat file");
			}
			byte[] salt = new byte[HEADER_SALT_LEN];
			byte pepper = (byte) srandom.nextInt();
			srandom.nextBytes(salt);

			buffer.limit(HEADER_LEN);
			buffer.position(HEADER_MOD_LEN + HEADER_USER_LEN);

			// Compute a digest of the password pepper and salt.
			digest.reset();
			digest.update(keyCode);
			digest.update(salt);
			digest.update(pepper);
			byte[] checkSum = digest.digest();
			// Add salt and password check-sum into the channel.
			buffer.put(salt);

			if (checkSum.length == HEADER_HASH_LEN) {
				buffer.put(checkSum);
			} else {
				System.err.println("Unexpected check-sum length: " + checkSum.length);
				buffer.put(Arrays.copyOf(checkSum, HEADER_HASH_LEN));
			}

			// Initialize the header.
			buffer.flip();
			channel.write(buffer);
		} else {
			boolean verified = verifyPassword(channel, buffer, passwordLock);
			if (!verified) {
				throw new PasswordInvalidException();
			}
		}

		synchronized (LOCK) { // prevent any multiple or overlapping locks within the VM.
			try (FileLock lock = channel.lock(HEADER_MOD_LEN, HEADER_USER_LEN, false)) {
				// get the next user profile id and then increment the next user profile id.
				channel.position(HEADER_MOD_LEN);
				readFully(HEADER_USER_LEN, false);
				userID = buffer.getInt(0);
				buffer.putInt(userID + 1);
				buffer.flip();

				channel.position(HEADER_MOD_LEN);
				channel.write(buffer);
				userProfiles.put(userID, new UserStats("User-" + userID));

			}
		}

		lastReadIndex = HEADER_LEN;
	}

	/**
	 * Sets the state of the current user
	 *
	 * @param state the state flag to send
	 * @param on whether if this state is on or off.
	 * @throws IOException if an error occurs while sending the message.
	 */
	public void changeState(State state, boolean on) throws IOException {
		sendMessage(new Message((byte) (state.flagStart + (on ? 1 : 0)), userID, null));
	}

	/**
	 * This sends the leave message and also closes the file channel. Note that once this is called, the user cannot reconnect.
	 *
	 * @see #disconnect()
	 * @throws IOException if an error occurs while closing the chat.
	 */
	@Override
	public void close() throws IOException {
		if (closed) {
			return;
		}
		disconnect();
		channel.close();
		closed = true;
	}

	/**
	 * Joins the chatroom using the last user-name used (or the default user-name). If a user already joined the chat, this has no effect.
	 *
	 * @throws IOException if an error occurred when joining
	 */
	public void connect() throws IOException {
		connect(null);
		// Change
	}

	/**
	 * Joins the chatroom with a user name. If a user already joined the chat, this has no effect.
	 *
	 * @param name name of the user.
	 * @throws IOException if an error occurred when joining
	 */
	public void connect(String name) throws IOException {
		if (!connected) {
			sendMessage(new Message(Message.FLAG_CONNECT, userID, name.getBytes("utf-8")));
			sendMessage(new Message(Message.FLAG_ALIAS, userID, System.getProperty("user.name").getBytes("utf-8")));
			connected = true;
			userProfiles.get(userID).setUserName(name);

			if (!cache.isEmpty()) {
				for (Message msg : cache) {
					sendMessage(msg);
				}
				cache.clear();
			}
		}
	}

	/**
	 * Disconnects the user from the chatroom. This is not the same as closing the chatroom, but if any further messages are sent, they will be not be sent until the user reconnects.
	 *
	 * @throws IOException if an error occurs while sending the leave message.
	 */
	public void disconnect() throws IOException {
		if (connected) {
			sendMessage(new Message(Message.FLAG_DISCONNECT, userID, null));
			connected = false;
		}
	}

	/**
	 * Obtains the name of this chat.
	 *
	 * @return name for this chatroom.
	 */
	public String getChatName() {
		return chatName;
	}

	/**
	 * @return the path of this chatroom.
	 */
	public Path getPathChat() {
		return pathChat;
	}

	/**
	 * @return the profile user ID
	 */
	public int getUserID() {
		return userID;
	}

	/**
	 * Retrieves the current user name.
	 *
	 * @return the current user name.
	 */
	public String getUserName() {
		return userProfiles.get(userID).getUserName();
	}

	public String getUserName(int sender) {
		if (userProfiles.get(sender) == null) {
			return "User" + sender;
		}
		return userProfiles.get(sender).getUserName();
	}

	/**
	 * @return whether if this chatroom is closed.
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @return whether if the user is connected to the chatroom
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Posts a chat message.
	 *
	 * @param msg the message body.
	 * @throws IOException if an error occurs while sending the message.
	 */
	public void post(String msg) throws IOException {
		sendMessage(new Message(Message.FLAG_POST, userID, msg.getBytes("utf-8")));
	}

	/**
	 * This method checks for any new messages
	 *
	 * @return the number of new messages
	 * @throws IOException if an i/o exception occurs or if decoding was unsuccessful.
	 */
	public synchronized Message[] updateMessage() throws IOException {
		if (closed) {
			throw new IOException("Chatroom already closed.");
		}
		channel.position(0);

		// checks the modifications of this file.
		readFully(8, true);
		long curMod = buffer.getLong();

		if (curMod < mod) {
			// Warning: someone has been tampering with the mod ids.
			mod = curMod;
		}

		Message[] msgs = new Message[(int) (curMod - mod)];

		channel.position(lastReadIndex);
		for (int i = 0; curMod > mod; mod++, i++) {
			// Read message length
			readFully(4, true); // read a four byte block.

			// Read message body.
			int msgLength = buffer.getInt();
			ensureCap(msgLength);
			readFully(msgLength, true); // read the msg block

			// Decrypt the message.
			// TO DO: remove byte arrays.
			byte[] encryptedBytes = new byte[msgLength];
			buffer.get(encryptedBytes);
			byte[] msgBytes = decrypt(encryptedBytes);
			ensureCap(msgBytes.length);
			((ByteBuffer) buffer.rewind().limit(msgBytes.length)).put(msgBytes).flip();

			// Parses and handles the message.
			Message msg = Message.parse(buffer);
			UserStats profile = userProfiles.get(msg.getSender());
			msgs[i] = msg;

			// Check modification id
			if (msg.getMod() != mod) {
				System.err.println("Error in misaligned mod: " + msg.getMod() + " " + mod + ". Realigning");
				mod = msg.getMod();
			}

			if (profile == null) {
				if (msg.getFlag() == Message.FLAG_CONNECT) {
					String userName = new String(msg.getBody());
					userProfiles.put(msg.getSender(), new UserStats(userName.isEmpty() ? "User-" + userID : userName));
				} else {
					// Invalid Message.
					msgs[i] = new Message(Message.FLAG_INVALID, msg.getSender(), null);
				}
				lastReadIndex = channel.position();
				continue;
			}
			switch (msg.getFlag()) {
			case Message.FLAG_ACTIVE:
				profile.setActive(true);
				profile.setLastActive(msg.getTimeStamp());
				break;
			case Message.FLAG_INACTIVE:
				profile.setActive(false);
				profile.setTyping(false);
				profile.setLastActive(msg.getTimeStamp());
				break;
			case Message.FLAG_CONNECT:
				profile.setConnected(true);
				if (msg.getBody().length != 0) {
					profile.setUserName(new String(msg.getBody()));
				}
				break;
			case Message.FLAG_DISCONNECT:
				profile.setConnected(false);
				profile.setActive(false);
				profile.setTyping(false);
				break;
			case Message.FLAG_TYPING:
				profile.setTyping(true); //There are problems in this area. The program often displays that a user is typing even if no one is in the chat room.
				break;
			case Message.FLAG_STOP_TYPING:
			case Message.FLAG_POST:
				profile.setTyping(false);
				break;
			default:
			}
			lastReadIndex = channel.position();
		}
		return msgs;
	}

	private byte[] decrypt(byte[] msg) throws IOException {
		try {
			// Initialize iv
			int ivSize = coder.getBlockSize();
			byte[] iv = new byte[ivSize];
			System.arraycopy(msg, 0, iv, 0, ivSize);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			coder.init(Cipher.DECRYPT_MODE, chatKey, ivSpec);

			// Decrypt
			return coder.doFinal(msg, ivSize, msg.length - ivSize);

		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			throw new IOException("Cannot decrypt message");
		}
	}

	private byte[] encrypt(byte[] msg) throws IOException {
		try {
			coder.init(Cipher.ENCRYPT_MODE, chatKey);
			byte[] iv = coder.getIV();
			byte[] encrypted = coder.doFinal(msg);
			byte[] combined = new byte[encrypted.length + iv.length];

			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
			return combined;
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new IOException("Cannot encrypt message");
		}
	}

	private void ensureCap(int minCap) {
		buffer = ensureCap(buffer, minCap);
	}

	private void readFully(int bytes, boolean doLock) throws IOException {
		readFully(channel, buffer, bytes, doLock);
	}

	private synchronized void sendMessage(Message msg) throws IOException {
		if (closed) {
			throw new IOException("Chatroom already closed.");
		}
		if (!connected) { // if the user is currently not connected, save it in the cache and send it later.
			cache.add(msg);
			return;
		}
		synchronized (LOCK) {
			int encodedLength = msg.getEncodedLength();
			ensureCap(encodedLength);

			FileLock lock = null;
			try {
				lock = channel.lock(0, HEADER_MOD_LEN, false);
				// Read modification index
				channel.position(0);
				readFully(HEADER_MOD_LEN, false);
				long curMod = buffer.getLong();
				msg.setMod(curMod);

				// TO DO: remove byte arrays.
				// Encrypt message
				buffer.rewind().limit(encodedLength);
				msg.encoded(buffer);
				buffer.flip();
				byte[] msgBytes = new byte[buffer.remaining()];
				buffer.get(msgBytes);

				// Append encrypted bytes.
				channel.position(channel.size());
				byte[] encryptedBytes = encrypt(msgBytes);
				ensureCap(encryptedBytes.length);
				((ByteBuffer) buffer.rewind().limit(encryptedBytes.length + 4)).putInt(encryptedBytes.length).put(encryptedBytes).flip();
				channel.write(buffer);

				// Increment modification
				channel.position(0);
				((ByteBuffer) buffer.rewind().limit(HEADER_MOD_LEN)).putLong(0, curMod + 1);
				channel.write(buffer);
			} finally {
				if (lock != null && lock.isValid()) {
					lock.release();
				}
			}
		}
	}
}
