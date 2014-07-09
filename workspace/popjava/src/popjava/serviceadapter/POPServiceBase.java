package popjava.serviceadapter;

import popjava.annotation.POPClass;
import popjava.base.POPObject;
import popjava.base.Semantic;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the paroc_service_base parallel object of POP-C++
 */
@POPClass(classId = 0, className = "paroc_service_base")
public class POPServiceBase extends POPObject {
	/**
	 * Default constructor of POPCodeManager.
	 * Create a POP-C++ object CodeMgr
	 */
	public POPServiceBase() {
		setClassId(0);
		definedMethodId = true;
		
		Class<?> c = POPServiceBase.class;
		defineConstructor(c, 10);
		defineConstructor(c, 11, String.class);
		defineMethod(c, "start", 12, Semantic.SEQUENCE | Semantic.SYNCHRONOUS);
		defineMethod(c, "start", 13, Semantic.SEQUENCE | Semantic.SYNCHRONOUS,String.class);
		defineMethod(c, "stop", 14, Semantic.SEQUENCE | Semantic.SYNCHRONOUS,String.class);
	}

	/**
	 * Constructor of POPServiceBase with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
	public POPServiceBase(String challenge) {

	}

	/**
	 * Start the service
	 */
	public void start() {

	}

	/**
	 * Start the service with a challenge string for the stop
	 * @param challenge	Challenge string needed for the service stop
	 */
	public void start(String challenge) {

	}

	/**
	 * Stop the service by giving a challenge string
	 * @param challenge	Challenge string needed for the service stop
	 */
	public void stop(String challenge) {

	}

}
