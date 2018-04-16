package ch.icosys.popjava.core.broker;

import ch.icosys.popjava.core.base.Semantic;
import ch.icosys.popjava.core.buffer.*;
import ch.icosys.popjava.core.combox.*;
import ch.icosys.popjava.core.util.POPRemoteCaller;
/**
 * This class symbolize a request between the interface-side and the broker-side.
 *
 */
public class Request {

    //TODO: use an enum
	public static final int PENDING = 0;
	public static final int SERVING = 1;
	public static final int SERVED = 2;
	
	protected int classId;
	protected int methodId;
	protected int semantics;
	protected int requestId;
	
	protected Broker broker;
	protected POPBuffer buffer;
	protected ComboxReceiveRequest receivedCombox;
	protected ComboxConnection combox;	
	protected int status;
	
	protected POPRemoteCaller remoteCaller;

	/**
	 * Creating a new pending request
	 */
	public Request() {
		status = PENDING;
	}
	
	/**
	 * Get the class identifier of the current request
	 * @return class identifier
	 */
	public int getClassId() {
		return classId;
	}

	/**
	 * Set the class identifier of the current request
	 * @param classId	Class ID to be set
	 */
	public void setClassId(int classId) {
		this.classId = classId;
	}

	/**
	 * Get the method identifier of this request
	 * @return method identifier
	 */
	public int getMethodId() {
		return methodId;
	}

	/**
	 * Set the method identifier of the current request
	 * @param methodId	Method ID to be set
	 */
	public void setMethodId(int methodId) {
		this.methodId = methodId;
	}
	
	public void setRequestID(int requestId){
		this.requestId = requestId;
	}
	
	public int getRequestID(){
		return requestId;
	}

	/**
	 * Get the semantic of the current request
	 * @return	Semantic of the current request as an int value
	 */
	public int getSenmatics() {
		return semantics;
	}

	/**
	 * Set the semantic of the current request
	 * @param semantics Semantic to be set on the current request as an int value
	 */
	public void setSenmatics(int semantics) {
		this.semantics = semantics;
	}

	/**
	 * Get the associated borker
	 * @return reference to the associated broker
	 */
	public Broker getBroker() {
		return broker;
	}

	/**
	 * Set an associated broker
	 * @param broker	Reference to the associated broker to be set
	 */
	public void setBroker(Broker broker) {
		this.broker = broker;
	}

	/**
	 * Get the associated buffer
	 * @return reference to the associated buffer
	 */
	public POPBuffer getBuffer() {
		return buffer;
	}

	/**
	 * Set an associated buffer
	 * @param buffer Reference to the associated buffer
	 */
	public void setBuffer(POPBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * Get the request current status
	 * @return status of the current request
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Set the current status of the request
	 * @param status Status to be set
	 */
	public void setStatus(int status) {
		this.status = status;
	}	

	/**
	 * Get the combox which received the request
	 * @return combox which received the request
	 */
	public ComboxReceiveRequest getReceiveCombox() {
		return receivedCombox;
	}

	/**
	 * Get the combox which received the request
	 * @param combox Combox which received the request
	 */
	public void setReceiveCombox(ComboxReceiveRequest combox) {
		receivedCombox = combox;
	}

	/**
	 * Get the associated combox
	 * @return associated combox
	 */
	public ComboxConnection getConnection() {
		return combox;
	}

	/**
	 * Set the associated combox
	 * @param combox Combox to be associate
	 */
	public void setCombox(ComboxConnection combox) {
		this.combox = combox;
	}

	/**
	 * Set associated buffer	
	 * @param bufferType BufferType to be associate
	 */
	public void setBufferType(String bufferType) {
		receivedCombox.setBuffer(bufferType);
	}
	
	/**
	 * Returns true if this request is a synchronous request, false if asynchronous
	 * @return true if synchronous, false otherwise
	 */
	public boolean isSynchronous(){
		return (getSenmatics() & Semantic.SYNCHRONOUS) != 0;
	}
	
	/**
	 * Returns true if this request is a concurrent request, false otherwise
	 * @return true if concurrent, false otherwise
	 */
	public boolean isConcurrent(){
		return (getSenmatics() & Semantic.CONCURRENT) != 0;
	}
	
	/**
	 * Returns true if this request is a mutex request, false otherwise
	 * @return true if mutex, false otherwise
	 */
	public boolean isMutex(){
		return (getSenmatics() & Semantic.MUTEX) != 0;
	}
	
	/**
	 * Returns true if this request is a sequential request, false otherwise
	 * @return true if sequential, false otherwise
	 */
	public boolean isSequential(){
		return !isConcurrent() && !isMutex();
	}

	/**
	 * Return from where the call to this request come from
	 * @return the remote caller of this request
	 */
	public POPRemoteCaller getRemoteCaller() {
		return remoteCaller;
	}

	public void setRemoteCaller(POPRemoteCaller callSource) {
		this.remoteCaller = callSource;
	}

	boolean isLocalhost() {
		return (getSenmatics() & Semantic.LOCALHOST) != 0;
	}
	
	public RequestQueue getRequestQueue() {
		return this.receivedCombox.getRequestQueue();
	}
}
