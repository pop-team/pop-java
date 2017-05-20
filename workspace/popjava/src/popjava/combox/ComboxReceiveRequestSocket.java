package popjava.combox;

import java.io.IOException;
import java.net.Socket;

import popjava.broker.Broker;
import popjava.broker.RequestQueue;

/**
 * This class is responsible to receive the new request for the associated combox
 */
public class ComboxReceiveRequestSocket extends ComboxReceiveRequest {

 
	/**
	 * Crate a new instance of ComboxReceiveRequestSocket
	 * @param broker		The associated broker
	 * @param requestQueue	The associated request queue
	 * @param socket		The associated socket
	 * @throws IOException	Thrown if any exception occurred during the process 
	 */
	public ComboxReceiveRequestSocket(Broker broker,
			RequestQueue requestQueue, Socket socket) throws IOException {
		super(broker, requestQueue, new ComboxSocket(socket));
	}

}
