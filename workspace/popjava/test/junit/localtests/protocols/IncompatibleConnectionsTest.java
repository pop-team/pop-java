package junit.localtests.protocols;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.Util;
import popjava.util.ssl.KeyPairDetails;
import popjava.util.ssl.KeyStoreDetails;
import popjava.util.ssl.SSLUtils;

/**
 *
 * @author dosky
 */
public class IncompatibleConnectionsTest {
	
	A socket;
	A ssl;
	
	static File keystore;
	static File userConfig;
	static KeyPairDetails keyDetails;
	static KeyStoreDetails ksDetails;
	
	@BeforeClass
	public static void setup() throws IOException {
		userConfig = File.createTempFile("popjunit", ".properties");
		keystore = new File(String.format("popjunit-%s.jks", Util.generateUUID()));
		keyDetails = new KeyPairDetails("myTest");
		ksDetails = new KeyStoreDetails("storepass", "keypass", keystore);
		
		Configuration conf = Configuration.getInstance();
		conf.setDebug(true);
		
		SSLUtils.generateKeyStore(ksDetails, keyDetails);
		conf.setSSLKeyStoreOptions(ksDetails);
		conf.setUserConfig(userConfig);
		
		conf.store();
	}
	
	@AfterClass
	public static void cleanup() {
		userConfig.deleteOnExit();
		keystore.deleteOnExit();
		Configuration.getInstance().setUserConfig(null);
		Configuration.getInstance().setDebug(false);
	}
	
	@Before
	public void before() {
		POPSystem.initialize();
		socket = PopJava.newActive(this, A.class, "localhost", new String[]{"socket"});
		ssl    = PopJava.newActive(this, A.class, "localhost", new String[]{"ssl"});
	}
	
	@After
	public void end() {
		POPSystem.end();
	}
	
	@Test(timeout = 2000, expected = Exception.class)
	@Ignore
	public void sslToSocket() {
		POPAccessPoint socketAP = socket.getAccessPoint();
		POPAccessPoint socketAsSSL = new POPAccessPoint(socketAP.toString());
		socketAsSSL.get(0).setProtocol("ssl");
		
		System.out.format("From %s to %s\n", socketAP, socketAsSSL);
		A shouldThrow = PopJava.connect(A.class, "myTest", socketAsSSL);
		shouldThrow.sync();
	}
	
	@Test(timeout = 2000, expected = Exception.class)
	public void socketToSSL() {
		POPAccessPoint sslAP = ssl.getAccessPoint();
		POPAccessPoint sslAsSocket = new POPAccessPoint(sslAP.toString());
		sslAsSocket.get(0).setProtocol("socket");
		
		System.out.format("From %s to %s\n", sslAP, sslAsSocket);
		A shouldThrow = PopJava.newActiveConnect(this, A.class, sslAsSocket);
		shouldThrow.sync();
	}
}
