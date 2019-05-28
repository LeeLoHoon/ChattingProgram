//https://github.com/LeeLoHoon/ChattingProgram

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {

		try {
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while (true) {
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

class ChatThread extends Thread {
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;

	public ChatThread(Socket sock, HashMap hm) {
		this.sock = sock;
		this.hm = hm;
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized (hm) {
				hm.put(this.id, pw);
			}
			initFlag = true;
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public void run() {
		//check라는 boolean type을 이용하여 check가 true인 경우는 warning method를 호출하여 입력한 client에게 경고 메세지를 출력
		//false인 경우는 원래 하려는 senmsg와 broadcast를 정상적으로 실행
		boolean check;
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.equals("/quit"))
					break;
				//여기서 미리 체크하고 뒤로 if문으로 line 쓰기
				if (line.indexOf("/to ") == 0) {
					//각각의 line을 check하기 위해 checkline method를 정의하여 boolean type을 return 하도록 설계
					check = checkline(line);
					if (check == false)
						sendmsg(line);
					else
						//아무것도 받지않고 아무것도 return하지 않지만 method 내에서 입력한 client에게 경고메세지가 출력되도록 설계
						warning();
				}
				// /userlist라는 값을 client로 부터 입력 받으면 send_userlist() 실행
				else if (line.equals("/userlist")) {
					send_userlist();
				} else {
					check = checkline(line);
					if (check == false)
						broadcast(id + " : " + line);
					else
						warning();
				}
			} //
		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			synchronized (hm) {
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try {
				if (sock != null)
					sock.close();
			} catch (Exception ex) {
			}
		}
	}

	public void sendmsg(String msg) {
		int start = msg.indexOf(" ") + 1;
		int end = msg.indexOf(" ", start);
		if (end != -1) {
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end + 1);
			Object obj = hm.get(to);
			if (obj != null) {
				PrintWriter pw = (PrintWriter) obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			}
		}
	}

	public void broadcast(String msg) {
		try {
			synchronized (hm) {
				//broadcast 중 자신이 쓴 글을 자신에게 broadcast되지 못하도록 만들기
				//send_userlist에서 썼던 것과 같은 논리로 msg를 입력한 client의 id를 이용해 해당 client의 pw return
				Object obj = hm.get(id);
				PrintWriter myPw = (PrintWriter) obj;
				Collection collection = hm.values();
				Iterator iter = collection.iterator();
				while (iter.hasNext()) {
					PrintWriter pw = (PrintWriter) iter.next();
					//위에서 return한 myPw와 loop를 돌며 정해지는 pw중 myPw와 같은 것을 가리키는 pw가 나오면 if문 실행하지 않음
					if (!pw.equals(myPw)) {
						pw.println(msg);
						pw.flush();
					}

				}

			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	//method를 호출한 client에게 결과값을 보여줘야 함
	public void send_userlist() {
		synchronized (hm) {
			Object obj = hm.get(id); //해당 client에게 할당된 chattread field의 id를 파라미터로 hm의 pw return 
			PrintWriter myPw = (PrintWriter) obj;
			Iterator<String> iter = hm.keySet().iterator(); //hm에 저장되어 있는 모든 id를 array로 받음
			while (iter.hasNext()) {
				String key = (String) iter.next();
				myPw.println(key); //호출한 client화면에 print
				myPw.flush();
			}
		}

	}
	//check할 line을 파라미터로 받음
	public boolean checkline(String line) {
		//우선 차단할 string 값들을 ArrayList로 저장
		ArrayList<String> ben = new ArrayList<String>();
		boolean point = false;
		ben.add("fuck");
		ben.add("shit");
		ben.add("hell");
		ben.add("asshole");
		ben.add("easy");
		//ArrayList를 loop 돌면서 line에 있는지 확인하고 있으면 point는 true 
		//여기서 index가 금지어가 걸리면 브레이크 하기, index가 0이 아닌 경우에는 true로 안바뀜 
		for (String word : ben) {
			if (line.indexOf(word) == 0)
				point = true;
		}
		
		//없으면 point는 false
		return point;
	}
	
	//send_userlist에서 썼던 것과 같은 논리로 msg를 입력한 client의 id를 이용해 해당 client의 pw return
	public void warning() {
		//여기서 어떤 욕설을 써서 걸렸는지 알 수 없음.
		Object obj = hm.get(id);
		PrintWriter myPw = (PrintWriter) obj;
		myPw.println("Don't use slang");
		myPw.flush();

	}
}
