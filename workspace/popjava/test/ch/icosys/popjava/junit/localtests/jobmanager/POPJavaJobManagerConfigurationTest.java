package ch.icosys.popjava.junit.localtests.jobmanager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

import ch.icosys.popjava.core.service.jobmanager.POPJavaJobManager;
import ch.icosys.popjava.core.service.jobmanager.Resource;
import ch.icosys.popjava.core.service.jobmanager.external.POPNetworkDetails;
import ch.icosys.popjava.core.service.jobmanager.network.POPNetwork;
import ch.icosys.popjava.core.service.jobmanager.network.POPNetworkDescriptor;
import ch.icosys.popjava.core.service.jobmanager.network.POPNode;
import ch.icosys.popjava.core.service.jobmanager.network.POPNodeDirect;
import ch.icosys.popjava.core.service.jobmanager.network.POPNodeJobManager;
import ch.icosys.popjava.core.service.jobmanager.network.POPNodeTFC;
import ch.icosys.popjava.core.service.jobmanager.yaml.YamlConnector;
import ch.icosys.popjava.core.service.jobmanager.yaml.YamlJobManager;
import ch.icosys.popjava.core.service.jobmanager.yaml.YamlNetwork;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Util;

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

		@SuppressWarnings("unused")
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
		Assert.assertTrue(jmConfig.exists());
	}

	@Test
	public void emptyFile() throws IOException {
		File jmConfig = tf.newFile();
		@SuppressWarnings("unused")
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
	}

	@Test
	public void customResources() throws IOException {
		Resource setInitial = new Resource(700f, 600f, 500f);
		Resource setLimit = new Resource(100f, 200f, 50f);
		int setMaxJobs = 1000;

		String[] file = {
				String.format(Locale.US, "machineResources: {memory: %.0f, flops: %.0f, bandwidth: %.0f}",
						setInitial.getMemory(), setInitial.getFlops(), setInitial.getBandwidth()),
				String.format(Locale.US, "jobResources: {memory: %.0f, flops: %.0f, bandwidth: %.0f}",
						setLimit.getMemory(), setLimit.getFlops(), setLimit.getBandwidth()),
				"jobLimit: " + setMaxJobs };

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

	@Test(expected = Exception.class)
	public void wronglyWrittenConfigFile() throws IOException {
		String[] file = { "machineResources: {memory: 1233, flops: 4333, bandwidth: 343}", "thisIsAnUnknownParameter",
				"networks: {}" };

		File jmConfig = tf.newFile();
		try (PrintWriter out = new PrintWriter(jmConfig)) {
			for (String line : file) {
				out.println(line);
			}
		}

		@SuppressWarnings("unused")
		POPJavaJobManager jm = new POPJavaJobManager("localhost:2711", jmConfig.getAbsolutePath());
	}

	@Test
	public void networks() throws IOException {
		Map<String, POPNode[]> networks = new HashMap<>();
		networks.put("n1", new POPNode[] { new POPNodeDirect("1", 0), new POPNodeJobManager("0", 0, "ssl") });
		networks.put("n2", new POPNode[] { new POPNodeDirect("3", 0, "daemon"), new POPNodeJobManager("2", 0, "ssl") });
		networks.put("n3", new POPNode[] { new POPNodeTFC("4", 0, "ssl") });

		POPNetworkDetails[] expectedNetworks = { new POPNetworkDetails(new POPNetwork("n1", "n1", null)),
				new POPNetworkDetails(new POPNetwork("n2", "n2", null)),
				new POPNetworkDetails(new POPNetwork("n3", "n3", null)) };

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
					ArrayList<Map<String, Object>> yamlNodes = new ArrayList<>();
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
