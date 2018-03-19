package popjava.combox.plugin;

import popjava.buffer.POPBuffer;
import popjava.combox.Combox;

/**
 * This class defined the interface for each new combox plug-in
 */
public class ComboxPlugin extends Combox<Object> {

	public ComboxPlugin() {
		super(null);
	}

	@Override
	public void close(int connectionID) {

	}

	@Override
	protected boolean connectToServer() {
		return false;
	}

	@Override
	public int receive(POPBuffer buffer, int requestId, int connectionID) {
		return 0;
	}

	@Override
	public int send(POPBuffer buffer, int connectionID) {
		return 0;
	}

	@Override
	protected boolean exportConnectionInfo() {
		return false;
	}

	@Override
	protected boolean sendNetworkName() {
		return false;
	}

	@Override
	protected boolean receiveNetworkName() {
		return false;
	}

	@Override
	protected boolean serverAccept() {
		return false;
	}

	@Override
	protected void closeInternal() {
		
	}

}
