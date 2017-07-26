package junit.localtests.arrays;

import org.junit.Assert;
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
	

	@Test
	public void testEmptyArray(){

        POPSystem.initialize();
        
        ArrayObject array =  PopJava.newActive(ArrayObject.class);

		Assert.assertArrayEquals(new String[0], array.empty1d());
		
        POPSystem.end();
	}
	
	@Test
	public void testTriangularBidimensional() {
		System.out.println("Get triangular array test started...");
        POPSystem.initialize();
        
        ArrayObject array =  PopJava.newActive(ArrayObject.class);
		
		Assert.assertArrayEquals(new String[][] { { "a", "b" }, { "c" } }, array.strings2d());
		Assert.assertArrayEquals(new String[0][0], array.empty2d());
        
        POPSystem.end();
	}
    
}
