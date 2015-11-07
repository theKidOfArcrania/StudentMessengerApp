package messenger.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import messenger.ui.image.ImageHelper;

public class MessengerAbout extends JDialog {
	private static final long serialVersionUID = -367381677750609936L;

	private final BufferedImage about;

	private final JPanel contentPanel = new JPanel() {
		private static final long serialVersionUID = 4543424477473925145L;

		@Override
		public void paint(Graphics g) {
			if (about == null) {
				return;
			}
			g.drawImage(about, 0, 0, getWidth(), getHeight(), this);
		}
	};

	public MessengerAbout(Window owner) {
		super(owner);

		setResizable(true);
		setTitle("About");

		BufferedImage loading;
		try {
			loading = ImageHelper.loadImage("messenger/About.png");
			this.dispose();
		} catch (IOException e) {
			loading = null;
			e.printStackTrace();
		}
		this.setModal(true);
		about = loading;
		if (about != null) {
			setBounds(0, 0, about.getWidth(), about.getHeight());
		} else {
			setBounds(100, 100, 450, 300);
		}
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				setLocationRelativeTo(null);
			}
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
	}

}
