package popjava.combox.ssl;

import popjava.broker.Broker;
import popjava.broker.RequestQueue;
import popjava.util.LogWriter;
import popjava.combox.ComboxReceiveRequest;

import java.io.IOException;
import java.util.LinkedList;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

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
	protected SSLServerSocket serverSocket;
	protected int status = EXIT;
	protected final LinkedList<SSLSocket> concurentConnections = new LinkedList<>();

	/**
	 * Create a new instance of the ComboxAccept socket
	 * @param broker		The associated broker
	 * @param requestQueue	The associated request queue
	 * @param serverSocket		The associated combox socket
	 */
	public ComboxAcceptSecureSocket(Broker broker, RequestQueue requestQueue,
			SSLServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		this.broker = broker;
		this.requestQueue = requestQueue;
	}

	/**
	 * Start the local thread
	 */
	public void run() {
		while (status != EXIT) {
			SSLSocket connection = null;
			try {
				connection = (SSLSocket) serverSocket.accept();
				LogWriter.writeDebugInfo("[SSL Accept] Connection accepted "+connection.getLocalPort()+" local:"+connection.getPort());	
				if(broker != null){
					broker.onNewConnection();
				}
				
				// force srart of handshake from server side
				connection.startHandshake();
				
				synchronized (concurentConnections) {
					concurentConnections.add(connection);
				}

				Runnable runnable = new ComboxReceiveRequest(broker, requestQueue, new ComboxSecureSocket(connection));
				Thread thread = new Thread(runnable, "Combox request acceptance");
				thread.start();
			} catch (IOException e) {
				LogWriter.writeDebugInfo("[SSL Accept] Error while setting up connection: %s", e.getMessage());
				break;
			}
		}
		
		LogWriter.writeDebugInfo("[SSL Accept] Combox Server finished");
		this.close();
	}

	/**
	 * Close the current connection
	 */
	public void close() {
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
