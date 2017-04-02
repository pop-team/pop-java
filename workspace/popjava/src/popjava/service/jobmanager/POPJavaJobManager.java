package popjava.service.jobmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPParameter.Direction;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPSyncConc;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.dataswaper.POPFloat;
import popjava.dataswaper.POPString;
import popjava.service.jobmanager.network.Network;
import popjava.service.jobmanager.network.NetworkNode;
import popjava.service.jobmanager.protocol.ProtocolFactory;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;

@POPClass
public class POPJavaJobManager extends POPJobService {
	
	/** Currently used resources of a node */
	protected final Resource available = new Resource();
	/** Total resources a job can have */
	protected final Resource limit = new Resource();
	
	
	/** Total number of requests received */
	protected AtomicInteger requestCounter;
	
	/** Number of job alive, mapped by {@link AppResource#id} */
	protected Map<Integer,AppResource> jobs = new HashMap<>();
	
	/** Networks saved in this JobManager */
	protected Map<String,Network> networks = new HashMap<>();
	
	/** Max number of jobs */
	protected int maxJobs;
	
	/** Node extra information, value as List if multiple are supplied */
	protected Map<String, List<String>> nodeExtra = new HashMap<>();
	
	/** Mutex for some operations */
	protected ReentrantLock mutex = new ReentrantLock(true);

	@POPObjectDescription(url = "localhost:" + POPJobManager.DEFAULT_PORT)
	public POPJavaJobManager() {
		//init(Configuration.DEFAULT_JM_CONFIG_FILE);
	}
	
	public POPJavaJobManager(@POPConfig(Type.URL) String url) {
		init(Configuration.DEFAULT_JM_CONFIG_FILE);
	}
	
	public POPJavaJobManager(@POPConfig(Type.URL) String url, String conf) {
		init(conf);
	}
	
	/**
	 * Read configuration file and setup system
	 * Has some sane defaults
	 * @param confFile Path to configuration file
	 */
	private void init(String confFile) {
		File config = new File(confFile);
		
		// early exit
		if (!config.exists()) {
			LogWriter.writeDebugInfo("Open config file [" + confFile + "] fail");
			POPException.throwObjectNoResource();
		}
		
		// default num of jobs
		maxJobs = 100;
		
		// set resource by default hoping for the best
		available.add(new Resource(300, 256, 100));
		// no restrictions on default limit
		limit.add(available);
		
		// config file is read line by line, information is extracted as see fit
		try (BufferedReader br = new BufferedReader(new FileReader(config))) {
			String line;
			String[] token;
			while ((line = br.readLine()) != null) {
				// split line for reading
				token = line.trim().split("\\s+");
				
				// skip only key or empty lines
				if (token.length < 2 )
					continue;
				// skip commented lines
				if (token[0].startsWith("#"))
					continue;
				
				// handle first token
				switch (token[0]) {
					// create network
					// format: network <name> <protocol> [params...]
					case "network": 
						// not enough elements
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("Network %s not enough parameters supplied", token[1]));
							continue;
						}
						
						// check if exists
						if (networks.containsKey(token[1])) {
							LogWriter.writeDebugInfo(String.format("Network %s already exists", token[1]));
							continue;
						}
						
						// get extra information
						String[] other = new String[ token.length - 3 ];
						System.arraycopy(token, 3, other, 0, token.length - 3);
						
						// create network
						Network network = new Network(token[1], token[2], this, other);
						// add to map
						networks.put(token[1], network);
						break;
						
						
						
					// handle node in network
					// format: node <network> <params...>
					case "node":
						// not enough elements
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("Node not enough parameters supplied: %s", line));
							continue;
						}
						
						// get network
						network = networks.get(token[1]);
						// no network
						if (network == null) {
							LogWriter.writeDebugInfo(String.format("Node, network %s not found", token[1]));
							continue;
						}
						
