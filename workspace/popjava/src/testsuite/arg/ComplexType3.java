package testsuite.arg;

import java.util.Vector;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

public class ComplexType3 implements IPOPBase {
	private Vector<Integer> vector;
	private int value;
	
	public ComplexType3(){
		vector = new Vector<Integer>(10);
		for (int i = 0; i < vector.capacity(); i++) {
			vector.add(new Integer(i));
		}
		value = vector.size();
	}
		
	@Override
	public boolean deserialize(POPBuffer buffer) {
		value = buffer.getInt();
		int size = buffer.getInt();
		int[] array = buffer.getIntArray(size);
		vector = new Vector<Integer>(size);
		for (int i = 0; i < array.length; i++) {
			vector.add(new Integer(array[i]));
		}
		return true;
	}
	
	@Override
	public boolean serialize(POPBuffer buffer) {
		int size = value;
		buffer.putInt(size);
		int[] array = new int[vector.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = vector.get(i).intValue();
		}
		buffer.putIntArray(array);
		return true;
	}
}
