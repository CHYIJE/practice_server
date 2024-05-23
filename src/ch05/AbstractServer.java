package ch05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// 상속의 활용
public abstract class AbstractServer {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private BufferedReader readerStream;
	private PrintWriter writerStream;
	private BufferedReader keyboardReader;

	// set 메서드
	// 메서드 의존 주입(멤버 변수의 참조 변수 할당) 
	protected void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	protected void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	// get 메서드
	protected ServerSocket getServerSocket() {
		return this.serverSocket;
	}
	
	// 실행의 흐름이 필요하다.(순서가 중요)
	// 메서드에서 final을 쓰면 자식 클래스에서 오버라이딩 불가
	public final void run() {
		// 1. 서버 셋팅 = 포트번호 할당
		try {
			setupServer();
			connection();
			setupStream();
			startService(); // 내부적으로 while 동작
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 1. 포트번호 할당(구현 클래스에서 직접 설계)
	protected abstract void setupServer () throws IOException;
	
	// 2. 클라이언트 연결 대기 실행 (구현 클래스)
	protected abstract void connection() throws IOException;
	
	// 3. 스트림 초기화 (연결된 소켓에서 스트림을 뽑아야 함) - 여기서 함
	private void setupStream() throws IOException {
		readerStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writerStream = new PrintWriter(socket.getOutputStream(), true);
		keyboardReader = new BufferedReader(new InputStreamReader(System.in));
	}
	// 4. 서비스 시작
	private void startService() {
		// while <--
		Thread readThread = createReadThread();
		// while -->
		Thread wrThread = createWriteThread();
		
		readThread.start();
		wrThread.start();
		
		try {
			readThread.join();
			wrThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 캡슐화 
	private Thread createReadThread() {
		return new Thread(() -> {
			try {
				String msg;
				// 
				while((msg = readerStream.readLine()) != null) {
					// 서버측 콘솔에 출력
					System.out.println("client 측 msg : " + msg);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	private Thread createWriteThread() {
		return new Thread(() -> {
			try {
				String msg;
				while((msg = keyboardReader.readLine()) != null) {
				writerStream.println(msg);
				writerStream.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	// 캡슐화 - 소켓 자원 종료
	private void cleanup() {
		try {
			if(socket != null) {
				socket.close();
			}
			if(serverSocket != null) {
				serverSocket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
