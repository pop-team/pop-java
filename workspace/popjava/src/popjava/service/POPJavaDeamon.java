package popjava.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import popjava.system.POPJavaConfiguration;
import popjava.util.SystemUtil;

/**
 * This class starts a deamon that listens to incoming POP requests.
 * It replaces the SSH service to start brokers.
 * @author Beat Wolf
 *
 */
public class POPJavaDeamon {

	public static final int POP_JAVA_DEAMON_PORT = 43424;
	private ServerSocket serverSocket;
	private static String secret = "";
	
	/**
	 * Class that handles the accepted connections and runs the Broker with the provided paramters
	 * @author Beat Wolf
	 *
	 */
	private static class Acceptor implements Runnable{
		
		private final Socket socket;
		private Random rand = new Random();
		private static final int SECRET_LENGTH = 10;
		
		public Acceptor(Socket socket){
			this.socket = socket;
		}
		
		private String createSecret(){
			String secret = "";
			
			for(int i = 0;i < SECRET_LENGTH; i++){
				secret += (char)(rand.nextInt(26) + 'a');
			}
			
			return secret;
		}
		
		@Override
		public void run() {
			
			try {
				String salt = createSecret();
				System.out.println("SALT: "+salt);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				writer.write(salt+"\n");
				writer.flush();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//TODO: add security
				
				
				//Read command to execute
				
				//Execute command
				List<String> commands = new ArrayList<String>();
				
				String line = null;
				
				System.out.println("Execute command: ");
				boolean isJava = false;
				int parameterIndex = 0;
				
				boolean isClassPath = false;
				
				String saltedHash = getSaltedHash(salt, secret);
				
				while((line = reader.readLine()) != null){
					//The supplied secret was wrong
					if(parameterIndex == 0 && !saltedHash.equals(line)){
						System.err.println("The supplied secret was wrong : "+line+" should be "+saltedHash);
						return;
					}
					
					if(isJava && isClassPath){ //If the current parameter is the classpath, modify it to fit local system
						if(!line.contains(File.pathSeparator) &&
								!new File(line).exists()){
							String temp = POPJavaConfiguration.getPOPJavaCodePath();
							if(temp != null && !temp.isEmpty()){
								line = temp;
							}							
						}
					}
					
					if(parameterIndex > 0){
						commands.add(line);
						System.out.print(line+" ");
					}

					if(parameterIndex == 1 && line.equals("java")){
						isJava = true;
					}
					
					parameterIndex++;
					
					isClassPath = line.equals("-cp");
				}
				System.out.println();
				
				SystemUtil.runCmd(commands);
				
				reader.close();
				writer.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String ... args) throws IOException{
		POPJavaDeamon deamon = new POPJavaDeamon();
		deamon.start();
	}
	
	/**
	 * Starts the POP-Java listener deamon
	 * @throws IOException
	 */
	public void start() throws IOException{
		serverSocket = new ServerSocket(POP_JAVA_DEAMON_PORT);
		
		Executor executor = Executors.newCachedThreadPool();
		
		System.out.println("Started POP-Java deamon");
		
		while(!Thread.interrupted()){
			Socket socket = serverSocket.accept();
			System.out.println("Accepted connection");
			executor.execute(new Acceptor(socket));
		}
		
		serverSocket.close();
		System.out.println("Closed POP-Java deamon");
	}
	
	/**
	 * Stops the POP-Java listener deamon
	 * @throws IOException 
	 */
	public void stop() throws IOException{
		serverSocket.close();
	}
	
	public static String getSaltedHash(String salt, String secret){
		return ""+(secret + salt).hashCode();
	}
}
