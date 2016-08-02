package junit.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import popjava.interfacebase.Interface;
import popjava.serviceadapter.POPAppService;
import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.util.Util;
import popjava.util.Util.OSType;

public class POPSystemTest {

	@Test
	public void testGetIp(){
		String ip = POPSystem.getHostIP();
		assertNotSame("", ip);
		assertNotSame("127.0.0.1", ip);
	}
	
	
	
	@Test
	public void testClassId(){
		POPAppService service = new POPAppService();
		assertEquals("AppCoreService", service.getClassName());
	}
	
	@Test
	public void testClassPathDetection(){
		String classpath = POPJavaConfiguration.getPOPJavaCodePath();
		if(Util.getOSType() == OSType.Windows){
			assertFalse("Class path can not start with a /", classpath.startsWith("/"));
		}
	}
	
    @Test
    public void testWildcard(){
        POPSystem.registerCode("asdf", "*");
        
        String test = Interface.getCodeFile(Interface.getAppcoreService(), "my test");
        
        assertTrue(test.contains("asdf"));
    }
    
    @Test
    public void testJarDetection(){
        assertFalse(POPJavaConfiguration.isJar());
    }
}
