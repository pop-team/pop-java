package ch.icosys.popjava.core.serviceadapter;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.codemanager.AppService;

/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the AppService parallel
 * object of POP-C++
 */
@POPClass(classId = 20, className = "AppCoreService", deconstructor = true)
public class POPAppService extends POPObjectMonitor implements AppService{
	
	/**
	 * Default constructor of POPAppService. Create a POP-C++ object
	 * AppCoreService
	 */
	@POPObjectDescription(id = 10)
	public POPAppService() {
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
	@POPObjectDescription(id = 11)
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
	@POPSyncSeq(id = 14)
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

	@POPSyncSeq(id = 15)
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
	@POPSyncSeq(id = 16)
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
	@POPSyncSeq(id = 17)
	public boolean unregisterService(String name) {
		return true;
	}

	@Override
	@POPSyncSeq(id = 13)
    public String getPOPCAppID() {
		return "";
	}
}
