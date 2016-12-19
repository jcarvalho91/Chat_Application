import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.net.*;

@SuppressWarnings("serial")
public class Server extends JFrame {

	// Frame & Pane
	private JTextField textfield;
	private JTextPane pane;

	// Connections
	private Socket clientSocket;
	private ServerSocket serverSocket;

	// Streams
	private ObjectInputStream incomingMessage;
	private ObjectOutputStream outgoingMessage;

	public Server() {

		// TextField Setup
		textfield = new JTextField();
		textfield.setPreferredSize(new Dimension(150, 35));
		textfield.setEditable(false);
		textfield.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					Send(e.getActionCommand());
				} catch (BadLocationException bad) {
					bad.printStackTrace();
				}
				textfield.setText("");
			}
		});

		setTitle("Server - Chat");
		add(textfield, BorderLayout.AFTER_LAST_LINE);

		// Text Area - Pane
		pane = new JTextPane();
		pane.setBorder(null);
		pane.setEditable(true);
		add(new JScrollPane(pane));
		setPreferredSize(new Dimension(500, 500));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		pack();
	}

	// Main Method - will run entire chat
	public void mainChat() throws BadLocationException {
		try {
			serverSocket = new ServerSocket(2222);
			while (true) {
				try {
					Connections();
					Streams();
					Chat();
				} catch (EOFException | ClassNotFoundException eofe) {
					Show("\nConnection Ended");
				} finally {
					closeStreams();
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Setup Socket Connections
	public void Connections() throws IOException {
		Show("Waiting for Connection");
		clientSocket = serverSocket.accept();
	}

	// Connect Streams
	public void Streams() throws IOException {
		outgoingMessage = new ObjectOutputStream(clientSocket.getOutputStream());
		outgoingMessage.flush();
		incomingMessage = new ObjectInputStream(clientSocket.getInputStream());
	}

	// Chat With Client
	public void Chat() throws IOException, ClassNotFoundException, BadLocationException {

		String message = "Connected";
		Send(message);
		OpenTextField(true);
		do {
			message = (String) incomingMessage.readObject();
			ShowClient("\nCLIENT: ");
			Show(message);

		} while (!message.equals("CLIENT: Bye"));
		Show("\n Client has ended the conversation");
	}

	// Allow Server To Type Message on TextField
	// Prevents Bad data to flow while streams are connecting
	private void OpenTextField(boolean b) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textfield.setEditable(b);
			}
		}
	);
}

	// Close Streams When Conversation is Done
	public void closeStreams() {
		Show("\nClosing Connections");
		try {
			outgoingMessage.close();
			incomingMessage.close();
			clientSocket.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Sends Message to Client
	public void Send(String txt) throws BadLocationException {

		try {
			outgoingMessage.writeObject(txt);
			outgoingMessage.flush();
			ShowServer("\nSERVER: ");
			Show(txt);
		} catch (IOException ioe) {
			Document doc = pane.getDocument();
			doc.insertString(doc.getLength(), "\nFailure Sending Message", null);
		}
	}

	// Shows Message on GUI Text Box
	private void Show(String text) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Document doc = pane.getDocument();
				try {
					doc.insertString(doc.getLength(), text, null);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	);
}
	
	//Customize Alias Name - Font Type & Color
	public String Design(JTextPane pane, String msg, Color color) {

		StyleContext stylecontext = StyleContext.getDefaultStyleContext();
		AttributeSet attribute = stylecontext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
		attribute = stylecontext.addAttribute(attribute, StyleConstants.FontFamily, "Lucida Console");
		attribute = stylecontext.addAttribute(attribute, StyleConstants.ALIGN_LEFT, StyleConstants.ALIGN_JUSTIFIED);

		int len = pane.getDocument().getLength();
		pane.setCaretPosition(len);
		pane.setCharacterAttributes(attribute, false);
		pane.replaceSelection(msg);
		return msg;

	}
	//Show Message From Server
	private void ShowServer(String text) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Design(pane, text, Color.red);

			}
		}
	);
}
	//Show Message From Client
	private void ShowClient(String text) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Design(pane, text, Color.BLUE);
				}
			}
		);
	}
}
