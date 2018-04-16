package testsuite.popjavatest;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

public class SerialObject implements IPOPBase {
	
	int i;
	String test;
	
	public SerialObject(){
		test="";
		i=0;
	}
	
	public SerialObject(int i, String test){
		this.i=i;
		this.test = test;
	}
	
	@Override
	public boolean deserialize(POPBuffer buffer) {
		i = buffer.getInt();
		test = buffer.getString();
		return true;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		int putI = i;
		buffer.putInt(putI);
		buffer.putString(test);
		return true;
	}
	
}
