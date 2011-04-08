package popjava.combox;


import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.Buffer;
/**
 * This class defined the interface for new combox factory plug-in
 */
public class ComboxFactoryPlugin extends ComboxFactory {

	@Override
	public Combox createClientCombox(POPAccessPoint accessPoint) {
		return null;
	}

	@Override
	public Combox createClientCombox(POPAccessPoint accessPoint, int timeout) {
		return null;
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint,
			Buffer buffer, Broker broker) {
		return null;
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint,
			int timeout, Buffer buffer, Broker broker) {
		return null;
	}

	@Override
	public String getComboxName() {
		return null;
	}

}
