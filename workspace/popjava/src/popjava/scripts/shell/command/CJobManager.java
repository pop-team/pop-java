package popjava.scripts.shell.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import popjava.JobManagerConfig;
import popjava.PopJava;
import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.combox.ComboxFactory;
import popjava.combox.ComboxFactoryFinder;
import popjava.dataswaper.POPString;
import popjava.scripts.POPJShell;
import popjava.scripts.shell.CommandHandler;
import popjava.scripts.shell.CommandInfo;
import popjava.scripts.shell.ICommand;
import popjava.scripts.shell.Parameter;
import popjava.scripts.shell.ParameterInfo;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.external.POPNetworkDetails;
import popjava.service.jobmanager.network.POPNetworkDescriptor;
import popjava.service.jobmanager.network.POPNetworkDescriptorFinder;
import popjava.service.jobmanager.network.POPNode;
import popjava.util.Configuration;
import popjava.util.ssl.SSLUtils;

/**
 * Handle the Job Manager
 * 
 * @author Davide Mazzoleni
 */
public class CJobManager implements ICommand {
	
	private final CommandHandler commandHandler = new CommandHandler();
	
	private POPJavaJobManager jobManager = null;
	private JobManagerConfig jobManagerConfig = null;
	
	public CJobManager() {
		initCommands();
	}

	private void initCommands() {
		commandHandler.add(new Start());
		commandHandler.add(new Stop());
		commandHandler.add(new Network());
		commandHandler.add(new Node());
	}

	@Override
	public String keyword() {
		return "jm";
	}

	@Override
	public int execute(CommandInfo info) {
		if (info.canAdvance()) {
			return commandHandler.execute(info.advance());
		} else {
			System.out.println(help());
			return 1;
		}
	}

	@Override
	public String help() {
		return commandHandler.help();
	}

	@Override
	public String description() {
		return "configuration of the local job manager";
	}
	
