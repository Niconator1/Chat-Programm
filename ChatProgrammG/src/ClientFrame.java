import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;


public class ClientFrame extends JFrame implements ActionListener{
	private static ChatProtokollClient chat;
	private JTextArea text = new JTextArea(2,10);
	private JTextArea input = new JTextArea(2,10);
	private JButton enter = new JButton("Senden");
	private JScrollPane scroll = new JScrollPane(text);
	public ClientFrame(){
		doWindow();
	}
	private void doWindow() {
		setTitle("Client");	
		text.setEditable(false);
		JScrollPane scroll2 = new JScrollPane(input);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scroll2,BorderLayout.CENTER);
		panel.add(enter,BorderLayout.EAST);
		add(scroll,BorderLayout.CENTER);
		add(panel,BorderLayout.SOUTH);
		input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
		input.getActionMap().put("send", doSe);
		input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), "newLine");
		input.getActionMap().put("newLine", doLine);
		enter.addActionListener(this);
	}
	public static void main(String[] args) {
		ClientFrame frame = new ClientFrame();
		chat = new ChatProtokollClient(frame);
		chat.start();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setBounds(0, 0, 320, 180);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src==enter){
			doSend();
		}
	}
	Action doLine = new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
	        doLine();
	    }		
	};
	Action doSe = new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
	        doSend();
	    }
	};
	private void doLine() {
		if(!input.getText().equals("")){
			input.append("\n");
		}
	}
	private void doSend() {
		if(!input.getText().equals("")){
			chat.write(input.getText());
			input.setText("");
		}		
	}
	public void writeMsg(String string) {
		text.append(string+"\n");
		scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
	}
}
