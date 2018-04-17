package ch.icosys.popjava.testsuite.popc;

import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.dataswaper.IPOPBase;

public class SerialObject implements IPOPBase {
	public int i;

	public double d;

	public SerialObject() {
		i = 0;
		d = 0.0;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		i = buffer.getInt();
		d = buffer.getDouble();
		return true;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		int value = i;
		buffer.putInt(value);
		double dValue = d;
		buffer.putDouble(dValue);
		return true;
	}
}
