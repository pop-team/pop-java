package testsuite.popc;

import popjava.buffer.Buffer;
import popjava.dataswaper.IPOPBase;

public class SerialObject implements IPOPBase {
	public int i;
	public double d;
	
	public SerialObject(){
		i = 0;
		d = 0.0;
	}
	
	@Override
	public boolean deserialize(Buffer buffer) {
		i = buffer.getInt();
		d = buffer.getDouble();
		return true;
	}

	@Override
	public boolean serialize(Buffer buffer) {
		int value = i;
		buffer.putInt(value);
		double dValue = d;
		buffer.putDouble(dValue);
		return true;
	}
}
