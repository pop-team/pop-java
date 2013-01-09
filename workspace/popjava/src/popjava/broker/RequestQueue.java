package popjava.broker;

import java.io.File;
import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.locks.*;

import popjava.base.Semantic;
import popjava.util.LogWriter;
/**
 * This class represents the request queue used in the broker-side
 * Every requests are put into this request queue and are served in FIFO order
 */
public class RequestQueue {
	protected final Lock lock = new ReentrantLock();
	protected final Condition canPeek = lock.newCondition();
	protected final Condition canInsert = lock.newCondition();
	protected ArrayList<Request> requests = new ArrayList<Request>();
	protected Request availableRequest = null;
	protected int maxQueue = 200;

	/**
	 * Creates a new instance of POPRequestQueue
	 */
	public RequestQueue() {

	}

	/**
	 * Give the actual number of requests in the queue
	 * @return number of requests
	 */
	public synchronized int size() {
		return requests.size();
	}

	/**
	 * Return the maximum number of requests in the queue
	 * @return max requests number in the queue
	 */
	public synchronized int getMaxQueue() {
		return maxQueue;
	}

	/**
	 * Set the maximum number of requests in the queue
	 * @param maxQueue	Maximum number of requests
	 */
	public synchronized void setMaxQueue(int maxQueue) {
		this.maxQueue = maxQueue;
	}

	/**
	 * Put a new request in the queue
	 * @param request Request to add
	 * @return true if the request is added correctly
	 */
	public boolean add(Request request) {		
		lock.lock();
		try {
			while (maxQueue <= requests.size()){
				canInsert.await();
			}
			requests.add(request);
			canPeek();
		} catch (InterruptedException e) {

		} finally {
			lock.unlock();
		}
		return true;
	}

	/**
	 * Peek a request in the queue. If there is no request to peek, this method waits the time passed in parameters
	 * @param time		Time to wait
	 * @param timeUnit	Unit of time
	 * @return Request peeked in the queue
	 */
	public Request peek(int time, TimeUnit timeUnit) {
		Request request = null;
		boolean waitSuccess = false;
		lock.lock();

		try {			
			if (availableRequest == null){
				waitSuccess = canPeek.await(time, timeUnit);
			} else {
				waitSuccess = true;
			}
			
			if (waitSuccess) {
				request = availableRequest;
				request.setStatus(Request.Serving);
				availableRequest = null;
				canPeek();
			}
		} catch (InterruptedException exception) {
		} finally {
			lock.unlock();
		}
		
		if (requests.size() > 0 && !waitSuccess && requests.get(0).getStatus() == Request.Pending) {
			Request temp = requests.get(0);
			String info = String.format("Request.MethodId=%d.Semantics=%s",
					temp.getMethodId(), temp.getSenmatics());
			LogWriter.writeLogfile(info, LogWriter.LogFolder
					+ File.separator + "bug.txt");
		}
		return request;
	}

	/**
	 * Peek a request into the queue
	 * @return The request peeked
	 */
	public Request peek() {
		Request request = null;
		lock.lock();
		try {
			while (availableRequest == null){
				canPeek.await();
			}
			request = availableRequest;
			request.setStatus(Request.Serving);
			availableRequest = null;
			canPeek();
		} catch (InterruptedException exception) {

		} finally {
			lock.unlock();
		}
		return request;
	}

	/**
	 * Remove a specific request from the queue
	 * @param request Request to be removed
	 * @return true if the request is correctly removed
	 */
	public boolean remove(Request request) {
		lock.lock();
		try {
			requests.remove(request);
			canPeek();
			canInsert.signal();
		}

		finally {
			lock.unlock();
		}
		return true;
	}

	/**
	 * Clear the queue
	 * @return true if the queue if correctly cleared
	 */
	public synchronized boolean clear() {
		availableRequest = null;
		requests.clear();
		return true;
	}

	/**
	 * Check if there is request to peek
	 * @return true if a request can be peeked
	 */
	public synchronized boolean canPeek() {
		if (availableRequest != null) {
			canPeek.signal();
			return true;
		}
		
		int requestCount = requests.size();
		if (requestCount > 0) {
			for (int i = 0; i < requestCount; i++) {
				Request currentRequest = requests.get(i);
				if (canPeek(currentRequest)) {
					if (availableRequest == null) {
						availableRequest = currentRequest;
					}
					canPeek.signal();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Check if a specific request can be peeked
	 * @param request	Request to be peeked
	 * @return true if the request can be peeked
	 */
	public synchronized boolean canPeek(Request request) {
		if (request.getStatus() != Request.Pending)
			return false;
		int requestCount = requests.size();
		if (requestCount > 0) {
			if (request.isMutex()) {
				for (int i = 0; i < requestCount; i++) {
					Request currentRequest = requests.get(i);
					if (currentRequest.getStatus() == Request.Serving)
						return false;
				}
			}
			if (request.isConcurrent()) {
				for (int i = 0; i < requestCount; i++) {
					Request currentRequest = requests.get(i);
					if (currentRequest.getStatus() == Request.Serving
							&& (currentRequest.getSenmatics() & Semantic.Mutex) != 0)
						return false;
				}
			}
			if (request.isSequential()) {
				for (int i = 0; i < requestCount; i++) {
					Request currentRequest = requests.get(i);
					if (currentRequest.getStatus() == Request.Serving
							&& (currentRequest.isMutex() || currentRequest.isSequential()))
						return false;
				}
			}
			return true;
		}
		return false;
	}
}
