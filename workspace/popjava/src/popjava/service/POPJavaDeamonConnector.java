package popjava.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Connector class for the POPJavaDeamon.
 * Connects to the remote deamon and starts a command
 * @author Beat Wolf
 *
 */
public class POPJavaDeamonConnector {

	private final Socket socket;
	
	public POPJavaDeamonConnector(String url) throws UnknownHostException, IOException{
		this(url, POPJavaDeamon.POP_JAVA_DEAMON_PORT);
	}
	
	public POPJavaDeamonConnector(String url, int port) throws UnknownHostException, IOException{
		socket = new Socket(url, port);
	}
	
	/**
	 * Sends a command to the remote deamon and closes the connection.
	 * @param secret
	 * @param command
	 * @throws IOException
	 */
	public boolean sendCommand(String secret, List<String> command) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String salt = reader.readLine();
		String saltedHash = POPJavaDeamon.getSaltedHash(salt, secret);
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		writer.write(saltedHash+"\n");
		writer.write(command.size()+"\n");
		for(String part: command){
			writer.write(part+"\n");
		}
		writer.flush();
		
		String answer = reader.readLine();
		writer.close();
		
		return answer != null && answer.equals(POPJavaDeamon.SUCCESS);
	}
}
