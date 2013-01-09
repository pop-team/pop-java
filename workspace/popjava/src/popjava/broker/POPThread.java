package popjava.broker;

/**
 * Base class of POPThread. Used to handle broker-side semantics
 *
 */
public class POPThread implements Runnable {

	/**
	 * Request to be served in the thread
	 */
	private Request request;
	
	/**
	 * Creates a new instance of POPThread with a request
	 */
	public POPThread(Request request) {
		this.request = request;
	}

	/**
	 * Return the request handled in the current POPThread
	 * @return Request currently handled
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * Set the request to be handled in this POPThread
	 * @param request Request to be handled
	 */
	public void setRequest(Request request) {
		this.request = request;
	}

	/**
	 * Launch the execution of the current POPThread
	 */
	public void run() {
		try {
			request.getBroker().invoke(request);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
