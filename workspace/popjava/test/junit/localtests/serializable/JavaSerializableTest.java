package junit.localtests.serializable;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;
import popjava.PopJava;
import popjava.system.POPSystem;
import popjava.util.Configuration;

/**
 * Test for serialization/deserialization of java objects.
 * 
 * @author Davide Mazzoleni
 */
public class JavaSerializableTest {
	
	@Before
	public void before() {
		POPSystem.initialize();
		Configuration.getInstance().setDebug(true);
	}
	
	@After
	public void after() {
		Configuration.getInstance().setDebug(false);
		POPSystem.end();
	}
	
	@Test
	public void sendSerializable() {
		Obj obj = PopJava.newActive(this, Obj.class);
		boolean equals = obj.isAEquals(Obj.A);
		assertTrue(equals);
	}
	
	@Test
	public void getSerializable() {
		Obj obj = PopJava.newActive(this, Obj.class);
		MySerializable b = obj.getB();
		assertEquals(Obj.B, b);
	}
	
	@Test(expected = Exception.class)
	public void errorSendUnserializableStruct() {
		Obj obj = PopJava.newActive(this, Obj.class);
		boolean equals = obj.areStructEquals(Obj.C);
		assertTrue(equals);
	}
	
	@Test(expected = Exception.class)
	public void errorGetUnserializableStruct() {
		Obj obj = PopJava.newActive(this, Obj.class);
		List<MySerializable> c = obj.getStruct();
		assertEquals(Obj.C, c);
	}
	
	@Test
	public void sendSerializableStruct() {
		Obj obj = PopJava.newActive(this, Obj.class);
		boolean equals = obj.areSerializableStructEquals(Obj.D);
		assertTrue(equals);
	}
	
	@Test
	public void getSerializableStruct() {
		Obj obj = PopJava.newActive(this, Obj.class);
		List<MySerializable> d = obj.getSerializableStruct();
		assertEquals(Obj.D, d);
	}
}
