package popjava.combox;

import popjava.baseobject.AccessPoint;
import popjava.broker.Broker;
import popjava.broker.RequestQueue;
/**
 * This class represent the server side of a socket connection
 */
public abstract class ComboxServer {
	
	static public final int RUNNING = 0;
	static public final int EXIT = 1;
	static public final int ABORT = 2;
	
	protected int status = EXIT;
	protected Broker broker;
	protected int timeOut = 0;
	protected AccessPoint accessPoint;

	/**
	 * Default constructor
	 * @param accessPoint	Access point of the combox server
	 * @param timeout		Connection timeout
	 * @param broker		Associated broker
	 */
	public ComboxServer(AccessPoint accessPoint, int timeout, Broker broker) {
		timeOut = timeout;
		this.broker = broker;
		this.accessPoint = accessPoint;
	}

	/**
	 * Get the associated request queue
	 * @return	The associated request queue
	 */
	public RequestQueue getRequestQueue() {
		return broker.getRequestQueue();
	}

}
