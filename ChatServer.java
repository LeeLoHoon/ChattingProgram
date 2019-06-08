//구현 전략 세우기
//실행 흐름 이해
//inputthread의 이해
//client와 thread 사이의 상호작용
//hashmap이 어떻게 쓰였는지 &synchronized의 해석

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
//아래의 class 구현 중 필요한 class를 가져와서 쓰기 위해 import함
//java.net package 안에 있는 class: Serversocket, Socket
//java.util package 안에 있는 class: Hashmap, Collection, Iterator
//java.io package 안에 있는 class: BufferedReader, PrintWriter, OutoutStreamWriter, InputStremReader

public class ChatServer {

	public static void main(String[] args) {  
	// start 지점(static method) - 객체없어도 실행가능
		try{
			Date Today = new Date();
			SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
			ServerSocket server = new ServerSocket(10001); 
			//10001을 포트로 하는 소켓 통신 서버 만들기
			System.out.println("["+Time.format(Today)+ "]" +" Waiting connection...");
			HashMap hm = new HashMap(); //
			ArrayList<String> spam = new ArrayList<String>(); 
			//set으로 설계하는게 더 효율적(이미 있는 spam은 추가 안됨)
			while(true){
				Socket sock = server.accept();
				//client로 부터 socket 접속을 accept하여 socket 연결
				ChatThread chatthread = new ChatThread(sock, hm, spam);
				//chatthread는 ChatThread의 instance이며 ChatThread의 constructor로 생성됨(sock과 hm과 spam parameter로 받음)
				//client마다 하나씩 thread를 만들어 client 개개인의 입력을 1:1로 처리
				chatthread.start();
				//ChatThread class는 Thread class를 상속받는데 Thread class 안에 있는 start method를 통해 ChatThread에 override 되어있는 run method를 실행
			} 
		}catch(Exception e){
			System.out.println(e);
		}//try중에 error가 발생하는 경우 Exception 이라는 class의 instance를 만든 후 에러 내용 print해줌
	} 
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private ArrayList<String> spam;
	private boolean initFlag = false;
	//field에는 각각의 client들이 가져야 하는 변수들
	public ChatThread(Socket sock, HashMap hm,ArrayList<String> spam){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		this.sock = sock;
		//client 마다 자신만의 sock가짐
		this.hm = hm;
		//hm 공유
		this.spam=spam;
		//spam 공유
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			//outputStream->outputStreamWriter->printWriter 
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//inputStream->inputStreamReader->bufferedReader
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("["+Time.format(Today)+"]" + " [Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}//동기화 기능 수행, 해당 thread의 client 정보 중 id와 pw를 hm에 저장 및 동기화(이 과정이 없으면 충돌 발생-->줄세우기)
			 //화장실문을 열고 닫는 개념
			initFlag = true;
			//안쓰임
		}catch(Exception ex){
			System.out.println(ex);
		} //ex라는 Exception instance 를 에러가 발생할 시 만들어 print
	} // 여기 까지가 ChatThread의 construcor
	public void run(){//각각의 chatthread들은 client의 채팅을 기다리며 계속 run실행 중
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		try{
			String line = null;
			String str = null;
			while((line = br.readLine()) != null){ //line이 들어오면 읽어들임
				if(line.equals("/quit"))
					break;
					//client로 부터 받아온 line이 /quit 이라면 break하고 while문 나옴
				if((str = checkword(line))!= null){ //checkword method를 line을 받아서 실행
					warning(str); 
					//warning method를 str(spam에 등록된 단어)을 받아서 실행
				}
				else if(line.equals("/spamlist")) { 
					printSpamList(); 
					//spamlist에 있는 단어들을 보여줌
				}
				else if(line.indexOf("/addspam")==0) {
					addedSpam(line); 
					//spamlist에 line에 들어있는 spam처리할 단어 추가
				}
				else if(line.equals("/userlist")){
					senduserlist(); 
					//userlist를 보여줌
				}
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
					// 어떤 유저에게 귓속말
				}else
					broadcast(id + " : " + line);
					//본인을 제외하고 모든이에게 broadcast
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{ //무조건 실행
			synchronized(hm){
				hm.remove(id); 
				//나가는 유저의 id를 hm에서 제거
			}
			broadcast("["+Time.format(Today)+"]" + id + " exited.");
			//나갔다고 말해줌
			try{
				if(sock != null)
					sock.close();
				//socket연결 끊기
			}catch(Exception ex){}
		}
	} 
	private void printSpamList() {
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		Object obj = hm.get(id);
		//obj는 hm의 해당 id가 갖는 pw를 받음
		if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				//obj를 PrintWriter로 받음
				synchronized(spam) {
					for(String element : spam) {
						pw.println("["+Time.format(Today)+"]" + element);
						pw.flush();
						//flush를 해야 println한 문장이 해당 pw를 가진 유저에게 보여짐
					}
				}				
		}
	}
	private void addedSpam(String line) {
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		int start = line.indexOf(" ") +1;
		// /addspam 뒤에 단어의 첫부분 인덱스
		synchronized(spam) {
		if(start != -1){ //단어가 있다면
			String addspam = line.substring(start);
			spam.add(addspam);
		}
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
			iter = hm.keySet().iterator(); //id를 iterator로 받음
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
				//check라는 string이 spam안에 있으면 check return
		}
		return null;
		//아니면 null return
	}
	public void warning(String msg){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		Object obj = hm.get(id);
		if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("["+Time.format(Today)+"]" + "Don't use "+ msg);
				pw.flush();
		} 
	}
	public void sendmsg(String msg){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		int start = msg.indexOf(" ") +1; 
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end); 
			//user id
			String msg2 = msg.substring(end+1);
			//msg
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("["+Time.format(Today)+"]" + id + " whisphered. : " + msg2);
				pw.flush();
			} 
		}
	}
	public void broadcast(String msg){
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		synchronized(hm){
			Collection collection = hm.values(); //hm의 value 값들을 collection에 넣음
			Iterator iter = collection.iterator(); //collection을 iterator로 받음
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next(); //순차적으로 iter에서 pw받음
				PrintWriter pw2 = (PrintWriter)hm.get(id); //자신의 pw
				if(pw==pw2) continue; //뒤엣거 수행안함.
				pw.println("["+Time.format(Today)+"]" + msg);
				pw.flush();
			}
		}
	} 
}

