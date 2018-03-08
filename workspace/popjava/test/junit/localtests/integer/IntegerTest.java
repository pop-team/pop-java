package junit.localtests.integer;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import testsuite.integer.Integer;

public class IntegerTest {

	@Test
	public void testInteger(){
		Configuration.getInstance().setDebug(true);
		
		POPSystem.initialize();
		Integer i1 = (Integer) PopJava.newActive(Integer.class);
		Integer i2 = (Integer) PopJava.newActive(Integer.class);
		i1.set(23);
		i2.set(25);
		System.out.println("i1 = "+i1.get());
		System.out.println("i2 = "+i2.get());
		i1.add(i2);
		int sum =  i1.get();
		System.out.println("i1+i2 = "+sum);
		POPSystem.end();
		
		assertEquals(48, sum);
		
	}
}
