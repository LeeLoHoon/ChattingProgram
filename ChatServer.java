import java.net.*;
import java.io.*;
import java.util.*;
//아래의 class 구현 중 필요한 class를 가져와서 쓰기 위해 import함
//java.net package 안에 있는 class: Serversocket method, Socket method,
//java.util package 안에 있는 class: Hashmap, Collection, Iterator
//java.io package 안에 있는 class: BufferedReader, PrintWriter, OutoutStreamWriter, InputStremReader, 

public class ChatServer { //chatserver 의 instance는 언제 만들어지나? 안만들어짐.
public static void main(String[] args) { // start 지점(class method/static method) - 객체없어도 실행가능
try{
			ServerSocket server = new ServerSocket(10001);//사용할 ip주소
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
//chatthread는 ChatThread의 instance 이며 ChatThread의 constructor로 생성됨(sock과 hm을 parameter로 받음)
				chatthread.start();
//ChatThread class 는 Thread class 를 상속받는데 Thread class 안에 있는 start method를 통해 ChatThread에 override 되어있는 run method를 실행한다.
			} // while
		}catch(Exception e){
			System.out.println(e);
		}//try중에 error가 발생하는 경우 Exception 이라는 class의 instance를 만든 후 에러 내용 print해줌
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
		this.hm = hm; //main에 있는 hm의 reference를 가져옴 (실체 x)
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));//출력하는 역할
			
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));//read line하는 역할
			id = br.readLine();
			broadcast(id + " entered."); //개개인에 배당된 chattread가 모든 client들에게 전달
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}//동기화 기능 수행 hm이라는 instance에 들어오는 id와 printwriter를 hm에 저장 및 동기화(이 과정이 없으면 충돌 발생--> 줄세우기)
			//thread를 사용할때 가장 신경써야 하는 부분이 충돌
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		} //ex라는 Exception instance 를 에러가 발생할 시 만들어 print
	} // 여기 까지가 ChatThread의 construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){ 
				if(line.equals("/quit"))
					break;
//client로 부터 받아온 line이 /quit 이라면 break하고 while문 나옴
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
//client로 부터 받아온 line이 /to 라면 sendmsg method 실행
				}
				else if(line.equals("/userlist")) {
					send_userlist();
				}
				else
					broadcast(id + " : " + line);
//그 외의 경우엔 모두 broadcast 실행
			}//
		}catch(Exception ex){ 
			System.out.println(ex);  
//ex라는 Exception instance 를 에러가 발생할 시 만들어 print
		}finally{
//finally는 위에 에러가 발생하든 안하든 실행
			synchronized(hm){
				hm.remove(id);
			}//hm에서 해당 id를 삭제하고 동기화
			broadcast(id + " exited."); //나갔다고 전함
			try{ 
				if(sock != null)
					sock.close();  //sock이 null이 아니라면 그 socket을 close함(연결끊음)
			}catch(Exception ex){} 
//ex라는 Exception instance 를 에러가 발생할 시 만듦

		}
	} // Thread에도 run method가 있지만 여기서 override로 같은 method를 정의
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1; 
//이 method는 /to를 포함하는 string을 파라미터로 받기 때문에 첫 blanck space가 나온 다음 index부터 메세지를 보내고자하는 client명의 시작이 된다. (ex. /to park hi)
		int end = msg.indexOf(" ", start);
//다음 blanck space가 나오는 곳이 end index 
		if(end != -1){ //end index가 있는경우
			String to = msg.substring(start, end); 
			//start ~ end index 앞까지가 받을 client 명
			String msg2 = msg.substring(end+1);
			//두번째 blanck space 이후 부터가 보낼 message
			Object obj = hm.get(to); //읽든 쓰든 sync를 쓰는게 안전.
	      //hm안에 해당 id(client)가 있는지 확인하고 있다면 printwriter return 
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj; 
//printwriter class는 object class를 상속하므로 이런 선언 가능
				pw.println(id + " whisphered. : " + msg2);
//printwriter 화면에 print함
				pw.flush();
//물내리기(해당 printwriter안에 println청소
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator(); 
//hm에 있던 values들 즉, 각 socket의 printwriter들을 모아 저장
			while(iter.hasNext()){ //다음이 있으면 true 없으면 false
				PrintWriter pw = (PrintWriter)iter.next();
				pw.println(msg);
				pw.flush();
			} 
//pw를 바꿔가면서 println 실행(존재하는 모든 pw에 println실행)
		}
	} // broadcast
	
	//함수 호출한 client의 pw 로 전체 hm의 keySet을 print pw를어떻게?
	public void send_userlist(){
		synchronized(hm) {
			ArrayList<String> IdArray =(ArrayList<String>) hm.keySet();
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			for(String Id:IdArray) {
				pw.println(Id);
			}
		}
	}
}

