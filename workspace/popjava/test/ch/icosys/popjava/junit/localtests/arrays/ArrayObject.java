package junit.localtests.arrays;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class ArrayObject extends POPObject{

    @POPObjectDescription(url = "localhost")
    public ArrayObject(){
        
    }
    
    @POPSyncConc
    public void testArray(byte [] array){
        
    }
	
	@POPSyncConc
	public String[][] strings2d() {
		return new String[][] {
			{ "a", "b" },
			{ "c" }
		};
	}
	
	@POPSyncConc
	public String[][] empty2d() {
		return new String[0][0];
	}

	@POPSyncConc
	public String[] empty1d(){
		return new String[0];
	}
}
