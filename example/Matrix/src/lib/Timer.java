package lib;

/**
 * Helper class to measure time
 * @author Beat Wolf
 *
 */
public class Timer{
	
	private double start_point = 0;
	private double elapsed = 0;
	private boolean isRunning = false;
	
	/**
	 * Starts the timer. The call to this method is ignored if the timer is already running
	 */
	public void start(){
		if (!isRunning) {
	      isRunning = true;
	      start_point = getCurrentTime();
	    }
	}
	
	/**
	 * Stops the timer. The call is ignored if the timer is not running
	 */
	public void stop(){
		if (isRunning){
	      elapsed += getCurrentTime() - start_point;
	      isRunning = false;
	    }
	}
	
	/**
	 * Resets the timer
	 */
	public void reset(){
		//TODO: this is probably wrong, semantic unclear. What happens if timer is not running?
		start_point=getCurrentTime();
	    elapsed=0;
	}
	
	/**
	 * Returns the time in seconds that elapsed since the start of the timer if the timmer is still running
	 * or the timer between the start and stop call on this timer, if the timer is stopped.
	 * @return
	 */
	public double elapsed(){
		if (isRunning){
			return elapsed + getCurrentTime() - start_point;
		}
		return elapsed;
	}
	
	/**
	 * returns the current system time in seconds.
	 * @return
	 */
	private double getCurrentTime(){		
		return System.currentTimeMillis() / 1000.;
	}
	
}