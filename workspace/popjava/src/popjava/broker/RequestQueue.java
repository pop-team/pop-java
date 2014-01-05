package popjava.broker;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.locks.*;
/**
 * This class represents the request queue used in the broker-side
 * Every requests are put into this request queue and are served in FIFO order
 */

public class RequestQueue {
	protected final Lock lock = new ReentrantLock();
	protected final Condition canPeek = lock.newCondition();
	protected final Condition canInsert = lock.newCondition();
	
	protected ArrayList<Request> requestsConc = new ArrayList<Request>();
	protected ArrayList<Request> requestsSeq = new ArrayList<Request>();
	protected ArrayList<Request> requestsMutex = new ArrayList<Request>();
	
	protected Request servingMutex = null;
	protected ArrayList<Request> servingConcurrent = new ArrayList<Request>();
	protected Request servingSequential = null;
	
	protected Request availableRequest = null;
	
	protected int maxQueue = 300;

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
		return requestsConc.size() + requestsSeq.size() + requestsMutex.size() +
				(servingMutex == null ? 0 : 1) + 
				servingConcurrent.size() +
				(servingSequential == null ? 0 : 1);
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
		//LogWriter.writeDebugInfo("Add request, there are already "+size()+" requests");
		//LogWriter.writeDebugInfo("DEBUG: "+requestsConc.size() +" "+ requestsSeq.size() +" "+ requestsMutex.size()+" "+
				//servingConcurrent.size());
		
		lock.lock();
		try {
			if(request.isConcurrent()){
				//LogWriter.writeDebugInfo("Add request "+request.getMethodId()+ " "+requestsConc.size());
				while (requestsConc.size() + servingConcurrent.size() >= maxQueue){
					canInsert.await();
				}
				
				requestsConc.add(request);
			}else if(request.isSequential()){
				//LogWriter.writeDebugInfo("Add request "+request.getMethodId()+ " "+requestsSeq.size());
				while (requestsSeq.size() >= maxQueue){
					canInsert.await();
				}
				
				requestsSeq.add(request);
			}else if(request.isMutex()){
				//LogWriter.writeDebugInfo("Add request "+request.getMethodId()+ " "+requestsMutex.size());
				while (requestsMutex.size() >= maxQueue){
					canInsert.await();
				}
				
				requestsMutex.add(request);
			}
			
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
			//LogWriter.writeDebugInfo("Peek, queue contains "+size()+" requests "+System.currentTimeMillis());
			if (availableRequest == null){
				//LogWriter.writeDebugInfo("Search for new request "+System.currentTimeMillis());
				waitSuccess = canPeek.await(time, timeUnit);
			} else {
				waitSuccess = true;
			}
			
			//LogWriter.writeDebugInfo("Got request? "+waitSuccess+" "+System.currentTimeMillis());
			if (waitSuccess) {
				request = availableRequest;
				request.setStatus(Request.Serving);
				
				serveRequest(request);
				
				availableRequest = null;
				canPeek();
			}
		} catch (InterruptedException exception) {
		} finally {
			lock.unlock();
		}
		
		/*if (requestsConc.size() + requestsSeq.size() + requestsMutex.size() > 0 &&
				!waitSuccess && requests.get(0).getStatus() == Request.Pending) {
			Request temp = requests.get(0);
			String info = String.format("Request.MethodId=%d.Semantics=%s\n",
					temp.getMethodId(), temp.getSenmatics());
			LogWriter.writeLogfile(info, LogWriter.LogFolder
					+ File.separator + "bug.txt");
		}*/
		return request;
	}

	private void serveRequest(Request request){
		//Migrate request to serving queue
		if(request.isConcurrent()){
			requestsConc.remove(request);
		}else if(request.isSequential()){
			requestsSeq.remove(request);
		}else if(request.isMutex()){
			requestsMutex.remove(request);
		}
		
		if(request.isMutex()){
			servingMutex = request;
		}else if(request.isSequential()){
			servingSequential = request;
		}else{
			servingConcurrent.add(request);
		}
	}

	/**
	 * Remove a specific request from the queue
	 * @param request Request to be removed
	 * @return true if the request is correctly removed
	 */
	public boolean remove(Request request) {
		lock.lock();
		try {
			if(request.isMutex() && servingMutex == request){
				servingMutex = null;
				requestsMutex.remove(request);
			}else if(request.isSequential() && servingSequential == request){
				servingSequential = null;
				requestsSeq.remove(request);
			}else{
				servingConcurrent.remove(request);
				requestsConc.remove(request);
			}
			
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
		requestsConc.clear();
		requestsMutex.clear();
		requestsSeq.clear();
		servingMutex = null;
		servingSequential = null;
		servingConcurrent.clear();
		
		return true;
	}

	/**
	 * Check if there is request to peek
	 * @return true if a request can be peeked
	 */
	public boolean canPeek() {
		if (availableRequest != null ||
				canPeekType(requestsConc) ||
				canPeekType(requestsSeq) ||
				canPeekType(requestsMutex)) {
			
			canPeek.signal();
			return true;
		}

		return false;
	}

	private boolean canPeekType(List<Request> requests){
		for (Request currentRequest: requests) {
			if (canPeek(currentRequest)) {
				if (availableRequest == null) {
					availableRequest = currentRequest;
				}
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check if a specific request can be peeked
	 * @param request	Request to be peeked
	 * @return true if the request can be peeked
	 */
	private boolean canPeek(Request request) {
		if (request.getStatus() != Request.Pending){
			return false;
		}
		
		if (request.isMutex()) {
			if (servingMutex != null && servingMutex.getStatus() == Request.Serving){
				return false;
			}
		}
		
		if (request.isConcurrent()) {
			for (int i = 0; i < servingConcurrent.size(); i++) {
				Request currentRequest = servingConcurrent.get(i);
				if (currentRequest.getStatus() == Request.Serving && currentRequest.isMutex()){
					return false;
				}
			}
		}
		
		if (request.isSequential()) {
			if (servingSequential != null && servingSequential.getStatus() == Request.Serving){
				return false;
			}
		}
		return true;
	}
}
