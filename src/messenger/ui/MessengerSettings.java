package messenger.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MessengerSettings extends JDialog {

	// Messenger App Version
	final double ver = 1.0;
	final String date = "11/4/2015";
	int theme = 0;

	// Creates Buttons
	JButton buttonUsername = new JButton("Change Username"),
	buttonColor = new JButton("Change User Color"),
	buttonAvatar = new JButton("Change Avatar"),
	buttonTheme = new JButton("Change Theme");
	
	private final JPanel panelText, panelMain;

	public MessengerSettings(Window owner) {
		super(owner);
		
		// Creates Labels
		JLabel labelTitle = new JLabel("Student Messenger App");
		JLabel labelVersion = new JLabel("Version: " + ver);
		JLabel labelCredits = new JLabel("Created By:");
		JLabel labelCaleb = new JLabel("   Caleb Hoff   ");
		JLabel labelHenry = new JLabel("   Henry Wang   ");
		JLabel labelDaniel = new JLabel("   Daniel Wong   ");
		JLabel labelSpace = new JLabel("            ");

		// Sets sizes of buttons and labels
		buttonUsername.setPreferredSize(new Dimension(168, 39));
		buttonColor.setPreferredSize(new Dimension(168, 39));
		buttonAvatar.setPreferredSize(new Dimension(168, 39));
		buttonTheme.setPreferredSize(new Dimension(168, 39));

		// Sets up tooltips
		ToolTipManager.sharedInstance().setDismissDelay(3500);

		// Makes tooltip text
		buttonUsername.setToolTipText("Change Your Username");
		buttonColor.setToolTipText("Change Your User Color");
		buttonAvatar.setToolTipText("Change your Avatar");
		buttonTheme.setToolTipText("Change the Messenger theme");
		labelVersion.setToolTipText("Build Date: " + date);
		labelCaleb.setToolTipText("Original Concept and UI");
		labelHenry.setToolTipText("Main Code Design");
		labelDaniel.setToolTipText("Code Design");
		
		// Sets fonts
		buttonUsername.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		buttonColor.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		buttonAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		buttonTheme.setFont(new Font("Segoe UI", Font.PLAIN, 18));
		labelTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
		labelVersion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		labelSpace.setFont(new Font("Segoe UI", Font.PLAIN, 28));
		labelCredits.setFont(new Font("Segoe UI", Font.BOLD, 20));
		labelCaleb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		labelHenry.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		labelDaniel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

		// Sets Mnemonics
		buttonUsername.setMnemonic(KeyEvent.VK_U);
		buttonColor.setMnemonic(KeyEvent.VK_C);
		buttonAvatar.setMnemonic(KeyEvent.VK_A);
		buttonTheme.setMnemonic(KeyEvent.VK_T);

		// Makes Panel
		panelText = new JPanel();
		panelMain = new JPanel();

		// Adds the Buttons and Labels to the Main Panel
		panelText.add(labelTitle);
		panelText.add(labelVersion);
		panelText.add(labelSpace);
		panelText.add(labelCredits);
		panelText.add(labelCaleb);
		panelText.add(labelHenry);
		panelText.add(labelDaniel);
		panelMain.add(buttonUsername);
		panelMain.add(buttonColor);
		panelMain.add(buttonAvatar);
		panelMain.add(buttonTheme);

		// Adds panel to frame
		add(panelText);
		add(panelMain);

		
		// If buttons are clicked
		buttonUsername.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				// TO DO: username changing dialog, should write name change to
				// chat
				JOptionPane.showMessageDialog(panelMain, "This feauture is not yet complete", "Change Username",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		buttonColor.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				// TO DO: color picker dialog
				JOptionPane.showMessageDialog(panelMain, "This feauture is not yet complete", "Change User Color",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		buttonAvatar.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				// TO DO: file picker/picture chooser dialog
				JOptionPane.showMessageDialog(panelMain, "This feauture is not yet complete", "Change Avatar",
						JOptionPane.ERROR_MESSAGE);

			}
		});
		buttonTheme.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				// TO DO: make radio buttons to choose theme

				if (theme == 0) {
					try {
						UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
						com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme("Noire", "Messenger App", "Messenger");
						buttonTheme.setText("Use Noire Theme");
						buttonTheme.setToolTipText("Using: Hifi Theme");
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e) {
						JOptionPane.showMessageDialog(MessengerSettings.this, "Unable to change theme.", "Messenger",
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
				if (theme == 1) {
					try {
						UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
						com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme("Hifi", "Messenger App", "Messenger");
						buttonTheme.setText("Use Hifi Theme");
						buttonTheme.setToolTipText("Using: Noire Theme (Default)");
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e) {
						JOptionPane.showMessageDialog(MessengerSettings.this, "Unable to change theme.", "Messenger",
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
				// Repaint theme.
				Window owner = MessengerSettings.this.getOwner();
				if (owner != null) {
					owner.repaint();
				}
				repaint();
				if (theme == 0)
					theme = 1;
				else
					theme = 0;
			}
		});

		// Settings Window (When one is made)
		this.setModal(true);
		setLayout(new GridLayout(1, 5));
		setSize(350, 230);
		setResizable(false);
		setTitle("Settings");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Makes Settings window spawn in the center
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				setLocationRelativeTo(null);
			}
		});
	}
}