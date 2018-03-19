package popjava.combox;


import java.util.HashSet;
import java.util.Set;

import popjava.base.MessageHeader;
import popjava.baseobject.POPAccessPoint;
import popjava.broker.Broker;
import popjava.buffer.BufferFactory;
import popjava.buffer.BufferFactoryFinder;
import popjava.buffer.BufferRaw;
import popjava.buffer.BufferXDR;
import popjava.buffer.POPBuffer;
import popjava.combox.socket.raw.ComboxAcceptSocket;
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
	
	private String networkUUID;
	protected POPRemoteCaller remoteCaller;
	
	protected final Configuration conf = Configuration.getInstance();
	private Broker broker = null; //Broken to be used when making this combox bidirectional

	private Set<Integer> openConnections = new HashSet<Integer>();
	private int connectionCounter = 10;
		
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
	
	protected int registerNewConnection() {
		int id;
		
		do {
			id = connectionCounter++;
		}while(openConnections.contains(id));
		
		openConnections.add(id);
		
		return id;
	}

	/**
	 * Connect to a ServerCombox on the other side, this will result in a Combox (client mode) communicating with a 
	 * Combox (server mode).
	 * @param broker	Broker that is behind this connection
	 * @param accesspoint	Access point of the other side combox
	 * @param timeout		Connection time out
	 * @return true if the connection is established
	 */
	public final boolean connectToServer(Broker broker, POPAccessPoint accesspoint, int timeout) {
		this.accessPoint = accesspoint;
		this.timeOut = timeout;
		this.broker = broker;
		System.out.println("ConnectToServer "+broker);
		return connectToServer() && sendNetworkName() && exportConnectionInfo() && sendLocalAP(broker);
	}
	
	/**
	 * Accept a connection from {@link #connectToServer() }.
	 * Communicate with Client mode Combox.
	 * @param peerConnection the incoming connection
	 * @return true if the connection is established correctly, false otherwise
	 */
	public final boolean serverAccept(Broker broker, T peerConnection) {
		System.out.println("Accept connection "+broker);
		this.peerConnection = peerConnection;
		this.broker = broker;
		return serverAccept() && receiveNetworkName() && exportConnectionInfo() && receiveRemoveAP();
	}
	
	/**
	 * Setup POPRemoteCaller (remoteCaller) variable which is going to be made available to the user.
	 * @return true if the operation succeeded
	 */
	protected abstract boolean exportConnectionInfo();
	
	/**
	 * Called by the client, it send the name of the network its in.
	 * This must use the basic peerConnection capabilities.
	 * @return true if the operation succeeded
	 */
	protected abstract boolean sendNetworkName();
	
	/**
	 * Called by the server client, it will receive the network name
	 * This must use the basic peerConnection capabilities.
	 * @return true if the operation succeeded
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
	
	private boolean sendLocalAP(Broker broker) {
		MessageHeader messageHeader = new MessageHeader();
		messageHeader.setRequestID(1234);
		messageHeader.setRequestType(MessageHeader.REQUEST);
		POPBuffer buffer = bufferFactory.createBuffer();
		buffer.setHeader(messageHeader);
		buffer.putBoolean(broker != null);
		if(broker != null) {
			buffer.putValue(broker.getAccessPoint(), POPAccessPoint.class);
		}
		
		send(buffer, 0);
		
		return true;
	}
	
	private boolean receiveRemoveAP() {
		POPBuffer buffer = bufferFactory.createBuffer();
		receive(buffer, 1234, 0);
		
		POPAccessPoint ap;
		if(buffer.getBoolean()) {
			ap = (POPAccessPoint)buffer.getValue(POPAccessPoint.class);
		}else {
			ap = new POPAccessPoint();
		}

		if(remoteCaller != null) {
			remoteCaller.setBrokerAP(ap);
		}
		
		return true;
	}

	/**
	 * Send the buffer to the other side
	 * @param buffer	The buffer to send
	 * @return	Number of byte sent
	 */
	public abstract int send(POPBuffer buffer, int connectionID);

	/**
	 * Receive buffer from the other side
	 * @param buffer	Buffer to receive
	 * @param requestId	The ID of the request
	 * @return	Number of byte received
	 */
	public abstract int receive(POPBuffer buffer, int requestId, int connectionID );

	/**
	 * Close the connection
	 */
	public void close(int connectionID) {
		if(connectionID == 0 || connectionID == 1) {
			openConnections.remove(0);
			openConnections.remove(1);
		}else {
			openConnections.remove(connectionID);
		}
		
		if(openConnections.size() == 0) {
			closeInternal();
		}else {			
			MessageHeader messageHeader = new MessageHeader();
	        messageHeader.setRequestID(2);
	        messageHeader.setMethodId(3);
	        messageHeader.setRequestType(MessageHeader.REQUEST);
	        POPBuffer buffer = bufferFactory.createBuffer();
	        buffer.setHeader(messageHeader);
	        buffer.putInt(connectionID);
	        
	        send(buffer, 0);
		}
		
	}
	
	protected abstract void closeInternal();

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
	 * @return the access point
	 */
	public POPAccessPoint getAccessPoint() {
		return accessPoint;
	}

	/**
	 * Information about who we are talking too
	 * @return the client connected on the other side
	 */
	public POPRemoteCaller getRemoteCaller() {
		return remoteCaller;
	}

	/**
	 * The network we are connecting or are connected to.
	 * @return the network UUID
	 */
	public String getNetworkUUID() {
		return networkUUID == null ? "" : networkUUID;
	}

	/**
	 * Set the new ID of this network
	 * @param networkUUID set the network UUID
	 */
	public void setNetworkUUID(String networkUUID) {
		this.networkUUID = networkUUID;
	}
	
	/**
	 * This function can be called to transform a client combox into a server combox.
	 */
	public int makeBidirectional(Broker broker) {
		
		if(this.broker == null) {
			this.broker = broker;
		}else if(broker != null && this.broker != broker) {
			System.out.println("Cannot two different brokers for same combox!");
		}
		
	    MessageHeader messageHeader = new MessageHeader();
        messageHeader.setRequestID(2);
        messageHeader.setMethodId(2);
        messageHeader.setRequestType(MessageHeader.REQUEST);
        POPBuffer buffer = bufferFactory.createBuffer();
        buffer.setHeader(messageHeader);
        
        buffer.putBoolean(this.broker != null);
        if(this.broker != null) {
        	buffer.putValue(this.broker.getAccessPoint(), POPAccessPoint.class);
        }
        
        send(buffer, 0);
        
        int answer = receive(buffer, 2, 0);
        if(answer > 0) {
            if(buffer.getHeader().getRequestType() == MessageHeader.EXCEPTION) {
                System.out.println("Bidirectional connection failed, exception "+buffer.getHeader().getExceptionCode());
                return -1;
            }
            int connectionID = buffer.getInt();;
            System.out.println("Combox accepted bidirectional on connection "+connectionID);
            openConnections.add(connectionID);
            
            return connectionID;
        }
        
        return -1;
	}
	
	protected boolean bindToBroker(int connectionID) {
	    System.out.println("Rebind combox to broker using connection ID "+connectionID);
	    if(broker != null) {
	        ComboxAcceptSocket.serveConnection(broker, broker.getRequestQueue(), this, connectionID);
	        return true;
	    }
	    
	    return false; //TODO: Throw exception?
	}
}
