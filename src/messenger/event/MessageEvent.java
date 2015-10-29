package messenger.event;

import java.util.EventObject;

import messenger.Message;

public class MessageEvent extends EventObject {
	private static final long serialVersionUID = 998349578630002105L;
	private final Message msg;

	public MessageEvent(Object source, Message msg) {
		super(source);
		this.msg = msg;
	}

	/**
	 * @return the message
	 */
	public Message getMessage() {
		return msg;
	}

}
