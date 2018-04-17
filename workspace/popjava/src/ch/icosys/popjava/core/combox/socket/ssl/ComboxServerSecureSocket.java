package ch.icosys.popjava.core.combox.socket.ssl;

import java.io.IOException;
import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.broker.Broker;
import ch.icosys.popjava.core.buffer.*;
import ch.icosys.popjava.core.combox.socket.ComboxServerSocket;

/**
 * This class is an implementation of the combox with the protocol ssl for the
 * server side.
 */
public class ComboxServerSecureSocket extends ComboxServerSocket {

	/**
	 * Default constructor. Create a new instance of a socket combox
	 *
	 * @param accessPoint
	 *            Access point of the combox
	 * @param timeout
	 *            Connection timeout
	 * @param buffer
	 *            Buffer associated with this combox
	 * @param broker
	 *            Broker associated with this combox
	 * @throws java.io.IOException
	 *             if any problem occurs
	 */
	public ComboxServerSecureSocket(AccessPoint accessPoint, int timeout, POPBuffer buffer, Broker broker)
			throws IOException {
		super(accessPoint, timeout, broker);
	}

	@Override
	protected String getProtocol() {
		return ComboxSecureSocketFactory.PROTOCOL;
	}

	@Override
	protected ComboxAcceptSecureSocket createCombox() throws IOException {
		return new ComboxAcceptSecureSocket(broker, getRequestQueue(), serverSocket);
	}
}
