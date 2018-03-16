package junit.localtests.bidirectional;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class BiDirectionalObject extends POPObject{

	private int value;
	
	public BiDirectionalObject() {
		
	}
	
	@POPObjectDescription(url = "localhost")
	public BiDirectionalObject(int value, @POPConfig(Type.LOCAL_JVM) boolean local) {
		this.value = value;
	}
	
	@POPSyncConc
	public int test() {
		BiDirectionalObject b =  PopJava.newActive(BiDirectionalObject.class, 5678, false);
		return b.test1(this);
	}
	
	@POPSyncConc
	public int test1(BiDirectionalObject ref) {
		return ref.getValue();
	}
	
	@POPSyncConc
	public int getValue() {
		return value;
	}
}
