package popjava.broker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import popjava.util.LogWriter;

/**
 * This class represents the request queue used in the broker-side
 * Every requests are put into this request queue and are served in FIFO order
 */

public class RequestQueue {
	
	private final Lock lock = new ReentrantLock();
	private final Condition canPeek = lock.newCondition();
	private final Condition canInsert = lock.newCondition();
	
	private List<Request> requestsConc = new ArrayList<Request>();
	private List<Request> requestsSeq = new ArrayList<Request>();
	private List<Request> requestsMutex = new ArrayList<Request>();
	
	private int requestType = 0;
	private List<List<Request>> requests = new ArrayList<List<Request>>();
	
	private Request servingMutex = null;
	private Request servingSequential = null;
	private ArrayList<Request> servingConcurrent = new ArrayList<Request>();
	
	private Request availableRequest = null;
	
	public static final int DEFAULT_REQUEST_QUEUE_SIZE = 250;
	
	private int maxQueue = DEFAULT_REQUEST_QUEUE_SIZE;

	/**
	 * Creates a new instance of POPRequestQueue
	 */
	public RequestQueue() {
		requests.add(requestsConc);
		requests.add(requestsSeq);
		requests.add(requestsMutex);
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
		//LogWriter.writeDebugInfo(hashCode()+" Add request, there are already "+size()+" requests, "+request.getClassId()+" "+request.getMethodId());

		lock.lock();
		try {
			if(request.isConcurrent()){
				while (requestsConc.size() + servingConcurrent.size() >= maxQueue){
					canInsert.await();
				}
				
				requestsConc.add(request);
			}else if(request.isSequential()){
				while (requestsSeq.size() >= maxQueue){
					canInsert.await();
				}
				
				requestsSeq.add(request);
			}else if(request.isMutex()){
				while (requestsMutex.size() >= maxQueue){
					canInsert.await();
				}
				
				requestsMutex.add(request);
			}
			
			canPeek();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		} finally {
			lock.unlock();
		}
		
		//LogWriter.writeDebugInfo(hashCode()+" Added request "+request.getClassId()+" "+request.getMethodId());
		
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
			//LogWriter.writeDebugInfo("Peek, queue contains "+size()+" requests "+hashCode());
			if (availableRequest == null){
				//LogWriter.writeDebugInfo("Search for new request "+hashCode());
				waitSuccess = canPeek.await(time, timeUnit);
			} else {
				waitSuccess = true;
			}
			
			//LogWriter.writeDebugInfo("Got request? "+waitSuccess+" "+hashCode());
			if (waitSuccess) {
				request = availableRequest;
				request.setStatus(Request.SERVING);
		        
				serveRequest(request);
				
				availableRequest = null;
				canPeek();
			}
		} catch (InterruptedException exception) {
		} finally {
			lock.unlock();
		}
		
		return request;
	}

	/**
	 * Move request from availableRequest to its proper request queue
	 * @param request
	 */
	private void serveRequest(Request request){
	    
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
			}else if(request.isSequential() && servingSequential == request){
				servingSequential = null;
			}else{
				servingConcurrent.remove(request);
			}
			
			canPeek();
			canInsert.signal();
		} finally {
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
		requestType = (requestType + 1) % requests.size(); //Rotate through conc, seq, mutex to give them equal time
		if (availableRequest == null) {
			
			for(int i = 0; i < 3; i++){
				if(canPeekType(requests.get((requestType + i) % requests.size()))){
					canPeek.signal();
					return true;
				}
			}
		}else{
			canPeek.signal();
			return true;
		}

		return false;
	}

	private boolean canPeekType(List<Request> requests){
		for(int i = 0; i < requests.size(); i++){
			Request currentRequest = requests.get(i);
			if (canPeek(currentRequest)) {
				if (availableRequest == null) {
					//System.out.println("Accepted");
					availableRequest = currentRequest;
					requests.remove(i);
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
		if (request.getStatus() != Request.PENDING){
			return false;
		}
		
		//If any mutex request is currently running, dont serve this request
		if (servingMutex != null && servingMutex.getStatus() == Request.SERVING){
			return false;
		}
		
		
		if (request.isMutex() || request.isSequential()) {
			//Dont serve mutex or seq requests if there is any sequential request running
			if (servingSequential != null && servingSequential.getStatus() == Request.SERVING){
				return false;
			}
			
			if(request.isMutex()){
				//Dont serve mutex request if any concurrent request is running
				for (int i = 0; i < servingConcurrent.size(); i++) {
					Request currentRequest = servingConcurrent.get(i);
					
					//TODO: is the trailing isMutex check necessary? or even wrong?
					if (currentRequest.getStatus() == Request.SERVING && currentRequest.isMutex()){
						return false;
					}
				}
			}
		}
		return true;
	}
}
