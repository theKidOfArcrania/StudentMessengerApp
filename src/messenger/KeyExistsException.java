package messenger;

public class KeyExistsException extends Exception {

	public KeyExistsException() {
		super();
	}

	public KeyExistsException(String message) {
		super(message);
	}

	public KeyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public KeyExistsException(Throwable cause) {
		super(cause);
	}

}
