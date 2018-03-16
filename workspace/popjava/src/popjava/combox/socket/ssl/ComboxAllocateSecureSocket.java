package popjava.combox.socket.ssl;

import popjava.util.ssl.SSLUtils;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import popjava.combox.ComboxAllocate;
import popjava.combox.ComboxUtils;
import popjava.util.LogWriter;

/**
 * This class is responsible to send an receive message on the server combox socket
 */
public class ComboxAllocateSecureSocket extends ComboxAllocate {
	
	protected ServerSocket serverSocket = null;	
	protected SSLSocketFactory sslFactory = null;
	
	/**
	 * Create a new instance of the ComboxAllocateSocket
	 */
	public ComboxAllocateSecureSocket(boolean enableUPNP) {		
		try {
			SSLContext sslContext = SSLUtils.getSSLContext();
			sslFactory = sslContext.getSocketFactory();			
			serverSocket = ComboxUtils.createServerSocket(0, ss -> ss.setSoTimeout(30000), enableUPNP);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	/**
	 * Start the socket and wait for a connection
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void startToAcceptOneConnection() {
		try {
			Socket plainConnection = serverSocket.accept();
			SSLSocket sslConnection = (SSLSocket) sslFactory.createSocket(plainConnection, plainConnection.getInputStream(), true);
			sslConnection.setUseClientMode(false);
			sslConnection.setNeedClientAuth(true);
			combox = new ComboxSecureSocket();
			combox.serverAccept(sslConnection);
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
		super.close();
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}

		} catch (IOException e) {
		}
	}

}
