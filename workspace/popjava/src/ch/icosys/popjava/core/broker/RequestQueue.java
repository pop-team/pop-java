package ch.icosys.popjava.core.broker;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents the request queue used in the broker-side. Every
 * requests are put into this request queue and are served in FIFO order.
 * 
 * @author Gisler Christophe
 *
 */

public class RequestQueue {

	private final Lock lock = new ReentrantLock();

	private final Condition canPick = lock.newCondition();

	private final Condition canInsert = lock.newCondition();

	private Request nextRequestToPick = null;

	private Request mutexRequest = null;

	private final LinkedList<Request> concurrentRequests = new LinkedList<>();

	private final LinkedList<Request> sequentialRequests = new LinkedList<>();

	private final LinkedList<Request> waitingRequests = new LinkedList<>();

	private boolean nextRequestIsConc = false;

	public static final int DEFAULT_REQUEST_QUEUE_SIZE = 250;

	private int maxQueue = DEFAULT_REQUEST_QUEUE_SIZE;

	/**
	 * Creates a new instance of POPRequestQueue
	 */
	public RequestQueue() {}

	/**
	 * Give the actual number of requests in the queue
	 * 
	 * @return number of requests
	 */
	public synchronized int size() {
		return concurrentRequests.size() + sequentialRequests.size() + (mutexRequest == null ? 0 : 1) + waitingRequests.size();
	}

	/**
	 * Return the maximum number of requests in the queue
	 * 
	 * @return max requests number in the queue
	 */
	public synchronized int getMaxQueue() {
		return maxQueue;
	}

	/**
	 * Set the maximum number of requests in the queue
	 * 
	 * @param maxQueue
	 *            Maximum number of requests
	 */
	public synchronized void setMaxQueue(int maxQueue) {
		this.maxQueue = maxQueue;
	}

