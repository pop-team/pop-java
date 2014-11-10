package testsuite.pure.sync;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;

@POPClass
public class ObjectB {

    @POPObjectDescription(url = "localhost")
    public ObjectB(){
        
    }
    
    @POPAsyncConc
    public void test1(){
        test2();
    }
    
    @POPAsyncConc
    public void test2(){
        ObjectB b = new ObjectB();
        
        b.test1();
    }
    
    @POPAsyncConc
    public void test3(){
        this.test2();
    }
    
}
