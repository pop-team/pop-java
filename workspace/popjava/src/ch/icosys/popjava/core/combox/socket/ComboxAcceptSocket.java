package ch.icosys.popjava.core.combox.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import ch.icosys.popjava.core.broker.Broker;
import ch.icosys.popjava.core.broker.RequestQueue;
import ch.icosys.popjava.core.combox.Combox;
import ch.icosys.popjava.core.combox.ComboxConnection;
import ch.icosys.popjava.core.combox.ComboxReceiveRequest;

public abstract class ComboxAcceptSocket<E extends Socket> implements Runnable {

    //TODO: replace with enum
	static public final int RUNNING = 0;
	static public final int EXIT = 1;
	static public final int ABORT = 2;
	
	protected final Broker broker;
	protected final RequestQueue requestQueue;
	protected final ServerSocket serverSocket;
	protected int status = EXIT;
	protected final List<E> concurentConnections = new LinkedList<E>();
	
	protected ComboxAcceptSocket(Broker broker, RequestQueue requestQueue, ServerSocket serverSocket) {
		this.broker = broker;
		this.requestQueue = requestQueue;
		this.serverSocket = serverSocket;
	}
	


	/**
	 * Close the current connection
	 */
	public void close() {
		status = EXIT;
		for (E s : concurentConnections) {
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
		
	public static void serveConnection(Broker broker, RequestQueue requestQueue, Combox serverClient, int connectionID) {
	    Runnable runnable = new ComboxReceiveRequest(broker, requestQueue, new ComboxConnection(serverClient, connectionID));
        Thread thread = new Thread(runnable, "Combox request acceptance "+serverClient.getAccessPoint()+" "+connectionID);
        thread.start();
	}
}
