package junit.localtests.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import popjava.PopJava;
import popjava.combox.ssl.ComboxSecureSocketFactory;
import popjava.combox.ssl.POPTrustManager;
import popjava.service.jobmanager.network.NodeTFC;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.ssl.KeyStoreOptions;
import popjava.util.ssl.SSLUtils;


/**
 *
 * @author dosky
 */
public class MethodAccessTest {
	
	static KeyStoreOptions optionsTemporary;
	static KeyStoreOptions optionsTrusted;
	
	static Path configTemporary;
	static Path configTrusted;
	
	static NodeTFC node = new NodeTFC("localhost", 2711, "socket");
	
	Configuration conf = Configuration.getInstance();
	
	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		try {
			// init
			Configuration conf = Configuration.getInstance();
			conf.setDebug(true);
			
			optionsTemporary = new KeyStoreOptions(String.format("%x@mynet", node.hashCode()), "mypass", "keypass", "test_store1.jks");
			optionsTemporary.setTempCertFolder("temp1");
			
			optionsTrusted = new KeyStoreOptions(String.format("%x@mynet2", node.hashCode()), "mypass", "keypass", "test_store2.jks");
			optionsTrusted.setTempCertFolder("temp2");
			
			// remove possible leftovers
			Files.deleteIfExists(Paths.get(optionsTemporary.getTempCertFolder(), "cert1.cer"));
			Files.deleteIfExists(Paths.get(optionsTemporary.getTempCertFolder()));
			Files.deleteIfExists(Paths.get(optionsTrusted.getTempCertFolder()));
			Files.deleteIfExists(Paths.get(optionsTemporary.getKeyStoreFile()));
			Files.deleteIfExists(Paths.get(optionsTrusted.getKeyStoreFile()));
			
			configTemporary = Files.createTempFile("pop-junit-", ".properties");
			configTrusted = Files.createTempFile("pop-junit-", ".properties");
			
			// create temporary
			conf.setSSLKeyStoreOptions(optionsTemporary);
			SSLUtils.generateKeyStore(optionsTemporary);

			// setup first keystore
			Certificate opt1Pub = SSLUtils.getLocalPublicCertificate();

			// write certificate to dir
			Path temp1 = Paths.get(optionsTemporary.getTempCertFolder());
			if (!temp1.toFile().exists()) {
				Files.createDirectory(temp1);
			}
			byte[] certificateBytes = SSLUtils.certificateBytes(opt1Pub);
			Path p = Paths.get(optionsTemporary.getTempCertFolder(), "cert1.cer");
			Files.createFile(p);
			Files.write(p, certificateBytes, StandardOpenOption.APPEND);

			// remove own certificate from keystore
			SSLUtils.removeConfidenceLink(node, "mynet");
			conf.setUserConfig(configTemporary.toFile());
			conf.store();
			
			// create truststore
			conf.setSSLKeyStoreOptions(optionsTrusted);
			SSLUtils.generateKeyStore(optionsTrusted);
			Path temp2 = Paths.get(optionsTrusted.getTempCertFolder());
			if (!temp2.toFile().exists()) {
				Files.createDirectory(temp2);
			}
			conf.setUserConfig(configTrusted.toFile());
			conf.store();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void afterClass() throws IOException {
		Files.deleteIfExists(Paths.get(optionsTemporary.getTempCertFolder(), "cert1.cer"));
		Files.deleteIfExists(Paths.get(optionsTemporary.getKeyStoreFile()));
		Files.deleteIfExists(Paths.get(optionsTrusted.getKeyStoreFile()));
		Files.deleteIfExists(Paths.get(optionsTemporary.getTempCertFolder()));
		Files.deleteIfExists(Paths.get(optionsTrusted.getTempCertFolder()));	
		Files.deleteIfExists(configTrusted);
		Files.deleteIfExists(configTemporary);
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
		POPTrustManager.getInstance().reloadTrustManager();
		
		ComboxSecureSocketFactory factory = new ComboxSecureSocketFactory();
		assertTrue(factory.isAvailable());
	}
	
	@Test
	public void testTemporaryConfidenceLink() throws Exception {
		conf.load(configTemporary.toFile());
		POPTrustManager.getInstance().reloadTrustManager();
		
		X509Certificate[] certs = POPTrustManager.getInstance().getAcceptedIssuers();
		for (X509Certificate cert : certs) {
			String f = SSLUtils.certificateFingerprint(cert);
			assertFalse(POPTrustManager.getInstance().isConfidenceLink(f));
		}
	}
	
	@Test
	public void testTrustedConnection() throws Exception {
		conf.load(configTrusted.toFile());
		POPTrustManager.getInstance().reloadTrustManager();
		
		A a = PopJava.newActive(A.class);
		a.sync();
		System.out.println("AP Trust: " + a.getAccessPoint());
		assertTrue(a.isCallFromCL());
	}
}
