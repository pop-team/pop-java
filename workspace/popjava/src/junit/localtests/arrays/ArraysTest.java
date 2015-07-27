package junit.localtests.arrays;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class ArraysTest {

    @Test
    public void testCallback(){
        System.out.println("Callback test started ...");
        POPSystem.initialize();
        
        ArrayObject array =  PopJava.newActive(ArrayObject.class);
        
        array.testArray(null);
        
        POPSystem.end();
        
    }
    
}
