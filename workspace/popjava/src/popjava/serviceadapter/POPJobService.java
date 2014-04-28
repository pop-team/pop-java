package popjava.serviceadapter;

import popjava.annotation.POPClass;
import popjava.base.Semantic;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.ObjectDescriptionInput;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the JobMgr parallel object of POP-C++
 */
@POPClass(classId = 10, className = "JobCoreService")
public class POPJobService extends POPServiceBase {
	/**
	 * Default constructor of POPJobService.
	 * Create a POP-C++ object JobCoreService
	 */
	public POPJobService() {
		setClassId(10);
		definedMethodId = true;
		
		Class<?> c = POPJobService.class;
		defineConstructor(c, 10);
		defineConstructor(c, 11, String.class);
		//Define method with same MethodID as POP-C++ MethodID
		defineMethod(c, "createObject", 12, Semantic.Concurrent | Semantic.Synchronous, 
				POPAccessPoint.class, String.class,	ObjectDescriptionInput.class, 
				int.class, POPAccessPoint[].class, int.class, POPAccessPoint[].class);
	}

	/**
	 * Constructor of POPAppService with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
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
	public int createObject(POPAccessPoint localservice, String objname,
			ObjectDescriptionInput od, int howmany, POPAccessPoint[] objcontacts, int howmany2, POPAccessPoint[] remotejobcontacts) {
		return 0;
	}



}
