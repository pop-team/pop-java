package popjava.combox;

import popjava.buffer.POPBuffer;

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
	public void close() {
		if(combox != null){
			combox.close();
		}
	}

	/**
	 * Send a message to the other-side
	 * @param buffer	Buffer to be send
	 * @return	Number of byte sent
	 */
	public final int send(POPBuffer buffer) {
		return combox.send(buffer);
	}

	/**
	 * Receive a new message from the other-side
	 * @param buffer	Buffer to receive the message
	 * @return	Number of byte read
	 */
	public final int receive(POPBuffer buffer) {
		return combox.receive(buffer, -1);
	}
	
	/**
	 * Is the combox 
	 * @return true if the combox is connected
	 */
	public final boolean isComboxConnected(){
		return combox != null;
	}
}
