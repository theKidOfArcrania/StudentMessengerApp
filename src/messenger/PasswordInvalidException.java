package messenger;

public class PasswordInvalidException extends Exception {

	private static final long serialVersionUID = 132602288380665984L;

	public PasswordInvalidException() {
	}

	public PasswordInvalidException(String message) {
		super(message);
	}

	public PasswordInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordInvalidException(Throwable cause) {
		super(cause);
	}
}
