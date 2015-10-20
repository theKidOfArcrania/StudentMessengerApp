package messenger.event;

import java.util.EventListener;

public interface ChatListModListener extends EventListener {
	public void addChat(ChatListModEvent evt);

	public void removeChat(ChatListModEvent evt);

	public void updatedList(ChatListModEvent evt);
}
