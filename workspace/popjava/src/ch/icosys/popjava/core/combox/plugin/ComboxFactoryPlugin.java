package ch.icosys.popjava.core.combox.plugin;

import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.broker.Broker;
import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.combox.Combox;
import ch.icosys.popjava.core.combox.ComboxAllocate;
import ch.icosys.popjava.core.combox.ComboxFactory;
import ch.icosys.popjava.core.combox.ComboxServer;

/**
 * This class defined the interface for new combox factory plug-in
 */
public class ComboxFactoryPlugin extends ComboxFactory {

	@Override
	public Combox createClientCombox(String networkUUID) {
		return null;
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint, POPBuffer buffer, Broker broker) {
		return null;
	}

	@Override
	public ComboxServer createServerCombox(AccessPoint accessPoint, int timeout, POPBuffer buffer, Broker broker) {
		return null;
	}

	@Override
	public String getComboxName() {
		return null;
	}

	@Override
	public ComboxAllocate createAllocateCombox(boolean enableUPNP) {
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
