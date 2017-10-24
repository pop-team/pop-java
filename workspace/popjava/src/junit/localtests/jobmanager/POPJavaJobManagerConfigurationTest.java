package junit.localtests.jobmanager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.Resource;
import popjava.service.jobmanager.external.POPNetworkDetails;
import popjava.service.jobmanager.network.POPNetwork;
import popjava.service.jobmanager.network.POPNetworkDescriptor;
import popjava.service.jobmanager.network.POPNodeDirect;
import popjava.service.jobmanager.network.POPNodeJobManager;
import popjava.service.jobmanager.network.POPNodeTFC;
import popjava.service.jobmanager.network.POPNode;
import popjava.service.jobmanager.yaml.YamlConnector;
import popjava.service.jobmanager.yaml.YamlJobManager;
import popjava.service.jobmanager.yaml.YamlNetwork;
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
			String.format("machineResources: {memory: %f, flops: %f, bandwidth: %f}",
				setInitial.getMemory(), setInitial.getFlops(), setInitial.getBandwidth()),
			String.format("jobResources: {memory: %f, flops: %f, bandwidth: %f}",
				setLimit.getMemory(), setLimit.getFlops(), setLimit.getBandwidth()),
			"jobLimit: " + setMaxJobs
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
		networks.put("n1", new POPNode[]{ new POPNodeDirect("1", 0), new POPNodeJobManager("0", 0, "ssl") });
		networks.put("n2", new POPNode[]{ new POPNodeDirect("3", 0, "daemon"), new POPNodeJobManager("2", 0, "ssl") });
		networks.put("n3", new POPNode[]{ new POPNodeTFC("4", 0, "ssl") });
		
		POPNetworkDetails[] expectedNetworks = {
			new POPNetworkDetails(new POPNetwork("n1", "n1", null)),
			new POPNetworkDetails(new POPNetwork("n2", "n2", null)),
			new POPNetworkDetails(new POPNetwork("n3", "n3", null))
		};
		
		YamlJobManager yamlJm = new YamlJobManager();
		List<YamlNetwork> yamlNetworks = new ArrayList<>();
		yamlJm.setNetworks(yamlNetworks);
		File jmConfig = tf.newFile();
		try (PrintWriter out = new PrintWriter(jmConfig)) {
			for (Map.Entry<String, POPNode[]> entry : networks.entrySet()) {
				YamlNetwork yamlNetwork = new YamlNetwork();
				yamlNetwork.setUuid(entry.getKey());
				yamlNetwork.setFriendlyName(entry.getKey());
				
				List<YamlConnector> connectors = new ArrayList<>();
				yamlNetwork.setConnectors(connectors);
				
				POPNode[] nodes = entry.getValue();
				for (POPNode node : nodes) {
					YamlConnector yamlConnector = new YamlConnector();
					yamlConnector.setType(node.toYamlResource().get("connector").toString());
					ArrayList<Map<String,Object>> yamlNodes = new ArrayList<>();
					yamlNodes.add(node.toYamlResource());
					yamlConnector.setNodes(yamlNodes);
					connectors.add(yamlConnector);
				}
				
				yamlNetworks.add(yamlNetwork);
			}
			
			Yaml yaml = new Yaml();
			String output = yaml.dumpAs(yamlJm, Tag.MAP, DumperOptions.FlowStyle.AUTO);
			out.print(output);
		}
		
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
		
		POPNetworkDetails[] availableNetworks = jm.getAvailableNetworks();
		
		Assert.assertArrayEquals(expectedNetworks, availableNetworks);
		
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
