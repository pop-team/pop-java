package junit.localtests.jobmanager;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import popjava.util.Configuration;
import popjava.util.ssl.KeyStoreDetails;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.external.POPNetworkDetails;
import popjava.util.Util;
import popjava.util.ssl.KeyPairDetails;
import popjava.util.ssl.SSLUtils;
/**
 *
 * @author dosky
 */
public class POPJavaJobManagerLiveConfigurationTest {

	@Rule
	public TemporaryFolder tf = new TemporaryFolder();
	
	@BeforeClass
	public static void bc() {
		Configuration.getInstance().setSSLKeyStoreOptions(new KeyStoreDetails());
	}
	
	@Test
	public void networks() throws IOException {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", tf.newFile().getAbsolutePath());
		assertNotNull(jm);
		
		// networks
		String ONE = "1", TWO = "2", TRE = "3";
		
		// new networks
		String N1 = jm.createNetwork(ONE).getUUID();
		assertEquals(1, jm.getAvailableNetworks().length);
		String N2 = jm.createNetwork(TWO).getUUID();
		assertEquals(2, jm.getAvailableNetworks().length);
		String N3 = jm.createNetwork(TRE).getUUID();
		assertEquals(3, jm.getAvailableNetworks().length);
		
		// duplicates
		jm.createNetwork(N1, ONE);
		assertEquals(3, jm.getAvailableNetworks().length);
		jm.createNetwork(N2, TWO);
		assertEquals(3, jm.getAvailableNetworks().length);
		jm.createNetwork(N3, TRE);
		assertEquals(3, jm.getAvailableNetworks().length);
		
		// remove
		jm.removeNetwork(N1);
		assertEquals(2, jm.getAvailableNetworks().length);
		jm.removeNetwork(N2);
		assertEquals(1, jm.getAvailableNetworks().length);
		jm.removeNetwork(N3);
		assertEquals(0, jm.getAvailableNetworks().length);
	}
	
	@Test
	public void nodes() throws IOException {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", tf.newFile().getAbsolutePath());
		assertNotNull(jm);
		
		String N = "n", M = "m";
		// node params (creation)
		String[] ONE = { "host=1", "port=0", "connector=direct", "protocol=ssh" };
		String[] TWO = { "host=0", "port=0", "connector=jobmanager", "protocol=ssl" };
		String[] TRE = { "host=3", "port=0", "connector=direct", "protocol=daemon", "secret=daemon" };
		
		// two networks
		String NID = jm.createNetwork(N).getUUID();
		String MID = jm.createNetwork(M).getUUID();
		
		// add nodes to networks
		jm.registerNode(NID, ONE);
		assertEquals(1, jm.getNetworkNodes(NID).length);
		jm.registerNode(NID, TWO);
		assertEquals(2, jm.getNetworkNodes(NID).length);
		jm.registerNode(MID, TRE);
		assertEquals(1, jm.getNetworkNodes(MID).length);
		
		// duplicates
		jm.registerNode(NID, ONE);
		assertEquals(2, jm.getNetworkNodes(NID).length);
		jm.registerNode(NID, TWO);
		assertEquals(2, jm.getNetworkNodes(NID).length);
		jm.registerNode(MID, TRE);
		assertEquals(1, jm.getNetworkNodes(MID).length);
		
		// remove
		jm.unregisterNode(NID, ONE);
		assertEquals(1, jm.getNetworkNodes(NID).length);
		jm.unregisterNode(NID, TWO);
		assertEquals(0, jm.getNetworkNodes(NID).length);
		jm.unregisterNode(MID, TRE);
		assertEquals(0, jm.getNetworkNodes(MID).length);
	}
	
	@Test
	public void mixed() throws IOException {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", tf.newFile().getAbsolutePath());
		assertNotNull(jm);
		
		String N = "n", M = "m";
		// node params (creation)
		String[] ONE = { "host=1", "port=0", "connector=direct", "protocol=ssh" };
		String[] TWO = { "host=0", "port=0", "connector=jobmanager", "protocol=ssl" };
		String[] TRE = { "host=3", "port=0", "connector=direct", "protocol=daemon", "secret=daemon" };
		
		// two networks
		String N1 = jm.createNetwork(N).getUUID();
		String M1 = jm.createNetwork(M).getUUID();
		
		// add nodes to networks
		jm.registerNode(N1, ONE);
		assertEquals(1, jm.getNetworkNodes(N1).length);
		jm.registerNode(N1, TWO);
		assertEquals(2, jm.getNetworkNodes(N1).length);
		jm.registerNode(M1, TRE);
		assertEquals(1, jm.getNetworkNodes(M1).length);
		
		// remove networks
		jm.removeNetwork(N1);
		assertEquals(1, jm.getAvailableNetworks().length);
		jm.removeNetwork(M1);
		assertEquals(0, jm.getAvailableNetworks().length);
		
		// get from unexisting network
		assertEquals(0, jm.getNetworkNodes(N1).length);
	}
	
	@Test
	public void withSSL() throws IOException {
		Configuration conf = Configuration.getInstance();
		File userConfig = File.createTempFile("popjunit", ".properties");
		File keystore = new File(String.format("popjunit-%s.jks", Util.generateUUID()));
		KeyPairDetails keyDetails = new KeyPairDetails("myTest");
		KeyStoreDetails ksDetails = new KeyStoreDetails("storepass", "keypass", keystore);

		conf.setDebug(false);

		try {
			SSLUtils.generateKeyStore(ksDetails, keyDetails);
			conf.setSSLKeyStoreOptions(ksDetails);
			conf.setUserConfig(userConfig);

			conf.store();
			
			POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", tf.newFile().getAbsolutePath());
			
			POPNetworkDetails N1 = jm.createNetwork("random");
			assertEquals(1, jm.getAvailableNetworks().length);
			assertTrue(SSLUtils.getCertificateFromAlias(N1.getUUID()) != null);
		} finally {
			userConfig.deleteOnExit();
			keystore.deleteOnExit();
			conf.setUserConfig(null);
		}
	}
}
