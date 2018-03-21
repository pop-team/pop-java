

package popjava.combox.socket.raw;

import popjava.broker.Broker;
import popjava.buffer.*;
import popjava.baseobject.AccessPoint;

import java.net.*;
import java.io.*;
import popjava.combox.ComboxServer;
import popjava.combox.ComboxUtils;
import popjava.combox.socket.ComboxServerSocket;
/**
 * This class is an implementation of the combox with the protocol socket for the server side.
 */
public class ComboxServerRawSocket extends ComboxServerSocket {

	/**
	 * Default constructor. Create a new instance of a socket combox
	 * @param accessPoint	Access point of the combox
	 * @param timeout		Connection timeout
	 * @param buffer		Buffer associated with this combox
	 * @param broker		Broker associated with this combox
	 * @throws java.io.IOException if any problem occurs
	 */
	public ComboxServerRawSocket(AccessPoint accessPoint, int timeout,
			POPBuffer buffer, Broker broker) throws IOException {
		super(accessPoint, timeout, broker);
	}	

	@Override
	protected String getProtocol() {
		return ComboxSocketFactory.PROTOCOL;
	}

	@Override
	protected ComboxAcceptRawSocket createCombox() {
		return new ComboxAcceptRawSocket(broker, getRequestQueue(), serverSocket);
	}
}
