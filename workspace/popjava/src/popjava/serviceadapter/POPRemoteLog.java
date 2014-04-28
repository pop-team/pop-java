package popjava.serviceadapter;

import popjava.annotation.POPClass;
import popjava.base.Semantic;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the RemoteLog parallel object of POP-C++
 */
@POPClass(classId = 3, className = "RemoteLog")
public class POPRemoteLog extends POPServiceBase {
	
	/**
	 * Default constructor of POPRemoteLog.
	 * Create a POP-C++ object RomoteLog
	 */
	public POPRemoteLog() {
		hasDestructor(true);
		setClassId(3);
		Class<?> c = POPRemoteLog.class;
		this.definedMethodId = true;
		defineConstructor(c,10);		
		defineConstructor(c,11,String.class);
		defineMethod(c, "log", 13,Semantic.Sequence | Semantic.Asynchronous,String.class);
		defineMethod(c, "logPJ", 14, Semantic.Sequence | Semantic.Asynchronous,String.class, String.class);
	}
	
	/**
	 * Constructor of POPAppService with parameters
	 * @param challange	Challenge string to stop the service
	 */
	public POPRemoteLog(String challange) {

	}
	
	/**
	 * Write a remote log
	 * @param info	Information to be written into the remote log file
	 */
	public void log(String info) {

	}
	
	public void logPJ(String appID, String info){
		
	}

}
