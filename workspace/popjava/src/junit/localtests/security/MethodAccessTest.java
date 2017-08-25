package junit.localtests.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import popjava.PopJava;
import popjava.service.jobmanager.network.NodeTFC;
import popjava.util.Configuration;
import popjava.util.ssl.KeyStoreOptions;
import popjava.util.ssl.SSLUtils;


/**
 *
 * @author dosky
 */
public class MethodAccessTest {
	
	/*static KeyStoreOptions optionsTemporary;
	static KeyStoreOptions optionsTrusted;
	
	static NodeTFC node = new NodeTFC("localhost", 2711, "socket");
	
	@BeforeClass
	public static void before() {
		try {
			optionsTemporary = new KeyStoreOptions(String.format("%x@mynet", node.hashCode()), "mypass", "keypass", "test_store1.jks");
			optionsTemporary.setTempCertFolder("temp1");
			Configuration.SSL_KEY_STORE_OPTIONS = optionsTemporary;
			SSLUtils.generateKeyStore(optionsTemporary);

			optionsTrusted = new KeyStoreOptions(String.format("%x@mynet2", node.hashCode()), "mypass", "keypass", "test_store2.jks");
			optionsTrusted.setTempCertFolder("temp2");
			Configuration.SSL_KEY_STORE_OPTIONS = optionsTrusted;
			SSLUtils.generateKeyStore(optionsTrusted);

			// setup first keystore
			Configuration.SSL_KEY_STORE_OPTIONS = optionsTemporary;
			Certificate opt1Pub = SSLUtils.getLocalPublicCertificate();

			// write certificate to dir
			Files.createDirectory(Paths.get(optionsTemporary.getTempCertFolder()));
			byte[] certificateBytes = SSLUtils.certificateBytes(opt1Pub);
			Path p = Paths.get(optionsTemporary.getTempCertFolder(), "cert1.cer");
			Files.write(p, certificateBytes);

			// remove own certificate from keystore
			SSLUtils.removeConfidenceLink(node, "mynet");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void after() throws IOException {
		Files.deleteIfExists(Paths.get(optionsTemporary.getTempCertFolder()));
		Files.deleteIfExists(Paths.get(optionsTrusted.getTempCertFolder()));
		Files.deleteIfExists(Paths.get(optionsTemporary.getKeyStoreFile()));
		Files.deleteIfExists(Paths.get(optionsTrusted.getKeyStoreFile()));
	}
	
	@Test
	public void testTemporaryConnection() {
		Configuration.SSL_KEY_STORE_OPTIONS = optionsTemporary;
		
		A a = PopJava.newActive(A.class);	
		assertFalse(a.isCallFromCL());
	}
	
	@Test
	public void testTrustedConnection() {
		Configuration.SSL_KEY_STORE_OPTIONS = optionsTrusted;
		
		A a = PopJava.newActive(A.class);
		assertTrue(a.isCallFromCL());
	}*/
}
