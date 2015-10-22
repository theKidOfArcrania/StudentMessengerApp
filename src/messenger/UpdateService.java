package messenger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class UpdateService {
	private UpdateService() {
	}

	public void checkVersion() throws IOException {
		byte[] version = getVersion();
		byte[] head = fetchHeadVersion();

		if (!Arrays.equals(version, head)) {

		}
	}

	/**
	 * Fetches the head binary program
	 *
	 * @return the newest program binary data
	 * @throws IOException if an error occurs while connecting the the update server.
	 */
	public InputStream fetchHead() throws IOException {
		return null;
	}

	/**
	 * Fetches the head (newest) version of this binary program.
	 *
	 * @return the newest version in a md5 digest.
	 * @throws IOException if an error occurs while connecting the the update server.
	 */
	public byte[] fetchHeadVersion() throws IOException {
		// TO DO: implement this method.
		return null;
	}

	/**
	 * Retrieves the version of this binary file.
	 *
	 * @return the version of this binary file in a md5 digest.
	 */
	public byte[] getVersion() {
		// TO DO: implement this method.
		return null;
	}
}
