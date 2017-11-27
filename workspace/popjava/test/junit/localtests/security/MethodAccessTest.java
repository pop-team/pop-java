package junit.localtests.security;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import popjava.PopJava;
import popjava.combox.ssl.ComboxSecureSocketFactory;
import popjava.service.jobmanager.network.POPNodeTFC;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.ssl.KeyPairDetails;
import popjava.util.ssl.KeyStoreDetails;
import popjava.util.ssl.SSLUtils;


/**
 *
 * @author dosky
 */
public class MethodAccessTest {
	
	static KeyStoreDetails ksTemporary;
	static KeyStoreDetails ksTrusted;
	static KeyPairDetails keyTemporary;
	static KeyPairDetails keyTrusted;
	
	static Path configTemporary;
	static Path configTrusted;
	
	static Certificate opt1Pub;
	
	Configuration conf = Configuration.getInstance();
	
	public static final String NETA = "myUUID1";
	public static final String NETB = "myUUID2";
	
	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		try {
			POPNodeTFC node = new POPNodeTFC("localhost", 2711, "socket");
			// init
			Configuration conf = Configuration.getInstance();
			conf.setDebug(true);
			
			ksTemporary = new KeyStoreDetails("mypass", "keypass", new File("test_store1.jks"));
			keyTemporary = new KeyPairDetails(NETA);
			
			ksTrusted = new KeyStoreDetails("mypass", "keypass", new File("test_store2.jks"));
			keyTrusted = new KeyPairDetails(NETB);
			
			// remove possible leftovers
			Files.deleteIfExists(ksTemporary.getKeyStoreFile().toPath());
			Files.deleteIfExists(ksTrusted.getKeyStoreFile().toPath());
			
			configTemporary = Files.createTempFile("pop-junit-", ".properties");
			configTrusted = Files.createTempFile("pop-junit-", ".properties");
			
			// create temporary
			conf.setSSLKeyStoreOptions(ksTemporary);
			SSLUtils.generateKeyStore(ksTemporary, keyTemporary);
			SSLUtils.reloadPOPManagers();
			
			// setup first keystore
			opt1Pub = SSLUtils.getCertificateFromAlias(keyTemporary.getAlias());

			// remove own certificate from keystore
			conf.setDefaultNetwork(NETA);
			SSLUtils.removeConfidenceLink(node, NETA);
			conf.setUserConfig(configTemporary.toFile());
			conf.store();
			
			// create truststore
			conf.setSSLKeyStoreOptions(ksTrusted);
			conf.setDefaultNetwork(NETB);
			SSLUtils.generateKeyStore(ksTrusted, keyTrusted);
			SSLUtils.reloadPOPManagers();
			conf.setUserConfig(configTrusted.toFile());
			conf.store();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void afterClass() throws IOException {
		Files.deleteIfExists(ksTemporary.getKeyStoreFile().toPath());
		Files.deleteIfExists(ksTrusted.getKeyStoreFile().toPath());
		Files.deleteIfExists(configTrusted);
		Files.deleteIfExists(configTemporary);
		Configuration.getInstance().setUserConfig(null);
	}
	
	@Before
	public void beforePop() {
		POPSystem.initialize();
		conf = Configuration.getInstance();
	}
	
	@After
	public void endPop() {
		POPSystem.end();
	}
	
	@Test
	public void sslComboxWorking() throws Exception {
		conf.load(configTemporary.toFile());
		SSLUtils.reloadPOPManagers();
		
		ComboxSecureSocketFactory factory = new ComboxSecureSocketFactory();
		assertTrue(factory.isAvailable());
	}
	
	@Test
	@Ignore
	public void testTemporaryConfidenceLink() throws Exception {
		conf.load(configTemporary.toFile());
		//POPTrustManager.getInstance().reloadTrustManager();

		/*X509Certificate[] certs = POPTrustManager.getInstance().getAcceptedIssuers();
		for (X509Certificate cert : certs) {
			String f = SSLUtils.certificateFingerprint(cert);
			assertFalse(SSLUtils.isConfidenceLink(f));
		}*/
	}
	
	@Test
	public void testTrustedConnection() throws Exception {
		conf.load(configTrusted.toFile());
		SSLUtils.reloadPOPManagers();
		
		A a = PopJava.newActive(A.class);
		a.sync();
		System.out.println("AP Trust: " + a.getAccessPoint());
		assertTrue(a.isCallFromCL());
	}
}
