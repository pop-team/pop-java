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
import popjava.combox.socket.ComboxAcceptSocket;

/**
 * This class is responsible to accept the new connection for the associated server combox socket
 */
public class ComboxAcceptRawSocket extends ComboxAcceptSocket<Socket> {

	/**
	 * Create a new instance of the ComboxAccept socket
	 * @param broker		The associated broker
	 * @param requestQueue	The associated request queue
	 * @param socket		The associated combox socket
	 */
	public ComboxAcceptRawSocket(Broker broker, RequestQueue requestQueue,
			ServerSocket socket) {
		super(broker, requestQueue, socket);
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
				if (serverClient.serverAccept(broker, connection)) {
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
}
