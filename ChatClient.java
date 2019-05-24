import java.net.*;
import java.io.*;
//아래의 class 구현 중 필요한 class를 가져와서 쓰기 위해 import함
//java.net package 안에 있는 class: Socket
//java.io package 안에 있는 class: BufferedReader, PrintWriter, OutoutStreamWriter, InputStremReader,

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1);
		}//초기 입력값이 3개의 파라미터가 아니면 메세지 던지고 종료
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			sock = new Socket(args[1], 10001);
// 본인 ip와 server의 포트를 연결
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
//컨셉:sock안에서 아웃풋 스트림을 리턴하고 그걸 받은 아웃풋 스트림 라이터 객체를 만들고 그걸 받은 프린트 라이터 객체를 만들고 그것을 이용해 인스턴스를 만듦
			
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//컨셉:sock안에서 인풋 스트림을 리턴하고 그걸 받은 인풋 스트림 리더라는 객체를 만들고 그것을 파라미터로 받은 버퍼리터 객체를 만들고 그것을 이용해 인스턴스를 만듦 
		
			
			
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			//BufferedReader는 문장을 바로 읽을 수 없음 여러 컨버터를 연결하는 것 처럼InputStreamReader가 젠더 역할 수행하여 client가 입력한 내용을 이용
			
			
			pw.println(args[0]);
			//유저 name을 파라미터로 system.out.println 과 같은 기능이지만 종착역이 다름.
			//pw에 입력하는 기능 
			pw.flush();
			InputThread it = new InputThread(sock, br);
			it.start(); //ChatServer와 마찬가지로 InputThread의 run을 실행
			String line = null;
			while((line = keyboard.readLine()) != null){
				pw.println(line);
				pw.flush();
				if(line.equals("/quit")){
					endflag = true;
					break;
				}
			}//while loop를 계속 돌면서 /quit 이 나오기 전까지 계속 입력이 들어오기를 기다림 이때 입력이 들어오는 순간 ChatServer는 들어온 입력값을 받아 처리
			System.out.println("Connection closed.");//  	/quit을 받은 경우 실행
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);  // /quit이 원인이 아닌 다른 ex라는 error 발생시 
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		} // connection 을 끊을때 pw,br,sock 이 null이 아닌경우는 닫아 줘야함.
	} 
} 

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	} //constructor 이며 개인에게 할당된 socket과 입력되어 있는 문장들을 읽어들일 BufferedReader를 파라미터로 받
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line); //입력된 문장들을 해당  client의 화면에 띄우기 위한 과정, 위에서 close하면 br.readline()이 null됨 
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} 
}

