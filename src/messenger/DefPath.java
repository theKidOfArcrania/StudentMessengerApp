package messenger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefPath {
	
	public static Path DEF_MAIN_ROOT = Paths.get("S:/Templates/Review/StudentMessengerApp");
	
	// For Testing:
	// public static final Path DEF_MAIN_ROOT = Paths.get("C:/Users/Henry/StudentMessengerApp"); //Henry's computer
	//public static final Path DEF_MAIN_ROOT = Paths.get("C:/Users/Caleb/Desktop/messenger/StudentMessengerApp"); //Caleb's Computer
	// public static final Path DEF_MAIN_ROOT = Paths.get("H:/Desktop/messenger/StudentMessengerApp"); //School computer
	private DefPath() {
		if (Files.notExists(DEF_MAIN_ROOT)) {
		    // ...
		}
	}
}
