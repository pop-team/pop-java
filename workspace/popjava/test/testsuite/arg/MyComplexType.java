package testsuite.arg;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

public class MyComplexType implements IPOPBase {
	private int theInt;
	private double theDouble;
	private int[] someInt;
	
	public MyComplexType(){}
	
	public MyComplexType(int i, double d, int[] ia){
		theInt = i;
		theDouble = d;
		someInt = ia;
	}
	
	@Override
	public boolean deserialize(POPBuffer buffer) {
		theInt = buffer.getInt();
		theDouble = buffer.getDouble();
		int size = buffer.getInt();
		someInt = buffer.getIntArray(size);
		return true;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		int is = theInt;
		buffer.putInt(is);
		double ds = theDouble;
		buffer.putDouble(ds);
		int[] ias = someInt;
		buffer.putIntArray(ias);		
		return true;
	}
	
	public String toString(){
		String output="";
		output+="MyComplexType --> int:"+theInt+" double:"+theDouble+" array: ";
		for (int i = 0; i < someInt.length; i++) {
			output+= someInt[i]+" ";
		}
		return output;
	}


}
