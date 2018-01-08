package junit.localtests.interfaces;

import popjava.PopJava;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class RemoteObject extends POPObject implements RemoteInterface{

	private POPObjectImpl temp;
	
	public RemoteObject() {
		
	}
	
	public RemoteObject(@POPConfig(Type.URL) String url) {
		
	}
	
	@Override
	@POPSyncConc
	public POPObjectImpl getObject() {
		temp = PopJava.newActive(POPObjectImpl.class);
		return temp;
	}
	
	@Override
	@POPSyncConc
	public POPObjectImpl getObject2() {
		temp = PopJava.newActive(POPObjectImpl.class, "localhost");
		return temp;
	}

}
