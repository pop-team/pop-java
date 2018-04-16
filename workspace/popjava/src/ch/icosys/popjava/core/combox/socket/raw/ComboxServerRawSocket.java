

package ch.icosys.popjava.core.combox.socket.raw;

import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.broker.Broker;
import ch.icosys.popjava.core.buffer.*;
import ch.icosys.popjava.core.combox.socket.ComboxServerSocket;

import java.io.*;
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
