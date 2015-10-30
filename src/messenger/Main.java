package messenger;

import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import messenger.ui.MessengerApp;
import messenger.ui.image.ImageHelper;

public class Main {

	public static void main(String[] args) throws Exception {
		runProgram();
	}

	public static void runProgram() throws IOException {
		final BufferedImage splash = ImageHelper.loadImage("messenger/About.png");

		Window splashScreen = new Window(null) {
			private static final long serialVersionUID = 8733767680868899639L;

			@Override
			public void paint(Graphics g) {
				g.drawImage(splash, 0, 0, this);
			}
		};
		splashScreen.setSize(splash.getWidth(), splash.getHeight());
		splashScreen.setVisible(true);
		splashScreen.setLocationRelativeTo(null);
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
			com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme("Hifi", "Messenger App", "Messenger App");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}

		MessengerApp app = new MessengerApp();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		splashScreen.dispose();
		app.setVisible(true);
	}
}
