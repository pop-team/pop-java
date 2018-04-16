package ch.icosys.popjava.junit.localtests.jvmObject;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;

/**
 * Special test for the functionality to create a POPJava object in the same VM as the creator. 
 * 
 * @author beat
 *
 */
public class JVMObjectTest {

	@Before
	public void before(){
		POPSystem.initialize();
	}
	
	@After
	public void after(){
		POPSystem.end();
	}
	
	@Test
	public void testLocalCreation(){
		LocalObject obj = PopJava.newActive(this, LocalObject.class, new Double(1));
		
		assertEquals(obj.value, obj.getValue());
		assertNotSame(-1, obj.value);
	}
	

	@Test
	public void testRemoteCreation(){
		LocalObject obj = PopJava.newActive(this, LocalObject.class, "localhost");
		
		assertEquals(-1, obj.value);
		assertNotSame(obj.value, obj.getValue());
		assertNotSame(-1, obj.getValue());
	}
	
	@Test
	public void testReferences(){
		LocalObject local = PopJava.newActive(this, LocalObject.class, 3.0);
		LocalObject remote = PopJava.newActive(this, LocalObject.class, "localhost");

		assertNotSame(-1, local.value);
		assertEquals(-1, remote.value);
		
		POPAccessPoint localAP = PopJava.getAccessPoint(local);
		POPAccessPoint remoteAP = PopJava.getAccessPoint(remote);
		
		remote.setReference(local);
		
		assertEquals(local.value, remote.getRefernceValue());
		assertEquals(local.value, remote.getReference().getValue());
	}
	
	@Test
	public void testReferences2(){
		LocalObject local = PopJava.newActive(this, LocalObject.class, new Double(1));
		LocalObject remote = PopJava.newActive(this, LocalObject.class, "localhost");

		POPAccessPoint localAP = PopJava.getAccessPoint(local);
		POPAccessPoint remoteAP = PopJava.getAccessPoint(remote);
		
		local.setReference(remote);
		
		assertEquals(remote.getValue(), local.getRefernceValue());
		assertEquals(remote.getValue(), local.getReference().getValue());
	}
	
	@Test
	public void testMultipleLocalObjects(){
		LocalObject [] objs = new LocalObject[10];
		LocalObject remote = PopJava.newActive(this, LocalObject.class, "localhost");
		
		assertFalse(remote.getAccessPoint().toString().isEmpty());
		
		Set<String> accessPoints = new HashSet<>();
		
		for(int i = 0; i < objs.length; i++){
			objs[i] = PopJava.newActive(this, LocalObject.class, new Double(1));
			objs[i].setReference(remote);
		}
		
		for(int i = 0; i < objs.length; i++){
			POPAccessPoint ap = PopJava.getAccessPoint(objs[i]);
			accessPoints.add(ap.toString());
		}
		
		assertEquals(objs.length, accessPoints.size());		
		
		for(int i = 0; i < objs.length; i++){
			assertEquals(objs[i].value, objs[i].getValue());

			assertEquals(remote.getValue(), objs[i].getRefernceValue());
			assertEquals(remote.getValue(), objs[i].getReference().getValue());
		}
		
		for(int i = 0; i < objs.length; i++){
			remote.setReference(objs[i]);
			
			assertEquals(objs[i].value, remote.getRefernceValue());
			assertEquals(objs[i].value, remote.getReference().getValue());
		}
	}
	
	@Test
	public void testChainedObjectCreation(){
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, new Double(1));
		LocalObject local2 = PopJava.newActive(this, LocalObject.class, new Double(1));
		
		local1.createReference("localhost", true);
		LocalObject remote1 = local1.getReference();
		

		local2.createReference("localhost", false);
		LocalObject remote2 = local1.getReference();
		
		assertNotNull(remote1);
		assertNotNull(remote2);
		
		assertNotNull(local1.getReference());
		assertNotNull(local2.getReference());
		
		assertNotSame(local1.getReference().getValue(), local2.getReference().getValue());
		
		local1.getReference().createReference("localhost", true);
		
		assertNotSame(local1.getReference().getValue(), remote1.getReference().getValue());
	}
	
	@Test
	public void testChainedObjectCreation2(){
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, new Double(1));
		
		LocalObject remote = PopJava.newActiveConnect(this, LocalObject.class, PopJava.getThis(local1).getAccessPoint());
		
		local1.createReference("localhost", true);
		LocalObject remote1 = local1.getReference();
		LocalObject remote2 = remote.getReference();
		
		assertNotNull(remote1);
		assertNotNull(remote2);
		
		assertEquals(remote1.getValue(), remote2.getValue());
	}
	
	@Test
	public void multipleConnections(){
		Configuration.getInstance().setDebug(true);
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, new Double(1));
		
		LocalObject remote = PopJava.newActiveConnect(this, LocalObject.class, PopJava.getThis(local1).getAccessPoint());
		LocalObject remote2 = PopJava.newActiveConnect(this, LocalObject.class, PopJava.getThis(local1).getAccessPoint());
		
		assertEquals(remote.getValue(), remote2.getValue());
	}
	
	@Test(expected = Exception.class)
	public void testLocalURLAnnotationFail(){
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, true, "144.33.11.33");
	}

	@Test
	public void testLocalURLAnnotationLocal(){
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, true, "localhost");
		
		assertNotNull(local1);
	}
	
	@Test
	public void testNullParameterConstructor(){
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, true, null);
		assertNull(local1);
	}
	
	@Test
	public void testNullParameterConstructor2(){
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, true, "", null);
	}
	
	@Test(expected = Exception.class)
	public void testConstructorCreation(){
		LocalObject local1 = PopJava.newActive(this, LocalObject.class, new Long(1000));
		
		assertEquals(local1.getValue(), local1.getReference().getReference().getValue());
		
	}
	
	@Test
	@Ignore
	public void testAsyncCall() throws Exception {
		LocalObject local = PopJava.newActive(this, LocalObject.class, 2d);
		
		int n = 3;
		int m = 5;
		
		local.setAsyncVal(n);
		local.asyncCheck(m);
		local.setAsyncVal(m);
		assertFalse(local.getAsyncReport());
		TimeUnit.MILLISECONDS.sleep(500);
		assertTrue(local.getAsyncReport());
	}
	
	@Test
	public void testAsyncCallWithThis() throws Exception {
		LocalObject local = PopJava.getThis(PopJava.newActive(this, LocalObject.class, 2d));		
		int n = 3;
		int m = 5;
		
		local.setAsyncVal(n);
		local.asyncCheck(m);
		local.setAsyncVal(m);
		assertFalse(local.getAsyncReport());
		TimeUnit.MILLISECONDS.sleep(500);
		assertTrue(local.getAsyncReport());
	}
	
	@Test
	public void testProtocolsSpecificObject() {
		LocalObject localDoubleSocket = PopJava.getThis(PopJava.newActive(this, LocalObject.class, true));
		assertEquals(500, localDoubleSocket.getValue());
		
		POPAccessPoint ap = localDoubleSocket.getAccessPoint();
		System.out.println("AP: " + ap);
		assertEquals(2, ap.size());
		assertEquals(14000, ap.get(0).getPort());
	}
}
