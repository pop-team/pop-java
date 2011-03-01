package popjava.serviceadapter;

import popjava.base.Semantic;
/**
 * Partial POP-Java class implementation to be used with the POP-C++ runtime
 * This class declares the necessary methods to use the RemoteLog parallel object of POP-C++
 */
public class POPRemoteLog extends POPServiceBase {
	
	/**
	 * Default constructor of POPRemoteLog.
	 * Create a POP-C++ object RomoteLog
	 */
	public POPRemoteLog() {
		this.setClassId(3);
		this.hasDestructor(true);
		this.setClassName("RemoteLog");
		Class<?> c = POPRemoteLog.class;
		this.definedMethodId = true;
		defineConstructor(c,10);		
		defineConstructor(c,11,String.class);
		defineMethod(c, "log", 13,Semantic.Sequence | Semantic.Asynchronous,String.class);
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

}
