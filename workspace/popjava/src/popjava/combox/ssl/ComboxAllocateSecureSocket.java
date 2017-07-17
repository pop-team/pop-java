package popjava.combox.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import popjava.buffer.POPBuffer;
import popjava.combox.ComboxAllocate;
import popjava.system.POPSystem;
import popjava.util.LogWriter;

/**
 * This class is responsible to send an receive message on the server combox socket
 */
public class ComboxAllocateSecureSocket extends ComboxAllocate {
	
	private static final int SOCKET_TIMEOUT_MS = 30000;
	
	protected ServerSocket serverSocket = null;	
	private ComboxSecureSocket combox = null;
	
	/**
	 * Create a new instance of the ComboxAllocateSocket
	 */
	public ComboxAllocateSecureSocket() {		
		try {
			SSLContext sslContext = POPTrustManager.getNewSSLContext();
			SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
			
			InetSocketAddress sockAddr = new InetSocketAddress(POPSystem.getHostIP(), 0);
			serverSocket = factory.createServerSocket();
			serverSocket.bind(sockAddr);
			serverSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	/**
	 * Start the socket and wait for a connection
	 */
	@Override
	public void startToAcceptOneConnection() {
		try {
			SSLSocket peerConnection = (SSLSocket) serverSocket.accept();
			peerConnection.setNeedClientAuth(true);
			combox = new ComboxSecureSocket(peerConnection);
		} catch (IOException e) {
			e.printStackTrace();
			LogWriter.writeExceptionLog(e);
		}
	}

	/**
	 * Get URL of this socket
	 * @return	The URL as a string value
	 */
	@Override
	public String getUrl() {
		return String.format("%s://%s:%d", ComboxSecureSocketFactory.PROTOCOL,
				serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
	}

	/**
	 * Close the current connection
	 */
	@Override
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
	@Override
	public int send(POPBuffer buffer) {
		return combox.send(buffer);
	}

	/**
	 * Receive a new message from the other-side
	 * @param buffer	Buffer to receive the message
	 * @return	Number of byte read
	 */
	@Override
	public int receive(POPBuffer buffer) {
		return combox.receive(buffer, -1);
	}
	
	@Override
	public boolean isComboxConnected(){
		return combox != null;
	}

}
