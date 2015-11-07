package messenger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

public class UserStats {

	private String userName;
	private boolean active, connected, typing;
	private Date lastActive;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public UserStats(String userName) {
		this.userName = userName;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * @return the last time that this user is active.
	 */
	public Date getLastActive() {
		return lastActive;
	}

	/**
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return whether if this user is active.
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return whether if this user is connected.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @return whether if this user is typing.
	 */
	public boolean isTyping() {
		return typing;
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	/**
	 * @param active true if this user is active, false if this user is inactive.
	 */
	public void setActive(boolean active) {
		boolean oldActive = this.active;
		this.active = active;
		pcs.firePropertyChange("active", oldActive, active);
	}

	/**
	 * @param connected whether if this user is connected.
	 */
	public void setConnected(boolean connected) {
		boolean oldConnected = connected;
		this.connected = connected;
		pcs.firePropertyChange("connected", oldConnected, connected);
	}

	/**
	 * @param lastActive the last time that this user is active.
	 */
	public void setLastActive(Date lastActive) {
		Date oldLastActive = this.lastActive;
		this.lastActive = lastActive;
		pcs.firePropertyChange("lastActive", oldLastActive, lastActive);
	}

	/**
	 * @param typing true if the user is typing, false otherwise.
	 */
	public void setTyping(boolean typing) {
		boolean oldTyping = this.typing;
		this.typing = typing;
		pcs.firePropertyChange("typing", oldTyping, typing);
	}

	/**
	 * @param userName the user name to set
	 */
	public void setUserName(String userName) {
		String oldUserName = this.userName;
		this.userName = userName;
		pcs.firePropertyChange("userName", oldUserName, userName);
	}

}
