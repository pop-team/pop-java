package junit.localtests.security;

import java.io.File;
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
import org.junit.Ignore;
import org.junit.Test;
import popjava.PopJava;
import popjava.combox.ssl.ComboxSecureSocketFactory;
import popjava.combox.ssl.POPTrustManager;
import popjava.service.jobmanager.network.POPNodeTFC;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.ssl.KeyStoreCreationOptions;
import popjava.util.ssl.SSLUtils;


/**
 *
 * @author dosky
 */
public class MethodAccessTest {
	
	static KeyStoreCreationOptions optionsTemporary;
	static KeyStoreCreationOptions optionsTrusted;
	
	static Path configTemporary;
	static Path configTrusted;
	
	static File tempFolder;
	static File trustFolder;
	
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
			
			optionsTemporary = new KeyStoreCreationOptions(NETA, "mypass", "keypass", new File("test_store1.jks"));
			tempFolder = new File("temp1");
			
			optionsTrusted = new KeyStoreCreationOptions(NETB, "mypass", "keypass", new File("test_store2.jks"));
			trustFolder = new File("temp2");
			
			// remove possible leftovers
			Files.deleteIfExists(Paths.get(tempFolder.getAbsolutePath(), "cert1.cer"));
			Files.deleteIfExists(optionsTemporary.getKeyStoreFile().toPath());
			Files.deleteIfExists(optionsTrusted.getKeyStoreFile().toPath());
			Files.deleteIfExists(tempFolder.toPath());
			Files.deleteIfExists(trustFolder.toPath());	
			
			configTemporary = Files.createTempFile("pop-junit-", ".properties");
			configTrusted = Files.createTempFile("pop-junit-", ".properties");
			
			// create temporary
			conf.setSSLKeyStoreOptions(optionsTemporary);
			conf.setSSLTemporaryCertificateDirectory(tempFolder);
			SSLUtils.generateKeyStore(optionsTemporary);

			// setup first keystore
			Certificate opt1Pub = SSLUtils.getLocalPublicCertificate();

			// write certificate to dir
			Path temp1 = tempFolder.toPath();
			if (!temp1.toFile().exists()) {
				Files.createDirectory(temp1);
			}
			byte[] certificateBytes = SSLUtils.certificateBytes(opt1Pub);
			Path p = Paths.get(tempFolder.getAbsolutePath(), "cert1.cer");
			Files.createFile(p);
			Files.write(p, certificateBytes, StandardOpenOption.APPEND);

			// remove own certificate from keystore
			SSLUtils.removeConfidenceLink(NETA);
			conf.setUserConfig(configTemporary.toFile());
			conf.store();
			
			// create truststore
			conf.setSSLKeyStoreOptions(optionsTrusted);
			conf.setSSLTemporaryCertificateDirectory(trustFolder);
			SSLUtils.generateKeyStore(optionsTrusted);
			Path temp2 = trustFolder.toPath();
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
		Files.deleteIfExists(Paths.get(tempFolder.getAbsolutePath(), "cert1.cer"));
		Files.deleteIfExists(optionsTemporary.getKeyStoreFile().toPath());
		Files.deleteIfExists(optionsTrusted.getKeyStoreFile().toPath());
		Files.deleteIfExists(tempFolder.toPath());
		Files.deleteIfExists(trustFolder.toPath());	
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
		//POPTrustManager.getInstance().reloadTrustManager();
		
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
		//POPTrustManager.getInstance().reloadTrustManager();
		
		A a = PopJava.newActive(A.class);
		a.sync();
		System.out.println("AP Trust: " + a.getAccessPoint());
		assertTrue(a.isCallFromCL());
	}
}
