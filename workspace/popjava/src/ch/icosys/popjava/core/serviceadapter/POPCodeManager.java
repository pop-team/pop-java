package ch.icosys.popjava.core.serviceadapter;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncSeq;
import ch.icosys.popjava.core.dataswaper.POPString;

/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the CodeMgr parallel object
 * of POP-C++
 */
@POPClass(classId = 2, className = "CodeMgr", deconstructor = true)
public class POPCodeManager extends POPRemoteLog {

	public static final String ALL_PLATFORMS = "*-*";

	/**
	 * Default constructor of POPCodeManager. Create a POP-C++ object CodeMgr
	 */
	@POPObjectDescription(id = 10)
	public POPCodeManager() {
	}

	/**
	 * Constructor of POPCodeManager with challenge string
	 * 
	 * @param challenge
	 *            challenge string to stop the service
	 */
	@POPObjectDescription(id = 11)
	public POPCodeManager(String challenge) {
	}

	/**
	 * Register a executable code file in the CodeMgr service
	 * 
	 * @param objname
	 *            Name of the parallel object
	 * @param platform
	 *            Platform of the executable
	 * @param codefile
	 *            Path of the executable code file
	 */
	@POPSyncSeq(id = 13)
	public void registerCode(String objname, String platform, String codefile) {
	}

	/**
	 * Query the CodeMgr to retrieve the code file for a specific object on a
	 * specific architecture
	 * 
	 * @param objname
	 *            Name of the object
	 * @param platform
	 *            Platform desired
	 * @param codefile
	 *            Output argument - code file for the specific object and the
	 *            specific platform
	 * @return 0 if the code file is not available
	 */
	@POPSyncSeq(id = 14)
	public int queryCode(String objname, String platform, POPString codefile) {

		return 0;
	}

	/**
	 * Query the CodeMgr to know the platforms of a specific object
	 * 
	 * @param objname
	 *            Name of the object
	 * @param platform
	 *            Output argument - platform available for the object
	 * @return number of platform available
	 */
	@POPSyncSeq(id = 15)
	public int getPlatform(String objname, POPString platform) {
		return 0;
	}

}
