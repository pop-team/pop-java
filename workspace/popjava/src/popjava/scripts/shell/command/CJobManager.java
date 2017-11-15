package popjava.scripts.shell.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import popjava.JobManagerConfig;
import popjava.PopJava;
import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.dataswaper.POPString;
import popjava.scripts.POPJShell;
import popjava.scripts.shell.CommandHandler;
import popjava.scripts.shell.CommandInfo;
import popjava.scripts.shell.ICommand;
import popjava.scripts.shell.Parameter;
import popjava.scripts.shell.ParameterInfo;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.external.POPNetworkDetails;
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
			return "usage: jm create [OPTIONS]\n" +
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
			return "usage: jm remove [OPTIONS]\n" +
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
			
			// print network list
			if (!info.canAdvance()) {
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
			
			// network info
			else {
				Parameter params = info.extractParameter(
					new ParameterInfo("uuid", "--uuid", "-u")
				);

				String uuid = params.get("uuid", null);

				if (uuid == null && info.canAdvance()) {
					uuid = info.getParams()[0];
				} else {
					uuid = params.get("uuid");
				}
				
				// TODO nice print
				POPNode[] nodes = jmc.networkNodes(uuid);
				for (POPNode node : nodes) {
					System.out.println(node.getConnectorDescriptor().getGlobalName() + " - " + node.getHost());
				}
				
				return 0;
			}
		}

		@Override
		public String help() {
			return "usage: jm list [OPTIONS]\n" +
				description() +
				"\n" +
				"Available options:\n" +
				"  --uuid, -u          The UUID of the network to remove";
		}

		@Override
		public String description() {
			return "list all current available networks in the jobmanager";
		}
	}
	
}
