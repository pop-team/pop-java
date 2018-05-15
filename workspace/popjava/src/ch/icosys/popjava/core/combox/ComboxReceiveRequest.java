package ch.icosys.popjava.core.combox;

import ch.icosys.popjava.core.base.MessageHeader;
import ch.icosys.popjava.core.broker.Broker;
import ch.icosys.popjava.core.broker.Request;
import ch.icosys.popjava.core.broker.RequestQueue;
import ch.icosys.popjava.core.buffer.BufferFactory;
import ch.icosys.popjava.core.buffer.BufferFactoryFinder;
import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.util.LogWriter;

/**
 * This class is responsible to receive the new request for the associated
 * combox
 */
public final class ComboxReceiveRequest implements Runnable {

	// TODO: use enum
	static public final int RUNNING = 0;

	static public final int EXIT = 1;

	static public final int ABORT = 2;

	protected ComboxConnection combox;

	protected final RequestQueue requestQueue;

	protected final Broker broker;

	protected int status = EXIT;

	/**
	 * Crate a new instance of ComboxReceiveRequestSocket
	 * 
	 * @param broker
	 *            The associated broker
	 * @param requestQueue
	 *            The associated request queue
	 * @param combox
	 *            The associated combox
	 */
	public ComboxReceiveRequest(Broker broker, RequestQueue requestQueue, ComboxConnection combox) {
		this.broker = broker;
		this.requestQueue = requestQueue;
		this.combox = combox;
	}

	/**
	 * Start the thread
	 */
	@Override
	public void run() {
		setStatus(RUNNING);
		while (getStatus() == RUNNING) {
			Request popRequest = new Request();
			popRequest.setRemoteCaller(combox.getRemoteCaller());
			try {
				if (!receiveRequest(popRequest)) {
					setStatus(EXIT);
					break;
				}

				// add request to fifo list
				if (broker != null && !broker.popCall(popRequest)) {
					// replace buffer sent information using local annotation
					// (if possible)
					broker.finalizeRequest(popRequest);

					requestQueue.add(popRequest);
				}
			} catch (Exception e) {
				LogWriter.writeExceptionLog(e);
				setStatus(EXIT);
			}
		}

		close();
	}

	/**
	 * Get request from the buffer
	 * 
	 * @param request
	 *            The request
	 * @return true if the new request if complete or false if it's incomplete
	 */
	public boolean receiveRequest(Request request) {
		POPBuffer buffer = combox.getCombox().getBufferFactory().createBuffer();
		int receivedLength = combox.receive(buffer, -1);

		if (receivedLength > 0) {
			request.setBroker(broker);
			MessageHeader messageHeader = buffer.extractHeader();
			request.setClassId(messageHeader.getClassId());
			request.setMethodId(messageHeader.getMethodId());
			request.setSemantics(messageHeader.getSemantics());
			request.setRequestID(messageHeader.getRequestID());
			request.setBuffer(buffer);
			request.setReceiveCombox(this);
			request.setCombox(combox);
			return true;
		}

		return false;
	}

	/**
	 * Close the current connection
	 */
	public void close() {
		if (combox != null) {
			broker.onCloseConnection(hashCode() + " " + combox);
			combox.close();
			combox = null;
		}
	}

	/**
	 * Get the status of the current connection
	 * 
	 * @return Current connection status
	 */
	public synchronized int getStatus() {
		return status;
	}

	/**
	 * Set the current status
	 * 
	 * @param status
	 *            The new status
	 */
	public synchronized void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Associate a buffer with this receiving combox
	 * 
	 * @param bufferType
	 *            Type of the buffer
	 */
	public void setBuffer(String bufferType) {
		BufferFactoryFinder finder = BufferFactoryFinder.getInstance();
		BufferFactory factory = finder.findFactory(bufferType);
		combox.getCombox().setBufferFactory(factory);
	}

	/**
	 * Get server socket request queue
	 * 
	 * @return the global queue
	 */
	public RequestQueue getRequestQueue() {
		return requestQueue;
	}

	/**
	 * Method called before destruction of the instance
	 */
	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

}
