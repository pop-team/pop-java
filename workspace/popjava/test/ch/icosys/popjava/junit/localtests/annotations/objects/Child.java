package junit.localtests.annotations.objects;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;

@POPClass(classId = 1235)
public class Child extends Parent{

    public Child(){
    }
    
    @POPSyncConc(id = 20)
    public void childTest(){
        
    }
    
    @Override
    @POPSyncConc(id = 22)
    public void test2(){
        
    }
}
