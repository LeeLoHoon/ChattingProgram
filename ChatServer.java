import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatServer {

	public static void main(String[] args) {
		try{
			Date Today = new Date();
			SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
			ServerSocket server = new ServerSocket(10001);
			System.out.println("["+Time.format(Today)+ "]" +" Waiting connection...");
			HashMap hm = new HashMap();
			ArrayList<String> spam = new ArrayList<String>(); 
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm, spam);
// 뭔가를 물어본다... 예를 들어 클라이언트 아이디를..
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private ArrayList<String> spam;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm,ArrayList<String> spam){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		this.sock = sock;
		this.hm = hm;
		this.spam=spam;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("["+Time.format(Today)+"]" + " [Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		try{
			String line = null;
			String str = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if((str = checkword(line))!= null){
					warning(str);
				}
				else if(line.equals("/spamlist")) {
					printSpamList();
				}
				else if(line.indexOf("/addspam")==0) {
					addedSpam(line);
				}
				else if(line.equals("/userlist")){
					senduserlist();
				}
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast("["+Time.format(Today)+"]" + id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	private void printSpamList() {
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		Object obj = hm.get(id);
		if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				for(String element : spam) {
					pw.println("["+Time.format(Today)+"]" + element);
					pw.flush();
				}
		}
	}
	private void addedSpam(String line) {
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		int start = line.indexOf(" ") +1;
		if(start != -1){
			String addspam = line.substring(start);
			spam.add(addspam);
		}
	}
		
	private void senduserlist(){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		int j = 1;
		PrintWriter pw = null;
		Object obj = null;
		Iterator<String> iter = null;
		synchronized(hm){
			iter = hm.keySet().iterator();
			obj = hm.get(id);
		}
		if(obj != null){
				pw = (PrintWriter)obj;
		}
		pw.println("<User list>");
		while(iter.hasNext()){
				String list = (String)iter.next();
				pw.println("["+Time.format(Today)+"]" + j+". "+list);
				j++;
		}
		j--;
		pw.println("["+Time.format(Today)+"]" + "Total : "+j+".");
		pw.flush();
	}

	public String checkword(String msg){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		int b = 1;
		for(String check : spam){
			if(msg.contains(check))
				return check;
		}
		return null;
	}
	public void warning(String msg){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		Object obj = hm.get(id);
		if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("["+Time.format(Today)+"]" + "Don't use "+ msg);
				pw.flush();
		} // if
	}
	public void sendmsg(String msg){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("["+Time.format(Today)+"]" + id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				PrintWriter pw2 = (PrintWriter)hm.get(id);
				if(pw==pw2) continue; //뒤엣거 수행안함.
				pw.println("["+Time.format(Today)+"]" + msg);
				pw.flush();
			}
		}
	} // broadcast
}

