import java.net.*;
import java.io.*;
import java.util.*;


public class ChatServer { 

	public static void main(String[] args) {  

try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} 
		}catch(Exception e){
			System.out.println(e);
		}
	} 
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br; 
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm; 
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered."); 
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		} 
	} 
	public void run(){
		boolean check;
		try{
			String line = null;
			while((line = br.readLine()) != null){ 
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					check=checkline(line);
					if(check==false)
						sendmsg(line);
					else warning();
				}
				else if(line.equals("/userlist")) {
					send_userlist();
				}
				else {
					check=checkline(line);
					if(check==false)
						broadcast(id + " : " + line);
					else warning();}
			}//
		}catch(Exception ex){ 
			System.out.println(ex);  
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited."); 
			try{ 
				if(sock != null)
					sock.close();  
			}catch(Exception ex){} 

		}
	} 
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1; 
		int end = msg.indexOf(" ", start);
		if(end != -1){ 
			String to = msg.substring(start, end); 
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to); 
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj; 
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} 
		}
	}
	public void broadcast(String msg){
		try {
			synchronized(hm){
				Object obj = hm.get(id);
				PrintWriter myPw = (PrintWriter) obj;
				Collection collection = hm.values();
				Iterator iter = collection.iterator(); 
				while(iter.hasNext()){
					PrintWriter pw = (PrintWriter)iter.next();
					if(!pw.equals(myPw)) {
						pw.println(msg);
						pw.flush();
					}

				} 

		}
	}catch(Exception e){
		System.out.println(e);
	} 
	}
	public void send_userlist(){
			synchronized(hm) {
				Object obj = hm.get(id);
				PrintWriter myPw = (PrintWriter) obj;
				Iterator<String> iter = hm.keySet().iterator();
			while(iter.hasNext()) {
				String key=(String)iter.next();
				myPw.println(key);
				myPw.flush();
			}}
		

		}
	public boolean checkline(String line) {
		ArrayList<String> ben= new ArrayList<String>();
		boolean point=false;
		ben.add("fuck");
		ben.add("shit");
		ben.add("hell");
		ben.add("asshole");
		ben.add("easy");
		for(String word:ben) {
			if(line.indexOf(word)==0)
				point=true;
		}
		return point;
	}
	public void warning() {
		Object obj = hm.get(id);
		PrintWriter myPw = (PrintWriter) obj;
		myPw.println("Don't use slang");
		myPw.flush();
		
	}
}
