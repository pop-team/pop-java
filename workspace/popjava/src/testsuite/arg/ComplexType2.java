package testsuite.arg;

import popjava.buffer.Buffer;
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
	public boolean deserialize(Buffer buffer) {
		size = buffer.getInt();
		int size2 = buffer.getInt();
		d = buffer.getDoubleArray(size2);
		return true;
	}

	@Override
	public boolean serialize(Buffer buffer) {
		int v = size;
		buffer.putInt(v);
		double[] din = d;
		buffer.putDoubleArray(din);
		return true;
	}

}
