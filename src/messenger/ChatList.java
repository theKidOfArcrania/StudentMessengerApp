package messenger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Formatter;

import javax.swing.event.EventListenerList;

import messenger.event.ChatListModEvent;
import messenger.event.ChatListModListener;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class ChatList {

	// School root:
	private static final Path DEF_MAIN_ROOT = Paths.get("S:/Templates/Review/StudentMessengerApp2");
	// Test Root:
	// private static final Path DEF_MAIN_ROOT = Paths.get("C:/Users/Henry/StudentMessengerApp");
	// School Test root:
	// private static final Path root = Paths.get("H:/StudentMessengerApp");

	private static Path mainRoot = DEF_MAIN_ROOT;
	private static ChatList mainChatList;

	static {
		try {
			// Attempt to load chat list...
			mainChatList = new ChatList();
		} catch (@SuppressWarnings("unused") IOException e) {
			// ...and fail silently...
			e.printStackTrace();
		}
	}

	public static ChatList getMainChatList() throws IOException {
		if (mainChatList == null) {
			mainChatList = new ChatList();
		}
		return mainChatList;
	}

	public static Path getMainRoot() {
		return mainRoot;
	}

	public static Path getUpdatePath() throws IOException {
		try {
			// See if the chat folders exist or not.
			Files.readAttributes(mainRoot.resolve("update"), BasicFileAttributes.class);
		} catch (IOException e) {
			try {
				// Doesn't exist, attempt to make directories and make the root hide.
				Files.createDirectories(mainRoot.resolve("update"));
				Files.setAttribute(mainRoot, "dos:hidden", true);
			} catch (IOException e2) {
				e2.printStackTrace();
				throw new IOException("Unable to create chatroom directory.");
			}
		}
		return mainRoot.resolve("update");
	}

	public static void setMainRoot(Path mainRoot) throws IOException {
		mainChatList = new ChatList();
		ChatList.mainRoot = mainRoot;
	}

	private static String decode(String chat) {
		int start = 0;
		int index = 0;
		StringBuilder decoded = new StringBuilder(chat.length());
		while ((index = chat.indexOf('%', start)) != -1) {
			decoded.append(chat.substring(start, index));
			decoded.append((char) Byte.parseByte(chat.substring(index + 1, index + 3), 16));
			start = index + 3;
		}
		if (start <= chat.length()) {
			decoded.append(chat.substring(start));
		}
		return decoded.toString();
	}

	private static String encode(String chat) {
		StringBuilder encoded = new StringBuilder((int) (chat.length() * 1.5));
		for (char c : chat.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)) {
				encoded.append(c);
			} else {
				@SuppressWarnings("resource")
				Formatter format = new Formatter();
				format.format("%%%2h", (int) c);
				encoded.append(format.out());
			}
		}
		return encoded.toString();
	}

	private static byte[] encodeBase64(byte[] data) {
		byte[] base64 = Base64.getEncoder().encode(data);
		for (int i = 0; i < base64.length; i++) {
			if (base64[i] == '/') {
				base64[i] = '-';
			}
		}
		return base64;
	}

	private static void initFolders(Path root) throws IOException {
		try {
			// See if the chat folders exist or not.
			Files.readAttributes(root.resolve("chats").resolve("unlist"), BasicFileAttributes.class);
		} catch (IOException e) {
			try {
				// Doesn't exist, attempt to make directories and make the root hide.
				Files.createDirectories(root.resolve("chats").resolve("unlist"));
				Files.setAttribute(root, "dos:hidden", true);
			} catch (IOException e2) {
				e2.printStackTrace();
				throw new IOException("Unable to create chatroom directory.");
			}
		}
	}

	private final WatchService wtsvPrivateChat;

	private final EventListenerList listeners = new EventListenerList();

	private final Path root;

	public ChatList() throws IOException {
		this(mainRoot);
	}

	@SuppressWarnings("unused")
	public ChatList(Path root) throws IOException {
		this.root = root;
		try {
			// See if the chat folders exist or not.
			Files.readAttributes(root.resolve("chats").resolve("unlist"), BasicFileAttributes.class);
		} catch (IOException e) {
			try {
				// Doesn't exist, attempt to make directories and make the root hide.
				Files.createDirectories(root.resolve("chats").resolve("unlist"));
				Files.setAttribute(root, "dos:hidden", true);
			} catch (IOException e2) {
				e2.printStackTrace();
				throw new IOException("Unable to create chatroom directory.");
			}
		}

		wtsvPrivateChat = root.getFileSystem().newWatchService();
		root.resolve("chats").register(wtsvPrivateChat, ENTRY_CREATE, ENTRY_DELETE, OVERFLOW);

		Thread updater = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						WatchKey events = wtsvPrivateChat.take();

						for (WatchEvent<?> event : events.pollEvents()) {
							processEvent(event);
						}
						events.reset();
					} catch (ClosedWatchServiceException e) {
						// This watch service became closed.
						e.printStackTrace();
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void addModificationListener(ChatListModListener list) {
		listeners.add(ChatListModListener.class, list);
	}

	public Path chatRoomPath(String chatName, boolean unlisted) throws IOException {
		try {
			if (unlisted) {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] digested = digest.digest(chatName.getBytes("utf-8"));

				byte[] chatID = encodeBase64(digested);
				return root.resolve("chats").resolve("unlist").resolve(new String(chatID) + ".crm");
			} else {
				return root.resolve("chats").resolve(encode(chatName) + ".crm");
			}
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new IOException("Unable to generate chat-room path.");
		}
	}

	public ArrayList<String> getPrivateChats() throws IOException {
		ArrayList<String> chats = new ArrayList<>();
		DirectoryStream.Filter<Path> filter = new Filter<Path>() {
			@Override
			public boolean accept(Path path) throws IOException {
				return path.getFileName().toString().endsWith(".crm");
			}
		};
		try (DirectoryStream<Path> chatStream = Files.newDirectoryStream(root.resolve("chats"), filter)) {
			for (Path chat : chatStream) {
				String chatName = chat.getFileName().toString();
				chatName = chatName.substring(0, chatName.length() - 4);
				chats.add(decode(chatName));
			}
		}
		return chats;
	}

	public boolean privateChatExists(String chatName) throws IOException {
		return privateChatExists(chatName, false);
	}

	@SuppressWarnings("unused")
	public boolean privateChatExists(String chatName, boolean unlisted) throws IOException {

		Path chatPath = chatRoomPath(chatName, unlisted);
		try {
			Files.readAttributes(chatPath, BasicFileAttributes.class);
		} catch (IOException e) {
			// File doesn't exist or is unable to access
			return false;
		}
		return true;
	}

	public ChatRoom privateChatRoom(String chatName, char[] passwordLock) throws IOException {
		return privateChatRoom(chatName, passwordLock);
	}

	public ChatRoom privateChatRoom(String chatName, char[] passwordLock, boolean unlisted) throws IOException, PasswordInvalidException {
		return new ChatRoom(chatRoomPath(chatName, unlisted), chatName, passwordLock);
	}

	public ChatRoom publicChatRoom() throws IOException {
		try {
			return new ChatRoom(root.resolve("Public.crm"), "<Public>", null);
		} catch (PasswordInvalidException e) {
			e.printStackTrace();
			throw new IOException("Public chat-room is password protected!!");
		}
	}

	public void removeModificationListener(ChatListModListener list) {
		listeners.remove(ChatListModListener.class, list);
	}

	private void fireAddEvent(String context) {
		ChatListModEvent evt = new ChatListModEvent(root, context);
		for (ChatListModListener list : listeners.getListeners(ChatListModListener.class)) {
			list.addChat(evt);
		}
	}

	private void fireRemoveEvent(String context) {
		ChatListModEvent evt = new ChatListModEvent(root, context);
		for (ChatListModListener list : listeners.getListeners(ChatListModListener.class)) {
			list.removeChat(evt);
		}
	}

	private void fireUpdateEvent() {
		ChatListModEvent evt = new ChatListModEvent(root, null);
		for (ChatListModListener list : listeners.getListeners(ChatListModListener.class)) {
			list.updatedList(evt);
		}
	}

	private void processEvent(WatchEvent<?> event) {
		String chatName = null;
		if (event.context() instanceof Path) {
			String fileName = ((Path) event.context()).getFileName().toString();
			if (fileName.endsWith(".crm")) {
				chatName = fileName.substring(0, fileName.length() - 4);
				chatName = decode(chatName);
			}
		}
		if (event.kind() == ENTRY_CREATE) {
			if (chatName == null) {
				return;
			}
			fireAddEvent(chatName);
		} else if (event.kind() == ENTRY_DELETE) {
			if (chatName == null) {
				return;
			}
			fireRemoveEvent(chatName);
		} else if (event.kind() == OVERFLOW) {
			fireUpdateEvent();
		}
	}
}
