package popjava.combox.socket.raw;

import popjava.broker.Broker;
import popjava.broker.RequestQueue;
import popjava.util.LogWriter;

import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.io.*;

import popjava.combox.Combox;
import popjava.combox.ComboxConnection;
import popjava.combox.ComboxReceiveRequest;

/**
 * This class is responsible to accept the new connection for the associated server combox socket
 */
public class ComboxAcceptSocket implements Runnable {

    //TODO: replace with enum
	static public final int RUNNING = 0;
	static public final int EXIT = 1;
	static public final int ABORT = 2;
	
	protected final Broker broker;
	protected final RequestQueue requestQueue;
	protected final ServerSocket serverSocket;
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
				LogWriter.writeDebugInfo("[Socket Accept] Connection accepted "+connection.getLocalPort()+" local:"+connection.getPort());	
				if(broker != null){
					broker.onNewConnection();
				}

				ComboxRawSocket serverClient = new ComboxRawSocket();
				if (serverClient.serverAccept(connection)) {
				    serveConnection(broker, requestQueue, serverClient, 1);
	                concurentConnections.add(connection);
				}
			} catch (IOException e) {
				LogWriter.writeDebugInfo("[Socket Accept] Error while setting up connection: %s", e.getMessage());
			}
		}
		
		LogWriter.writeDebugInfo("[Socket Accept] Combox Server finished");
		this.close();
	}
	
	public static void serveConnection(Broker broker, RequestQueue requestQueue, Combox serverClient, int connectionID) {
	    Runnable runnable = new ComboxReceiveRequest(broker, requestQueue, new ComboxConnection(serverClient, connectionID));
        Thread thread = new Thread(runnable, "Combox request acceptance");
        thread.start();
	}

	/**
	 * Close the current connection
	 */
	public void close() {
		status = EXIT;
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
