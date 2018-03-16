package popjava.combox.socket.raw;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import popjava.combox.ComboxAllocate;
import popjava.combox.ComboxUtils;
import popjava.util.Configuration;
import popjava.util.LogWriter;

/**
 * This class is responsible to send an receive message on the server combox socket
 */
public class ComboxAllocateSocket extends ComboxAllocate<ComboxRawSocket> {
	
	protected ServerSocket serverSocket = null;
	
	/**
	 * Create a new instance of the ComboxAllocateSocket
	 */
	public ComboxAllocateSocket(boolean enableUPNP) {		
		try {
			serverSocket = ComboxUtils.createServerSocket(0, ss->ss.setSoTimeout(Configuration.getInstance().getConnectionTimeout()), enableUPNP);
		} catch (IOException e) {
		    LogWriter.writeExceptionLog(e);
		}
	}

	/**
	 * Start the socket and wait for a connection
	 */
	@Override
	public void startToAcceptOneConnection() {
		try {
			Socket peerConnection = serverSocket.accept();
			combox = new ComboxRawSocket();
			combox.serverAccept(peerConnection);
		} catch (IOException e) {
			LogWriter.writeExceptionLog(e);
		}
	}

	/**
	 * Get URL of this socket
	 * @return	The URL as a string value
	 */
	@Override
	public String getUrl() {
		return String.format("%s://%s:%d", ComboxSocketFactory.PROTOCOL,
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
