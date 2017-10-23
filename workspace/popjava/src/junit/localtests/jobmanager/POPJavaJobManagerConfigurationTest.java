package junit.localtests.jobmanager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import popjava.service.jobmanager.network.POPNetworkDescriptor;
import popjava.service.jobmanager.network.POPNodeDirect;
import popjava.service.jobmanager.network.POPNodeJobManager;
import popjava.service.jobmanager.network.POPNodeTFC;
import popjava.service.jobmanager.network.POPNode;
import popjava.system.POPSystem;
import popjava.util.Util;

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
		Map<String,POPNode[]> networks = new HashMap<>();
		networks.put("1", new POPNode[]{ new POPNodeDirect("1", 0), new POPNodeJobManager("0", 0, "ssl") });
		networks.put("2", new POPNode[]{ new POPNodeDirect("3", 0, "daemon"), new POPNodeJobManager("2", 0, "ssl") });
		networks.put("3", new POPNode[]{ new POPNodeTFC("4", 0, "ssl") });
		
		File jmConfig = tf.newFile();
		try (PrintWriter out = new PrintWriter(jmConfig)) {
			for (Map.Entry<String, POPNode[]> entry : networks.entrySet()) {
				String network = entry.getKey();
				POPNode[] nodes = entry.getValue();
				
				out.println("network " + network);
				for (POPNode node : nodes) {
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
			Set<POPNode> networkNodes = new HashSet<>();
			for (int i = 0; i < networkStringNodes.length; i++) {
				ArrayList<String> params = new ArrayList<>(Arrays.asList(networkStringNodes[i]));
				String connector = Util.removeStringFromList(params, "connector=");
				POPNetworkDescriptor descriptor = POPNetworkDescriptor.from(connector);
				networkNodes.add(descriptor.createNode(params));
			}
			
			Set<POPNode> original = new HashSet<>(Arrays.asList(networks.get(network)));
			
			Assert.assertEquals(original, networkNodes);
		}
	}
}
