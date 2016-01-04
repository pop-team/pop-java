package popjava.combox;

import popjava.base.*;
import popjava.broker.Broker;
import popjava.broker.Request;
import popjava.broker.RequestQueue;
import popjava.buffer.*;
import popjava.util.LogWriter;

import java.net.*;
import java.util.Random;
import java.io.*;

/**
 * This class is responsible to receive the new request for the associated combox
 */
public class ComboxReceiveRequestSocket implements Runnable {

    //TODO: use enum
	static public final int RUNNING = 0;
	static public final int EXIT = 1;
	static public final int ABORT = 2;
	
	protected ComboxSocket combox;
	protected RequestQueue requestQueue;
	protected Broker broker;
	protected int status = EXIT;  
	
	/**
	 * Crate a new instance of ComboxReceiveRequestSocket
	 * @param broker		The associated broker
	 * @param requestQueue	The associated request queue
	 * @param socket		The associated socket
	 * @throws IOException	Thrown if any exception occurred during the process 
	 */
	public ComboxReceiveRequestSocket(Broker broker,
			RequestQueue requestQueue, Socket socket) throws IOException {
		this.broker = broker;
		this.requestQueue = requestQueue;
		combox = new ComboxSocket(socket);		
	}

	/**
	 * Start the thread 
	 */
	public void run() {
		setStatus(RUNNING);
		while (getStatus() == RUNNING) {
			Request popRequest = new Request();
			try {
				if (!receiveRequest(popRequest)) {
					setStatus(EXIT);
					break;
				}
				
				// add request to fifo list
				if (broker != null && !broker.popCall(popRequest)) {
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
	 * @param request	The request
	 * @return	true if the new request if complete or false if it's incomplete
	 */
	public boolean receiveRequest(Request request) {		
		POPBuffer buffer = combox.getBufferFactory().createBuffer();
		int receivedLength = combox.receive(buffer, -1);
		if (receivedLength > 0) {
			request.setBroker(broker);
			MessageHeader messageHeader = buffer.extractHeader();
			request.setClassId(messageHeader.getClassId());
			request.setMethodId(messageHeader.getMethodId());
			request.setSenmatics(messageHeader.getSenmatics());
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
		broker.onCloseConnection();
		combox.close();
	}

	/**
	 * Get the status of the current connection
	 * @return	Current connection status
	 */
	public synchronized int getStatus() {
		return status;
	}

	/**
	 * Set the current status
	 * @param status	The new status
	 */
	public synchronized void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Associate a buffer with this receiving combox 
	 * @param bufferType Type of the buffer
	 */
	public void setBuffer(String bufferType) {
		BufferFactoryFinder finder = BufferFactoryFinder.getInstance();
		BufferFactory factory = finder.findFactory(bufferType);		
		combox.setBufferFactory(factory);		
	}

	/**
	 * Method called before destruction of the instance
	 */
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

}
