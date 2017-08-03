package junit.localtests.jobmanager;

import java.io.IOException;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import popjava.service.jobmanager.POPJavaJobManager;

/**
 *
 * @author dosky
 */
public class POPJavaJobManagerLiveConfigurationTest {

	@Rule
	public TemporaryFolder tf = new TemporaryFolder();
	
	@Test
	public void networks() throws IOException {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", tf.newFile().getAbsolutePath());
		
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
	public void nodes() throws IOException {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", tf.newFile().getAbsolutePath());
		
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
	public void mixed() throws IOException {
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", tf.newFile().getAbsolutePath());
		
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
