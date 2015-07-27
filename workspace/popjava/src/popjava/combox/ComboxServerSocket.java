

package popjava.combox;

import popjava.broker.Broker;
import popjava.buffer.*;
import popjava.baseobject.AccessPoint;
import popjava.system.POPSystem;

import java.net.*;
import java.io.*;
/**
 * This class is an implementation of the combox with the protocol socket for the server side.
 */
public class ComboxServerSocket extends ComboxServer {
    
	public static final int BUFFER_LENGTH = 1024;
    private final int RECEIVE_BUFFER_SIZE = 1024 * 8 * 500;
    
	protected ServerSocket serverSocket = null;
	private ComboxAcceptSocket serverCombox = null;

	/**
	 * Default constructor. Create a new instance of a socket combox
	 * @param accessPoint	Access point of the combox
	 * @param timeout		Connection timeout
	 * @param buffer		Buffer associated with this combox
	 * @param broker		Broker associated with this combox
	 */
	public ComboxServerSocket(AccessPoint accessPoint, int timeout,
			POPBuffer buffer, Broker broker) {
		super(accessPoint, timeout, broker);
		createServer();
	}

	/**
	 * Get the URL of the combox
	 * @return	URL as a string value
	 */
	public String getUrl() {
		return String.format("%s://%s:%d", ComboxSocketFactory.PROTOCOL,
				POPSystem.getHostIP(), serverSocket.getLocalPort());
	}

	/**
	 * Create and start the combox server
	 */
	public void createServer() {
		try {
			serverSocket = new ServerSocket();
            serverSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
			serverSocket.bind(new InetSocketAddress(accessPoint.getPort()));			
			serverCombox = new ComboxAcceptSocket(broker, requestQueue,
					serverSocket);
			serverCombox.setStatus(RUNNING);
			Thread thread = new Thread(serverCombox, "Server combox acception thread");
			thread.start();
			accessPoint.setProtocol(ComboxSocketFactory.PROTOCOL);
			accessPoint.setHost(POPSystem.getHostIP());
			accessPoint.setPort(serverSocket.getLocalPort());
		} catch (IOException e) {
		}
	}
}
