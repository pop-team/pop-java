package ch.icosys.popjava.core.broker;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents the request queue used in the broker-side Every
 * requests are put into this request queue and are served in FIFO order
 */

public class RequestQueue {

	private final Lock lock = new ReentrantLock();

	private final Condition canPeek = lock.newCondition();

	private final Condition canInsert = lock.newCondition();

	private Request availableRequest = null;

	private Request requestMutex = null;
	
	private final List<Request> requestsConc = new LinkedList<>();

	private final List<Request> requestsSeq = new LinkedList<>();

	private final LinkedList<Request> requestsWaiting = new LinkedList<>();

	private Request servingMutex = null;

	private Request servingSequential = null;

	private final List<Request> servingConcurrent = new LinkedList<>();

	private boolean requestTypeConc = false;

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
		return requestsConc.size() + requestsSeq.size() + requestsWaiting.size() + (servingMutex == null ? 0 : 1)
				+ servingConcurrent.size() + (servingSequential == null ? 0 : 1);
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
			if (requestMutex == null) {
				if (request.isMutex()) {
					requestMutex = request;
					//System.out.println("Mutex " + request.getMethodId() + " added");
				} else if (request.isConcurrent()) {
					while (size() >= maxQueue) {
						canInsert.await();
					}
					requestsConc.add(request);
					//System.out.println("Conc " + request.getMethodId() + " added");
				} else if (request.isSequential()) {
					while (size() >= maxQueue) {
						canInsert.await();
					}
					requestsSeq.add(request);
					//System.out.println("Seq " + request.getMethodId() + " added");
				}
			} else { // requestMutex is not null
				while (size() >= maxQueue) {
					canInsert.await();
				}
				requestsWaiting.add(request);
				//System.out.println("Mutex is set. Request " + request.getMethodId() + " added to waiting queue of size: " + requestsWaiting.size());
			}
			canPeek();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		//LogWriter.writeDebugInfo(hashCode() + " Added request " + request.getClassId() + " " + request.getMethodId());
		return true;
	}

	/**
	 * Peek a request in the queue. If there is no request to peek, this method
	 * waits the time passed in parameters
	 * 
	 * @param time
	 *            Time to wait
	 * @param timeUnit
	 *            Unit of time
	 * @return Request peeked in the queue
	 */
	public Request peek(int time, TimeUnit timeUnit) {
		Request request = null;
		boolean waitSuccess = false;
		lock.lock();
		try {
			// LogWriter.writeDebugInfo("Peek, queue contains " + size() + " requests " + hashCode());
			// LogWriter.writeDebugInfo("Search for new request " + hashCode());
			waitSuccess = availableRequest != null || canPeek.await(time, timeUnit);
			// LogWriter.writeDebugInfo("Got request? " + waitSuccess + " " + hashCode());
			if (waitSuccess) {
				request = availableRequest;
				request.setStatus(Request.SERVING);
				serveRequest(request);
				availableRequest = null;
				canPeek();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return request;
	}

	/**
	 * Move request from availableRequest to its proper request queue
	 * 
	 * @param request
	 *            the request to add to the various queues
	 */
	private void serveRequest(Request request) {
		if (request.isMutex()) {
			servingMutex = request;
			//requestMutex = null;
		} else if (request.isSequential()) {
			servingSequential = request;
			requestsSeq.remove(request);
		} else {
			servingConcurrent.add(request);
			requestsConc.remove(request);
		}
		//System.out.println("Request " + request.getMethodId() + " served");
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
			if (request.isMutex() && servingMutex == request) {
				servingMutex = null;
				// Move queued waiting requests to adequate queues until reaching the next mutex (if any is waiting)
				Request queuedRequest;
				while ((queuedRequest = requestsWaiting.poll()) != null) {
					if (queuedRequest.isMutex()) {
						requestMutex = queuedRequest;
						//System.out.println("New mutex " + queuedRequest.getMethodId() + " set");
						break;
					} else if (queuedRequest.isConcurrent()) {
						requestsConc.add(queuedRequest);
						//System.out.println("Conc " + queuedRequest.getMethodId() + " moved from waiting to conc queue");
					} else if (queuedRequest.isSequential()) {
						requestsSeq.add(queuedRequest);
						//System.out.println("Seq " + queuedRequest.getMethodId() + " moved from waiting to seq queue");
					}
					//System.out.println("Wating queue size: " + requestsWaiting.size());
				}
			} else if (request.isSequential() && servingSequential == request) {
				servingSequential = null;
				//requestsSeq.remove(request);
			} else if (request.isConcurrent() && servingConcurrent.contains(request)) {
				servingConcurrent.remove(request);
				//requestsConc.remove(request);
			} /*else if (requestsWaiting.contains(request)) {
				requestsWaiting.remove(request);
			}*/
			canPeek();
			canInsert.signal();
		} finally {
			lock.unlock();
		}
		return true;
	}

	/**
	 * Clear the queue
	 * 
	 * @return true if the queue if correctly cleared
	 */
	public synchronized boolean clear() {
		availableRequest = null;
		requestMutex = null;
		requestsConc.clear();
		requestsSeq.clear();
		requestsWaiting.clear();
		servingMutex = null;
		servingSequential = null;
		servingConcurrent.clear();
		return true;
	}

	/**
	 * Check if there is request to peek
	 * 
	 * @return true if a request can be peeked
	 */
	public boolean canPeek() { 
		if (availableRequest == null) {
			if (requestsSeq.isEmpty() && requestsConc.isEmpty()) {
				if (requestMutex != null) {
					Request request = requestMutex;
					if (canPeek(request)) {
						if (availableRequest == null) {
							//System.out.println("Accepted1 " + request.getMethodId());
							availableRequest = request;
							requestMutex = null;
						}
						return true;
					}
				}
			} else { 
				List<Request> requests = null;
				if (requestsSeq.isEmpty()) {
					requests = requestsConc;
				} else if (requestsConc.isEmpty()) {
					requests = requestsSeq;
				} else {
					requests = requestTypeConc ? requestsConc : requestsSeq;
					requestTypeConc = !requestTypeConc;
					
				}
				if (canPeek(requests)) {
					canPeek.signal();
					//System.out.println("availableRequest  = " + availableRequest.getMethodId());
					return true;
				}
			}
		} else {
			canPeek.signal();
			return true;
		}
		return false;
	}

	private boolean canPeek(List<Request> requests) {
		for (Request request: requests) {
			if (canPeek(request)) {
				if (availableRequest == null) {
					//System.out.println("Accepted2 " + request.getMethodId());
					availableRequest = request;
					requests.remove(request);
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if a specific request can be peeked
	 * 
	 * @param request
	 *            Request to be peeked
	 * @return true if the request can be peeked
	 */
	private boolean canPeek(Request request) {
		if (request.getStatus() != Request.PENDING) {
			return false;
		}

		// If any mutex request is currently running, don't serve this request
		if (servingMutex != null && servingMutex.getStatus() == Request.SERVING) {
			return false;
		}

		if (request.isMutex() || request.isSequential()) {
			// Dont serve mutex or seq requests if there is any sequential request running
			if (servingSequential != null && servingSequential.getStatus() == Request.SERVING) {
				return false;
			}

			if (request.isMutex()) {
				// Dont serve mutex request if any concurrent request is running
				for (Request currentRequest : servingConcurrent) {
					// TO DO is the trailing isMutex check necessary? or even wrong?
					if (currentRequest.getStatus() == Request.SERVING && currentRequest.isMutex()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
