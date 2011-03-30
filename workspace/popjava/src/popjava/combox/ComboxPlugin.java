package popjava.combox;

import popjava.buffer.Buffer;

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
	public int receive(Buffer buffer) {
		return 0;
	}

	@Override
	public int send(Buffer buffer) {
		return 0;
	}

}
