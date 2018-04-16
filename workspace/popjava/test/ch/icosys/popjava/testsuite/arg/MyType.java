package testsuite.arg;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.*;

public class MyType implements IPOPBase {
	private String theString;
	private int theInt;
	
	public void setString(String s){
		theString = s;
	}
	
	public void setInt(int i){
		theInt = i;
	}
	
	public int getInt(){
		return theInt;
	}
	
	public String getString(){
		return theString;
	}
	
	@Override
	public boolean deserialize(POPBuffer buffer) {
		theInt = buffer.getInt();
		theString = buffer.getString();
		return true;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putInt(getInt());
		buffer.putString(getString());
		return true;
	}
	
	public String toString(){
		String output = "";
		output+=theInt+" ";
		output+=theString;
		return output;
	}

}
