package popjava.combox.ssl;

import java.io.FileInputStream;
import popjava.broker.Broker;
import popjava.buffer.*;
import popjava.baseobject.AccessPoint;
import popjava.util.Configuration;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import popjava.combox.ComboxServer;

/**
 * This class is an implementation of the combox with the protocol ssl for the server side.
 */
public class ComboxServerSecureSocket extends ComboxServer {

	public static final int BUFFER_LENGTH = 1024;
	private final int RECEIVE_BUFFER_SIZE = 1024 * 8 * 500;

	protected ServerSocket serverSocket = null;

	// we use the simple socker implementation of this since it beahve the same way
	private ComboxAcceptSecureSocket serverCombox = null;

	/**
	 * Default constructor. Create a new instance of a socket combox
	 *
	 * @param accessPoint	Access point of the combox
	 * @param timeout	Connection timeout
	 * @param buffer	Buffer associated with this combox
	 * @param broker	Broker associated with this combox
	 */
	public ComboxServerSecureSocket(AccessPoint accessPoint, int timeout,
			POPBuffer buffer, Broker broker) {
		super(accessPoint, timeout, broker);
		createServer();
	}

	/**
	 * Get the URL of the combox
	 *
	 * @return	URL as a string value
	 */
	public String getUrl() {
		return String.format("%s://%s:%d", ComboxSecureSocketFactory.PROTOCOL,
				serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort());
	}

	/**
	 * Create and start the combox server
	 */
	public void createServer() {
		try {
			// server only, read private key
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(new FileInputStream(Configuration.TRUST_STORE), Configuration.TRUST_STORE_PWD.toCharArray());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, Configuration.TRUST_STORE_PK_PWD.toCharArray());

			SSLContext sslContext = SSLContext.getInstance(Configuration.SSL_PROTOCOL);
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			
			// XXX WHY DO WE NEED THIS ?!!!
			sslContext.getSupportedSSLParameters();
			
			SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
			serverSocket = factory.createServerSocket();
			
			serverSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
			serverSocket.bind(new InetSocketAddress(accessPoint.getPort()));
			serverCombox = new ComboxAcceptSecureSocket(broker, requestQueue, serverSocket);
			serverCombox.setStatus(RUNNING);
			Thread thread = new Thread(serverCombox, "Server combox acception thread");
			thread.start();
			accessPoint.setProtocol(ComboxSecureSocketFactory.PROTOCOL);
			accessPoint.setHost(accessPoint.getHost());
			accessPoint.setPort(serverSocket.getLocalPort());
		} catch (Exception e) {
		}
	}
}
