package ch.icosys.popjava.core.serviceadapter;

import ch.icosys.popjava.core.annotation.POPAsyncSeq;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;

/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the RemoteLog parallel
 * object of POP-C++
 */
@POPClass(classId = 3, className = "RemoteLog", deconstructor = true)
public class POPRemoteLog extends POPServiceBase {

	/**
	 * Default constructor of POPRemoteLog. Create a POP-C++ object RomoteLog
	 */
	@POPObjectDescription(id = 10)
	public POPRemoteLog() {
	}

	/**
	 * Constructor of POPAppService with parameters
	 * 
	 * @param challenge
	 *            Challenge string to stop the service
	 */
	@POPObjectDescription(id = 11)
	public POPRemoteLog(String challenge) {
	}

	/**
	 * Write a remote log
	 * 
	 * @param info
	 *            Information to be written into the remote log file
	 */
	@POPAsyncSeq(id = 13)
	public void log(String info) {

	}

	@POPAsyncSeq(id = 14)
	public void logPJ(String appID, String info) {

	}

}
