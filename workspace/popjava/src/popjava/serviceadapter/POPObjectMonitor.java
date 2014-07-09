package popjava.serviceadapter;

import popjava.annotation.POPClass;
import popjava.base.Semantic;
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
	public POPObjectMonitor() {
		setClassId(4);
		hasDestructor(true);
		
		Class<?> c = POPObjectMonitor.class;
		this.definedMethodId = true;
		defineConstructor(c,10);
		defineConstructor(c,11,String.class);
		defineMethod(c, "killAll", 13,Semantic.CONCURRENT | Semantic.ASYNCHRONOUS);
		defineMethod(c, "manageObject",14, Semantic.SEQUENCE
				| Semantic.ASYNCHRONOUS,String.class);
		defineMethod(c, "unManageObject", 15,Semantic.SEQUENCE
				| Semantic.ASYNCHRONOUS,String.class);
		
		defineMethod(c, "checkObjects", 16,Semantic.CONCURRENT
				| Semantic.ASYNCHRONOUS);
	}

	/**
	 * Constructor of POPAppService with parameters
	 * @param challenge		challenge string to stop the parallel object
	 */
	public POPObjectMonitor(String challenge) {

	}

	/**
	 * Ask the ObjectMonitor service to kill all parallel object
	 */
	public void killAll() {

	}

	/**
	 * Ask the ObjectMinotr service to manage a new object
	 * @param p	acces point to this object
	 */
	public void manageObject(String p) {

	}

	/**
	 * Ask the ObjectMinotr service to stop the management of an object
	 * @param p	acces point to this object
	 */
	public void unManageObject(String p) {
	}

	/**
	 * Check how many parallel objects are currently alive
	 * @return	Number of currently alive parallel objects
	 */
	public int checkObjects() {
		return 0;
	}

}
