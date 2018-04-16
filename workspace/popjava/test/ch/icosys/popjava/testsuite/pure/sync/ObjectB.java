package ch.icosys.popjava.testsuite.pure.sync;

import ch.icosys.popjava.core.annotation.POPAsyncConc;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;

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
