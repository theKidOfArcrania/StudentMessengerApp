package messenger;

import java.io.IOException;

public class ParseException extends IOException {

	private static final long serialVersionUID = -1370098097868775949L;

	public ParseException() {
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

}
