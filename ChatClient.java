import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClient {

	public static void main(String[] args) {
		if(args.length != 2){ //필요한 args는 id와 ip
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1); //끝내기
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		Date Today = new Date();
		SimpleDateFormat Time = new SimpleDateFormat("HH:mm:ss");
		try{
			sock = new Socket(args[1], 10001); //받은 ip로 10001포트와의 socket 생성
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			//keyboard로 부터 받은 입력값 처리
			pw.println(args[0]);
			//여기서 pw로 id를 쓰면 server에서 해당 client의 thread에서는 br로 이 id를 받음
			pw.flush();
			InputThread it = new InputThread(sock, br);
			//해당 유저의 sock과 br을 받아 client마다 InputThread 생성
			it.start();
			//it의 run실행
			String line = null;
			while((line = keyboard.readLine()) != null){ //keyboard로 부터 문장 입력받음
				pw.println(line); 
				//line을 pw에 입력하고 이것을 server에서 해당 client의 thread에서는 br로 이 문장을 받음
				pw.flush();
				if(line.equals("/quit")){//server에서도 quit
					endflag = true;
					break;
				}
			}
			System.out.println("["+Time.format(Today)+ "]" +"Connection closed."); //해당 clent화면에 출력
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
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
		} // 모두 실행 종료
	} 
} 

class InputThread extends Thread{ //목적:thread를 추가적으로 실행해서 client화면에 자기가 쓴 문장이 보이도록 하기 위함
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){ 
			//입력된 문장을 읽어들임
				System.out.println(line);
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

