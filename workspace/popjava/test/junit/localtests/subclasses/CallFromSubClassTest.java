package junit.localtests.subclasses;

import org.junit.Test;
import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;
import popjava.system.POPSystem;

import static org.junit.Assert.*;

/**
 *
 * @author Davide Mazzoleni
 */
public class CallFromSubClassTest {
	
	@Test
	public void testCallAsSubClasses() {
		POPSystem.initialize();
		
		System.out.println("Call to A0 object");
		A0 a0 = PopJava.newActive(A0.class);
		assertEquals("A0a", a0.a());
		assertEquals("A0b", a0.b());
		assertEquals("A0c", a0.c());
		
		System.out.println("Call to A1 object (extends A0)");
		A1 a1 = PopJava.newActive(A1.class);
		assertEquals("override A0.a", "A1a", a1.a());
		assertEquals("call A0.b",     "A0b", a1.b());
		assertEquals("call A0.c",     "A0c", a1.c());
		assertEquals("own method",    "A1d", a1.d());
		
		System.out.println("Call to A2 object (extends A1)");
		A2 a2 = PopJava.newActive(A2.class);
		assertEquals("call A1.a",     "A1a", a2.a());
		assertEquals("override A0.b", "A2b", a2.b());
		assertEquals("call A0.c",     "A0c", a2.c());
		assertEquals("own method",    "A2e", a2.e());
		
		System.out.println("Call to A1 object (treated as A0)");
		A0 a1asa0 = PopJava.newActive(A0.class, a1.getAccessPoint());
		assertEquals("override A0.a", "A1a", a1asa0.a());
		assertEquals("call A0.b",     "A0b", a1asa0.b());
		assertEquals("call A0.c",     "A0c", a1asa0.c());
		
		System.out.println("Call to A2 object (treated as A0)");
		A0 a2asa0 = PopJava.newActive(A0.class, a2.getAccessPoint());
		assertEquals("call A1.a",     "A1a", a2asa0.a());
		assertEquals("override A0.b", "A2b", a2asa0.b());
		assertEquals("call A0.c",     "A0c", a2asa0.c());
		
		System.out.println("Call to A2 object (treated as A1)");
		A1 a2asa1 = PopJava.newActive(A1.class, a2.getAccessPoint());
		assertEquals("call A1.a",     "A1a", a2asa1.a());
		assertEquals("override A0.b", "A2b", a2asa1.b());
		assertEquals("call A0.c",     "A0c", a2asa1.c());
		assertEquals("a1 own method", "A1d", a2asa1.d());
		
		POPSystem.end();
	}
	
	@POPClass
	public static class A0 extends POPObject {

		@POPObjectDescription(url = "localhost")
		public A0() {
		}
		
		@POPSyncSeq
		public String a() {
			return "A0a";
		}
		
		@POPSyncSeq
		public String b() {
			return "A0b";
		}
		
		@POPSyncSeq
		public String c() {
			return "A0c";
		}
	}
	
	@POPClass
	public static class A1 extends A0 {

		@POPObjectDescription(url = "localhost")
		public A1() {
		}
		
		@POPSyncSeq
		@Override
		public String a() {
			return "A1a";
		}
		
		@POPSyncSeq
		public String d() {
			return "A1d";
		}
	}
	
	@POPClass
	public static class A2 extends A1 {

		@POPObjectDescription(url = "localhost")
		public A2() {
		}
		
		@POPSyncSeq
		@Override
		public String b() {
			return "A2b";
		}
		
		@POPSyncSeq
		public String e() {
			return "A2e";
		}
	}
}
