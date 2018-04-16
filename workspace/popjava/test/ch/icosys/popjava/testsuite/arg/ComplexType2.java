package testsuite.arg;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

public class ComplexType2 implements IPOPBase {
    
	private int size;
	private double[] d;
	
	public void setInt(int value){
		size = value;
	}
	
	public int getInt(){
		return size;
	}
	
	public void setDouble(double[] array){
		d = array;
	}
	
	public double[] getDouble(){
		return d;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		size = buffer.getInt();
		int size2 = buffer.getInt();
		d = buffer.getDoubleArray(size2);
		return true;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		int v = size;
		buffer.putInt(v);
		double[] din = d;
		buffer.putDoubleArray(din);
		return true;
	}

}
