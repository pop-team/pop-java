package popjava.combox;

import popjava.buffer.POPBuffer;

/**
 * This class defined the interface for each new combox plug-in
 */
public class ComboxPlugin extends Combox {

	@Override
	public void close() {

	}

	@Override
	public boolean connect() {
		return false;
	}

	@Override
	public int receive(POPBuffer buffer, int requestId) {
		return 0;
	}

	@Override
	public int send(POPBuffer buffer) {
		return 0;
	}

}
