package messenger.event;

import java.util.EventObject;

public class ChatListModEvent extends EventObject {
	private static final long serialVersionUID = 92188789680418740L;
	private final String chatContext;

	public ChatListModEvent(Object source, String chatContext) {
		super(source);
		this.chatContext = chatContext;
	}

	/**
	 * @return the chat name that is changed in context.
	 */
	public String getChatContext() {
		return chatContext;
	}

}
