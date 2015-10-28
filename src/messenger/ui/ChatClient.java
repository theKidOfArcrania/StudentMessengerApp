package messenger.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.FileLockInterruptionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.jtattoo.plaf.texture.TextureUtils;

import messenger.ChatRoom;
import messenger.Message;
import messenger.event.MessageEvent;
import messenger.event.MessageListener;

public class ChatClient extends JPanel {
	private static final long serialVersionUID = 2063945626125343638L;
	private static final boolean DEBUG = false;
	private JTextField txtInput;
	private JScrollPane srpnMsgs;
	private JTextArea txtMsgs;
	private final ChatRoom room;
	private boolean stop = false;
	private final Thread autoUpdate;

	/**
	 * Create the chat client.
	 *
	 * @param room the chat-room that this client will use.
	 * @param username the selected username.
	 */
	@SuppressWarnings({ "serial", "unused" })
	public ChatClient(ChatRoom room, String username) {

		this.room = room;
		initUI();
		this.autoUpdate = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stop) {
					try {
						updateMessages();
					} catch (InterruptedException | AsynchronousCloseException | FileLockInterruptionException e) {
						// Interruption
						if (stop) {
							return;
						}
						// Make sure to reset interrupted flag.
						Thread.interrupted();
					} catch (IOException e) {
						// Place this here just in case if some how an interruption-related exception occurs.
						if (stop) {
							return;
						}
						// Make sure to reset interrupted flag.
						Thread.interrupted();
						e.printStackTrace();
						JOptionPane.showMessageDialog(ChatClient.this, "Unable to load messages", "Messenger", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						// Make sure to reset interrupted flag.
						Thread.interrupted();
						e.printStackTrace();
						JOptionPane.showMessageDialog(ChatClient.this, "Unexpected error occured", "Messager", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		autoUpdate.start();
		try {
			room.connect(username);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Unable to connect.", "Messenger", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void addMessageListener(MessageListener list) {
		listenerList.add(MessageListener.class, list);
	}

	public ChatRoom getChatRoom() {
		return room;
	}

	public void leave() throws IOException {
		stop = true;
		txtInput.setEnabled(false);
		room.close();
		while (autoUpdate.isAlive()) {
			try {
				autoUpdate.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void removeMessageListener(MessageListener list) {
		listenerList.remove(MessageListener.class, list);
	}

	@SuppressWarnings("serial")
	private void initUI() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] { 40, 0, 30 };
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		setLayout(gridBagLayout);

		JPanel pnlTitleBar = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				TextureUtils.fillComponent(g, this, TextureUtils.MENUBAR_TEXTURE_TYPE);
			}
		};
		GridBagConstraints gbc_pnlTitleBar = new GridBagConstraints();
		gbc_pnlTitleBar.fill = GridBagConstraints.BOTH;
		gbc_pnlTitleBar.gridx = 0;
		gbc_pnlTitleBar.gridy = 0;
		add(pnlTitleBar, gbc_pnlTitleBar);
		GridBagLayout gbl_pnlTitleBar = new GridBagLayout();
		gbl_pnlTitleBar.columnWidths = new int[] { 443, 0 };
		gbl_pnlTitleBar.rowHeights = new int[] { 35, 0 };
		gbl_pnlTitleBar.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_pnlTitleBar.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlTitleBar.setLayout(gbl_pnlTitleBar);

		JLabel lblChatTitle = new JLabel(room.getChatName() + " Chatroom");
		lblChatTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
		lblChatTitle.setForeground(Color.WHITE);
		lblChatTitle.setBackground(Color.RED);
		GridBagConstraints gbc_lblChatTitle = new GridBagConstraints();
		gbc_lblChatTitle.insets = new Insets(5, 10, 10, 10);
		gbc_lblChatTitle.fill = GridBagConstraints.BOTH;
		gbc_lblChatTitle.gridx = 0;
		gbc_lblChatTitle.gridy = 0;
		pnlTitleBar.add(lblChatTitle, gbc_lblChatTitle);

		srpnMsgs = new JScrollPane();
		srpnMsgs.setAutoscrolls(true);
		srpnMsgs.setBorder(null);
		srpnMsgs.setViewportBorder(null);
		srpnMsgs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		srpnMsgs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(srpnMsgs, gbc_scrollPane);

		txtMsgs = new JTextArea();
		txtMsgs.setWrapStyleWord(true);
		txtMsgs.setLineWrap(true);
		txtMsgs.setEditable(false);
		txtMsgs.setBackground(Color.DARK_GRAY);
		txtMsgs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		srpnMsgs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		srpnMsgs.setViewportView(txtMsgs);

		txtInput = new JTextField();
		txtInput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (txtInput.getText().isEmpty()) {
					return;
				}

				try {
					room.post(txtInput.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(ChatClient.this, "Unable to post message", "Messenger", JOptionPane.ERROR_MESSAGE);
				} finally {
					txtInput.setText("");
				}
			}
		});
		txtInput.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (txtInput.getForeground().equals(Color.GRAY)) {
					txtInput.setForeground(Color.BLACK);
					txtInput.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (txtInput.getText().isEmpty()) {
					txtInput.setText("Send a message...");
					txtInput.setForeground(Color.GRAY);

				}
			}
		});
		txtInput.setForeground(Color.GRAY);
		txtInput.setText("Send a message...");
		GridBagConstraints gbc_txtInput = new GridBagConstraints();
		gbc_txtInput.fill = GridBagConstraints.BOTH;
		gbc_txtInput.gridx = 0;
		gbc_txtInput.gridy = 2;
		add(txtInput, gbc_txtInput);
		txtInput.setColumns(10);
	}

	private void updateMessages() throws IOException, InterruptedException {
		Message[] msgs = room.updateMessage();

		if (msgs.length == 0) {
			return;
		}

		for (Message msg : msgs) {
			String messageLine = msg.toString(room.getUserName(msg.getSender()));
			if (msg.getFlag() == Message.FLAG_POST || msg.getFlag() == Message.FLAG_CONNECT || msg.getFlag() == Message.FLAG_DISCONNECT) {
				if (txtMsgs.getText().isEmpty()) {
					txtMsgs.setText(messageLine);
				} else {
					txtMsgs.setText(txtMsgs.getText() + "\n" + messageLine);
				}
			}
			try {
				fireMessageRecievedEvent(new MessageEvent(this, msg));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int end = txtMsgs.getText().length();
		txtMsgs.select(end - 2, end - 1);
		txtMsgs.moveCaretPosition(end - 1);
		txtMsgs.repaint();
		Thread.sleep(100);
	}

	protected void fireMessageRecievedEvent(MessageEvent evt) {
		for (MessageListener list : listenerList.getListeners(MessageListener.class)) {
			list.messageRecieved(evt);
		}
	}

}