						// params for node, at least one
						other = new String[ token.length - 2 ];
						System.arraycopy(token, 2, other, 0, token.length - 2);
						
						// create the node for the network
						NetworkNode node = network.makeNode(other);
						// add it to the network
						network.add(node);
						break;
						
						
						
					// set available resources
					// format: resource <ram|memory|bandwidth> <value>
					case "resource":
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("Resource set fail, not enough parameters: %s", line));
							continue;
						}
						
						// type of set <ram|memory|bandwidth>
						switch (token[1]) {
							case "power":
								try {
									available.setFlops(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("Resource set fail, power value: %s", token[2]));
								}
								break;
							case "memory":
								try {
									available.setBandwidth(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("Resource set fail, memory value: %s", token[2]));
								}
								break;
							case "bandwidth":
								try {
									available.setBandwidth(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("Resource set fail, bandwidth value: %s", token[2]));
								}
								break;
							default:
								LogWriter.writeDebugInfo(String.format("Resource set fail, unknow resource: %s", token[1]));
						}
						break;
						
						
						
					// set jobs limit, resources and number
					// format: job <limit|ram|memory|bandwidth> <value>
					case "job":
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("Limit set fail, not enough parameters: %s", line));
							continue;
						}
						// type of set <limit|ram|memory|bandwidth>
						switch (token[1]) {
							case "limit":
								try {
									maxJobs = Integer.parseInt(token[2]);
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("Limit set fail, limit value: %s", token[2]));
								}
								break;
							case "ram":
								try {
									limit.setMemory(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("Limit set fail, ram value: %s", token[2]));
								}
								break;
							case "memory":
								try {
									limit.setBandwidth(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("Limit set fail, memory value: %s", token[2]));
								}
								break;
							case "bandwidth":
								try {
									limit.setBandwidth(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("Limit set fail, bandwidth value: %s", token[2]));
								}
								break;
							default:
								LogWriter.writeDebugInfo(String.format("Limit set fail, unknow resource: %s", token[1]));
						}
						break;
						
						
					// any other value is store in a map for possible future use
					default:
						// get or create and add to map
						List<String> val = nodeExtra.get(token[0]);
						if (val == null) {
							val = new ArrayList<>();
							nodeExtra.put(token[0], val);
						}
						// add extra value
						val.add(line.substring(token[0].length()));
						break;
				}
			}
		} catch (IOException e) {
			throw new POPException(0, e.getMessage());
		}
	}
	
	@Override
	public int createObject(POPAccessPoint localservice,
			String objname,
			@POPParameter(Direction.IN) ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts,
			int howmany2, POPAccessPoint[] remotejobcontacts) {		
		if(howmany <= 0){
			return 0;
		}
		
		// get network from od
		String networkString = od.getNetwork();
		// get real network
		Network network = networks.get(networkString);
		
		// throw exception if no network is found
		if (network == null)
			throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, networkString);
		
		// call the protocol specific createObject
		return network.getProtocol().createObject(localservice, objname, od, howmany, objcontacts, howmany2, remotejobcontacts);
	}

	/**
	 * 
	 * @param objname
	 * @param howmany
	 * @param reserveIDs
	 * @param localservice
	 * @param objcontacts
	 * @return 
	 */
	@POPSyncConc
	public int execObj(POPString objname, int howmany, int[] reserveID, String localservice, POPAccessPoint[] objcontacts) {
		// check reservation
		
		
		return 0;
	}

	/**
	 * NOTE: not in parent class, same signature as POPC w/ POPFloat for float transfer
	 * Add a resource reservation
	 * @param od The request OD
	 * @param iofitness 
	 * @param popAppId
	 * @param reqID
	 * @return the reservation ID for this request used in the other methods
	 */
	@POPSyncConc(id = 16)
	public int reserve(@POPParameter(Direction.IN) ObjectDescription od, @POPParameter(Direction.OUT) POPFloat iofitness, String popAppId, String reqID) {
		update();
		
		Resource weighted = new Resource();
		float fitness = 1f;
		float flops = 0;
		//float walltime = 0;
		float mem = 0;
		float bandwidth = 0;

		try {
			mutex.lock();
		
			// if we have an od
			if (!od.isEmpty()) {
				float require, min;

				// power
				require = od.getPowerReq();
				min = od.getPowerMin();
				if (require > 0) {
					if (min < 0) {
						min = require;
					}

					if (require > available.getFlops() || require > limit.getFlops()) {
						flops = min(available.getFlops(), limit.getFlops());
						fitness = flops / require;
					} else {
						flops = require;
						fitness = min(available.getFlops(), limit.getFlops()) / require;
					}
					if (fitness < iofitness.getValue()) {
						return 0;
					}
				}

				// memory
				require = od.getMemoryReq();
				min = od.getMemoryMin();
				if (require > 0) {
					LogWriter.writeDebugInfo(String.format("Require memory %f, at least: %f (available: %f)", require, min, available.getMemory()));
					if (min < 0) {
						min = require;
					}
					if (min > available.getMemory()) {
						LogWriter.writeDebugInfo("[JM] Local Match Failed (reason: memory)");
						return 0;
					}
					float fitness1;
					if (require > available.getMemory()) {
						mem = available.getMemory();
						fitness1 = mem / require;
					} else {
						mem = require;
						fitness1 = mem / available.getMemory();
					}
					if (fitness1 < fitness) {
						if (fitness1 < iofitness.getValue()) {
							return 0;
						}
						fitness = fitness1;
					}
				}

				
				// TODO? walltime; 
				// TODO bandwidth; not in POPC
				
				weighted = new Resource(flops, mem, bandwidth);
			}

			// output fitness
			iofitness.setValue(fitness);

			// new app resource
			AppResource app = new AppResource();
			app.setId(requestCounter.incrementAndGet());
			app.add(weighted);
			// TODO walltime?
			app.setAppId(popAppId);
			app.setReqId(reqID);
			// reservation time
			app.setAccessTime(System.currentTimeMillis());

			// add job
			jobs.put(app.getId(), app);
			// remove available resources
			available.subtract(app);


			return app.getId();
		} finally {
			mutex.unlock();
		}
	}
	
	/**
	 * NOTE: same as POPC signature
	 * @param req An array of {@link AppResource#id}
	 * @param howmany Redundant for Java, number of element in array
	 */
	@POPAsyncSeq
	public void cancelReservation(@POPParameter(Direction.IN) int[] req, int howmany) {
		// remove from job map and free resources
		AppResource resource;
		for (int reqId : req) {
			// remove resource from queue
			resource = jobs.remove(reqId);
			
			// skip next step if resource is not in job queue
			if (resource == null)
				continue;
			
			// free JM resource
			available.add(resource);
		}
	}

	@POPAsyncSeq
	public void dump() {
		
	}

	@Override
	public void start() {
		super.start();
	}

	/**
	 * Query something and return a formatted string
	 * @param type What to query
	 * @param value A formatted String with some data in it
	 * @return true if the query is successful, false otherwise
	 */
	public boolean query(String type, @POPParameter(Direction.INOUT) POPString value) {
		switch (type) {
			case "platform":
				value.setValue(POPSystem.getPlatform());
				return true;
			case "host":
				value.setValue(POPSystem.getHostIP());
				return true;
			case "jobs":
				update(); 
				value.setValue(String.format("%d/%d", jobs.size(), maxJobs));
				return true;
			case "joblist":
				update();
				// clone for report
				Set<AppResource> apps = new HashSet<>(jobs.values());
				StringBuilder sb = new StringBuilder();
				for (AppResource app : apps) {
					if (app.getContact().isEmpty() || app.getAppService().isEmpty())
						continue;
					sb.append(String.format("APP=%s/JOB=%s\n", app.getAppService().toString(), app.getContact().toString()));
				}
				value.setValue(sb.toString());
				return true;
			case "pausejobs":
				// XXX: Not implemented
				return false;
			case "neighbors":
				// XXX: Not implemented
				return false;
			case "networks":
				sb = new StringBuilder();
				networks.forEach((k,v) -> sb.append(String.format("%s=%s\n", k, v.getProtocol().getClass())));
				value.setValue(sb.toString().trim());
				return true;
			case "power_available":
				update();
				value.setValue(String.valueOf(available.getFlops()));
				return true;
		}
		
		// other cases
		List<String> vals = nodeExtra.get(type);
		if (vals == null)
			return false;
		// we can have a list of it
		StringBuilder sb = new StringBuilder();
		vals.forEach((s) -> sb.append(s + "\n"));
		value.setValue(sb.toString().trim());
		return true;
	}

	@POPAsyncSeq
	public void selfRegister() {
		
	}
	
	/**
	 * Create a new network
	 * @param name A unique name of the network
	 * @param protocol The protocol to use
	 * @param params An array of String that will be processed to {@link ProtocolFactory#makeProtocol(java.lang.String)}
	 * @return true if created or already exists, false if already exists but use a different protocol
	 */
	@POPSyncConc
	public boolean createNetwork(String name, String protocol, String... params) {
		// check if exists already
		Network network = networks.get(name);
		// create the new network; NOTE with do this here to avoid creating a protocol just for comparison
		Network newNetwork = new Network(name, protocol, this, params);
		
		// check existence
		if (network != null) {
			// also check if it's the same protocol
			return network.getProtocol().getClass() == newNetwork.getProtocol().getClass();
		}
		
		// add new network
		LogWriter.writeDebugInfo(String.format("Network %s added", name));
		networks.put(name, newNetwork);
		return true;
	}
	
	/**
	 * Remove a network
	 * @param name The unique name of the network
	 */
	@POPAsyncConc
	public void removeNetwork(String name) {
		if (!networks.containsKey(name)) {
			LogWriter.writeDebugInfo(String.format("Network %s not removed, not found", name));
			return;
		}
		LogWriter.writeDebugInfo(String.format("Network %s removed", name));
		networks.remove(name);
	}
	
	
	/**
	 * Register node to a network by supplying an array of string matching the format in the configuration file
	 * @param networkName The name of an existing network in this JM
	 * @param params An array of String that will be processed to {@link Network#makeNode(java.lang.String...)}
	 */
	@POPSyncConc
	public void registerNode(String networkName, String... params) {
		// get network
		Network network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo(String.format("Node %s not registered, network not found", Arrays.toString(params)));
			return;
		}
		
		LogWriter.writeDebugInfo(String.format("Node %s added", Arrays.toString(params)));
		network.add(network.makeNode(params));
	}
	
	/**
	 * Remove a node from a network
	 * @see #registerNode(java.lang.String, java.lang.String...) 
	 * @param networkName The name of an existing network in this JM
	 * @param params An array of String that will be processed to {@link Network#makeNode(java.lang.String...)}
	 */
	@POPAsyncConc
	public void unregisterNode(String networkName, String... params) {
		// get network
		Network network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo(String.format("Node %s not removed, network not found", Arrays.toString(params)));
			return;
		}
		
		LogWriter.writeDebugInfo(String.format("Node %s removed", Arrays.toString(params)));
		network.remove(network.makeNode(params));
	}

	/**
	 * XXX: Not compatible with Network base JM
	 * @param url 
	 */
	@POPSyncConc
	public void registerNode(String url) {
		// TODO default network?
	}

	/**
	 * NOTE: not in parent class
	 * XXX: Not compatible with Network base JM
	 * @param url 
	 */
	@POPAsyncConc
	public void unregisterNode(@POPParameter(Direction.IN) POPAccessPoint url) {
		// TODO remove from default 
	}

	private void update() {
	}
}
