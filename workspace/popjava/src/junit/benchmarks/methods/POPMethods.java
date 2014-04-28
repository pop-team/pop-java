package junit.benchmarks.methods;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncMutex;
import popjava.base.POPObject;

@POPClass
public class POPMethods extends POPObject{

	@POPObjectDescription(url = "localhost")
	public POPMethods(){
		
	}
	
	@POPSyncMutex
	public void noParamNoReturn(){
		
	}
	
	@POPSyncMutex
	public int noParamSimple(){
		return 100;
	}
	
	private static String [] complexReturn = new String[]{"asdfasdf", "asdfasdf", "asdfasdf"};
	
	@POPSyncMutex
	public String[] noParamComplex(){
		return complexReturn;
	}
}
