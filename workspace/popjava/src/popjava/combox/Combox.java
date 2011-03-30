package popjava.combox;


import popjava.baseobject.POPAccessPoint;
import popjava.buffer.*;
import popjava.util.Configuration;
/**
 * This class is the base implementation for all Combox in the POP-Java library
 * All other combox must inherit from this class
 */
public abstract class Combox {
	protected int timeOut = 0;
	protected POPAccessPoint accessPoint;
	protected boolean available = false;
	protected BufferFactory bufferFactory;
	
	/**
	 * Default constructor
	 */
	public Combox() {
		this(new POPAccessPoint(), 0);
	}

	/**
	 * Constructor with given values
	 * @param accesspoint	Access point to create the combox
	 * @param timeout		Connection time out
	 */
	public Combox(POPAccessPoint accesspoint, int timeout) {
		accessPoint = accesspoint;
		timeOut = timeout;
		bufferFactory = BufferFactoryFinder.getInstance().findFactory(
				Configuration.DefaultEncoding);
	}

	/**
	 * Connect the current combox to the other side combox
	 * @param accesspoint	Access point of the other side combox
	 * @param timeout		Connection time out
	 * @return true if the connection is established
	 */
	public boolean connect(POPAccessPoint accesspoint, int timeout)
	{
		this.accessPoint = accesspoint;
		this.timeOut = timeout;
		return connect();
	}

	/**
	 * Send the buffer to the other side
	 * @param buffer	The buffer to send
	 * @return	Number of byte sent
	 */
	public abstract int send(Buffer buffer);

	/**
	 * Receive buffer from the other side
	 * @param buffer	Buffer to receive
	 * @return	Number of byte received
	 */
	public abstract int receive(Buffer buffer);

	/**
	 * Close the connection
	 */
	public abstract void close();

	/**
	 * Connect to the other side
	 * @return	true if the connection succeed
	 */
	public abstract boolean connect();

	/**
	 * Associate a buffer factory to the combox
	 * @param bufferFactory	The buffer factory to associate
	 */
	public void setBufferFactory(BufferFactory bufferFactory) {		
		this.bufferFactory = bufferFactory;
	}

	/**
	 * Get the associated buffer factory
	 * @return	The associated buffer factory
	 */
	public BufferFactory getBufferFactory() {
		return bufferFactory;
	}	
}
