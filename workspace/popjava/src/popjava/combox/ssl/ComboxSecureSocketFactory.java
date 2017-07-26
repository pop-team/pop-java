package popjava.combox.ssl;

import javax.net.ssl.SSLContext;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.ConnectionProtocol;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxAllocate;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxServer;
import popjava.util.LogWriter;

/**
 * This class is the factory for all combox socket
 */
public class ComboxSecureSocketFactory extends ComboxFactory {
	
	/**
	 * Name of the implemented protocol
	 */
	public static final String PROTOCOL = ConnectionProtocol.SSL.getName();

	@Override
	public String getComboxName() {
		return PROTOCOL;
	}

	@Override
	public Combox createClientCombox(POPAccessPoint accessPoint) {
		return new ComboxSecureSocket(accessPoint, 0);
	}

	@Override
	public Combox createClientCombox(POPAccessPoint accessPoint, int timeout) {
		return new ComboxSecureSocket(accessPoint, timeout);
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint,
			POPBuffer buffer, Broker broker) {
		return new ComboxServerSecureSocket(accessPoint, 0, buffer, broker);
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint,
			int timeout, POPBuffer buffer, Broker broker) {
		return new ComboxServerSecureSocket(accessPoint, timeout, buffer, broker);
	}

	@Override
	public ComboxAllocate createAllocateCombox() {
		return new ComboxAllocateSecureSocket();
	}

	@Override
	public boolean isAvailable() {
		try {
			SSLContext context = SSLUtils.getSSLContext();
			return true;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[SSL Combox] can't be initialized correctly: %s", e.getMessage());
			return false;
		}
	}

}
