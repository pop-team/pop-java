package junit.localtests.jvmObject;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.system.POPSystem;

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
		LocalObject obj = PopJava.newActive(LocalObject.class);
		
		assertEquals(obj.value, obj.getValue());
	}
	

	@Test
	public void testRemoteCreation(){
		LocalObject obj = PopJava.newActive(LocalObject.class, "localhost");
		
		assertNotSame(obj.value, obj.getValue());
	}
	
	
	@Test
	public void testReferences(){
		LocalObject local = PopJava.newActive(LocalObject.class);
		LocalObject remote = PopJava.newActive(LocalObject.class, "localhost");

		POPAccessPoint localAP = PopJava.getAccessPoint(local);
		POPAccessPoint remoteAP = PopJava.getAccessPoint(remote);
		
		System.out.println("Local : "+localAP);
		System.out.println("Remote : "+remoteAP);
		
		remote.setReference(local);
		
		assertEquals(local.value, remote.getRefernceValue());
		assertEquals(local.value, remote.getReference().getValue());
	}
}
