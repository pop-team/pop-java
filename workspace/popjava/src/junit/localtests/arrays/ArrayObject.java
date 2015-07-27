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
}
