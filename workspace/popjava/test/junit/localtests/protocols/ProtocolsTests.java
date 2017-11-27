package junit.localtests.protocols;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import popjava.PopJava;
import popjava.system.POPSystem;
import popjava.util.Configuration;

/**
 *
 * @author dosky
 */
public class ProtocolsTests {
	
	@Before
	public void initPOP() {
		Configuration.getInstance().setDebug(false);
        POPSystem.initialize();
	}
	
	@After
	public void endPOP() {
		POPSystem.end();
	}
	
	@Test
	public void classicObject() {
		A a = PopJava.newActive(A.class);
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test
	public void useDefaultProtocol() {
		A a = PopJava.newActive(A.class, "localhost", new String[]{""});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test
	public void useDefaultProtocolOnPort() {
		A a = PopJava.newActive(A.class, "localhost:12050", new String[]{""});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test
	public void twoProtocols() {
		A a = PopJava.newActive(A.class, "localhost", new String[]{"socket:7000", "socket:7001"});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test
	public void twoProtocolsRandom() {
		A a = PopJava.newActive(A.class, "localhost", new String[]{"socket", "socket"});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test
	public void twoProtocolsFixedAndRandom() {
		A a = PopJava.newActive(A.class, "localhost", new String[]{"socket:8000", "socket"});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test
	public void singleChoosenProtocol() {
		A a = PopJava.newActive(A.class, "localhost:9030", new String[]{"socket"});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test
	public void singleChoosenProtocol2() {
		A a = PopJava.newActive(A.class, "localhost", new String[]{"socket:9040"});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test(expected = Exception.class)
	public void errorDoublePort() {
		A a = PopJava.newActive(A.class, "localhost:9020", new String[]{"socket:9040"});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
	
	@Test(expected = Exception.class)
	public void errorNotEnoughPorts() {
		A a = PopJava.newActive(A.class, "localhost:9050", new String[]{"socket", "socket"});
		System.out.format("AP: %s\n", a.getAccessPoint());
		a.sync();
	}
}
