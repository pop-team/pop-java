package popjava.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import popjava.util.SystemUtil;

/**
 * This class starts a deamon that listens to incoming POP requests.
 * It replaces the SSH service to start brokers.
 * @author Beat Wolf
 *
 */
public class POPJavaDeamon {

	public static final int POP_JAVA_DEAMON_PORT = 43424;
	
	/**
	 * Class that handles the accepted connections and runs the Broker with the provided paramters
	 * @author Beat Wolf
	 *
	 */
	private static class Acceptor implements Runnable{
		
		private final Socket socket;
		
		public Acceptor(Socket socket){
			this.socket = socket;
		}
		
		@Override
		public void run() {
			
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//TODO: add security
				
				//Read command to execute
				
				//Execute command
				List<String> commands = new ArrayList<String>();
				
				String line = null;
				
				System.out.println("Execute command: ");
				while((line = reader.readLine()) != null){
					commands.add(line);
					System.out.print(line+" ");
				}
				System.out.println();
				
				SystemUtil.runCmd(commands);
				
				reader.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String ... args) throws IOException{
		
		ServerSocket serverSocket = new ServerSocket(POP_JAVA_DEAMON_PORT);
		
		Executor executor = Executors.newCachedThreadPool();
		
		while(!Thread.interrupted()){
			Socket socket = serverSocket.accept();
			System.out.println("Accepted connection");
			executor.execute(new Acceptor(socket));
		}
		
		serverSocket.close();
	}
	
}
