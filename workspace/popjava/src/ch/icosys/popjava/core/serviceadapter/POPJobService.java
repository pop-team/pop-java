package ch.icosys.popjava.core.serviceadapter;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPParameter;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPParameter.Direction;
import ch.icosys.popjava.core.baseobject.ObjectDescription;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the JobMgr parallel object of POP-C++
 */
@POPClass(classId = 10, className = "JobCoreService", deconstructor = true)
public class POPJobService extends POPServiceBase {
	
	/**
	 * Default constructor of POPJobService.
	 * Create a POP-C++ object JobCoreService
	 */
	@POPObjectDescription(id = 10)
	public POPJobService() {
	}

	/**
	 * Constructor of POPAppService with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
	@POPObjectDescription(id = 11)
	public POPJobService(String challenge) {

	}

	/**
	 * Ask the JobCoreService service to create a new parallel object
	 * @param localservice	Access to the local application scope services
	 * @param objname		Name of the object to create
	 * @param od			Object description for the resource requirements of this object
	 * @param howmany		Number of objects to create
	 * @param objcontacts	Output arguments - contacts to the objects created
	 * @return 0 if the object is created correctly
	 */
	@POPSyncConc(id = 12)
	public int createObject(POPAccessPoint localservice, String objname,
			@POPParameter(Direction.IN) ObjectDescription od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		return 0;
	}



}
