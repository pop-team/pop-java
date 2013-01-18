package popjava.broker;

import popjava.base.Semantic;
import popjava.buffer.*;
import popjava.combox.*;
/**
 * This class symbolize a request between the interface-side and the broker-side.
 *
 */
public class Request {

	public static final int Pending = 0;
	public static final int Serving = 1;
	public static final int Served = 2;
	protected int classId;
	protected int methodId;
	protected int semantics;
	protected Broker broker;
	protected POPBuffer buffer;
	protected ComboxReceiveRequestSocket receivedCombox;
	protected Combox combox;	
	protected int status;

	/**
	 * Creating a new pending request
	 */
	public Request() {
		status = Pending;
	}

	/**
	 * Creating a new specific request
	 * @param classId	Class identifier for this request
	 * @param methodId	Method identifier for this request
	 * @param semantics	Semantics used for this methods
	 * @param broker	Broker associated with this request
	 * @param combox	Combox associated with this request
	 */
	public Request(int classId, int methodId, int semantics, Broker broker,
			Combox combox) {
		this.classId = classId;
		this.methodId = methodId;
		this.semantics = semantics;
		this.broker = broker;
		status = Pending;
		this.combox = combox;
	}

	/**
	 * Initializes an empty request
	 * @param classId	Class identifier for this request
	 * @param methodId	Method identifier for this request
	 * @param semantics	Semantics used for this methods
	 * @param broker	Broker associated with this request
	 * @param combox	Combox associated with this request
	 */
	public void init(int classId, int methodId, int semantics, Broker broker,
			Combox combox) {
		this.classId = classId;
		this.methodId = methodId;
		this.semantics = semantics;
		this.broker = broker;
		status = Pending;
		this.combox = combox;
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
	public ComboxReceiveRequestSocket getReceiveCombox() {
		return receivedCombox;
	}

	/**
	 * Get the combox which received the request
	 * @param combox Combox which received the request
	 */
	public void setReceiveCombox(ComboxReceiveRequestSocket combox) {
		receivedCombox = combox;
	}

	/**
	 * Get the associated combox
	 * @return associated combox
	 */
	public Combox getCombox() {
		return combox;
	}

	/**
	 * Set the associated combox
	 * @param combox Combox to be associate
	 */
	public void setCombox(Combox combox) {
		this.combox = combox;
	}

	/**
	 * Set associated buffer	
	 * @param bufferType BufferType to be associate
	 */
	public void setBuffer(String bufferType) {
		receivedCombox.setBuffer(bufferType);
	}
	
	/**
	 * Returns true if this request is a synchronous request, false if asynchronous
	 * @return
	 */
	public boolean isSynchronous(){
		return (getSenmatics() & Semantic.Synchronous) != 0;
	}
	
	/**
	 * Returns true if this request is a concurrent request, false otherwise
	 * @return
	 */
	public boolean isConcurrent(){
		return (getSenmatics() & Semantic.Concurrent) != 0;
	}
	
	/**
	 * Returns true if this request is a mutex request, false otherwise
	 * @return
	 */
	public boolean isMutex(){
		return (getSenmatics() & Semantic.Mutex) != 0;
	}
	
	/**
	 * Returns true if this request is a sequential request, false otherwise
	 * @return
	 */
	public boolean isSequential(){
		return !isConcurrent() && !isMutex();
	}
}
