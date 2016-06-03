package junit.localtests.bigData;

import static org.junit.Assert.assertEquals;


import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class BigDataTests {

    @Test
    public void testBigArray(){
        POPSystem.initialize();
        BigDataObject test = PopJava.newActive(BigDataObject.class);
        
        for(int i = 100000; i < (Integer.MAX_VALUE - 10000) / 8; i += 10000000){
        	assertEquals(i, test.arrayTest(new int[i]));
        }
        
        POPSystem.end();
        
    }
}
