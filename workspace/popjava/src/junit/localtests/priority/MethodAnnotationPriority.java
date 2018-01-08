package junit.localtests.priority;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import popjava.PopJava;
import popjava.system.POPSystem;
import static org.junit.Assert.*;
import popjava.base.POPException;

/**
 *
 * @author dosky
 */
public class MethodAnnotationPriority {
	
	@Before
	public void b() {
		POPSystem.initialize();
	}
	@After
	public void a() {
		POPSystem.end();
	}
	
	@Test
	public void BasB() {
		B b = PopJava.newActive(B.class);
		b.a();
		b.b();
		b.sync();
		assertEquals("Wrong method execution", 200, b.getValue());
	}
	
	@Test
	public void CasC() {
		C c = PopJava.newActive(C.class);
		c.a();
		c.b();
		c.sync();
		assertEquals("Wrong method execution", 200, c.getValue());
	}
	
	@Test
	public void CasB() {
		C c = PopJava.newActive(C.class);
		B b = PopJava.connect(B.class, "", c.getAccessPoint());
		b.a();
		b.b();
		b.sync();
		assertEquals("Wrong method execution", 200, b.getValue());
	}
	
	@Test
	public void XreturnIZasZ() {
		IX x = genIX();
		IZ z = x.getZ();
		assertEquals(100, z.getV());
	}
	
	IX genIX() {
		IX x = PopJava.newActive(X.class);
		return x;
	}
	
	@Test(expected = POPException.class)
	public void DchangeAnnotations() {
		D d = PopJava.newActive(D.class);
		d.sync();
	}
}
