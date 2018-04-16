package junit.localtests.interfaces;

import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class POPObjectImpl extends POPObject implements GenericObject{


	public POPObjectImpl() {
		
	}
	
	public POPObjectImpl(@POPConfig(Type.URL) String url) {
		
	}
	
	@POPSyncConc
	@Override
	public int test() {
		return 1234;
	}
	
}
