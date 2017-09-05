package junit.localtests.jobmanager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.Resource;
import popjava.service.jobmanager.network.NodeDirect;
import popjava.service.jobmanager.network.NodeJobManager;
import popjava.service.jobmanager.network.NodeTFC;
import popjava.service.jobmanager.network.POPNetworkNode;
import popjava.service.jobmanager.network.POPNetworkNodeFactory;
import popjava.system.POPSystem;

/**
 *
 * @author dosky
 */
public class POPJavaJobManagerConfigurationTest {
	
	TemporaryFolder tf;
	
	@Before
	public void setup() throws IOException {
		POPSystem.initialize();
		tf = new TemporaryFolder();
		tf.create();
	}
	
	@After
	public void destroy() {
		tf.delete();
		POPSystem.end();
	}
	
	@Test
	public void noFile() throws IOException {
		File jmConfig = tf.newFile();
		jmConfig.delete();
		
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
		Assert.assertTrue(jmConfig.exists());
	}
	
	@Test
	public void emptyFile() throws IOException {
		File jmConfig = tf.newFile();
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
	}
	
	@Test
	public void customResources() throws IOException {
		Resource setInitial = new Resource(700f, 600f, 500f);
		Resource setLimit = new Resource(100f, 200f, 50f);
		int setMaxJobs = 1000;
		
		String[] file = {
			"resource power " + setInitial.getFlops(),
			"resource memory " + setInitial.getMemory(),
			"resource bandwidth " + setInitial.getBandwidth(),
			"job limit " + setMaxJobs,
			"job power " + setLimit.getFlops(),
			"job memory " + setLimit.getMemory(),
			"job bandwidth " + setLimit.getBandwidth()
		};
		
		File jmConfig = tf.newFile();
		try (PrintWriter out = new PrintWriter(jmConfig)) {
			for (String line : file) {
				out.println(line);
			}
		}
		
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
		
		Resource initial = jm.getInitialAvailableResources();
		Resource limit = jm.getJobResourcesLimit();
		int maxJobs = jm.getMaxJobs();
		
		Assert.assertEquals(setInitial, initial);
		Assert.assertEquals(setLimit, limit);
		Assert.assertEquals(setMaxJobs, maxJobs);
	}
	
	@Test
	public void networks() throws IOException {
		Map<String,POPNetworkNode[]> networks = new HashMap<>();
		networks.put("1", new POPNetworkNode[]{ new NodeDirect("1", 0), new NodeJobManager("0", 0, "ssl") });
		networks.put("2", new POPNetworkNode[]{ new NodeDirect("3", 0, "daemon"), new NodeJobManager("2", 0, "ssl") });
		networks.put("3", new POPNetworkNode[]{ new NodeTFC("4", 0, "ssl") });
		
		File jmConfig = tf.newFile();
		try (PrintWriter out = new PrintWriter(jmConfig)) {
			for (Map.Entry<String, POPNetworkNode[]> entry : networks.entrySet()) {
				String network = entry.getKey();
				POPNetworkNode[] nodes = entry.getValue();
				
				out.println("network " + network);
				for (POPNetworkNode node : nodes) {
					out.print("node ");
					for (String el : node.getCreationParams()) {
						out.print(el + " ");
					}
					out.println();
				}
			}
		}
		
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
		
		String[] availableNetworks = jm.getAvailableNetworks();
		String[] configNetworks = networks.keySet().toArray(new String[0]);
		
		Assert.assertArrayEquals(configNetworks, availableNetworks);
		
		for (String network : networks.keySet()) {
			String[][] networkStringNodes = jm.getNetworkNodes(network);
			Set<POPNetworkNode> networkNodes = new HashSet<>();
			for (int i = 0; i < networkStringNodes.length; i++) {
				networkNodes.add(POPNetworkNodeFactory.makeNode(networkStringNodes[i]));
			}
			
			Set<POPNetworkNode> original = new HashSet<>(Arrays.asList(networks.get(network)));
			
			Assert.assertEquals(original, networkNodes);
		}
	}
}
