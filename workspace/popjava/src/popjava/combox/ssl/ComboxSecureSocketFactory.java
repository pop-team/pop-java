package popjava.combox.ssl;

import java.io.IOException;
import javax.net.ssl.SSLContext;
import popjava.util.ssl.SSLUtils;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxAllocate;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxServer;
import popjava.util.Configuration;
import popjava.util.LogWriter;

/**
 * This class is the factory for all combox socket
 */
public class ComboxSecureSocketFactory extends ComboxFactory {
	
	/**
	 * Name of the implemented protocol
	 */
	public static final String PROTOCOL = "ssl";
	private static final Configuration conf = Configuration.getInstance();

	@Override
	public String getComboxName() {
		return PROTOCOL;
	}

	@Override
	public Combox createClientCombox(POPAccessPoint accessPoint) {
		return createClientCombox(accessPoint, conf.getConnectionTimeout());
	}

	@Override
	public Combox createClientCombox(POPAccessPoint accessPoint, int timeout) {
		return new ComboxSecureSocket(accessPoint, timeout);
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint,
			POPBuffer buffer, Broker broker) throws IOException {
		return createServerCombox(accessPoint, conf.getConnectionTimeout(), buffer, broker);
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint,
			int timeout, POPBuffer buffer, Broker broker) throws IOException {
		return new ComboxServerSecureSocket(accessPoint, timeout, buffer, broker);
	}

	@Override
	public ComboxAllocate createAllocateCombox() {
		return new ComboxAllocateSecureSocket();
	}

	@Override
	public boolean isAvailable() {
		if (!super.isAvailable()) {
			return false;
		}
		try {
			SSLContext context = SSLUtils.getSSLContext();
			return true;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[SSL Combox] can't be initialized correctly: %s", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean isSecure() {
		return true;
	}
}
