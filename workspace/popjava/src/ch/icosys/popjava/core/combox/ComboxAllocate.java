package ch.icosys.popjava.core.combox;

import ch.icosys.popjava.core.base.MessageHeader;
import ch.icosys.popjava.core.buffer.POPBuffer;

/**
 * Generalize multiple ways to allocate and initiate a POP object
 * @param <T> Allocate a combox
 */
public abstract class ComboxAllocate<T extends Combox> {
	
	protected T combox;
	
	/**
	 * Start a server of some kind that will accept one (1) connection.
	 */
	public abstract void startToAcceptOneConnection();
	
	/**
	 * The URL where the server can be contacted.
	 * @return the url waiting for a broker connection
	 */
	public abstract String getUrl();
	
	/**
	 * Close the combox
	 */
	public void close(int connectionID) {
		if(combox != null){
			combox.close(connectionID);
		}
	}

	/**
	 * Receive a new message from the other-side
	 * @param buffer	Buffer to receive the message
	 * @return	Number of byte read
	 */
	public final int receive(POPBuffer buffer, int connectionID) {
		return combox.receive(buffer, -1, connectionID);
	}
	
	/**
	 * Is the combox 
	 * @return true if the combox is connected
	 */
	public final boolean isComboxConnected(){
		return combox != null;
	}
}
