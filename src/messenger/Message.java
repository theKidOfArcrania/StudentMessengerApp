package messenger;

import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Date;

//Message syntax (in bytes offset)
//offset 0-1: header
//offset 2: flags
//offset 3-6: time-stamp
//offset 7-9: user id
//offset 10: body

/**
 * This class encloses a message object, used to parse raw byte data for processing messages sent in from the chatroom file.
 *
 * @author Henry
 *
 */
public class Message {
	/**
	 * Invalid message.
	 */
	public static final byte FLAG_INVALID = -1;
	/**
	 * Flag to signify when a message is posted. Body is the message posted
	 */
	public static final byte FLAG_POST = 0;
	/**
	 * Flag to signify when a user connects to chatroom. Body is optional, identifies the name of the user.
	 */
	public static final byte FLAG_CONNECT = 1;
	/**
	 * Flag to signify when a user disconnects to chatroom. No body
	 */
	public static final byte FLAG_DISCONNECT = 2;

	/**
	 * Flag to signify when a user stops typing. This event is also inferred in a disconnect or a post-message. No body
	 */
	public static final byte FLAG_STOP_TYPING = 3;
	/**
	 * Flag to signify when a user is typing. No body
	 */
	public static final byte FLAG_TYPING = 4;
	/**
	 * Flag to signify when a user is not active.
	 */
	public static final byte FLAG_INACTIVE = 5;
	/**
	 * Flag to signify when a user is active.
	 */
	public static final byte FLAG_ACTIVE = 6;

	/**
	 * Flag to signify an alias name. Requires body.
	 */
	public static final byte FLAG_ALIAS = 7;

	private static final byte[] SIGNATURE = "MG".getBytes();
	private static final int MSG_HEADER = 23;

	private static final byte[] EMPTY = new byte[0];

	/**
	 * Parses a byte data of message into a Message object.
	 *
	 * @param bbMsg the raw byte message data in a buffer.
	 * @return the parsed message data represented by msgBytes
	 * @throws ParseException if an error occurs in parsing some bits of data.
	 */
	public static Message parse(ByteBuffer bbMsg) throws ParseException {
		byte[] sig = new byte[2];
		bbMsg.get(sig);
		if (sig[0] != SIGNATURE[0] || sig[1] != SIGNATURE[1]) {
			throw new ParseException("Invalid message heading");
		}

		if (bbMsg.limit() < MSG_HEADER - 1) {
			throw new ParseException("Invalid message heading");
		}

		long mod = bbMsg.getLong();
		byte flag = bbMsg.get();
		long timeStamp = bbMsg.getLong();
		int userID = bbMsg.getInt();
		byte[] body = new byte[bbMsg.remaining()];
		if (body.length != 0) {
			bbMsg.get(body);
		}

		return new Message(mod, flag, timeStamp, userID, body);
	}

	private long mod;
	private final byte flag;

	private final long timeStampNum;

	private final Date timeStamp;
	private final int sender;
	private final byte[] body;

	public Message(byte flag, int sender, byte[] body) {
		this(-1, flag, sender, body);
	}

	public Message(long mod, byte flag, int sender, byte[] body) {
		this.mod = mod;
		switch (flag) {
		case 0:
		case 7:
			if (body == null || body.length == 0) {
				throw new IllegalArgumentException("Message body expected.");
			}
			break;
		case -1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			if (body != null && body.length != 0) {
				throw new IllegalArgumentException("Message body is not expected.");
			}
			break;
		case 1:
			break;
		default:
			throw new IllegalArgumentException("Invalid message flag");
		}

		this.flag = flag;
		if (flag == -1) {
			this.timeStampNum = -1;
			this.timeStamp = null;
		} else {
			this.timeStampNum = System.currentTimeMillis();
			this.timeStamp = new Date(timeStampNum);
		}
		this.sender = sender;
		this.body = body == null ? EMPTY : body.clone();
	}

	private Message(long mod, byte flag, long timeStamp, int sender, byte[] body) throws ParseException {
		this.mod = mod;
		switch (flag) {
		case 0:
		case 7:
			if (body.length == 0) {
				throw new ParseException("Message body expected.");
			}
			break;
		case -1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
			if (body.length != 0) {
				throw new ParseException("Message body is not expected.");
			}
			break;
		case 1:
			break;
		default:
			throw new ParseException("Invalid message flag");
		}

		this.flag = flag;
		this.timeStampNum = timeStamp;
		this.timeStamp = new Date(timeStamp);
		this.sender = sender;
		this.body = body;
	}

	/**
	 * Encodes this Message object into a byte buffer. Note: the limit must be set so that the msg data will not overflow.
	 *
	 * @param bbMsg the byte buffer to store the data into.
	 * @see #getEncodedLength()
	 */
	public void encoded(ByteBuffer bbMsg) {
		if (getEncodedLength() > bbMsg.remaining()) {
			throw new BufferOverflowException();
		}

		bbMsg.put(SIGNATURE);
		bbMsg.putLong(mod);
		bbMsg.put(flag);
		bbMsg.putLong(timeStampNum);
		bbMsg.putInt(sender);

		if (body.length != 0) {
			bbMsg.put(body);
		}
	}

	/**
	 * @return the body
	 */
	public byte[] getBody() {
		return body;
	}

	public int getEncodedLength() {
		return MSG_HEADER + body.length;
	}

	/**
	 * Retrieves the flag options for this message.
	 *
	 * @return the flag
	 */
	public byte getFlag() {
		return flag;
	}

	/**
	 * Retrieve the modification id for this message used to verify any issues of concurrent modification.
	 *
	 * @return mod id.
	 */
	public long getMod() {
		return mod;
	}

	/**
	 * Retrieves the user ID who sent the message
	 *
	 * @return the userID
	 */
	public int getSender() {
		return sender;
	}

	/**
	 * Retrieves the time stamp or the time when this message was sent.
	 *
	 * @return the time stamp
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets the modification id for this message used to verify any issues of concurrent modification.
	 *
	 * @param mod mod id.
	 */
	public void setMod(long mod) {
		this.mod = mod;
	}

	@Override
	public String toString() {
		return toString("User" + sender);
	}

	public String toString(String userName) {
		switch (flag) {
		case -1:
			return "<INVALID MESSAGE>";
		case 0:
			try {
				return userName + ": " + new String(body, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new InternalError(e);
			}
		case 1:
			return "<" + userName + " connected>";
		case 2:
			return "<" + userName + " disconnected>";
		case 3:
			return "<" + userName + " stop typing>";
		case 4:
			return "<" + userName + " is typing>";
		case 5:
			return "<" + userName + " inactive>";
		case 6:
			return "<" + userName + " active>";
		case 7:
			try {
				return "<" + userName + " has alias of \"" + new String(body, "utf-8") + "\">";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new InternalError(e);
			}
		default:
			return "<INVALID MESSAGE>";
		}
	}
}
