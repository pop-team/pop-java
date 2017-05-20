package popjava.combox.ssl;

import java.io.IOException;
import java.net.Socket;

import popjava.broker.Broker;
import popjava.broker.RequestQueue;
import popjava.combox.ComboxReceiveRequest;

/**
 * This class is responsible to receive the new request for the associated combox
 */
public class ComboxReceiveRequestSecureSocket extends ComboxReceiveRequest {

	/**
	 * Crate a new instance of ComboxReceiveRequestSocket
	 * @param broker		The associated broker
	 * @param requestQueue	The associated request queue
	 * @param socket		The associated socket
	 * @throws IOException	Thrown if any exception occurred during the process 
	 */
	public ComboxReceiveRequestSecureSocket(Broker broker,
			RequestQueue requestQueue, Socket socket) throws IOException {
		super(broker, requestQueue, new ComboxSecureSocket(socket));
	}
}
