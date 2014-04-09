package popjava.serviceadapter;
import popjava.base.Semantic;
import popjava.dataswaper.POPString;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the CodeMgr parallel object of POP-C++
 */
public class POPCodeManager extends POPRemoteLog{
	
	/**
	 * Default constructor of POPCodeManager.
	 * Create a POP-C++ object CodeMgr
	 */
	public POPCodeManager() {
		this.setClassId(2);
		this.hasDestructor(true);
		this.setClassName("CodeMgr");
		Class<?> c = POPCodeManager.class;
		this.definedMethodId=true;
		defineConstructor(c,10);
		defineConstructor(c,11,String.class);
		defineMethod(c,"registerCode",13,Semantic.Sequence
				| Semantic.Synchronous,String.class,String.class,String.class);
		defineMethod(c, "queryCode",14,Semantic.Sequence | Semantic.Synchronous,String.class,String.class,POPString.class);
		defineMethod(c, "getPlatform",15,Semantic.Sequence | Semantic.Synchronous,String.class,POPString.class);		
		
	}
	
	/**
	 * Constructor of POPCodeManager with challenge string
	 * @param challenge	challenge string to stop the service
	 */
	public POPCodeManager(String challenge) {

	}

	/**
	 * Register a executable code file in the CodeMgr service
	 * @param objname	Name of the parallel object
	 * @param platform	Platform of the executable
	 * @param codefile	Path of the executable code file
	 */
	public void registerCode(String objname, String platform, String codefile) {

	}

	/**
	 * Query the CodeMgr to retrieve the code file for a specific object on a specific architecture
	 * @param objname	Name of the object
	 * @param platform	Platform desired
	 * @param codefile	Output argument - code file for the specific object and the specific platform
	 * @return	0 if the code file is not available
	 */
	public int queryCode(String objname, String platform, POPString codefile) {
		return 0;
	}

	/**
	 * Query the CodeMgr to know the platforms of a specific object
	 * @param objname	Name of the object
	 * @param platform	Output argument - platform available for the object
	 * @return	number of platform available
	 */
	public int getPlatform(String objname, POPString platform) {
		return 0;
	}

}
