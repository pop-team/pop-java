package ch.icosys.popjava.junit.localtests.bidirectional;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPConfig;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPConfig.Type;
import ch.icosys.popjava.core.base.POPObject;

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
		BiDirectionalObject b =  PopJava.newActive(this, BiDirectionalObject.class, 2, false);
		return b.test1(this);
	}
	
	@POPSyncConc
	public int test1(BiDirectionalObject ref) {
		return ref.test2(this);
	}
	
	@POPSyncConc
	public int test2(BiDirectionalObject ref) {
		return ref.getValue();
	}
	
	@POPSyncConc
	public int getValue() {
		return value;
	}
}