	private boolean isJobManagerRunning() {
		try {
			jobManager.query("power", new POPString());
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	private JobManagerConfig getJobManagerConfig() {
		if (jobManagerConfig == null) {
			jobManagerConfig = new JobManagerConfig();
		}
		return jobManagerConfig;
	}
	
	
	
	private class Start implements ICommand {

		public Start() {
		}

		@Override
		public String keyword() {
			return "start";
		}

		@Override
		public int execute(CommandInfo info) {
			if (isJobManagerRunning()) {
				System.err.println("The Job Manager is already running.");
				return 1;
			}
			
			Configuration conf = Configuration.getInstance();
			String[] protocols = conf.getJobManagerProtocols();
			int[] ports = conf.getJobManagerPorts();
			
			for (int i = 0; i < ports.length; i++) {
				protocols[i] += ":" + ports[i]; 
			}
			
			ObjectDescription od = new ObjectDescription();
			od.setNetwork(POPJShell.configuration.getPrivateNetwork());
			jobManager = PopJava.newActive(POPJavaJobManager.class, od, "localhost", protocols);
			jobManager.start();
			System.out.println("Job Manager started.");
			
			return 0;
		}

		@Override
		public String help() {
			return "usage: jm start\n" +
				description();
		}

		@Override
		public String description() {
			return "start a job manager process";
		}
	}

	private class Stop implements ICommand {

		public Stop() {
		}

		@Override
		public String keyword() {
			return "stop";
		}

		@Override
		public int execute(CommandInfo info) {
			if (!isJobManagerRunning()) {
				System.err.println("No job manager was found.");
				return 1;
			}
			
			PopJava.destroy(jobManager);
			jobManager = null;
			jobManagerConfig = null;
			
			return 0;
		}

		@Override
		public String help() {
			return "usage: jm start\n" +
				description();
		}

		@Override
		public String description() {
			return "stop the currently running job manager process";
		}
	}

	
	
	private class Network implements ICommand {
		
		private final CommandHandler commandNetworkHandler = new CommandHandler();

		public Network() {
			initNetworkCommands();
		}

		private void initNetworkCommands() {
			commandNetworkHandler.add(new NetworkCreate());
			commandNetworkHandler.add(new NetworkRemove());
			commandNetworkHandler.add(new NetworkList());
		}

		@Override
		public String keyword() {
			return "network";
		}

		@Override
		public int execute(CommandInfo info) {
			if (info.canAdvance()) {
				return commandNetworkHandler.execute(info.advance());
			} else {
				System.out.println(help());
				return 1;
			}
		}

		@Override
		public String help() {
			return commandNetworkHandler.help();
		}

		@Override
		public String description() {
			return "operation on the job manager network";
		}
	}

	private class NetworkCreate implements ICommand {

		public NetworkCreate() {
		}

		@Override
		public String keyword() {
			return "create";
		}

		@Override
		public int execute(CommandInfo info) {
			if (!isJobManagerRunning()) {
				System.err.println("The Job Manager is not running");
				return 1;
			}
			
			Parameter params = info.extractParameter(
				new ParameterInfo("name", "--name", "-n"),
				new ParameterInfo("uuid", "--uuid", "-u")
			);
			
			String name = params.get("name");
			String uuid = params.get("uuid");
			
			JobManagerConfig jmc = getJobManagerConfig();
			
			POPNetworkDetails details;
			if (uuid == null || uuid.trim().isEmpty()) {
				details = jmc.createNetwork(name);
			}
			else {
				details = jmc.createNetwork(uuid, name);
			}
			
			if (details != null) {
				System.out.format("Network '%s' created with id [%s]\n", details.getFriendlyName(), details.getUUID());
				byte[] pubCert = SSLUtils.certificateBytes(details.getCertificate());
				Path out = Paths.get(details.getFriendlyName() + "@" + details.getUUID() + ".cer");
				try {
					Files.write(out, pubCert);
					System.out.format("Network certificate available at '%s'\n", out.toAbsolutePath().toString());
				} catch (IOException ex) {
					System.err.println("Couldn't write public certificate to disk.");
				}
			} 
			else {
				System.err.println("Can't create network.");
				return 2;
			}
			
			return 0;
		}

		@Override
		public String help() {
			return "usage: jm netowrk create [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --name, -n          A friendly name for this network\n" +
				"  --uuid, -u          Assign a specific UUID to this network";
		}

		@Override
		public String description() {
			return "create a new network with a generated uuid or a fixed one";
		}
	}

	private class NetworkRemove implements ICommand {

		public NetworkRemove() {
		}

		@Override
		public String keyword() {
			return "remove";
		}

		@Override
		public int execute(CommandInfo info) {
			if (!isJobManagerRunning()) {
				System.err.println("The Job Manager is not running");
				return 1;
			}
			
			Parameter params = info.extractParameter(
				new ParameterInfo("uuid", "--uuid", "-u")
			);
			
			String uuid = params.get("uuid", null);
			
			if (uuid == null && info.canAdvance()) {
				uuid = info.getParams()[0];
			} else {
				uuid = params.get("uuid");
			}
			
			JobManagerConfig jmc = getJobManagerConfig();
			String id = uuid;
			POPNetworkDetails pnd = Arrays.asList(jmc.availableNetworks())
				.stream()
				.filter(d -> d.getUUID().equals(id))
				.findFirst().orElse(null);
			
			if (pnd != null) {
				jmc.removeNetwork(uuid);
				System.out.format("Removed network [%s](%s)\n", pnd.getFriendlyName(), pnd.getUUID());
			} else {
				System.out.format("Couldn't find network with id %s\n", uuid);
			}
			
			return 0;
		}

		@Override
		public String help() {
			return "usage: jm netowrk remove [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --uuid, -u          The UUID of the network to remove";
		}

		@Override
		public String description() {
			return "remove an existing network from the runnig job manager";
		}
	}

	private class NetworkList implements ICommand {

		public NetworkList() {
		}

		@Override
		public String keyword() {
			return "list";
		}

		@Override
		public int execute(CommandInfo info) {
			if (!isJobManagerRunning()) {
				System.err.println("The Job Manager is not running");
				return 1;
			}
			
			JobManagerConfig jmc = getJobManagerConfig();
			
			System.out.println("Note that networks are identified by their UUID.");
			System.out.println("+------------------------------------------+--------------------------------+");
			System.out.println("| UUID                                     | Friendly name                  |");
			System.out.println("+==========================================+================================+");

			POPNetworkDetails[] networks = jmc.availableNetworks();

			for (POPNetworkDetails network : networks) {
				System.out.format("| %-40s | %-30s |\n", network.getUUID(), network.getFriendlyName());
				System.out.println("+------------------------------------------+--------------------------------+");
			}

			return 0;
		}

		@Override
		public String help() {
			return "usage: jm netowrk list\n" +
				description();
		}

		@Override
		public String description() {
			return "list all networks in the jobmanager";
		}
	}
	
	

	private class Node implements ICommand {

		private final CommandHandler commandNodeHandler = new CommandHandler();
		
		public Node() {
			initNodeCommands();
		}

		private void initNodeCommands() {
			commandNodeHandler.add(new NodeAdd());
			commandNodeHandler.add(new NodeRemove());
			commandNodeHandler.add(new NodeList());
		}

		@Override
		public String keyword() {
			return "node";
		}

		@Override
		public int execute(CommandInfo info) {
			if (info.canAdvance()) {
				return commandNodeHandler.execute(info.advance());
			} else {
				System.out.println(help());
				return 1;
			}
		}

		@Override
		public String help() {
			return commandNodeHandler.help();
		}

		@Override
		public String description() {
			return "operation on the job manager nodes";
		}
	}

	private class NodeAdd implements ICommand {

		public NodeAdd() {
		}

		@Override
		public String keyword() {
			return "add";
		}

		@Override
		public int execute(CommandInfo info) {
			if (!isJobManagerRunning()) {
				System.err.println("The Job Manager is not running");
				return 1;
			}

			Parameter params = info.extractParameter(
				new ParameterInfo("type", "--type", "-t"),
				new ParameterInfo("uuid", "--uuid", "-u"),
				new ParameterInfo("host", "--host", "-H"),
				// potential parameters (all)
				new ParameterInfo("port", "--port", "-p"),
				new ParameterInfo("protocol", "--protocol", "-P"),
				new ParameterInfo("certificate", "--certificate", "-c"),
				// param (direct)
				new ParameterInfo("secret", "--secret", "-s")
			);
			
			String type = params.get("type");
			String uuid = params.get("uuid");
			
			// abort if we don't know which node it want to create
			// TODO use the POPNetworkDescriptor in POPConnectorXYZ? (currently package access)
			POPNetworkDescriptor descriptor = POPNetworkDescriptor.from(type);
			if (descriptor == null) {
				System.err.format("unknown node for type '%s'.\n", type);
				System.err.format("Available descriptors: %s\n", Arrays.toString(POPNetworkDescriptorFinder.getInstance().all()));
				return 2;
			}
			
			// check network
			List<POPNetworkDetails> networks = Arrays.asList(jobManager.getAvailableNetworks());
			boolean hasNetwork = networks.stream()
				.filter(d -> d.getUUID().equalsIgnoreCase(uuid))
				.count() == 1;
			if (!hasNetwork) {
				System.err.format("Can't find network with id '%s'", uuid);
				System.err.println("Available IDs:");
				for (POPNetworkDetails network : networks) {
					System.err.println("  " + network.getUUID());
				}
				return 3;
			}
			
			// handle parameters
			String host = params.get("host");
			String port = params.get("port");
			switch (descriptor.getGlobalName()) {
				case "jobmanager":
				case "tfc": {
					String protocol = params.get("protocol");
					String certificate = params.get("certificate");
					ComboxFactory factory = ComboxFactoryFinder.getInstance().findFactory(protocol);
					if (factory == null) {
						System.err.format("protocol '%s' not found, node not added\n", protocol);
						return 4;
					}
					
					if (!factory.isAvailable()) {
						System.out.println("Warning: this factory seems not to be available or disabled.");
					}
					
					Certificate cert = null;
					if (certificate != null && !certificate.isEmpty()) {
						try {
							byte[] bytes = Files.readAllBytes(Paths.get(certificate));
							cert = SSLUtils.certificateFromBytes(bytes);
						} catch (IOException ex) {
							System.err.println("Can't read certificate.");
							return 5;
						} catch (CertificateException ex) {
							System.err.println("Given file doesn't seems to be a X.501 certificate.");
							return 6;
						}
					}
					
					String[] nodeParams = {
						"host=" + host,
						"port=" + port,
						"protocol=" + protocol
					};
					POPNode node = descriptor.createNode(Arrays.asList(nodeParams));
					
					JobManagerConfig jmc = getJobManagerConfig();
					
					if (cert == null) {
						jmc.registerNode(uuid, node);
					} else {
						jmc.registerNode(uuid, node, cert);
					}
					System.out.format("Node added to network '%s'\n", uuid);
					return 0;
				}
				case "direct": {
					String protocol = params.get("protocol");
					if (protocol == null || protocol.isEmpty()) {
						protocol = "ssh";
					}
					
					
					return 0;
				}
			}
			return 1;
		}

		@Override
		public String help() {
			return "usage: jm node add [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --type, -t          The type of node we are working with (jobmanager, tfc, direct)\n" +
				"  --uuid, -u          The UUID of the network to add the node into\n" +
				"  --host, -H          The destination host of the node\n" +
				"  --port, -p          The destination port of the node\n" +
				"  --protocol, -P      The node specific protocol (socket, ssl, daemon)\n" +
				"  --certificate, -c   The node specific protocol (socket, ssl, daemon)\n" +
				"Node specific options will be asked.";
		}

		@Override
		public String description() {
			return "add a new node to a network";
		}
	}

	private class NodeRemove implements ICommand {

		public NodeRemove() {
		}

		@Override
		public String keyword() {
			return "remove";
		}

		@Override
		public int execute(CommandInfo info) {
			if (!isJobManagerRunning()) {
				System.err.println("The Job Manager is not running");
				return 1;
			}

			Parameter params = info.extractParameter(
				new ParameterInfo("uuid", "--uuid", "-u"),
				new ParameterInfo("id", "--id", "-I")
			);
			
			String uuid = params.get("uuid");
			// check network
			List<POPNetworkDetails> networks = Arrays.asList(jobManager.getAvailableNetworks());
			boolean hasNetwork = networks.stream()
				.filter(d -> d.getUUID().equalsIgnoreCase(uuid))
				.count() == 1;
			if (!hasNetwork) {
				System.err.format("Can't find network with id '%s'", uuid);
				System.err.println("Available IDs:");
				for (POPNetworkDetails network : networks) {
					System.err.println("  " + network.getUUID());
				}
				return 3;
			}
			
			JobManagerConfig jmc = getJobManagerConfig();

			// available nodes
			POPNode[] nodes = jmc.networkNodes(uuid);
			
			String sid = params.get("id", null);
			
			if (sid == null || sid.isEmpty()) {
				System.out.println("Choose a node:");
				for (int i = 0; i < nodes.length; i++) {
					System.out.format(" [%d] %s\n", i, nodes[i].toString());
				}
				sid = params.get("id");
			}
			
			try {
				int id = Integer.parseInt(sid);
				if (id >= nodes.length || id < 0) {
					System.err.println("Given ID is not valid.");
					return 4;
				}
				
				POPNode node = nodes[id];
				jmc.unregisterNode(uuid, node);
				
				System.out.println("Node removed.");
			} catch(NumberFormatException e) {
				System.out.println("Given ID is not a number.");
				return 5;
			}

			return 0;
		}

		@Override
		public String help() {
			return "usage: jm node remove [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --uuid, -u          The UUID of the network\n" +
				"  --id, -I            The id of the node\n";
		}

		@Override
		public String description() {
			return "remove a node from a network";
		}
	}

	private class NodeList implements ICommand {

		public NodeList() {
		}

		@Override
		public String keyword() {
			return "list";
		}

		@Override
		public int execute(CommandInfo info) {
			if (!isJobManagerRunning()) {
				System.err.println("The Job Manager is not running");
				return 1;
			}
			
			Parameter params = info.extractParameter(
				new ParameterInfo("uuid", "--uuid", "-u")
			);
			
			JobManagerConfig jmc = getJobManagerConfig();

			String uuid = params.get("uuid", null);

			if (uuid == null && info.canAdvance()) {
				uuid = info.getParams()[0];
			}
			else if (uuid == null) {
				commandHandler.execute(new CommandInfo("network list"));
				uuid = params.get("uuid");
			}

			// TODO nice print
			POPNode[] nodes = jmc.networkNodes(uuid);
			for (POPNode node : nodes) {
				System.out.println(node.getConnectorDescriptor().getGlobalName() + " @ " + node);
			}

			return 0;
		}

		@Override
		public String help() {
			return "usage: jm node list\n" +
				description();
		}

		@Override
		public String description() {
			return "list all nodes in a network";
		}
	}
	
}
