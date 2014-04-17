package junit.benchmarks.methods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer implements Runnable{

	public static final int PORT = 5543;
	
	private static String [] complexReturn = new String[]{"asdfasdf", "asdfasdf", "asdfasdf"};
	
	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(PORT);
			Socket socket = serverSocket.accept();
			
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			int data = -1;
			while((data = in.read()) != -1 && !Thread.interrupted()){
				switch(data){
				case 1:
					break;
				case 2:
					out.write(100);
					break;
				case 3:
					out.write(complexReturn.length);
					for(String s: complexReturn){
						out.write(s.length());
						out.write(s.getBytes());
					}
					break;
				}
			}
			
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	

}
