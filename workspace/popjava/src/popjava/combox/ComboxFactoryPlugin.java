package popjava.combox;


import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
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
			POPBuffer buffer, Broker broker) {
		return null;
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint,
			int timeout, POPBuffer buffer, Broker broker) {
		return null;
	}

	@Override
	public String getComboxName() {
		return null;
	}

	@Override
	public ComboxAllocate createAllocateCombox() {
		return null;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

}
