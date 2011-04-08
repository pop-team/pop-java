package popjava.combox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import popjava.buffer.*;
import popjava.system.POPSystem;

/**
 * This class is responsible to send an receive message on the server combox socket
 */
public class ComboxAllocateSocket {
	protected ServerSocket serverSocket = null;	
	private ComboxSocket combox=null;
	
	/**
	 * Create a new instance of the ComboxAllocateSocket
	 */
	public ComboxAllocateSocket() {		
		try {
			SocketAddress sockAddr = new InetSocketAddress(POPSystem.getHost(), 0);
			serverSocket = new ServerSocket();
			serverSocket.bind(sockAddr);
		} catch (IOException e) {}
	}

	/**
	 * Start the socket and wait for a connection
	 */
	public void startToAcceptOneConnection() {
		try {			
			Socket peerConnection = serverSocket.accept();
			combox=new ComboxSocket(peerConnection);
		} catch (IOException e) {
		}
	}

	/**
	 * Get URL of this socket
	 * @return	The URL as a string value
	 */
	public String getUrl() {
		return String.format("%s://%s:%d", ComboxSocketFactory.Protocol,
				POPSystem.getHost(), serverSocket.getLocalPort());
	}

	/**
	 * Close the current connection
	 */
	public void close() {
		try {
			if(combox!=null)
			combox.close();
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}

		} catch (IOException e) {
		}
	}

	/**
	 * Send a message to the other-side
	 * @param buffer	Buffer to be send
	 * @return	Number of byte sent
	 */
	public int send(Buffer buffer) {
		return combox.send(buffer);
	}

	/**
	 * Receive a new message from the other-side
	 * @param buffer	Buffer to receive the message
	 * @return	Number of byte read
	 */
	public int receive(Buffer buffer) {
		return combox.receive(buffer);
	}

}
