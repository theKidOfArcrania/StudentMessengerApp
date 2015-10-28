package messenger.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import messenger.ChatList;
import messenger.ChatRoom;
import messenger.Message;
import messenger.event.MessageEvent;
import messenger.event.MessageListener;
import messenger.ui.image.ImageHelper;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

public class MessengerApp extends JFrame {
	public class ChatTabComponent extends JPanel {
		private static final long serialVersionUID = 834974693980933308L;

		private JLabel titleLabel = null;
		private JButton closeButton = null;
		private JTabbedPane tabbedPane = null;
		private volatile String lastUser = null;
		private boolean rotate = false;
		private boolean removed = false;

		@SuppressWarnings("resource")
		public ChatTabComponent(JTabbedPane aTabbedPane, final ChatClient client) {
			super(new BorderLayout());
			final ChatRoom chatRoom = client.getChatRoom();
			final ChangeListener list = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent evt) {
					if (removed) {
						return;
					}
					if (ChatTabComponent.this == tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())) {
						lastUser = null;
						titleLabel.setText(chatRoom.getChatName() + "   ");
					}
				}
			};

			tabbedPane = aTabbedPane;

			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder(-2, 0, 0, 0));

			titleLabel = new JLabel(client.getChatRoom().getChatName() + "   ");
			titleLabel.setOpaque(false);
			titleLabel.setForeground(Color.WHITE);
			titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

			closeButton = new JButton(closerImage);
			closeButton.setRolloverIcon(closerRolloverImage);
			closeButton.setPressedIcon(closerPressedImage);
			closeButton.setBorderPainted(false);
			closeButton.setBorder(BorderFactory.createEmptyBorder());
			closeButton.setFocusPainted(false);
			closeButton.setRolloverEnabled(true);
			closeButton.setOpaque(false);
			closeButton.setContentAreaFilled(false);
			closeButton.setPreferredSize(new Dimension(closerImage.getIconWidth(), closerImage.getIconHeight()));
			closeButton.setSize(new Dimension(closerImage.getIconWidth(), closerImage.getIconHeight()));
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (int i = 0; i < tabbedPane.getTabCount(); i++) {
						if (ChatTabComponent.this == tabbedPane.getTabComponentAt(i)) {
							try {
								client.leave();
							} catch (Exception e1) {
								e1.printStackTrace();
								showMessageDialog(ChatTabComponent.this, "Unable to safely leave the chatroom "
										+ client.getName(), "Messenger", ERROR_MESSAGE);
							}
							removed = true;
							chatIDs.remove(i);
							chatRooms.remove(i);
							chatTabComps.remove(i);

							tabbedPane.removeChangeListener(list);
							tabbedPane.removeTabAt(i);
							break;
						}
					}
				}
			});
			
			client.addMessageListener(new MessageListener() {
				@Override
				public void messageRecieved(MessageEvent evt) {
					Message msg = evt.getMessage();
					if (msg.getFlag() != Message.FLAG_POST || ChatTabComponent.this == tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())) {
						return;
					}
					lastUser = chatRoom.getUserName(msg.getSender());
				}
			});

			tabbedPane.addChangeListener(list);
			add(titleLabel, BorderLayout.CENTER);
			add(closeButton, BorderLayout.EAST);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							if (removed) {
								return;
							}
							Thread.sleep(1000);
							rotate = !rotate;
							if (rotate && lastUser != null) {
								titleLabel.setText(lastUser + " said...   ");
							} else {
								titleLabel.setText(chatRoom.getChatName() + "   ");
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			t.setDaemon(true);
			t.start();
		}
	}

	private static ImageIcon closerImage = ImageHelper.loadImageIcon("closer.png");
	private static ImageIcon closerRolloverImage = ImageHelper.loadImageIcon("closer_rollover.png");
	private static ImageIcon closerPressedImage = ImageHelper.loadImageIcon("closer_pressed.png");

	private static final long serialVersionUID = 8989464213647457508L;

	private final ArrayList<String> chatIDs = new ArrayList<>();
	private final ArrayList<ChatRoom> chatRooms = new ArrayList<>();
	private final ArrayList<ChatTabComponent> chatTabComps = new ArrayList<>();

	private final JPanel contentPane;
	private final JTabbedPane chatTabPane;
	private final ChatList chatList;

	/**
	 * Create the frame.
	 */
	public MessengerApp() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(MessengerApp.class.getResource("/messenger/FavIcon.gif")));
		try {
			chatList = ChatList.getMainChatList();
		} catch (IOException e1) {
			e1.printStackTrace();
			showMessageDialog(this, e1.getMessage(), "Messenger", ERROR_MESSAGE);
			System.exit(1);
			throw new Error();
		}

		setTitle("Student Messenger App");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				promptClosing();
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 453, 351);

		//Creates Menu Bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		//Creates Exit Item
		JMenuItem mnuExit = new JMenuItem("Exit");
		mnuExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				promptClosing();
			}
		});
		
		//Creates About Item
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MessengerAbout about = new MessengerAbout(MessengerApp.this);
				about.setVisible(true);
			}
		});
		
		//Creates New Private Chat Item
		JMenuItem mnuNewPrivateChat = new JMenuItem("New Private Chat");
		mnuNewPrivateChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateChatPrompt prompt = new CreateChatPrompt(false, MessengerApp.this);
				prompt.setVisible(true);
				if (!prompt.isCanceled()) {
					addChatTab("Private", prompt.getResponse());
				}

			}
		});
		
		//Creates New Unlisted Chat Item
		JMenuItem mnuNewUnlistedChat = new JMenuItem("New Unlisted Chat");
		mnuNewUnlistedChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateChatPrompt prompt = new CreateChatPrompt(true, MessengerApp.this);
				prompt.setVisible(true);
				if (!prompt.isCanceled()) {
					addChatTab("Unlisted", prompt.getResponse());
				}

			}
		});
		
		//Creates Join Open Chat Item
		JMenuItem mnuPublicChat = new JMenuItem("Join Open Chat");
		mnuPublicChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addChatTab();
			}
		});
		
		//Creates Join Private Chat Item
		JMenuItem mnuLoadChat = new JMenuItem("Join Private Chat");
		mnuLoadChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				LoadChatPrompt prompt = new LoadChatPrompt(MessengerApp.this);
				prompt.setVisible(true);
				if (!prompt.isCanceled()) {
					addChatTab(prompt.isUnlisted() ? "Unlisted" : "Private", prompt.getResponse());
				}

			}
		});

		//Separators!
		JSeparator separator = new JSeparator();
		
		//Creates Mnomics and Items in Messenger tab
		JMenu mnuHelp = new JMenu("Messenger");
		mntmAbout.setMnemonic('A');
		mnuExit.setMnemonic('X');
		mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK));
		mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK));
		mnuHelp.add(mntmAbout);
		mnuHelp.add(separator);
		mnuHelp.add(mnuExit);

		//Creates Mnemonics and Items in New tab
		JMenu mnuNew = new JMenu("New");
		mnuNewPrivateChat.setMnemonic('P');
		mnuNewUnlistedChat.setMnemonic('U');
		mnuNewPrivateChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		mnuNewUnlistedChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
		mnuNew.add(mnuNewPrivateChat);
		mnuNew.add(separator);
		mnuNew.add(mnuNewUnlistedChat);
		
		//Creates Mnemonics and Items in Join tab
		JMenu mnuJoin = new JMenu("Join");
		mnuPublicChat.setMnemonic('O');
		mnuLoadChat.setMnemonic('P');
		mnuPublicChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnuLoadChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		mnuLoadChat.setActionCommand("Load Chat");
		mnuJoin.add(mnuPublicChat);
		mnuJoin.add(separator);
		mnuJoin.add(mnuLoadChat);
		
		//Mnemonics for tabs
		mnuNew.setMnemonic('N');
		mnuJoin.setMnemonic('J');
		mnuHelp.setMnemonic('M');
		
		//Adds tabs to menu bar
		menuBar.add(mnuHelp);
		menuBar.add(mnuNew);
		menuBar.add(mnuJoin);
		
		//Sets Menu Item Fonts
		mnuHelp.setFont(new Font("Segoe UI", Font.BOLD, 14));
		mnuNew.setFont(new Font("Segoe UI", Font.BOLD, 14));
		mnuJoin.setFont(new Font("Segoe UI", Font.BOLD, 14));
		mntmAbout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mnuExit.setFont(new Font("Segoe UI", Font.BOLD, 13));
		mnuNewPrivateChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mnuNewUnlistedChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mnuLoadChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		mnuPublicChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));

		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 466, 0 };
		gbl_contentPane.rowHeights = new int[] { 291, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		chatTabPane = new JTabbedPane(SwingConstants.TOP);
		GridBagConstraints gbc_chatTabPane = new GridBagConstraints();
		gbc_chatTabPane.weighty = 1.0;
		gbc_chatTabPane.weightx = 1.0;
		gbc_chatTabPane.fill = GridBagConstraints.BOTH;
		gbc_chatTabPane.gridx = 0;
		gbc_chatTabPane.gridy = 0;
		contentPane.add(chatTabPane, gbc_chatTabPane);
	}

	private void addChatTab() {
		try {
			addChatTab("Public", chatList.publicChatRoom());
		} catch (IOException e) {
			e.printStackTrace();
			showMessageDialog(this, "Unable to open chat-room page", "Messenger", ERROR_MESSAGE);
		}
	}

	private void addChatTab(String chatType, ChatRoom room) {
		String chatID = chatType + "-" + room.getChatName();
		String response = "";
		int index;
		if ((index = chatIDs.indexOf(chatID)) != -1) {
			chatTabPane.setSelectedIndex(index);
		} else {
			while (response.isEmpty()) {
				response = JOptionPane.showInputDialog(this, "Input your username.");
				if (response == null) {
					try {
						room.close();
					} catch (IOException e) {
						// Silently ignore any error.
						e.printStackTrace();
					}
					return;
				}
				if (response.isEmpty()) {
					showMessageDialog(this, "Invalid Username", "Messenger", WARNING_MESSAGE);
				}
			}

			ChatClient client = new ChatClient(room, response);
			ChatTabComponent tabComp = new ChatTabComponent(chatTabPane, client);
			chatIDs.add(chatID);
			chatRooms.add(room);
			chatTabComps.add(tabComp);
			chatTabPane.addTab(room.getChatName(), null, client, room.getChatName());
			chatTabPane.setTabComponentAt(chatTabComps.size() - 1, tabComp);
		}
	}

	private void promptClosing() {
		int response = showConfirmDialog(this, "Are you sure you want to exit?", "Messenger", YES_NO_OPTION, QUESTION_MESSAGE);
		if (response == YES_OPTION) {
			int count = chatTabPane.getTabCount();
			for (int i = 0; i < count; i++) {
				Component comp = chatTabPane.getComponentAt(i);
				if (comp instanceof ChatClient) {
					ChatClient client = (ChatClient) comp;
					try {
						client.leave();
					} catch (IOException e) {
						e.printStackTrace();
						showMessageDialog(this, "Unable to safely leave the chatroom " + client.getName(), "Messenger", ERROR_MESSAGE);
					}
				}
			}
			this.dispose();
		}

	}
}
