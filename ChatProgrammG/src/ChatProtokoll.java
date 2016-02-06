import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;


public class ChatProtokoll extends Thread{
	private Socket socket;
	private ServerManager m;
	public ChatProtokoll(Socket client,ServerManager m) {
		this.socket=client;
		this.m=m;
	}
	public void run(){
		try {
			BufferedReader iIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(socket.isConnected()){
				String msg = iIn.readLine();
				m.aktualisiereBanList();
				String s =((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
				if(m.isNotBlacklisted(s)){
					String[] x = msg.split(":");
					if(x.length>1){
						String msgreal = msg.substring(x[0].length()+1);
						if(msgreal.charAt(0)=='/'){
							if(m.hasPermission(s,"CMDSend")){
								m.sendMsg(msg);
							}
						}
						else{
							if(m.hasPermission(s,"Write")){
								m.sendMsg(msg);
							}
						}
					}
					else if(x.length==1){
						if(m.hasPermission(s,"Write")){
							m.sendMsg(msg);
						}
					}
				}
				else{
					m.sendBanMsg(socket);
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			m.removeSocket(socket);
		}
	}
}
