import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;


public class ServerManager extends Thread{
	private ServerFrame frame;
	private ArrayList<String> blacklist;
	private ArrayList<Socket> l = new ArrayList<Socket>();
	private static final String CDIR = "/cConfig";
	private static final String PDIR = "/cPerm";
	private static final String LDIR = "/cLog";
	public ServerManager(ServerFrame f){
		this.frame=f;
	}
	public void run(){
		File cDir = new File(CDIR);
		cDir.mkdirs();
		File pDir = new File(PDIR);
		pDir.mkdirs();
		File lDir = new File(LDIR);
		lDir.mkdirs();
		try {
			ServerSocket server = new ServerSocket(1111);
			while(server.isBound()){
				Socket client = server.accept();
				aktualisiereBanList();
				if(isNotBlacklisted(((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().getHostAddress())){
					l.add(client);
					System.out.println(client.getRemoteSocketAddress());
					ChatProtokoll cpk = new ChatProtokoll(client,this);
					cpk.start();
					aktualisierListe();
				}
				else{
					sendBanMsg(client);
				}
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void aktualisierListe(){
		frame.clearLText();
		for (int i = 0; i < l.size(); i++) {
			frame.writeMsgL(l.get(i).getInetAddress()+"");
		}	
	}
	public void removeSocket(Socket s) {
		l.remove(s);
		aktualisierListe();
	}
	public void sendBanMsg(Socket client) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			writer.write("Du wurdest gebannt"+"\n");
			writer.flush();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void aktualisiereBanList(){
		blacklist = loadBlackList();
	}
	public ArrayList<String> loadPermFile(String s) {
		File f = new File(PDIR+"/"+s+".txt");
		try {
			ArrayList<String> permissions = new ArrayList<String>();
			BufferedReader breader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			try {
				while(breader.ready()){
					String perm = breader.readLine();
					permissions.add(perm);
				}
				breader.close();
				return permissions;
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			System.out.println(f.getAbsolutePath());
			if(createNewPermFile(f)==true){
				return loadPermFile(s);
			}
		}
		return null;
	}
	private boolean createNewPermFile(File f) {
		Path s;
		try {
			s = Paths.get(this.getClass().getClassLoader().getResource("StandartPerm.txt").toURI());
			try {
				Files.copy(s, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	private ArrayList<String> loadBlackList() {
		File f = new File(CDIR+"/blist.txt");
		try {
			ArrayList<String> ips = new ArrayList<String>();
			BufferedReader breader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			try {
				while(breader.ready()){
					String ip = breader.readLine();
					System.out.println(ip);
					ips.add(ip);
				}
				breader.close();
				return ips;
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			System.out.println(f.getAbsolutePath());
			try {
				f.createNewFile();
				return loadBlackList();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return null;
	}
	public boolean isNotBlacklisted(String ip) {
		if(blacklist!=null){
			for(int i = 0;i<blacklist.size();i++){
				if(ip.equals(blacklist.get(i))){
					return false;
				}
			}
		}
		return true;
	}
	public void sendMsg(String msg){
		String[] li = frame.getText().split("\n");
		if(li.length>10){
			String t = "";
			for (int i = 1; i < li.length; i++) {
				t+=li[i]+"\n";
			}
			frame.setText(t);
		}
		
		frame.writeMsg(msg);
		BufferedWriter writer;
		for(int i = 0;i<l.size();i++){
			String s =((InetSocketAddress) l.get(i).getRemoteSocketAddress()).getAddress().getHostAddress();
			if(isNotBlacklisted(s)){
				String[] x = msg.split(":");
				if(x.length>1){
					String msgreal = msg.substring(x[0].length()+1);
					if(msgreal.charAt(0)=='/'){
						if(hasPermission(s,"CMDReceive")){
							try {
								writer = new BufferedWriter(new OutputStreamWriter(l.get(i).getOutputStream()));
								writer.write(msg+"\n");
								writer.flush();
								logCMDMsg(msg+"\n");
							} catch (IOException e) {
								l.remove(i);
								e.printStackTrace();
							}
						}
					}
					else{
						if(hasPermission(s,"Read")){
							try {
								writer = new BufferedWriter(new OutputStreamWriter(l.get(i).getOutputStream()));
								writer.write(msg+"\n");
								writer.flush();
								logMsg(msg+"\n");
							} catch (IOException e) {
								l.remove(i);
								e.printStackTrace();
							}
						}
					}
				}
				else if(x.length==1){
					if(hasPermission(s,"Read")){
						try {
							writer = new BufferedWriter(new OutputStreamWriter(l.get(i).getOutputStream()));
							writer.write(msg+"\n");
							writer.flush();
							logMsg(msg+"\n");
						} catch (IOException e) {
							l.remove(i);
							e.printStackTrace();
						}
					}
				}
			}
			else{
				sendBanMsg(l.get(i));
			}
		}
	}
	private void logMsg(String msg) {
		try {
			Files.write(Paths.get(LDIR+"/log.txt"), msg.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			try {
				Files.createFile(Paths.get(LDIR+"/log.txt"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}	
	}
	private void logCMDMsg(String msg) {
		try {
			Files.write(Paths.get(LDIR+"/cmdlog.txt"), msg.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			try {
				Files.createFile(Paths.get(LDIR+"/cmdlog.txt"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	public boolean hasPermission(String s,String key){
		ArrayList<String> perm = loadPermFile(s);
		if(perm==null){
			return false;
		}
		for (int i = 0; i < perm.size(); i++) {
			String[] line = perm.get(i).split(":");
			if(line.length==2){
				if(line[0].equals(key)){
					return Boolean.parseBoolean(line[1]);
				}
			}
		}
		return false;
	}
}
