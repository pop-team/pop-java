package popjava.serviceadapter;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the ObjectMonitor parallel object of POP-C++
 */
@POPClass(classId = 4, className = "ObjectMonitor")
public class POPObjectMonitor extends POPCodeManager {
	
	/**
	 * Default constructor of POPJobManager.
	 * Create a POP-C++ object JobMgr
	 */
    @POPObjectDescription(id = 10)
	public POPObjectMonitor() {
	}

	/**
	 * Constructor of POPAppService with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
    @POPObjectDescription(id = 11)
	public POPObjectMonitor(String challenge) {

	}

	/**
	 * Ask the ObjectMonitor service to kill all parallel object
	 */
	@POPAsyncConc(id = 13)
	public void killAll() {

	}

	/**
	 * Ask the ObjectMinotr service to manage a new object
	 * @param p	acces point to this object
	 */
	@POPAsyncSeq(id = 14)
	public void manageObject(String p) {

	}

	/**
	 * Ask the ObjectMinotr service to stop the management of an object
	 * @param p	acces point to this object
	 */
	@POPAsyncSeq(id = 15)
	public void unManageObject(String p) {
	}

	/**
	 * Check how many parallel objects are currently alive
	 * @return	Number of currently alive parallel objects
	 */
	@POPAsyncConc(id = 16)
	public int checkObjects() {
		return 0;
	}

}
