package messenger;

import java.security.GeneralSecurityException;

public class IllegalPermissionAccessException extends GeneralSecurityException {

	private static final long serialVersionUID = 3758624720148635323L;

	public IllegalPermissionAccessException() {
		super();
	}

	public IllegalPermissionAccessException(String message) {
		super(message);
	}

	public IllegalPermissionAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalPermissionAccessException(Throwable cause) {
		super(cause);
	}

}
