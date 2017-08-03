package junit.localtests.jobmanager;

import java.io.File;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import popjava.service.jobmanager.POPJavaJobManager;

/**
 *
 * @author dosky
 */
public class POPJavaJobManagerLiveConfigurationTest {
		
	TemporaryFolder tf;
	File config;
	
	@Before
	public void setup() throws IOException {
		tf = new TemporaryFolder();
		tf.create();
		config = tf.newFile();
	}
	
	@After
	public void destroy() {
		tf.delete();
	}
	
	@Test
	public void networks() {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", config.getAbsolutePath());
		
		// networks
		String ONE = "1", TWO = "2", TRE = "3";
		
		// new networks
		jm.createNetwork(ONE);
		Assert.assertEquals(1, jm.getAvailableNetworks().length);
		jm.createNetwork(TWO);
		Assert.assertEquals(2, jm.getAvailableNetworks().length);
		jm.createNetwork(TRE);
		Assert.assertEquals(3, jm.getAvailableNetworks().length);
		
		// duplicates
		jm.createNetwork(ONE);
		Assert.assertEquals(3, jm.getAvailableNetworks().length);
		jm.createNetwork(TWO);
		Assert.assertEquals(3, jm.getAvailableNetworks().length);
		jm.createNetwork(TRE);
		Assert.assertEquals(3, jm.getAvailableNetworks().length);
		
		// remove
		jm.removeNetwork(ONE);
		Assert.assertEquals(2, jm.getAvailableNetworks().length);
		jm.removeNetwork(TWO);
		Assert.assertEquals(1, jm.getAvailableNetworks().length);
		jm.removeNetwork(TRE);
		Assert.assertEquals(0, jm.getAvailableNetworks().length);
	}
	
	@Test
	public void nodes() {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", config.getAbsolutePath());
		
		String N = "n", M = "m";
		// node params (creation)
		String[] ONE = { "host=1", "port=0", "connector=direct", "protocol=ssh" };
		String[] TWO = { "host=0", "port=0", "connector=jobmanager", "protocol=ssl" };
		String[] TRE = { "host=3", "port=0", "connector=direct", "protocol=daemon", "secret=daemon" };
		
		// two networks
		jm.createNetwork(N);
		jm.createNetwork(M);
		
		// add nodes to networks
		jm.registerNode(N, ONE);
		Assert.assertEquals(1, jm.getNetworkNodes(N).length);
		jm.registerNode(N, TWO);
		Assert.assertEquals(2, jm.getNetworkNodes(N).length);
		jm.registerNode(M, TRE);
		Assert.assertEquals(1, jm.getNetworkNodes(M).length);
		
		// duplicates
		jm.registerNode(N, ONE);
		Assert.assertEquals(2, jm.getNetworkNodes(N).length);
		jm.registerNode(N, TWO);
		Assert.assertEquals(2, jm.getNetworkNodes(N).length);
		jm.registerNode(M, TRE);
		Assert.assertEquals(1, jm.getNetworkNodes(M).length);
		
		// remove
		jm.unregisterNode(N, ONE);
		Assert.assertEquals(1, jm.getNetworkNodes(N).length);
		jm.unregisterNode(N, TWO);
		Assert.assertEquals(0, jm.getNetworkNodes(N).length);
		jm.unregisterNode(M, TRE);
		Assert.assertEquals(0, jm.getNetworkNodes(M).length);
	}
	
	@Test
	public void mixed() {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", config.getAbsolutePath());
		
		String N = "n", M = "m";
		// node params (creation)
		String[] ONE = { "host=1", "port=0", "connector=direct", "protocol=ssh" };
		String[] TWO = { "host=0", "port=0", "connector=jobmanager", "protocol=ssl" };
		String[] TRE = { "host=3", "port=0", "connector=direct", "protocol=daemon", "secret=daemon" };
		
		// two networks
		jm.createNetwork(N);
		jm.createNetwork(M);
		
		// add nodes to networks
		jm.registerNode(N, ONE);
		Assert.assertEquals(1, jm.getNetworkNodes(N).length);
		jm.registerNode(N, TWO);
		Assert.assertEquals(2, jm.getNetworkNodes(N).length);
		jm.registerNode(M, TRE);
		Assert.assertEquals(1, jm.getNetworkNodes(M).length);
		
		// remove networks
		jm.removeNetwork(N);
		Assert.assertEquals(1, jm.getAvailableNetworks().length);
		jm.removeNetwork(M);
		Assert.assertEquals(0, jm.getAvailableNetworks().length);
		
		// get from unexisting network
		Assert.assertEquals(0, jm.getNetworkNodes(N).length);
	}
}
