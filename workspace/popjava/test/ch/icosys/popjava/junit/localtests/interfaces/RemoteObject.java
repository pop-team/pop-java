package junit.localtests.interfaces;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
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
		temp = PopJava.newActive(this, POPObjectImpl.class);
		return temp;
	}
	
	@Override
	@POPSyncConc
	public POPObjectImpl getObject2() {
		temp = PopJava.newActive(this, POPObjectImpl.class, "localhost");
		return temp;
	}

}
