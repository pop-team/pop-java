package popjava.combox;


import popjava.baseobject.POPAccessPoint;
import popjava.buffer.BufferFactory;
import popjava.buffer.BufferFactoryFinder;
import popjava.buffer.POPBuffer;
import popjava.util.Configuration;
import popjava.util.POPRemoteCaller;
/**
 * This class is the base implementation for all Combox in the POP-Java library
 * All other combox must inherit from this class
 * @param <T> The "real" connection of the Combox, be it a Socket or a Pipe.
 */
public abstract class Combox<T> {
	
	protected int timeOut = 0;
	protected POPAccessPoint accessPoint;
	protected boolean available = false;
	protected BufferFactory bufferFactory;
	
	protected T peerConnection;
	
	protected String networkUUID;
	protected POPRemoteCaller remoteCaller;
	
	protected final Configuration conf = Configuration.getInstance();

	/**
	 * This is used by ServerCombox (server).
	 * Create a new combox from a server.
	 * Call {@link #serverAccept(java.lang.Object)   } to let the client connect.
	 */
	public Combox() {
		this(null);
	}
	
	/**
	 * This is used by Combox (client).
	 * Create a combox for a client.
	 * Call {@link #connectToServer(popjava.baseobject.POPAccessPoint, int)  } to actually connect the client.
	 * @param networkUUID	The network UUID that will be send to the other end
	 */
	public Combox(String networkUUID) {
		this.networkUUID = networkUUID;
		this.accessPoint = new POPAccessPoint();
		bufferFactory = BufferFactoryFinder.getInstance().findFactory(conf.getDefaultEncoding());
	}

	/**
	 * Connect to a ServerCombox on the other side, this will result in a Combox (client mode) communicating with a 
	 * Combox (server mode).
	 * @param accesspoint	Access point of the other side combox
	 * @param timeout		Connection time out
	 * @return true if the connection is established
	 */
	public final boolean connectToServer(POPAccessPoint accesspoint, int timeout) {
		this.accessPoint = accesspoint;
		this.timeOut = timeout;
		System.out.println("=== Combox will send SNI = '"+ networkUUID +"'");
		boolean status = connectToServer();
		status &= sendNetworkName();
		status &= exportConnectionInfo();
		return status;
	}
	
	/**
	 * Accept a connection from {@link #connectToServer() }.
	 * Communicate with Client mode Combox.
	 * @param peerConnection
	 * @return 
	 */
	public final boolean serverAccept(T peerConnection) {
		this.peerConnection = peerConnection;
		boolean status = serverAccept();
		status &= receiveNetworkName();
		status &= exportConnectionInfo();
		return status;
	}
	
	/**
	 * Setup POPRemoteCaller (remoteCaller) variable which is going to be made available to the user.
	 * @return 
	 */
	protected abstract boolean exportConnectionInfo();
	
	/**
	 * Called by the client, it send the name of the network its in.
	 * This must use the basic peerConnection capabilities.
	 * @return 
	 */
	protected abstract boolean sendNetworkName();
	
	/**
	 * Called by the server client, it will receive the network name
	 * This must use the basic peerConnection capabilities.
	 * @return 
	 */
	protected abstract boolean receiveNetworkName();

	/**
	 * Connect to the other side
	 * @return	true if the connection succeed
	 */
	protected abstract boolean connectToServer();
	
	/**
	 * Server accept connection. Usually setup T I/O streams.
	 * NOTE you should NEVER use this method directly, use {@link #serverAccept(java.lang.Object) }
	 * @return	true if the server accept the connection successfully
	 */
	protected abstract boolean serverAccept();
	
	 /**
	  * This method should consistently return the same results when the same person connect to an object.
	  * This method should be called only after a connection is established.
	  * @return A identification string.
	  */
	public abstract String partyIdentification();

	/**
	 * Send the buffer to the other side
	 * @param buffer	The buffer to send
	 * @return	Number of byte sent
	 */
	public abstract int send(POPBuffer buffer);

	/**
	 * Receive buffer from the other side
	 * @param buffer	Buffer to receive
	 * @param requestId	The ID of the request
	 * @return	Number of byte received
	 */
	public abstract int receive(POPBuffer buffer, int requestId);

	/**
	 * Close the connection
	 */
	public abstract void close();

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

	/**
	 * Return the access point we are connected to
	 * @return 
	 */
	public POPAccessPoint getAccessPoint() {
		return accessPoint;
	}

	/**
	 * Information about who we are talking too
	 * @return 
	 */
	public POPRemoteCaller getRemoteCaller() {
		return remoteCaller;
	}

	/**
	 * The network we are connecting or are connected to.
	 * @return 
	 */
	public String getNetworkUUID() {
		return networkUUID;
	}
}
