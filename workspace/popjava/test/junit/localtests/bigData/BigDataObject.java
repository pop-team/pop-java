package junit.localtests.bigData;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPParameter.Direction;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass
public class BigDataObject extends POPObject{

	@POPObjectDescription(url = "localhost", jvmParameters = "-Xmx8800m")
	public BigDataObject(){
		
	}
	
    @POPSyncConc
    public int arrayTest(@POPParameter(Direction.IN) int [] array){
        return array.length;
    }
    
    @POPSyncConc
    public int arrayTest(@POPParameter(Direction.IN) char [] array){
        return array.length;
    }
    
    @POPSyncConc
    public int arrayTest(@POPParameter(Direction.IN) byte [] array){
        return array.length;
    }
    
    @POPSyncConc
    public int arrayTest(@POPParameter(Direction.IN) String string){
        return string.length();
    }
}

