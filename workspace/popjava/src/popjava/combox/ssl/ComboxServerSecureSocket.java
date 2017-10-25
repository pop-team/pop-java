package popjava.combox.ssl;

import java.io.IOException;
import popjava.util.ssl.SSLUtils;
import popjava.broker.Broker;
import popjava.buffer.*;
import popjava.baseobject.AccessPoint;
import java.net.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import popjava.combox.ComboxServer;

/**
 * This class is an implementation of the combox with the protocol ssl for the server side.
 */
public class ComboxServerSecureSocket extends ComboxServer {

	public static final int BUFFER_LENGTH = 1024;
	private final int RECEIVE_BUFFER_SIZE = 1024 * 8 * 500;

	protected SSLServerSocket serverSocket = null;

	// we use the simple socker implementation of this since it beahve the same way
	private ComboxAcceptSecureSocket serverCombox = null;

	/**
	 * Default constructor. Create a new instance of a socket combox
	 *
	 * @param accessPoint	Access point of the combox
	 * @param timeout	Connection timeout
	 * @param buffer	Buffer associated with this combox
	 * @param broker	Broker associated with this combox
	 * @throws java.io.IOException
	 */
	public ComboxServerSecureSocket(AccessPoint accessPoint, int timeout,
			POPBuffer buffer, Broker broker) throws IOException {
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
	 * @throws java.io.IOException
	 */
	public final void createServer() throws IOException {
		SSLContext sslContext;
		try {
			sslContext = SSLUtils.getSSLContext();
		} catch(CertificateException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
			throw new IOException(e);
		}

		SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
		serverSocket = (SSLServerSocket) factory.createServerSocket();
		serverSocket.setNeedClientAuth(true);

		serverSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
		serverSocket.bind(new InetSocketAddress(accessPoint.getPort()));
		serverCombox = new ComboxAcceptSecureSocket(broker, getRequestQueue(), serverSocket);
		serverCombox.setStatus(RUNNING);
		Thread thread = new Thread(serverCombox, "Server combox acception thread");
		thread.start();
		accessPoint.setProtocol(ComboxSecureSocketFactory.PROTOCOL);
		accessPoint.setHost(accessPoint.getHost());
		accessPoint.setPort(serverSocket.getLocalPort());
	}
}
