import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.net.*;

@SuppressWarnings("serial")
public class Client extends JFrame {

	// Frame
	private JTextField textfield;
	private JTextPane pane;

	// Connections
	private Socket Socket;
	private String message = "";

	// Streams
	private ObjectInputStream incomingMessage;
	private ObjectOutputStream outgoingMessage;

	public Client() {

		// Frame
		textfield = new JTextField();
		textfield.setEditable(false);
		textfield.setPreferredSize(new Dimension(150, 35));
		textfield.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					Send(e.getActionCommand());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				textfield.setText("");
			}
		});

		setTitle("Client - Chat");
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

	public void mainChat() {
		try {
			Connections();
			Streams();
			Chat();
		} catch (EOFException eofe) {
			Show("\nConnection Ended");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			closeStreams();
		}
	}

	public void Connections() throws IOException {
		Socket = new Socket("localhost", 2222);
		Show("Connection made");
	}

	public void Streams() throws IOException {
		outgoingMessage = new ObjectOutputStream(Socket.getOutputStream());
		outgoingMessage.flush();
		incomingMessage = new ObjectInputStream(Socket.getInputStream());
	}

	public void Chat() throws IOException {
		OpenTextField(true);
		do {
			try {
				message = (String) incomingMessage.readObject();
				ShowServer("\nSERVER: ");
				Show(message);

			} catch (ClassNotFoundException e) {
				Show("SOMETHING WENT WRONG!");
				e.printStackTrace();
			}
			
		} while (!message.equals("SERVER: bye"));
		Show("\n Server has ended the conversation");
	}

	public void closeStreams() {
		Show("\nClosing Connections");
		OpenTextField(false);
		try {
			outgoingMessage.close();
			incomingMessage.close();
			Socket.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void OpenTextField(boolean yes) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textfield.setEditable(yes);
			}
		});
	}

	// Sends Message to Client
	public void Send(String txt) throws BadLocationException {

		try {
			outgoingMessage.writeObject("CLIENT: " + txt);
			outgoingMessage.flush();
			ShowClient("\nCLIENT: ");
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
		});
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
	//Show Message From Client
	private void ShowClient(String text) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Design(pane, text, Color.BLUE);
			}
		});
	}
	//Show Message From Server
	private void ShowServer(String text) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Design(pane, text, Color.red);

			}
		});
	}
}
