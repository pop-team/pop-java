package ch.icosys.popjava.junit.localtests.bigData;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;
import ch.icosys.popjava.core.annotation.POPParameter;
import ch.icosys.popjava.core.annotation.POPSyncConc;
import ch.icosys.popjava.core.annotation.POPParameter.Direction;
import ch.icosys.popjava.core.base.POPObject;

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

