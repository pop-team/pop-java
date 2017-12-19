package junit.localtests.interfaces;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class POPObjectImpl extends POPObject implements GenericObject{


	@POPObjectDescription(localJVM = false)
	public POPObjectImpl() {
		
	}
	

	@POPObjectDescription(localJVM = false)
	public POPObjectImpl(@POPConfig(Type.URL) String url) {
		
	}
	
	@POPSyncConc
	@Override
	public int test() {
		return 1234;
	}
	
}
