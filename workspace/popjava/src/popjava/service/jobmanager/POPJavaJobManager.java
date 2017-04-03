package popjava.service.jobmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.network.Network;
import popjava.service.jobmanager.network.NetworkNode;
import popjava.service.jobmanager.protocol.ProtocolFactory;
import popjava.service.jobmanager.search.NodeRequest;
import popjava.service.jobmanager.search.NodeResponse;
import popjava.service.jobmanager.search.NodeWayback;
import popjava.serviceadapter.POPJobManager;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;

@POPClass(useAsyncConstructor = false)
public class POPJavaJobManager extends POPJobService {
	
	/** Currently used resources of a node */
	protected final Resource available = new Resource();
	/** Total resources a job can have */
	protected final Resource limit = new Resource();
	
	
	/** Total number of requests received */
	protected AtomicInteger requestCounter = new AtomicInteger(1 + (int) (Math.random() * Integer.MAX_VALUE));
	
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
	
	/** JobManager unique ID */
	protected String nodeId = UUID.randomUUID().toString();
	
	@POPObjectDescription(url = "localhost:" + POPJobManager.DEFAULT_PORT)
	public POPJavaJobManager() {
		init(Configuration.DEFAULT_JM_CONFIG_FILE);
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
	 * Execute an object locally
	 * @param objname The object to execute
	 * @param howmany How many object will be executed
	 * @param reserveIDs IDs received from {@link #reserve(popjava.baseobject.ObjectDescription, popjava.dataswaper.POPFloat, java.lang.String, java.lang.String)}
	 * @param localservice Application AppService
	 * @param objcontacts Where to answer when the object is created
	 * @return 
	 */
	@POPSyncConc
	public int execObj(@POPParameter(Direction.IN) POPString objname, int howmany, int[] reserveIDs, 
			String localservice, POPAccessPoint[] objcontacts) {
		if (howmany < 0)
			return 1;
		// get verified reservations
		List<AppResource> reservations = verifyReservation(reserveIDs);		
		// cancel reservations if something fail
		if (reservations == null) {
			cancelReservation(reserveIDs, howmany);
			return -1;
		}
		
		boolean status = true;
		try {
			// execute objects locally via Interface
			for (int i = 0; i < howmany; i++) {
				AppResource res = reservations.get(i);
				res.setAppService(new POPAccessPoint(localservice));
				// create request od and add the reservation params
				ObjectDescription od = new ObjectDescription();
				res.addTo(od);
				
				// force od to localhost
				od.setHostname("localhost");
				
				// execute locally, and save status
				status |= Interface.tryLocal(objname.getValue(), objcontacts[i], od);
				// set contact in resource
				res.setContact(objcontacts[i]);
				res.setAccessTime(System.currentTimeMillis());
			}
		} 
		// if any problem occour, cancel reservation
		catch (Throwable e) {
			status = false;
			cancelReservation(reserveIDs, howmany);
		}
		return status ? 0 : -1;
	}
	
	/**
	 * Return the list of AppResource associated with the Resource IDs
	 * @param ids A list of jobs ids
	 * @return The list of resource, null if some resources are invalid
	 */
	private List<AppResource> verifyReservation(int[] ids) {
		List<AppResource> reservations = new ArrayList<>();
		boolean ret = true;
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			// check reservation
			AppResource app = jobs.get(id);
			// no resource in jobs list
			if (app == null)
				return null;
			// resource already in use
			if (app.isUsed())
				return null;
			// no problem with the reservation
			app.setAccessTime(System.currentTimeMillis());
			app.setUsed(true);
			reservations.add(app);
		}
		return reservations;
	}

	/**
	 * NOTE: not in parent class, same signature as POPC w/ POPFloat for float transfer
	 * Add a resource reservation
	 * @param od The request OD
	 * @param iofitness [output] The fitness, compatibility of the request with this node
	 * @param popAppId 
	 * @param reqID
	 * @return the reservation ID for this request used in the other methods
	 */
	@POPSyncConc(id = 16)
	public int reserve(@POPParameter(Direction.IN) ObjectDescription od, @POPParameter(Direction.INOUT) POPFloat iofitness, String popAppId, String reqID) {
		update();
		
		if (jobs.size() >= maxJobs)
			return 0;
		
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

	/**
	 * Dump of JobManager information
	 */
	@POPAsyncSeq
	public void dump() {
		File dumpFile;
		int idx = 0;
		
		// look for new dump file
		do {
			String f = String.format("%s/dump/JobMgr.%d", System.getenv("POPJAVA_LOCATION"), idx++);
			dumpFile = new File(f);
		} while (dumpFile.exists());
		// see if we can write there
		if (!dumpFile.canWrite())
			dumpFile = new File("JobMgr." + idx);
		if (!dumpFile.canWrite()) {
			LogWriter.writeDebugInfo("No writable dump location found");
			return;
		}
		
		// write to file
		try (PrintStream out = new PrintStream(dumpFile)){
			out.println("[networks]");
			networks.forEach((k,net) -> {
				out.println(String.format("[%s]", net.getName()));
				out.println(String.format("protocol=%s", net.getProtocol()));
				out.println(String.format("members=", net.getMembers().size()));
				out.println(String.format("[%s.nodes]", net.getName()));
				net.getMembers().forEach((node) -> {
					out.print(String.format("node=%s", node.toString()));
				});
			});
			out.println("[config]");
			out.println(String.format("power=%f", available.getFlops()));
			out.println(String.format("memory=%f", available.getMemory()));
			out.println(String.format("bandwidth=%f", available.getBandwidth()));
			out.println(String.format("maxjobs=%d", maxJobs));
			out.println("[extra]");
			nodeExtra.forEach((k,v) -> {
				out.println(String.format("%s=%s", k, v));
			});
		} catch (IOException e) {
			LogWriter.writeDebugInfo("IO Error while dumping JobManager");
		}
	}

	/**
	 * Start object and parallel thread check for resources death
	 */
	@Override
	public void start() {
		super.start();
		// TODO start parallel thread (method)
	}
	
	@POPAsyncConc
	protected void checkJobsLiviness() {
		
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

	/**
	 * Check and remove finished or timed out resources.
	 * NOTE: With a lot of job this method need jobs.size connections to be made before continuing
	 */
	private void update() {
		try {
			mutex.lock();
			for (Iterator<AppResource> iterator = jobs.values().iterator(); iterator.hasNext();) {
				AppResource job = iterator.next();
				// job not started after timeout
				if (!job.isUsed()) {
					if (job.getAccessTime() + Configuration.RESERVE_TIMEOUT > System.currentTimeMillis()) {
						// manually remove from iterator
						available.add(job);
						iterator.remove();
					}
				}
				// dead objects check with min UPDATE_MIN_INTERVAL time between them
				else if (job.getAccessTime() + Configuration.UPDATE_MIN_INTERVAL > System.currentTimeMillis()) {
					job.setAccessTime(System.currentTimeMillis() + Configuration.UPDATE_MIN_INTERVAL);
					try {
						// connection to object ok
						new Interface(job.getContact());
					} catch (POPException e) {
						// manually remove from iterator
						available.add(job);
						iterator.remove();
					}
				}
			}
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * A copy of the current available resources
	 * @return A copy Resource object
	 */
	@POPSyncConc
	public Resource getAvailableResources() {
		return new Resource(available);
	}
	
	/**
	 * Unique ID for this node execution
	 * @return the UUID of the node
	 */
	@POPSyncConc
	public String getNodeId() {
		return nodeId;
	}
	
	
	
	
	/////
	//		Search Node Methods
	////
	
	public List<POPAccessPoint> launchDiscovery(@POPParameter(Direction.INOUT) NodeRequest request, int timeout) {
		return new ArrayList<POPAccessPoint>() {{ 
			add(new POPAccessPoint("socket://127.0.0.1:2712"));
		}};
	}
	
	public void askResourcesDiscovery(@POPParameter(Direction.INOUT) NodeRequest request, 
			@POPParameter(Direction.IN) POPAccessPoint jobManager, @POPParameter(Direction.IN) POPAccessPoint sender) {
		
	}
	
	public void callbackResult(@POPParameter(Direction.IN) NodeResponse response) {
		
	}
	
	public void rerouteResponse(@POPParameter(Direction.IN) NodeResponse response, 
			@POPParameter(Direction.IN) NodeWayback wayback) {
		
	}
}
