package popjava.broker;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.locks.*;

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
	private List<List<Request>> requests = new ArrayList<>();
	
	private Request servingMutex = null;
	private ArrayList<Request> servingConcurrent = new ArrayList<Request>();
	private Request servingSequential = null;
	
	private Request availableRequest = null;
	
	private int maxQueue = 300;

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
				//System.out.println("Add request "+request.getMethodId()+ " "+requestsSeq.size());
				//LogWriter.writeDebugInfo("Add request "+request.getMethodId()+ " "+requestsSeq.size());
				while (requestsSeq.size() >= maxQueue){
					canInsert.await();
				}
				
				requestsSeq.add(request);
				//System.out.println("Added request "+request.getMethodId()+ " "+requestsSeq.size()+" "+request.hashCode());
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
		for (Request currentRequest: requests) {
			//System.out.println("Test "+currentRequest.classId+" "+currentRequest.methodId+" "+currentRequest.hashCode()+" "+currentRequest.isMutex()+" "+currentRequest.isSequential()+" "+currentRequest.isConcurrent());
			if (canPeek(currentRequest)) {
				if (availableRequest == null) {
					//System.out.println("Accepted");
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
		
		//If any mutex request is currently running, dont serve this request
		if (servingMutex != null && servingMutex.getStatus() == Request.Serving){
			return false;
		}
		
		
		if (request.isMutex() || request.isSequential()) {
			//Dont serve mutex or seq requests if there is any sequential request running
			if (servingSequential != null && servingSequential.getStatus() == Request.Serving){
				return false;
			}
			
			if(request.isMutex()){
				//Dont serve mutex request if any concurrent request is running
				for (int i = 0; i < servingConcurrent.size(); i++) {
					Request currentRequest = servingConcurrent.get(i);
					if (currentRequest.getStatus() == Request.Serving && currentRequest.isMutex()){
						return false;
					}
				}
			}
		}
		return true;
	}
}
