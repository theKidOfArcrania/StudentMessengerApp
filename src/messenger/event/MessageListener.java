package messenger.event;

import java.util.EventListener;

public interface MessageListener extends EventListener {

	public void messageRecieved(MessageEvent evt);
}
