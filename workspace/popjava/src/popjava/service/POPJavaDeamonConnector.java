package popjava.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;

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
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		String salt = reader.readLine();
		String saltedHash = POPJavaDeamon.getSaltedHash(salt, secret);
		

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream()));
		
		writer.write(saltedHash+"\n");
		writer.flush();
		
		String connectionStatus = reader.readLine();
		
		if(POPJavaDeamon.ERROR_PWD.equals(connectionStatus)){
			reader.close();
			writer.close();
			return false;
		}
		
		//Reopen the stream, this time crypted
		reader = new BufferedReader(
				new InputStreamReader(
						new CipherInputStream(socket.getInputStream(), POPJavaDeamon.createKey(secret, false))));
		
		Cipher outputCipher = POPJavaDeamon.createKey(secret, true);
		
		CipherOutputStream outputCipherStream = new CipherOutputStream(socket.getOutputStream(), 
				outputCipher);
		
		writer = new BufferedWriter(new OutputStreamWriter(outputCipherStream));
		
		writer.write(command.size()+"\n");
		for(String part: command){
			writer.write(part+"\n");
		}
		
		//HACK: We need to fill the current data block for the cipher, otherwise it wont get sent on flush
		for(int i = 0; i < 100; i++){
			writer.write("\n");
		}
		
		writer.flush();
		
		String answer = reader.readLine();
		System.out.println(answer);
		
		reader.close();
		
		return answer != null && answer.equals(POPJavaDeamon.SUCCESS);
	}
}
