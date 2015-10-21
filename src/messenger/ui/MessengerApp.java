package messenger.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
			setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

			titleLabel = new JLabel(client.getChatRoom().getChatName() + "   ");
			titleLabel.setOpaque(false);
			titleLabel.setForeground(Color.WHITE);

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
										+ client.getName(), "MessengerApp", ERROR_MESSAGE);
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
			showMessageDialog(this, e1.getMessage(), "MessengerApp", ERROR_MESSAGE);
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

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnuChats = new JMenu("Chats");
		mnuChats.setMnemonic('C');
		menuBar.add(mnuChats);

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
		mnuNewPrivateChat.setMnemonic('P');
		mnuNewPrivateChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		mnuChats.add(mnuNewPrivateChat);

		JMenuItem mnuPublicChat = new JMenuItem("Public Open Chat");
		mnuPublicChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addChatTab();
			}
		});

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
		mnuNewUnlistedChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
		mnuNewUnlistedChat.setMnemonic('U');
		mnuChats.add(mnuNewUnlistedChat);

		JMenuItem mnuLoadChat = new JMenuItem("Load Chat");
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

		JSeparator separator_1 = new JSeparator();
		mnuChats.add(separator_1);
		mnuLoadChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		mnuLoadChat.setMnemonic('L');
		mnuLoadChat.setActionCommand("Load Chat");
		mnuChats.add(mnuLoadChat);
		mnuPublicChat.setMnemonic('O');
		mnuPublicChat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnuChats.add(mnuPublicChat);

		JSeparator separator = new JSeparator();
		mnuChats.add(separator);

		JMenuItem mnuExit = new JMenuItem("Exit");
		mnuExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				promptClosing();
			}
		});
		mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK));
		mnuExit.setMnemonic('X');
		mnuChats.add(mnuExit);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MessengerAbout about = new MessengerAbout(MessengerApp.this);
				about.setVisible(true);
			}
		});
		mnHelp.add(mntmAbout);
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
			showMessageDialog(this, "Unable to open chat-room page", "MessengerApp", ERROR_MESSAGE);
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
					showMessageDialog(this, "Username cannot be empty.", "MessengerApp", WARNING_MESSAGE);
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
		int response = showConfirmDialog(this, "Are you sure you want to exit?", "MessengerApp", YES_NO_OPTION, QUESTION_MESSAGE);
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
						showMessageDialog(this, "Unable to safely leave the chatroom " + client.getName(), "MessengerApp", ERROR_MESSAGE);
					}
				}
			}
			this.dispose();
		}

	}
}
