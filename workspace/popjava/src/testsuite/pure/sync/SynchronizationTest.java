package testsuite.pure.sync;

import popjava.annotation.POPClass;

@POPClass(isMain = true)
public class SynchronizationTest {

    public static void main(String [] args){
        
        ObjectA a = new ObjectA();
        
        long start = System.currentTimeMillis();
        
        a.getA();
        a.getB();
        System.out.println("TEST, SHOULD BE 2: "+a.getC());
        
        long total = System.currentTimeMillis() - start;
        
        System.out.println("Neeeded "+total+" ms, should be "+ObjectA.SLEEP);

        start = System.currentTimeMillis();
        
        a.testSelfCall();
        
        total = System.currentTimeMillis() - start;
        
        System.out.println("Neeeded "+total+" ms "+ObjectA.SLEEP);
        
        ObjectB b = new ObjectB();
        
    }
    
}
