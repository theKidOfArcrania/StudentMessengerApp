/*
 * Copyright 2002 and later by MH Software-Entwicklung. All rights reserved.
 * Use is subject to license terms.
 */
package messenger.ui.image;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Class to load images out of jar-files
 *
 * @author Michael Hagen
 */
public class ImageHelper {

	public static BufferedImage loadImage(String classRelativeFile) throws IOException {
		InputStream is = ClassLoader.getSystemResourceAsStream(classRelativeFile);

		if (is == null) {
			throw new IOException();
		}
		return ImageIO.read(is);
	}

	public static ImageIcon loadImageIcon(String name) {
		ImageIcon image = null;
		try {
			URL url = ImageHelper.class.getResource(name);
			if (url != null) {
				java.awt.Image img = Toolkit.getDefaultToolkit().createImage(url);
				if (img != null) {
					image = new ImageIcon(img);
				}
			}
		} catch (Throwable ex) {
			// TO DO: Fix error logging
			System.out.println("ERROR: loading image " + name + " failed. Exception: " + ex.getMessage());
		}
		return image;
	}

	private ImageHelper() {
	}
}
