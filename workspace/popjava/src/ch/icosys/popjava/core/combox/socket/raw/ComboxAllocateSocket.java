package ch.icosys.popjava.core.combox.socket.raw;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import ch.icosys.popjava.core.combox.ComboxAllocate;
import ch.icosys.popjava.core.combox.ComboxUtils;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.LogWriter;

/**
 * This class is responsible to send an receive message on the server combox
 * socket
 */
public class ComboxAllocateSocket extends ComboxAllocate<ComboxRawSocket> {

	protected ServerSocket serverSocket = null;

	/**
	 * Create a new instance of the ComboxAllocateSocket
	 */
	public ComboxAllocateSocket(boolean enableUPNP) {
		try {
			serverSocket = ComboxUtils.createServerSocket(0,
					ss -> ss.setSoTimeout(Configuration.getInstance().getConnectionTimeout()), enableUPNP);
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
			combox.serverAccept(null, peerConnection);
		} catch (IOException e) {
			LogWriter.writeExceptionLog(e);
		}
	}

	/**
	 * Get URL of this socket
	 * 
	 * @return The URL as a string value
	 */
	@Override
	public String getUrl() {
		InetAddress address = serverSocket.getInetAddress();
		
		String ip = address.getHostAddress();
		if(address.isAnyLocalAddress()) {
			ip = POPSystem.getHostIP();
		}
		
		return String.format("%s://%s:%d", ComboxSocketFactory.PROTOCOL, ip,
				serverSocket.getLocalPort());
	}

	/**
	 * Close the current connection
	 */
	@Override
	public void close(int connectionID) {
		super.close(connectionID);
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e) {
		}
	}
}
