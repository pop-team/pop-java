package ch.icosys.popjava.core.combox.socket.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import ch.icosys.popjava.core.combox.ComboxAllocate;
import ch.icosys.popjava.core.combox.ComboxUtils;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.LogWriter;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

/**
 * This class is responsible to send an receive message on the server combox
 * socket
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
			combox.serverAccept(null, sslConnection);
		} catch (IOException e) {
			e.printStackTrace();
			LogWriter.writeExceptionLog(e);
		}
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

	@Override
	protected String getProtocol() {
		return ComboxSecureSocketFactory.PROTOCOL;
	}

	@Override
	protected String getIP() {
		InetAddress address = serverSocket.getInetAddress();
		
		String ip = address.getHostAddress();
		if(address.isAnyLocalAddress()) {
			ip = POPSystem.getHostIP().getAddress().getHostAddress();
		}
		return null;
	}

	@Override
	protected int getPort() {
		return serverSocket.getLocalPort();
	}

}
