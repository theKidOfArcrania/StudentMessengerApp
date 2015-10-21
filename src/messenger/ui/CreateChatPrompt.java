package messenger.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import messenger.ChatList;
import messenger.ChatRoom;
import messenger.PasswordInvalidException;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class CreateChatPrompt extends JDialog {
	private static final long serialVersionUID = 1270471423315788161L;
	private static final char DEFAULT_ECHO_CHAR = '\u2022';

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel");
			com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme("Textile", "Student Messenger", "Student Messenger");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}
		try {
			CreateChatPrompt dialog = new CreateChatPrompt(null);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ChatRoom chatRoom = null;
	private final JButton cmdCreate = new JButton("Create");
	private final JPanel contentPanel = new JPanel();
	private final JTextField txtChatName;
	private final JPasswordField txtPassword;
	private final JPasswordField txtConfirm;
	private final ChatList chatList;

	public CreateChatPrompt(final boolean unlisted, final Window owner) {
		super(owner);
		try {
			chatList = ChatList.getMainChatList();
		} catch (IOException e1) {
			e1.printStackTrace();
			showMessageDialog(this, e1.getMessage(), "MessengerApp", ERROR_MESSAGE);
			System.exit(1);
			throw new Error();
		}

		if (unlisted) {
			setTitle("New Unlisted Private Chat...");
		} else {
			setTitle("New Private Chat...");
		}

		this.setModal(true);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				setLocationRelativeTo(owner);
			}
		});
		setResizable(false);
		setBounds(100, 100, 493, 181);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 250, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		JLabel lblChatName = new JLabel("Chat name:");
		GridBagConstraints gbc_lblChatName = new GridBagConstraints();
		gbc_lblChatName.insets = new Insets(0, 0, 5, 5);
		gbc_lblChatName.anchor = GridBagConstraints.WEST;
		gbc_lblChatName.gridx = 0;
		gbc_lblChatName.gridy = 0;
		contentPanel.add(lblChatName, gbc_lblChatName);

		txtChatName = new JTextField();
		txtChatName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				if (txtChatName.getText().isEmpty()) {
					cmdCreate.setEnabled(false);
				} else {
					cmdCreate.setEnabled(true);
				}
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				cmdCreate.setEnabled(true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (txtChatName.getText().isEmpty()) {
					cmdCreate.setEnabled(false);
				} else {
					cmdCreate.setEnabled(true);
				}
			}

		});
		GridBagConstraints gbc_txtChatName = new GridBagConstraints();
		gbc_txtChatName.weightx = 1.0;
		gbc_txtChatName.insets = new Insets(0, 0, 5, 0);
		gbc_txtChatName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtChatName.gridx = 1;
		gbc_txtChatName.gridy = 0;
		contentPanel.add(txtChatName, gbc_txtChatName);
		txtChatName.setColumns(10);

		JLabel lblPassword = new JLabel("New Password: ");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.anchor = GridBagConstraints.WEST;
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 1;
		contentPanel.add(lblPassword, gbc_lblPassword);

		txtPassword = new JPasswordField();
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.insets = new Insets(0, 0, 5, 0);
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 1;
		gbc_txtPassword.gridy = 1;
		contentPanel.add(txtPassword, gbc_txtPassword);

		JLabel lblConfirm = new JLabel("Confirm Password:");
		GridBagConstraints gbc_lblConfirm = new GridBagConstraints();
		gbc_lblConfirm.anchor = GridBagConstraints.WEST;
		gbc_lblConfirm.insets = new Insets(0, 0, 5, 5);
		gbc_lblConfirm.gridx = 0;
		gbc_lblConfirm.gridy = 2;
		contentPanel.add(lblConfirm, gbc_lblConfirm);

		txtConfirm = new JPasswordField();
		GridBagConstraints gbc_txtConfirm = new GridBagConstraints();
		gbc_txtConfirm.insets = new Insets(0, 0, 5, 0);
		gbc_txtConfirm.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtConfirm.gridx = 1;
		gbc_txtConfirm.gridy = 2;
		contentPanel.add(txtConfirm, gbc_txtConfirm);

		final JCheckBox chkShowPassword = new JCheckBox("Show Password");
		chkShowPassword.setOpaque(false);
		chkShowPassword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chkShowPassword.isSelected()) {
					txtPassword.setEchoChar((char) 0);
					txtConfirm.setEchoChar((char) 0);
				} else {
					txtPassword.setEchoChar(DEFAULT_ECHO_CHAR);
					txtConfirm.setEchoChar(DEFAULT_ECHO_CHAR);
				}
			}
		});
		GridBagConstraints gbc_chckbxShowPassword = new GridBagConstraints();
		gbc_chckbxShowPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxShowPassword.gridx = 1;
		gbc_chckbxShowPassword.gridy = 3;
		contentPanel.add(chkShowPassword, gbc_chckbxShowPassword);
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		GridBagLayout gbl_buttonPane = new GridBagLayout();
		gbl_buttonPane.columnWidths = new int[] { 270, 60, 60, 0 };
		gbl_buttonPane.rowHeights = new int[] { 24, 0 };
		gbl_buttonPane.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_buttonPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		buttonPane.setLayout(gbl_buttonPane);
		JButton cmdCancel = new JButton("Cancel");
		cmdCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GridBagConstraints gbc_cmdCancel = new GridBagConstraints();
		gbc_cmdCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmdCancel.anchor = GridBagConstraints.NORTH;
		gbc_cmdCancel.insets = new Insets(0, 0, 5, 5);
		gbc_cmdCancel.gridx = 1;
		gbc_cmdCancel.gridy = 0;
		buttonPane.add(cmdCancel, gbc_cmdCancel);
		cmdCreate.setEnabled(false);
		cmdCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				char[] password = txtPassword.getPassword();
				char[] confirm = txtConfirm.getPassword();
				if (!Arrays.equals(password, confirm)) {
					showMessageDialog(CreateChatPrompt.this, "The new password and confirm password fields are not the same.", "MessengerApp", WARNING_MESSAGE);
					txtConfirm.requestFocusInWindow();
					txtConfirm.selectAll();
					return;
				}

				try {
					if (chatList.privateChatExists(txtChatName.getText(), unlisted)) {
						showMessageDialog(CreateChatPrompt.this, "Chat name already exists.", "MessengerApp", WARNING_MESSAGE);
						txtChatName.requestFocusInWindow();
						txtChatName.selectAll();
						txtPassword.setText("");
						txtConfirm.setText("");
						return;
					}
					chatRoom = chatList.privateChatRoom(txtChatName.getText(), password, unlisted);
				} catch (PasswordInvalidException ex) {
					ex.printStackTrace();
					showMessageDialog(CreateChatPrompt.this, "Unexpected Error Occured.", "MessengerApp", ERROR_MESSAGE);
					txtChatName.requestFocusInWindow();
					txtChatName.selectAll();
					txtPassword.setText("");
					txtConfirm.setText("");
					return;
				} catch (IOException ex) {
					// TO DO: make more sophisticated error logging utility.
					ex.printStackTrace();
					showMessageDialog(CreateChatPrompt.this, ex.getMessage(), "MessengerApp", ERROR_MESSAGE);
					txtChatName.requestFocusInWindow();
					txtChatName.selectAll();
					txtPassword.setText("");
					txtConfirm.setText("");
					return;
				}
				dispose();
			}
		});
		GridBagConstraints gbc_cmdCreate = new GridBagConstraints();
		gbc_cmdCreate.insets = new Insets(0, 0, 5, 5);
		gbc_cmdCreate.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmdCreate.anchor = GridBagConstraints.NORTH;
		gbc_cmdCreate.gridx = 2;
		gbc_cmdCreate.gridy = 0;
		buttonPane.add(cmdCreate, gbc_cmdCreate);
		getRootPane().setDefaultButton(cmdCreate);

		this.pack();
	}

	/**
	 * @wbp.parser.constructor
	 */
	public CreateChatPrompt(Window owner) {
		this(false, owner);
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

}
