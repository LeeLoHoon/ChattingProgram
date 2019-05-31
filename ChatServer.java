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
	} // construcor
	public void run(){
		try{
			String line = null;
			String str = null;
			while((line = br.readLine()) != null){
				ArrayList<String> spamlist = new ArrayList<String>;
				if(line.equals("/quit"))
					break;
				if((str = checkword(line,spamlist))!= null){
					warning(str);
				}
				else if(line.equals("/spamlist")) {
					printSpamList(spamlist);
				}
				else if(line.indexOf("/addspam")==0) {
					spamlist=addedSpam(spamlist,line);
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
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	private void printSpamList(ArrayList<String> list) {
		for(string element : list) {
			System.out.println(element);
		}
	}
	private ArrayList<String> addedSpam(ArrayList<String> List, String line) {
		int start = line.indexOf(" ") +1;
		int end = line.indexOf(" ", start);
		if(start != -1){
			String addspam = line.substring(start, end);
			List.add(addspam);
		}
		return List;
		
	private void senduserlist(){
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
				pw.println(j+". "+list);
				j++;
		}
		j--;
		pw.println("Total : "+j+".");
		pw.flush();
	}

	public String checkword(String msg,ArrayList<String> spamCheck){
		int b = 1;
		for(String check : spamCheck)){
			if(msg.contains(check))
				return check;
		}
		return null;
	}
	public void warning(String msg){
		Object obj = hm.get(id);
		if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("Don't use "+ msg);
				pw.flush();
		} // if
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
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				PrintWriter pw2 = (PrintWriter)hm.get(id);
				if(pw==pw2) continue; //뒤엣거 수행안함.
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
}

