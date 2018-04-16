package ch.icosys.popjava.core.combox.socket;

import java.io.IOException;
import java.net.ServerSocket;

import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.broker.Broker;
import ch.icosys.popjava.core.combox.ComboxServer;
import ch.icosys.popjava.core.combox.ComboxUtils;

public abstract class ComboxServerSocket extends ComboxServer {
    
	public static final int BUFFER_LENGTH = 1024;
    private final int RECEIVE_BUFFER_SIZE = 1024 * 8 * 500;
    
	protected ServerSocket serverSocket = null;
	private ComboxAcceptSocket serverCombox = null;

	public ComboxServerSocket(AccessPoint accessPoint, int timeout, Broker broker) throws IOException {
		super(accessPoint, timeout, broker);
		createServer();
	}
	
	/**
	 * Create and start the combox server
	 * @throws java.io.IOException if any problem occurs
	 */
	public final void createServer() throws IOException {
		serverSocket = ComboxUtils.createServerSocket(accessPoint.getPort(), ss -> ss.setReceiveBufferSize(RECEIVE_BUFFER_SIZE), broker.isUPNPEnabled());
		serverCombox = createCombox();
		serverCombox.setStatus(RUNNING);
		Thread thread = new Thread(serverCombox, "Server combox acception thread");
		thread.start();
		accessPoint.setProtocol(getProtocol());
		accessPoint.setHost(accessPoint.getHost());
		accessPoint.setPort(serverSocket.getLocalPort());
	}

	/**
	 * Get the URL of the combox
	 * @return	URL as a string value
	 */
	public String getUrl() {
		return String.format("%s://%s:%d", getProtocol(),
				serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
	}

	@Override
	public void close() {
		serverCombox.close();
	}
	
	protected abstract String getProtocol();
	
	protected abstract ComboxAcceptSocket createCombox() throws IOException;
}
