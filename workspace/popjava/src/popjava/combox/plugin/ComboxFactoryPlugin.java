package popjava.combox.plugin;


import popjava.baseobject.AccessPoint;
import popjava.broker.Broker;
import popjava.buffer.POPBuffer;
import popjava.combox.Combox;
import popjava.combox.ComboxAllocate;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxServer;
/**
 * This class defined the interface for new combox factory plug-in
 */
public class ComboxFactoryPlugin extends ComboxFactory {

	@Override
	public Combox createClientCombox(String networkUUID) {
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
		return super.isAvailable();
	}

	@Override
	public boolean isSecure() {
		return false;
	}

}
