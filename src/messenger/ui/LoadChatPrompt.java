package messenger.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import messenger.ChatList;
import messenger.ChatRoom;
import messenger.PasswordInvalidException;
import messenger.event.ChatListModEvent;
import messenger.event.ChatListModListener;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class LoadChatPrompt extends JDialog {
	private class ChatFinderModel extends AbstractListModel<String> implements ComboBoxModel<String> {
		private static final long serialVersionUID = 3590120977226451727L;

		private Object selected;
		private final boolean noChats = true;
		private final ArrayList<String> chats = new ArrayList<>();

		public ChatFinderModel() throws IOException {
			updateList();
			chatList.addModificationListener(new ChatListModListener() {

				@Override
				public void addChat(ChatListModEvent evt) {
					int index = chats.size() - 1;
					chats.add(index, evt.getChatContext());
					fireIntervalAdded(ChatFinderModel.this, index, index);
				}

				@Override
				public void removeChat(ChatListModEvent evt) {
					int index = chats.indexOf(evt.getChatContext());
					chats.remove(index);
					fireIntervalRemoved(ChatFinderModel.this, index, index);
				}

				@Override
				public void updatedList(ChatListModEvent evt) {
					try {
						updateList();
					} catch (IOException e) {
						e.printStackTrace();
						showMessageDialog(LoadChatPrompt.this, e.getMessage(), "MessengerApp", ERROR_MESSAGE);
					}
				}
			});
		}

		@Override
		public String getElementAt(int index) {
			return chats.get(index);
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public int getSize() {
			return chats.size();
		}

		@Override
		public void setSelectedItem(Object anItem) {
			if (selected != null && !selected.equals(anItem) || selected == null && anItem != null) {
				selected = anItem;
				fireContentsChanged(this, -1, -1);
			}
		}

		private void updateList() throws IOException {
			chats.clear();
			chats.add("<Please Select A Chat>");
			chats.addAll(chatList.getPrivateChats());
			fireContentsChanged(this, -1, -1);
		}

	}

	private static final char DEFAULT_ECHO_CHAR = '\u2022';

	private static final long serialVersionUID = -6078977031949778327L;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel");
			com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme("Textile", "Student Messenger", "Student Messenger");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			LoadChatPrompt dialog = new LoadChatPrompt(null);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JButton cmdLoad = null;
	private final JPanel pnlLoadChat;
	private final CardLayout loadChatLayout = new CardLayout(0, 0);
	private final ButtonGroup chatType = new ButtonGroup();
	private final ChatList chatList;
	private JPasswordField txtPassword = null;
	private final JTextField txtChatName;
	private final JPasswordField txtUnlistedPassword;
	private final JComboBox<String> cmbChats;
	private ChatRoom chatRoom;

	public LoadChatPrompt(Window owner) {
		super(owner);
		try {
			chatList = ChatList.getMainChatList();
		} catch (IOException e1) {
			e1.printStackTrace();
			showMessageDialog(this, e1.getMessage(), "MessengerApp", ERROR_MESSAGE);
			System.exit(1);
			throw new Error();
		}
		this.setModal(true);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				setLocationRelativeTo(owner);
			}
		});

		setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 15, 2, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		JPanel pnlChatType = new JPanel();
		GridBagConstraints gbc_pnlChatType = new GridBagConstraints();
		gbc_pnlChatType.insets = new Insets(0, 0, 5, 0);
		gbc_pnlChatType.fill = GridBagConstraints.BOTH;
		gbc_pnlChatType.gridx = 0;
		gbc_pnlChatType.gridy = 0;
		getContentPane().add(pnlChatType, gbc_pnlChatType);
		GridBagLayout gbl_pnlChatType = new GridBagLayout();
		gbl_pnlChatType.columnWidths = new int[] { 0, 0 };
		gbl_pnlChatType.rowHeights = new int[] { 0, 0 };
		gbl_pnlChatType.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlChatType.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlChatType.setLayout(gbl_pnlChatType);

		JRadioButton optPrivateChat = new JRadioButton("Private Chat");
		optPrivateChat.setOpaque(false);
		optPrivateChat.setBackground(Color.WHITE);
		optPrivateChat.setActionCommand("Private");
		optPrivateChat.setSelected(true);
		optPrivateChat.addActionListener(evt -> changeChatType());
		chatType.add(optPrivateChat);
		GridBagConstraints gbc_optPrivateChat = new GridBagConstraints();
		gbc_optPrivateChat.insets = new Insets(5, 5, 0, 0);
		gbc_optPrivateChat.anchor = GridBagConstraints.WEST;
		gbc_optPrivateChat.gridx = 0;
		gbc_optPrivateChat.gridy = 0;
		pnlChatType.add(optPrivateChat, gbc_optPrivateChat);

		JRadioButton optUnlistedChat = new JRadioButton("Unlisted Chat");
		optUnlistedChat.setOpaque(false);
		optUnlistedChat.setActionCommand("Unlisted");
		optUnlistedChat.addActionListener(evt -> changeChatType());
		chatType.add(optUnlistedChat);
		GridBagConstraints gbc_optUnlistedChat = new GridBagConstraints();
		gbc_optUnlistedChat.anchor = GridBagConstraints.WEST;
		gbc_optUnlistedChat.insets = new Insets(0, 5, 5, 0);
		gbc_optUnlistedChat.gridx = 0;
		gbc_optUnlistedChat.gridy = 1;
		pnlChatType.add(optUnlistedChat, gbc_optUnlistedChat);

		JPanel pnlSeperator = new JPanel();
		pnlSeperator.setPreferredSize(new Dimension(2, 2));
		pnlSeperator.setMinimumSize(new Dimension(0, 0));
		pnlSeperator.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		GridBagConstraints gbc_pnlSeperator = new GridBagConstraints();
		gbc_pnlSeperator.insets = new Insets(0, 5, 5, 5);
		gbc_pnlSeperator.fill = GridBagConstraints.BOTH;
		gbc_pnlSeperator.gridx = 0;
		gbc_pnlSeperator.gridy = 1;
		getContentPane().add(pnlSeperator, gbc_pnlSeperator);

		pnlLoadChat = new JPanel();
		GridBagConstraints gbc_pnlLoadChat = new GridBagConstraints();
		gbc_pnlLoadChat.insets = new Insets(0, 0, 5, 0);
		gbc_pnlLoadChat.fill = GridBagConstraints.BOTH;
		gbc_pnlLoadChat.gridx = 0;
		gbc_pnlLoadChat.gridy = 2;
		getContentPane().add(pnlLoadChat, gbc_pnlLoadChat);
		pnlLoadChat.setLayout(loadChatLayout);

		JPanel pnlUnlistedChat = new JPanel();
		pnlLoadChat.add(pnlUnlistedChat, "Unlisted");
		GridBagLayout gbl_pnlUnlistedChat = new GridBagLayout();
		gbl_pnlUnlistedChat.columnWidths = new int[] { 0, 0, 0 };
		gbl_pnlUnlistedChat.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_pnlUnlistedChat.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_pnlUnlistedChat.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		pnlUnlistedChat.setLayout(gbl_pnlUnlistedChat);

		JLabel lblUnlistedChatName = new JLabel("Chat Name:");
		GridBagConstraints gbc_lblUnlistedChatName = new GridBagConstraints();
		gbc_lblUnlistedChatName.insets = new Insets(5, 5, 5, 5);
		gbc_lblUnlistedChatName.anchor = GridBagConstraints.WEST;
		gbc_lblUnlistedChatName.gridx = 0;
		gbc_lblUnlistedChatName.gridy = 0;
		pnlUnlistedChat.add(lblUnlistedChatName, gbc_lblUnlistedChatName);

		txtChatName = new JTextField();
		txtChatName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				if (txtChatName.getText().isEmpty()) {
					cmdLoad.setEnabled(false);
				} else {
					cmdLoad.setEnabled(true);
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				cmdLoad.setEnabled(true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (txtChatName.getText().isEmpty()) {
					cmdLoad.setEnabled(false);
				} else {
					cmdLoad.setEnabled(true);
				}
			}

		});
		GridBagConstraints gbc_txtChatName = new GridBagConstraints();
		gbc_txtChatName.insets = new Insets(5, 0, 5, 5);
		gbc_txtChatName.anchor = GridBagConstraints.NORTH;
		gbc_txtChatName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtChatName.gridx = 1;
		gbc_txtChatName.gridy = 0;
		pnlUnlistedChat.add(txtChatName, gbc_txtChatName);
		txtChatName.setColumns(10);

		JLabel lblUnlistedPassword = new JLabel("Password:");
		GridBagConstraints gbc_lblUnlistedPassword = new GridBagConstraints();
		gbc_lblUnlistedPassword.anchor = GridBagConstraints.WEST;
		gbc_lblUnlistedPassword.insets = new Insets(0, 5, 5, 5);
		gbc_lblUnlistedPassword.gridx = 0;
		gbc_lblUnlistedPassword.gridy = 1;
		pnlUnlistedChat.add(lblUnlistedPassword, gbc_lblUnlistedPassword);

		txtUnlistedPassword = new JPasswordField();
		GridBagConstraints gbc_txtUnlistedPassword = new GridBagConstraints();
		gbc_txtUnlistedPassword.insets = new Insets(0, 0, 5, 5);
		gbc_txtUnlistedPassword.anchor = GridBagConstraints.NORTH;
		gbc_txtUnlistedPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUnlistedPassword.gridx = 1;
		gbc_txtUnlistedPassword.gridy = 1;
		pnlUnlistedChat.add(txtUnlistedPassword, gbc_txtUnlistedPassword);

		JCheckBox chkShowUnlistedPassword = new JCheckBox("Show Password");
		chkShowUnlistedPassword.setOpaque(false);
		chkShowUnlistedPassword.addActionListener(e -> {
			if (chkShowUnlistedPassword.isSelected()) {
				txtUnlistedPassword.setEchoChar((char) 0);
			} else {
				txtUnlistedPassword.setEchoChar(DEFAULT_ECHO_CHAR);
			}
		});
		GridBagConstraints gbc_chkShowUnlistedPassword = new GridBagConstraints();
		gbc_chkShowUnlistedPassword.insets = new Insets(0, 0, 0, 5);
		gbc_chkShowUnlistedPassword.anchor = GridBagConstraints.WEST;
		gbc_chkShowUnlistedPassword.gridx = 1;
		gbc_chkShowUnlistedPassword.gridy = 2;
		pnlUnlistedChat.add(chkShowUnlistedPassword, gbc_chkShowUnlistedPassword);

		JPanel pnlPrivateChat = new JPanel();
		pnlLoadChat.add(pnlPrivateChat, "Private");
		GridBagLayout gbl_pnlPrivateChat = new GridBagLayout();
		gbl_pnlPrivateChat.columnWidths = new int[] { 0, 0, 0 };
		gbl_pnlPrivateChat.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_pnlPrivateChat.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_pnlPrivateChat.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		pnlPrivateChat.setLayout(gbl_pnlPrivateChat);

		JLabel lblLoadPrivateChat = new JLabel("Chat Name:");
		GridBagConstraints gbc_lblLoadPrivateChat = new GridBagConstraints();
		gbc_lblLoadPrivateChat.insets = new Insets(5, 5, 5, 5);
		gbc_lblLoadPrivateChat.anchor = GridBagConstraints.WEST;
		gbc_lblLoadPrivateChat.gridx = 0;
		gbc_lblLoadPrivateChat.gridy = 0;
		pnlPrivateChat.add(lblLoadPrivateChat, gbc_lblLoadPrivateChat);

		cmbChats = new JComboBox<>();

		try {
			cmbChats.setModel(new ChatFinderModel());
		} catch (IOException e) {
			e.printStackTrace();
			showMessageDialog(this, e.getMessage(), "MessengerApp", ERROR_MESSAGE);
		}
		cmbChats.setSelectedItem("<Please Select A Chat>");
		cmbChats.addActionListener((evt) -> {
			cmdLoad.setEnabled(cmbChats.getSelectedIndex() > 0);
			txtPassword.setText("");
		});
		GridBagConstraints gbc_cmbChats = new GridBagConstraints();
		gbc_cmbChats.insets = new Insets(5, 0, 5, 5);
		gbc_cmbChats.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbChats.gridx = 1;
		gbc_cmbChats.gridy = 0;
		pnlPrivateChat.add(cmbChats, gbc_cmbChats);

		JLabel lblPassword = new JLabel("Password:");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.anchor = GridBagConstraints.WEST;
		gbc_lblPassword.insets = new Insets(0, 5, 5, 5);
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 1;
		pnlPrivateChat.add(lblPassword, gbc_lblPassword);

		txtPassword = new JPasswordField();
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.insets = new Insets(0, 0, 5, 5);
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 1;
		gbc_txtPassword.gridy = 1;
		pnlPrivateChat.add(txtPassword, gbc_txtPassword);

		JCheckBox chkShowPassword = new JCheckBox("Show Password");
		chkShowPassword.setOpaque(false);
		chkShowPassword.addActionListener(e -> {
			if (chkShowPassword.isSelected()) {
				txtPassword.setEchoChar((char) 0);
			} else {
				txtPassword.setEchoChar(DEFAULT_ECHO_CHAR);
			}
		});
		GridBagConstraints gbc_chkShowPassword = new GridBagConstraints();
		gbc_chkShowPassword.anchor = GridBagConstraints.WEST;
		gbc_chkShowPassword.gridx = 1;
		gbc_chkShowPassword.gridy = 2;
		pnlPrivateChat.add(chkShowPassword, gbc_chkShowPassword);

		JPanel pnlResponse = new JPanel();
		GridBagConstraints gbc_pnlResponse = new GridBagConstraints();
		gbc_pnlResponse.fill = GridBagConstraints.BOTH;
		gbc_pnlResponse.gridx = 0;
		gbc_pnlResponse.gridy = 3;
		getContentPane().add(pnlResponse, gbc_pnlResponse);
		GridBagLayout gbl_pnlResponse = new GridBagLayout();
		gbl_pnlResponse.columnWidths = new int[] { 177, 60, 60, 0 };
		gbl_pnlResponse.rowHeights = new int[] { 24, 0 };
		gbl_pnlResponse.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_pnlResponse.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlResponse.setLayout(gbl_pnlResponse);

		JButton cmdCancel = new JButton("Cancel");
		cmdCancel.addActionListener(e -> {
			dispose();
		});
		GridBagConstraints gbc_cmdCancel = new GridBagConstraints();
		gbc_cmdCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmdCancel.anchor = GridBagConstraints.NORTH;
		gbc_cmdCancel.insets = new Insets(0, 0, 5, 5);
		gbc_cmdCancel.gridx = 1;
		gbc_cmdCancel.gridy = 0;
		pnlResponse.add(cmdCancel, gbc_cmdCancel);

		cmdLoad = new JButton("Load");
		cmdLoad.setEnabled(false);
		cmdLoad.addActionListener(e -> {
			boolean unlisted = isUnlisted();
			String chatName = unlisted ? txtChatName.getText() : cmbChats.getSelectedItem().toString();
			char[] password = unlisted ? txtUnlistedPassword.getPassword() : txtPassword.getPassword();
			try {
				if (!chatList.privateChatExists(chatName, unlisted)) {
					showMessageDialog(this, "Chatroom name or password is incorrect.", "MessengerApp", WARNING_MESSAGE);
					if (unlisted) {
						txtUnlistedPassword.setText("");
						txtChatName.requestFocusInWindow();
						txtChatName.selectAll();
					} else {
						txtPassword.setText("");
						cmbChats.requestFocusInWindow();
					}
					return;
				}

				chatRoom = chatList.privateChatRoom(chatName, password, unlisted);
				dispose();
			} catch (PasswordInvalidException ex) {
				showMessageDialog(this, "Chatroom name or password is incorrect.", "MessengerApp", WARNING_MESSAGE);
				if (unlisted) {
					txtChatName.requestFocusInWindow();
					txtChatName.selectAll();
					txtUnlistedPassword.setText("");
				} else {
					cmbChats.requestFocusInWindow();
					txtPassword.setText("");
				}
			} catch (IOException ex) {
				// TO DO: make more sophisticated error logging utility.
				ex.printStackTrace();
				showMessageDialog(this, ex.getMessage(), "MessengerApp", ERROR_MESSAGE);
				if (unlisted) {
					txtChatName.requestFocusInWindow();
					txtChatName.selectAll();
					txtUnlistedPassword.setText("");
				} else {
					cmbChats.requestFocusInWindow();
					txtPassword.setText("");
				}
			}
		});
		GridBagConstraints gbc_cmdLoad = new GridBagConstraints();
		gbc_cmdLoad.insets = new Insets(0, 0, 5, 5);
		gbc_cmdLoad.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmdLoad.anchor = GridBagConstraints.NORTH;
		gbc_cmdLoad.gridx = 2;
		gbc_cmdLoad.gridy = 0;
		pnlResponse.add(cmdLoad, gbc_cmdLoad);

		loadChatLayout.show(pnlLoadChat, "Private");
		pack();
	}

	/**
	 * Retrieves the response
	 *
	 * @return user-supplied response for the chat-room specs.
	 */
	public ChatRoom getResponse() {
		return chatRoom;
	}

	/**
	 * @return whether if this dialog has been canceled.
	 */
	public boolean isCanceled() {
		return chatRoom == null;
	}

	public boolean isUnlisted() {
		return chatType.getSelection().getActionCommand().equals("Unlisted");
	}

	private void changeChatType() {
		txtUnlistedPassword.setText("");
		txtPassword.setText("");
		loadChatLayout.show(pnlLoadChat, chatType.getSelection().getActionCommand());
		cmdLoad.setEnabled(isUnlisted() ? !txtChatName.getText().isEmpty() : cmbChats.getSelectedIndex() > 0);
	}

}
