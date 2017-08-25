package junit.localtests.jvmObject;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.system.POPSystem;
import popjava.util.Configuration;

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
		LocalObject obj = PopJava.newActive(LocalObject.class, new Double(1));
		
		assertEquals(obj.value, obj.getValue());
		assertNotSame(-1, obj.value);
	}
	

	@Test
	public void testRemoteCreation(){
		LocalObject obj = PopJava.newActive(LocalObject.class, "localhost");
		
		assertEquals(-1, obj.value);
		assertNotSame(obj.value, obj.getValue());
		assertNotSame(-1, obj.getValue());
	}
	
	@Test
	public void testReferences(){
		LocalObject local = PopJava.newActive(LocalObject.class, 3.0);
		LocalObject remote = PopJava.newActive(LocalObject.class, "localhost");

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
		LocalObject local = PopJava.newActive(LocalObject.class, new Double(1));
		LocalObject remote = PopJava.newActive(LocalObject.class, "localhost");

		POPAccessPoint localAP = PopJava.getAccessPoint(local);
		POPAccessPoint remoteAP = PopJava.getAccessPoint(remote);
		
		local.setReference(remote);
		
		assertEquals(remote.getValue(), local.getRefernceValue());
		assertEquals(remote.getValue(), local.getReference().getValue());
	}
	
	@Test
	public void testMultipleLocalObjects(){
		LocalObject [] objs = new LocalObject[10];
		LocalObject remote = PopJava.newActive(LocalObject.class, "localhost");
		
		assertFalse(remote.getAccessPoint().toString().isEmpty());
		
		Set<String> accessPoints = new HashSet<String>();
		
		for(int i = 0; i < objs.length; i++){
			objs[i] = PopJava.newActive(LocalObject.class, new Double(1));
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
		LocalObject local1 = PopJava.newActive(LocalObject.class, new Double(1));
		LocalObject local2 = PopJava.newActive(LocalObject.class, new Double(1));
		
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
		LocalObject local1 = PopJava.newActive(LocalObject.class, new Double(1));
		
		LocalObject remote = PopJava.newActive(LocalObject.class, PopJava.getThis(local1).getAccessPoint());
		
		local1.createReference("localhost", true);
		LocalObject remote1 = local1.getReference();
		LocalObject remote2 = remote.getReference();
		
		assertNotNull(remote1);
		assertNotNull(remote2);
		
		assertEquals(remote1.getValue(), remote2.getValue());
	}
	
	@Test
	public void multipleConnections(){
		Configuration.setDebug(true);
		LocalObject local1 = PopJava.newActive(LocalObject.class, new Double(1));
		
		LocalObject remote = PopJava.newActive(LocalObject.class, PopJava.getThis(local1).getAccessPoint());
		LocalObject remote2 = PopJava.newActive(LocalObject.class, PopJava.getThis(local1).getAccessPoint());
		
		assertEquals(remote.getValue(), remote2.getValue());
	}
	
	@Test(expected = Exception.class)
	public void testLocalURLAnnotationFail(){
		LocalObject local1 = PopJava.newActive(LocalObject.class, true, "144.33.11.33");
	}

	@Test
	public void testLocalURLAnnotationLocal(){
		LocalObject local1 = PopJava.newActive(LocalObject.class, true, "localhost");
		
		assertNotNull(local1);
	}
	
	@Test
	public void testNullParameterConstructor(){
		LocalObject local1 = PopJava.newActive(LocalObject.class, true, null);
		assertNull(local1);
	}
	
	@Test
	public void testNullParameterConstructor2(){
		LocalObject local1 = PopJava.newActive(LocalObject.class, true, "", null);
	}
	
	@Test(expected = Exception.class)
	public void testConstructorCreation(){
		LocalObject local1 = PopJava.newActive(LocalObject.class, new Long(1000));
		
		assertEquals(local1.getValue(), local1.getReference().getReference().getValue());
		
	}
}
