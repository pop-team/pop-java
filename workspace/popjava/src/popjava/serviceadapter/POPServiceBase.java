package popjava.serviceadapter;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the paroc_service_base parallel object of POP-C++
 */
@POPClass(classId = 0, className = "paroc_service_base", deconstructor = true)
public class POPServiceBase extends POPObject {
	/**
	 * Default constructor of POPCodeManager.
	 * Create a POP-C++ object CodeMgr
	 */
	@POPObjectDescription(id = 10)
	public POPServiceBase() {
	}

	/**
	 * Constructor of POPServiceBase with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
	@POPObjectDescription(id = 11)
	public POPServiceBase(String challenge) {

	}

	/**
	 * Start the service
	 */
	@POPAsyncConc(id = 12)
	public void start() {

	}

	/**
	 * Start the service with a challenge string for the stop
	 * @param challenge	Challenge string needed for the service stop
	 */
	@POPSyncSeq(id = 13)
	public void start(String challenge) {

	}

	/**
	 * Stop the service by giving a challenge string
	 * @param challenge	Challenge string needed for the service stop
	 */
	@POPSyncSeq(id = 14)
	public void stop(String challenge) {

	}

}
