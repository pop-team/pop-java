package popjava.combox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import popjava.buffer.POPBuffer;
import popjava.system.POPSystem;
import popjava.util.LogWriter;

/**
 * This class is responsible to send an receive message on the server combox socket
 */
public class ComboxAllocateSocket {
	
	private static final int SOCKET_TIMEOUT_MS = 30000;
	
	protected ServerSocket serverSocket = null;	
	private ComboxSocket combox = null;
	
	/**
	 * Create a new instance of the ComboxAllocateSocket
	 */
	public ComboxAllocateSocket() {		
		try {
			SocketAddress sockAddr = new InetSocketAddress(POPSystem.getHostIP(), 0);
			serverSocket = new ServerSocket();
			serverSocket.bind(sockAddr);
			serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	/**
	 * Start the socket and wait for a connection
	 */
	public void startToAcceptOneConnection() {
		try {
			Socket peerConnection = serverSocket.accept();
			combox = new ComboxSocket(peerConnection);
		} catch (IOException e) {
			LogWriter.writeExceptionLog(e);
		}
	}

	/**
	 * Get URL of this socket
	 * @return	The URL as a string value
	 */
	public String getUrl() {
		return String.format("%s://%s:%d", ComboxSocketFactory.PROTOCOL,
				POPSystem.getHostIP(), serverSocket.getLocalPort());
	}

	/**
	 * Close the current connection
	 */
	public void close() {
		try {
			if(combox != null){
				combox.close();
			}
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
	public int send(POPBuffer buffer) {
		return combox.send(buffer);
	}

	/**
	 * Receive a new message from the other-side
	 * @param buffer	Buffer to receive the message
	 * @return	Number of byte read
	 */
	public int receive(POPBuffer buffer) {
		return combox.receive(buffer, -1);
	}
	
	public boolean isComboxConnected(){
		return combox != null;
	}

}
