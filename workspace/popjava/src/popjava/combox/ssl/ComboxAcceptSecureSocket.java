package popjava.combox.ssl;

import popjava.broker.Broker;
import popjava.broker.RequestQueue;
import popjava.util.LogWriter;
import popjava.combox.ComboxReceiveRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import popjava.util.ssl.SSLUtils;

/**
 * This class is responsible to accept the new connection for the associated server combox socket
 */
public class ComboxAcceptSecureSocket implements Runnable {

    //TODO: replace with enum
	static public final int RUNNING = 0;
	static public final int EXIT = 1;
	static public final int ABORT = 2;
	
	protected Broker broker;
	protected RequestQueue requestQueue;
	protected ServerSocket serverSocket;
	protected int status = EXIT;
	protected final List<SSLSocket> concurentConnections = Collections.synchronizedList(new LinkedList<SSLSocket>());
	
	protected final SSLContext sslContext;

	/**
	 * Create a new instance of the ComboxAccept socket
	 * @param broker		The associated broker
	 * @param requestQueue	The associated request queue
	 * @param serverSocket		The associated combox socket
	 * @throws java.io.IOException
	 */
	public ComboxAcceptSecureSocket(Broker broker, RequestQueue requestQueue,
			ServerSocket serverSocket) throws IOException {
		this.serverSocket = serverSocket;
		this.broker = broker;
		this.requestQueue = requestQueue;
		
		try {
			sslContext = SSLUtils.getSSLContext();
		} catch(CertificateException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
			throw new IOException("Can't initialize SSL Context", e);
		}
	}

	/**
	 * Start the local thread
	 */
	public void run() {
		// used to upgrade plain sockets to SSL one
		SSLSocketFactory ssf = sslContext.getSocketFactory();
		
		while (status != EXIT) {
			try {
				Socket plainConnection = serverSocket.accept();
				SSLSocket sslConnection = (SSLSocket) ssf.createSocket(plainConnection, plainConnection.getInputStream(), true);
				// set SSL parameters
				sslConnection.setUseClientMode(false);
				sslConnection.setNeedClientAuth(true);
				
				LogWriter.writeDebugInfo("[SSL Accept] Connection accepted "+sslConnection.getLocalPort()+" local:"+sslConnection.getPort());	
				if(broker != null){
					broker.onNewConnection();
				}

				ComboxSecureSocket combox = new ComboxSecureSocket();
				if (combox.serverAccept(sslConnection)) {
					Runnable runnable = new ComboxReceiveRequest(broker, requestQueue, combox);
					Thread thread = new Thread(runnable, "Combox request acceptance");
					thread.start();
					concurentConnections.add(sslConnection);
				}
			} catch (IOException e) {
				LogWriter.writeDebugInfo("[SSL Accept] Error while setting up connection: %s", e.getMessage());
			}
		}
		
		LogWriter.writeDebugInfo("[SSL Accept] Combox Server finished");
		this.close();
	}

	/**
	 * Close the current connection
	 */
	public void close() {
		status = EXIT;
		for (SSLSocket s : concurentConnections) {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			if (!serverSocket.isClosed()){
				serverSocket.close();
			}
		} catch (IOException e) {			
		}
	}
	
	/**
	 * Get the current status
	 * @return	The current status
	 */
	public synchronized int getStatus() {
		return status;
	}

	/**
	 * Set the current status
	 * @param status	The new status
	 */
	public synchronized void setStatus(int status) {
		this.status = status;
	}

}
