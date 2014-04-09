package popjava.serviceadapter;

import popjava.base.Semantic;
import popjava.baseobject.POPAccessPoint;
import popjava.codemanager.AppService;

/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the AppService parallel
 * object of POP-C++
 */
public class POPAppService extends POPObjectMonitor implements AppService{
	/**
	 * Default constructor of POPAppService. Create a POP-C++ object
	 * AppCoreService
	 */
	public POPAppService() {
		this.setClassId(20);
		this.hasDestructor(true);
		this.setClassName("AppCoreService");
		Class<?> c = POPAppService.class;
		this.definedMethodId = true;
		defineConstructor(c, 10);
		defineConstructor(c, 11, String.class, boolean.class, String.class);
		defineMethod(c, "getPOPCAppID", 13, Semantic.Sequence | Semantic.Synchronous);

		defineMethod(c, "queryService", 14, Semantic.Sequence | Semantic.Asynchronous, String.class, POPServiceBase.class);
		defineMethod(c, "queryService", 15, Semantic.Sequence | Semantic.Asynchronous, String.class, POPAccessPoint.class);
		defineMethod(c, "registerService", 16, Semantic.Sequence | Semantic.Asynchronous, String.class, POPServiceBase.class);
		defineMethod(c, "unregisterService", 17, Semantic.Sequence | Semantic.Asynchronous, String.class);
	}

	/**
	 * Constructor of POPAppService with parameters
	 * 
	 * @param challenge
	 *            challenge string to stop the parallel object
	 * @param daemon
	 *            say if the parallel object is running as a deamon
	 * @param codelocation
	 *            path of the executable code
	 */
	public POPAppService(String challenge, boolean daemon, String codelocation) {
		this();
	}

	/**
	 * Ask the parallel object about the existence of a service in the runtime
	 * 
	 * @param name
	 *            Name of the service
	 * @param service
	 *            Access Point of the service
	 * @return true if the service exists
	 */
	public boolean queryService(String name, POPServiceBase service) {
		return true;
	}

	/**
	 * Ask the parallel object about the existence of a service in the runtime
	 * 
	 * @param name
	 *            Name of the service
	 * @param service
	 *            Access Point of the service
	 * @return true if the service exists
	 */
	public boolean queryService(String name, POPAccessPoint service) {
		return true;
	}

	/**
	 * Call the parallel object to register a new service in the runtime
	 * 
	 * @param name
	 *            Name of the new service
	 * @param newservice
	 *            Reference of the new service
	 * @return true if the service has been register correctly
	 */
	public boolean registerService(String name, POPServiceBase newservice) {
		return true;
	}

	/**
	 * Call the parallel object to unregister a service in the POP-C++ runtime
	 * 
	 * @param name
	 *            Name of the service to unregister
	 * @return true if the service has been unregister correctly
	 */
	public boolean unregisterService(String name) {
		return true;
	}

	public String getPOPCAppID() {
		return "";
	}
}