	/**
	 * Put a new request in the queue
	 * 
	 * @param request
	 *            Request to add
	 * @return true if the request is added correctly
	 */
	public boolean add(Request request) {
		/*LogWriter.writeDebugInfo(hashCode() + " Add request, there are already " + size() + " requests, " 
		 										+ request.getClassId() + " " + request.getMethodId());*/
		lock.lock();
		try {
			if (mutexRequest == null) {
				if (request.isMutex()) {
					mutexRequest = request;
					//System.out.println("Mutex request " + request.getMethodId() + " added");
				} else if (request.isConcurrent()) {
					while (size() >= maxQueue) {
						canInsert.await();
					}
					concurrentRequests.add(request);
					//System.out.println("Conc request " + request.getMethodId() + " added");
				} else if (request.isSequential()) {
					while (size() >= maxQueue) {
						canInsert.await();
					}
					sequentialRequests.add(request);
					//System.out.println("Seq request " + request.getMethodId() + " added");
				}
			} else { // requestMutex is not null
				while (size() >= maxQueue) {
					canInsert.await();
				}
				waitingRequests.add(request);
				//System.out.println("Mutex request is already set. Request " + request.getMethodId() + " added to waiting queue of size: " + waitingRequests.size());
			}
			canPick();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		//LogWriter.writeDebugInfo(hashCode() + " Added request " + request.getClassId() + " " + request.getMethodId());
		return true;
	}

	/**
	 * Pick a request in the queue. If there is no request to pick, this method
	 * waits the time passed in parameters
	 * 
	 * @param time
	 *            Time to wait
	 * @param timeUnit
	 *            Unit of time
	 * @return Request picked in the queue
	 */
	public Request pick(int time, TimeUnit timeUnit) {
		Request pickedRequest = null;
		lock.lock();
		try {
			// LogWriter.writeDebugInfo("Peek, queue contains " + size() + " requests " + hashCode());
			// LogWriter.writeDebugInfo("Search for new request " + hashCode());
			boolean waitSuccess = nextRequestToPick != null || canPick.await(time, timeUnit);
			// LogWriter.writeDebugInfo("Got request? " + waitSuccess + " " + hashCode());
			if (waitSuccess) {
				pickedRequest = nextRequestToPick;
				pickedRequest.setStatus(Request.SERVING);
				if (pickedRequest.isMutex()) {
					//System.out.println("Mutex request " + pickedRequest.getMethodId() + " picked");
				} else if (pickedRequest.isSequential()) {
					//System.out.println("Seq request " + pickedRequest.getMethodId() + " picked");
				} else {
					//System.out.println("Conq request " + pickedRequest.getMethodId() + " picked");
				}
				nextRequestToPick = null;
				canPick();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return pickedRequest;
	}

	/**
	 * Remove a specific request from its queue(s)
	 * 
	 * @param request
	 *            Request to be removed
	 * @return true if the request is correctly removed
	 */
	public boolean remove(Request request) {
		lock.lock();
		try {
			// If it is the requestMutex
			if (request.isMutex() && mutexRequest == request) {
				mutexRequest = null;
				//System.out.println("Mutex request " + request.getMethodId() + " removed");
				// Move queued waiting requests to adequate queues until reaching the next mutex (if any is waiting)
				Request waitingRequest;
				while ((waitingRequest = waitingRequests.poll()) != null) {
					if (waitingRequest.isMutex()) {
						mutexRequest = waitingRequest;
						//System.out.println("New mutex request " + waitingRequest.getMethodId() + " set");
						break;
					} else if (waitingRequest.isConcurrent()) {
						concurrentRequests.add(waitingRequest);
						//System.out.println("Conc request " + waitingRequest.getMethodId() + " moved from waiting to conc request queue");
					} else if (waitingRequest.isSequential()) {
						sequentialRequests.add(waitingRequest);
						//System.out.println("Seq request " + waitingRequest.getMethodId() + " moved from waiting to seq request queue");
					}
					//System.out.println("Waiting request queue size: " + waitingRequests.size());
				}
			} else if (request.isSequential()) {
				sequentialRequests.remove(request);
				//System.out.println("Seq request " + request.getMethodId() + " removed from seq request queue");
			} else if (request.isConcurrent()) {
				concurrentRequests.remove(request);
				//System.out.println("Conc request " + request.getMethodId() + " removed from conc request queue");
			} else if (waitingRequests.contains(request)) {
				waitingRequests.remove(request);
				//System.out.println("Request " + request.getMethodId() + " removed from waiting request queue");
			}
			canPick();
			canInsert.signal();
		} finally {
			lock.unlock();
			//System.out.println("RequestQueue size: " + size());
		}
		return true;
	}

	/**
	 * Check if there is request to peek
	 * 
	 * @return true if a request can be peeked
	 */
	private boolean canPick() { 
		if (nextRequestToPick == null) {
			// If both seq and conc request queues are empty, check if there is a pending mutex request set
			if (sequentialRequests.isEmpty() && concurrentRequests.isEmpty()) {
				if (mutexRequest != null && mutexRequest.getStatus() == Request.PENDING) {
					nextRequestToPick = mutexRequest;
				}
			} else if (sequentialRequests.isEmpty()) { // => requestConc queue is not empty
				for (Request request: concurrentRequests) {
					if (request.getStatus() == Request.PENDING) {
						nextRequestToPick = request;
						nextRequestIsConc = false;
						break;
					}
				}
			} else if (concurrentRequests.isEmpty()) { // => requestSeq queue is not empty
				if (sequentialRequests.getFirst().getStatus() == Request.PENDING) {
					nextRequestToPick = sequentialRequests.getFirst();
					nextRequestIsConc = true;
				}
			} else { // => Both seq and conc request queues are not empty
				if (!nextRequestIsConc && sequentialRequests.getFirst().getStatus() == Request.PENDING) {
					nextRequestToPick = sequentialRequests.getFirst();
					nextRequestIsConc = true;
				} else {
					for (Request request: concurrentRequests) {
						if (request.getStatus() == Request.PENDING) {
							nextRequestToPick = request;
							nextRequestIsConc = false;
							break;
						}
					}
				} 
			} 
		}
		if (nextRequestToPick != null) {
			//System.out.println("Next available request: " + nextRequestToPick.getMethodId());
			canPick.signal();
			return true;
		}
		return false;
	}

	/**
	 * Clear the queue
	 * 
	 * @return true if the queue if correctly cleared
	 */
	public synchronized boolean clear() {
		nextRequestToPick = null;
		mutexRequest = null;
		concurrentRequests.clear();
		sequentialRequests.clear();
		waitingRequests.clear();
		return true;
	}
}
