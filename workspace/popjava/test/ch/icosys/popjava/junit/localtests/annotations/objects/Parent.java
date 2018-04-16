package junit.localtests.annotations.objects;

import popjava.annotation.POPClass;
import popjava.annotation.POPPrivate;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;

@POPClass(classId = 1234)
public class Parent extends POPObject{

    public Parent(){
        
    }
    
    @POPSyncConc(id = 20)
    public void parentTest(){
        
    }
    
    @POPSyncConc(id = 21)
    public void test2(){
        
    }
    
    @POPPrivate
    public int testPrivate() {
    	return 1234;
    }
}
