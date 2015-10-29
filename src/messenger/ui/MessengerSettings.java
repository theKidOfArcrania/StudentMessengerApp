package messenger.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import messenger.ui.image.ImageHelper;

public class MessengerSettings extends JDialog {
	public MessengerSettings(Window owner) {
		super(owner);
		
		//What the button does: TO DO: Make JFrame with options
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
			com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme("Noire", "Messenger App", "Messenger App");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
		
		
		//Settings Window (When one is made)
		setResizable(false);
		setTitle("Settings");
	}
	}