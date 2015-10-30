package messenger.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;

import messenger.ui.image.ImageHelper;

public class MessengerSettings extends JFrame {
	
	//Messenger App Version
	double ver = 1.0;
	String date = "10/29/2015";
	
	//Creates Buttons
	JButton buttonUsername = new JButton("Change Username");
	JButton buttonColor = new JButton("Change User Color");
	JButton buttonAvatar = new JButton("Change Avatar");
	JButton buttonTheme = new JButton("Change Theme");
	
	private JPanel panelText;
	private JPanel panelMain;
	
	public MessengerSettings(Window owner) {
		
		//Creates Labels
		JLabel labelTitle = new JLabel("Student Messenger App");
		JLabel labelVersion = new JLabel("Version: " + ver);
		JLabel labelCredits = new JLabel("Created By:");
		JLabel labelCaleb = new JLabel("Caleb Hoff");
		JLabel labelHenry = new JLabel("Henry Wang");
		JLabel labelSpace = new JLabel(" ");
		
		//Sets sizes of buttons and labels
		buttonUsername.setPreferredSize(new Dimension(168, 39));
		buttonColor.setPreferredSize(new Dimension(168, 39));
		buttonAvatar.setPreferredSize(new Dimension(168, 39));
		buttonTheme.setPreferredSize(new Dimension(168, 39));
		
		//Sets up tooltips
		ToolTipManager.sharedInstance().setDismissDelay(2500);
		Border border = BorderFactory.createLineBorder(new Color(75,75,75));
		UIManager.put("ToolTip.border", border);
		UIManager.put("ToolTip.foreground", new ColorUIResource(55, 55, 55));
		UIManager.put("ToolTip.background", new ColorUIResource(215, 215, 215));
		
		//Makes tooltip text
		buttonUsername.setToolTipText("Change Your Username");
		buttonColor.setToolTipText("Change Your User Color");
		buttonAvatar.setToolTipText("Change your Avatar");
		buttonTheme.setToolTipText("Change the Messenger theme");
		labelVersion.setToolTipText("Build Date: " + date);
		
		//Sets colors
		labelTitle.setBackground(Color.BLACK);
		labelVersion.setBackground(Color.BLACK);
		
		//Sets fonts
		buttonUsername.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		buttonColor.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		buttonAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		buttonTheme.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		labelTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		labelVersion.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		labelSpace.setFont(new Font("Segoe UI", Font.PLAIN, 30));
		labelCredits.setFont(new Font("Segoe UI", Font.BOLD, 20));
		labelCaleb.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		labelHenry.setFont(new Font("Segoe UI", Font.PLAIN, 17));
		
		//Sets Mnemonics
		buttonUsername.setMnemonic(KeyEvent.VK_U);
		buttonColor.setMnemonic(KeyEvent.VK_C);
		buttonAvatar.setMnemonic(KeyEvent.VK_A);
		buttonTheme.setMnemonic(KeyEvent.VK_T);
		
		//Makes Panel
		panelText = new JPanel();
		panelMain = new JPanel();
		
		//Adds the Buttons and Labels to the Main Panel
		panelText.add(labelTitle);
		panelText.add(labelVersion);
		panelText.add(labelSpace);
		panelText.add(labelCredits);
		panelText.add(labelCaleb);
		panelText.add(labelHenry);
		panelMain.add(buttonUsername);
		panelMain.add(buttonColor);
		panelMain.add(buttonAvatar);
		panelMain.add(buttonTheme);
		
		//Adds panel to frame
		add(panelText);
		add(panelMain);
		
		//Settings Window (When one is made)
		setLayout(new GridLayout(1, 5));
		setSize(350, 230);
		setResizable(false);
		setTitle("Settings");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	}

/*
 //What the button does: TO DO: Make JFrame with options
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
			com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme("Noire", "Messenger App", "Messenger App");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}
 */