package popjava.combox;

import popjava.buffer.POPBuffer;

/**
 * Generalize multiple ways to allocate and initiate a POP object
 */
public abstract class ComboxAllocate {
	public abstract String getUrl();
	public abstract void startToAcceptOneConnection();
	public abstract boolean isComboxConnected();
	public abstract int receive(POPBuffer buffer);
	public abstract int send(POPBuffer buffer);
	public abstract void close();
}
