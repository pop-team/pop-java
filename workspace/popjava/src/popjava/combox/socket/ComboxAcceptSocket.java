package popjava.combox.socket;

import popjava.broker.Broker;
import popjava.broker.RequestQueue;
import popjava.util.LogWriter;

import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.io.*;
import popjava.combox.ComboxReceiveRequest;

/**
 * This class is responsible to accept the new connection for the associated server combox socket
 */
public class ComboxAcceptSocket implements Runnable {

    //TODO: replace with enum
	static public final int RUNNING = 0;
	static public final int EXIT = 1;
	static public final int ABORT = 2;
	
	protected Broker broker;
	protected RequestQueue requestQueue;
	protected ServerSocket serverSocket;
	protected int status = EXIT;
	protected final List<Socket> concurentConnections = new LinkedList<>();

	/**
	 * Create a new instance of the ComboxAccept socket
	 * @param broker		The associated broker
	 * @param requestQueue	The associated request queue
	 * @param socket		The associated combox socket
	 */
	public ComboxAcceptSocket(Broker broker, RequestQueue requestQueue,
			ServerSocket socket) {
		serverSocket = socket;
		this.broker = broker;
		this.requestQueue = requestQueue;
	}

	/**
	 * Start the local thread
	 */
	public void run() {
		while (status != EXIT) {
			Socket connection = null;
			try {
				connection = serverSocket.accept();
				LogWriter.writeDebugInfo("Connection accepted "+connection.getLocalPort()+" local:"+connection.getPort());	
				if(broker != null){
					broker.onNewConnection();
				}
				synchronized (concurentConnections) {
					concurentConnections.add(connection);
				}

				Runnable runnable = new ComboxReceiveRequest(broker, requestQueue, new ComboxSocket(connection));
				Thread thread = new Thread(runnable, "Combox request acceptance");
				thread.start();
			} catch (IOException e) {				
			}
		}
		
		LogWriter.writeDebugInfo("Combox Server finished");
		this.close();
	}

	/**
	 * Close the current connection
	 */
	public void close() {
		for (Socket s : concurentConnections) {
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
