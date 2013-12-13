package junit.localtests.annotations;

import static org.junit.Assert.*;

import org.junit.Test;

import popjava.PopJava;
import popjava.system.POPSystem;

public class AnnotationsTest {

	@Test
	public void testAnnotations() {
		POPSystem.initialize();
		Integer i1 = PopJava.newActive(Integer.class /* C */, "localhost");
		assertNotNull(i1);
		Integer i2 = PopJava.newActive(Integer.class /* C */, "localhost");
		assertNotNull(i2);
		// Create an array
		Integer[] tab = new Integer[3];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = PopJava.newActive(Integer.class /* C */, "localhost");
			assertNotNull(tab[i]);
		}
		i1.set(11);
		i2.set(14);
		System.out.println("i1=" + i1.get());
		System.out.println("i2=" + i2.get());
		i1.add(i2);
		System.out.println("i1+i2=" + i1.get());
		int[] testArray = new int[] { 1, 2, 3 };
		int temp = i1.arrayChanger(testArray);
		assertEquals(19, temp);
		
		assertEquals(2, testArray[0]);
		assertEquals(4, testArray[1]);
		assertEquals(6, testArray[2]);
		temp = i1.arrayChanger2(testArray);
		assertEquals(19, temp);
		assertEquals(2, testArray[0]);
		assertEquals(4, testArray[1]);
		assertEquals(6, testArray[2]);
		assertTrue(i1.arrayChanger3(testArray));
		POPSystem.end();
	}

}
