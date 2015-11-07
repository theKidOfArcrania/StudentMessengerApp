package messenger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DefPath {

	// public static Path DEF_MAIN_ROOT = Paths.get("S:/Templates/Review/StudentMessengerApp");

	// For Testing:
	// public static Path DEF_MAIN_ROOT = Paths.get("C:/Users/Caleb/Desktop/messenger/StudentMessengerApp");
	public static final Path DEF_MAIN_ROOT = Paths.get(System.getProperty("user.home"), "Desktop", "messenger", "StudentMessengerApp"); // If you want to play that way, this testing is more generic and flexible to your computer. And school computer.

}
