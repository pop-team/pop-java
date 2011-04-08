package testsuite.popjavatest;

import popjava.buffer.Buffer;
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
	public boolean deserialize(Buffer buffer) {
		i = buffer.getInt();
		test = buffer.getString();
		return true;
	}

	@Override
	public boolean serialize(Buffer buffer) {
		int putI = i;
		buffer.putInt(putI);
		buffer.putString(test);
		return true;
	}
	
}
