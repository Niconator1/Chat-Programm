import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class ServerFrame extends JFrame{
	private JTextArea text = new JTextArea(1, 10);
	private JTextArea list = new JTextArea(5,15);
	private JScrollPane scroll = new JScrollPane(text);
	public ServerFrame(){
		doWindow();
	}
	private void doWindow() {
		setTitle("Server");
		JScrollPane scroll2 = new JScrollPane(list);
		text.setEditable(false);
		list.setEditable(false);
		add(scroll,BorderLayout.CENTER);
		add(scroll2,BorderLayout.EAST);
	}
	public static void main(String[] args) {
		ServerFrame frame = new ServerFrame();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setBounds(0, 0, 500, 180);
		ServerManager manager = new ServerManager(frame);
		manager.start();
	}
	public void clearLText() {
		list.setText("");
	}
	public void writeMsg(String string) {
		text.append(string+"\n");
		scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
	}
	public void writeMsgL(String string) {
		list.append(string+"\n");		
	}
	public String getText() {
		return text.getText();
	}
	public void setText(String string) {
		text.setText(string);		
	}
}
