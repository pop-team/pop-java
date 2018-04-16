package ch.icosys.popjava.junit.localtests.interfaces;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

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
