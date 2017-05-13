package popjava.service.jobmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import popjava.PopJava;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter.Direction;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.annotation.POPParameter;
import popjava.annotation.POPSyncConc;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.dataswaper.POPMutableFloat;
import popjava.dataswaper.POPString;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.network.NodeJobManager;
import popjava.service.jobmanager.network.POPNetwork;
import popjava.service.jobmanager.network.POPNetworkNode;
import popjava.service.jobmanager.network.POPNetworkNodeFactory;
import popjava.service.jobmanager.connector.POPConnectorBase;
import popjava.service.jobmanager.connector.POPConnectorFactory;
import popjava.service.jobmanager.connector.POPConnectorJobManager;
import popjava.service.jobmanager.search.SNExploration;
import popjava.service.jobmanager.search.SNNodesInfo;
import popjava.service.jobmanager.search.SNRequest;
import popjava.service.jobmanager.search.SNResponse;
import popjava.service.jobmanager.search.SNWayback;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.Util;

@POPClass
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
	protected Map<String,POPNetwork> networks = new HashMap<>();
	
	/** Default network */
	protected POPNetwork defaultNetwork = null;
	
	/** Last network added */
	protected String lastNetwork = null;
	
	/** Max number of jobs */
	protected int maxJobs;
	
	/** Node extra information, value as List if multiple are supplied */
	protected Map<String, List<String>> nodeExtra = new HashMap<>();
	
	/** Mutex for some operations */
	protected ReentrantLock mutex = new ReentrantLock(true);
	
	/** JobManager unique ID */
	protected String nodeId = Util.generateUUID();
	
	/**
	 * Do not call this directly, way too many methods to this so no init was added.
	 */
	public POPJavaJobManager() {
		//init(Configuration.DEFAULT_JM_CONFIG_FILE);
	}
	
	// may also want to use  -XX:MaxHeapFreeRatio=?? -XX:MinHeapFreeRatio=??  if fine tuned
	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url) {
		init(Configuration.DEFAULT_JM_CONFIG_FILE);
	}
	
	@POPObjectDescription(jvmParameters = "-Xmx512m")
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
			LogWriter.writeDebugInfo("[JM] Open config file [" + confFile + "] fail");
			POPException.throwObjectNoResource();
		}
		
		// default num of jobs
		maxJobs = 100;
		
		// set resource by default hoping for the best
		available.add(new Resource(30000, 8192, 1e6f));
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
						String networkName = token[1];
						// not enough elements
						if (token.length < 2) {
							LogWriter.writeDebugInfo(String.format("[JM] Network %s not enough parameters supplied", networkName));
							continue;
						}
						
						// check if exists
						if (networks.containsKey(networkName)) {
							LogWriter.writeDebugInfo(String.format("[JM] Network %s already exists", networkName));
							continue;
						}
						
						// get extra information, if any
						String[] other = new String[ token.length - 2 ];
						System.arraycopy(token, 2, other, 0, token.length - 2);
						
						// create network
						POPNetwork network = new POPNetwork(networkName, this, other);
						
						// put as last added network
						if (!networkName.equals(lastNetwork))
							lastNetwork = networkName;
						
						// set as default network the first network
						// or change it if other tell us it's the default network
						if (defaultNetwork == null || Arrays.asList(other).contains("default")) {
							defaultNetwork = network;
						}
						
						// add to map
						LogWriter.writeDebugInfo(String.format("[JM] Network %s created", networkName));
						networks.put(networkName, network);
						break;
						
						
						
					// handle node in network
					// format: node <network> <params...>
					case "node":
						// not enough elements
						if (token.length < 2) {
							LogWriter.writeDebugInfo(String.format("[JM] Node not enough parameters supplied: %s", line));
							continue;
						}
						
						// params for node, at least one
						other = new String[ token.length - 1 ];
						System.arraycopy(token, 1, other, 0, token.length - 1);
						List<String> params = new ArrayList<>(Arrays.asList(other));
						
						// get specified network or use default
						String networkString = Util.removeStringFromList(params, "network=");
						if (networkString == null)
							networkString = lastNetwork;
						// check again and throw error if no network was previously set
						if (networkString == null)
							throw new RuntimeException(String.format("[JM Config] Setting up `node' before any network. %s", Arrays.toString(token)));
						
						// get network
						network = networks.get(networkString);
						// no network
						if (network == null) {
							LogWriter.writeDebugInfo(String.format("[JM] Node, network %s not found", token[1]));
							continue;
						}
						
						// create the node for the network
						POPNetworkNode node = POPNetworkNodeFactory.makeNode(params);
						// add it to the network
						LogWriter.writeDebugInfo(String.format("[JM] Node [%s] added to %s", node.toString(), networkString));
						network.add(node);
						break;
						
						
						
					// set available resources
					// format: resource <ram|memory|bandwidth> <value>
					case "resource":
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, not enough parameters: %s", line));
							continue;
						}
						
						// type of set <ram|memory|bandwidth>
						switch (token[1]) {
							case "power":
								try {
									available.setFlops(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, power value: %s", token[2]));
								}
								break;
							case "memory":
								try {
									available.setBandwidth(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, memory value: %s", token[2]));
								}
								break;
							case "bandwidth":
								try {
									available.setBandwidth(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, bandwidth value: %s", token[2]));
								}
								break;
							default:
								LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, unknow resource: %s", token[1]));
						}
						break;
						
						
						
					// set jobs limit, resources and number
					// format: job <limit|ram|memory|bandwidth> <value>
					case "job":
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, not enough parameters: %s", line));
							continue;
						}
						// type of set <limit|ram|memory|bandwidth>
						switch (token[1]) {
							case "limit":
								try {
									maxJobs = Integer.parseInt(token[2]);
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, limit value: %s", token[2]));
								}
								break;
							case "power":
								try {
									limit.setFlops(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, ram value: %s", token[2]));
								}
								break;
							case "memory":
								try {
									limit.setMemory(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, memory value: %s", token[2]));
								}
								break;
							case "bandwidth":
								try {
									limit.setBandwidth(Integer.parseInt(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, bandwidth value: %s", token[2]));
								}
								break;
							default:
								LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, unknow resource: %s", token[1]));
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
		try {
			if(howmany <= 0){
				return 0;
			}

			// get network from od
			String networkString = od.getNetwork();
			// use default if not set
			if (networkString.isEmpty())
				networkString = defaultNetwork.getName();
			// get real network
			POPNetwork network = networks.get(networkString);

			// throw exception if no network is found
			if (network == null)
				throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, networkString);

			// get the job manager connector specified in the od
			Class connectorClass = POPConnectorFactory.getConnectorClass(od.getConnector());
			POPConnectorBase connectorImpl = network.getConnector(connectorClass);

			if (connectorImpl == null)
				throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, networkString);

			// call the protocol specific createObject
			return connectorImpl.createObject(localservice, objname, od, howmany, objcontacts, howmany2, remotejobcontacts);
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[JM] Exception caught in createObject: %s", e.getMessage()));
			throw e;
		}
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
		try {
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
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[JM] Exception caught in createObject: %s", e.getMessage()));
			throw e;
		}
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
	public int reserve(@POPParameter(Direction.IN) ObjectDescription od, @POPParameter(Direction.INOUT) POPMutableFloat iofitness, String popAppId, String reqID) {
		//update();
		
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
					LogWriter.writeDebugInfo(String.format("[JM] Require memory %f, at least: %f (available: %f)", require, min, available.getMemory()));
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
			app.setNetwork(od.getNetwork());
			// reservation time
			app.setAccessTime(System.currentTimeMillis());

			// add job
			jobs.put(app.getId(), app);
			// remove available resources
			available.subtract(app);

			LogWriter.writeDebugInfo(String.format("[JM] Reserved [%s] resources", app));
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
		try {
			mutex.lock();
			for (int reqId : req) {
				// remove resource from queue
				resource = jobs.remove(reqId);

				// skip next step if resource is not in job queue
				if (resource == null)
					continue;

				// free JM resource
				available.add(resource);
				LogWriter.writeDebugInfo(String.format("[JM] Free up [%s] resources (cancellation).", resource));
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[JM] Exception caught in cancelReservation: %s", e.getMessage()));
		}
		finally {
			mutex.unlock();
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
			LogWriter.writeDebugInfo("[JM] No writable dump location found");
			return;
		}
		
		// write to file
		try (PrintStream out = new PrintStream(dumpFile)){
			out.println("[networks]");
			networks.forEach((k,net) -> {
				POPConnectorBase[] connectors = net.getConnectors();
				out.println(String.format("[%s]", net.getName()));
				out.println(String.format("connectors=%s", Arrays.toString(connectors)));
				out.println(String.format("members=", net.size()));
				out.println(String.format("[%s.nodes]", net.getName()));
				for (POPConnectorBase node : connectors) {
					out.print(String.format("node=%s", node.toString()));
				};
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
			LogWriter.writeDebugInfo("[JM] IO Error while dumping JobManager");
		}
	}

	/**
	 * Start object and parallel thread check for resources death
	 * TODO: make private
	 * TODO: find another method to call update like a signal from the Broker (should trigger after death)
	 */
	@POPAsyncConc
	@Override
	public void start() {
		while (true) {
			try {
				selfRegister();
				update();
				Thread.sleep(Configuration.UPDATE_MIN_INTERVAL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
				networks.forEach((k,v) -> sb.append(String.format("%s=%s\n", k, v.size())));
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

	private long nextSelfRegister = 0;
	/**
	 * 
	 */
	@POPAsyncConc
	protected void selfRegister() {
		// don't register if too close to last one
		if (System.currentTimeMillis() < nextSelfRegister)
			return;
		
		try {
			// for all members of each network
			Collection<POPNetwork> nets = networks.values();
			for (POPNetwork net : nets) {
				// all connectors in network
				POPConnectorBase[] connectors = net.getConnectors();

				for (POPConnectorBase connector : connectors) {
					// all nodes
					List<POPNetworkNode> nodes = net.getMembers(connector.getClass());	

					for (POPNetworkNode node : nodes) {
						// only contact JM types of nodes
						if (node instanceof NodeJobManager) {
							// connect to remove jm
							NodeJobManager jmn = (NodeJobManager) node;
							registerRemoteAsync(net.getName(), jmn);
						}
					}
				}
			}
		} finally {
			nextSelfRegister = System.currentTimeMillis() + Configuration.SELF_REGISTER_INTERVAL;
		}
	}
	
	/**
	 * Register to remote network and locally
	 * @param network
	 * @param node 
	 */
	@POPAsyncConc
	private void registerRemoteAsync(String network, NodeJobManager node) {
		try {
			POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, node.getJobManagerAccessPoint());
			String[] params = node.toString().split(" ");
			jm.registerNode(network, params);
			jm.exit();
			// TODO confirm local?
		} catch(POPException e) {
			// TODO remove from local?
		}
	}
	 
	/**
	 * Create a new network
	 * @param name A unique name of the network
	 * @param params An array of String that will be processed to {@link POPConnectorFactory#makeProtocol(java.lang.String)}
	 * @return true if created or already exists, false if already exists but use a different protocol
	 */
	@POPSyncConc
	public boolean createNetwork(String name, String... params) {
		try {
			// check if exists already
			POPNetwork network = networks.get(name);
			if (network != null)
				return true;

			// create the new network
			POPNetwork newNetwork = new POPNetwork(name, this, params);

			// TODO write to jobMngr file

			// add new network
			LogWriter.writeDebugInfo(String.format("[JM] Network %s added", name));
			networks.put(name, newNetwork);
			return true;
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[JM] Exception caught in createNetwork: %s", e.getMessage()));
			return false;
		}
	}
	
	/**
	 * Remove a network
	 * @param name The unique name of the network
	 */
	@POPAsyncConc
	public void removeNetwork(String name) {
		if (!networks.containsKey(name)) {
			LogWriter.writeDebugInfo(String.format("[JM] Network %s not removed, not found", name));
			return;
		}
		
		// TODO write to jobMngr file
		
		LogWriter.writeDebugInfo(String.format("[JM] Network %s removed", name));
		networks.remove(name);
	}
	
	
	/**
	 * Register node to a network by supplying an array of string matching the format in the configuration file
	 * @param networkName The name of an existing network in this JM
	 * @param params An array of String that will be processed to {@link POPNetwork#makeNode(java.lang.String[])}
	 */
	@POPSyncConc
	public void registerNode(String networkName, String... params) {
		// get network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo(String.format("[JM] Node %s not registered, network %s not found", Arrays.toString(params), network));
			return;
		}
		
		LogWriter.writeDebugInfo(String.format("[JM] Node %s added to %s", Arrays.toString(params), network));
		network.add(POPNetworkNodeFactory.makeNode(new ArrayList<>(Arrays.asList(params))));
	}
	
	/**
	 * Remove a node from a network
	 * @see #registerNode(java.lang.String, java.lang.String...) 
	 * @param networkName The name of an existing network in this JM
	 * @param params An array of String that will be processed to {@link POPNetwork#makeNode(java.lang.String[])}
	 */
	@POPAsyncConc
	public void unregisterNode(String networkName, String... params) {
		// get network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo(String.format("[JM] Node %s not removed, network not found", Arrays.toString(params)));
			return;
		}
		
		LogWriter.writeDebugInfo(String.format("[JM] Node %s removed", Arrays.toString(params)));
		network.remove(POPNetworkNodeFactory.makeNode(new ArrayList<>(Arrays.asList(params))));
	}

	/**
	 * XXX: Not compatible with Network base JM
	 * @param url 
	 */
	@POPSyncConc
	public void registerNode(String... params) {
		registerNode(defaultNetwork.getName(), params);
	}

	/**
	 * NOTE: not in parent class
	 * XXX: Not compatible with Network base JM
	 * @param url 
	 */
	@POPAsyncConc
	public void unregisterNode(String... params) {
		unregisterNode(defaultNetwork.getName(), params);
	}

	/**
	 * Check and remove finished or timed out resources.
	 * NOTE: With a lot of job this method need jobs.size connections to be made before continuing
	 */
	private long nextUpdate = 0;
	@POPAsyncConc
	public void update() {
		// don't update if too close to last one
		if (System.currentTimeMillis() < nextUpdate)
			return;
		
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
						LogWriter.writeDebugInfo(String.format("[JM] Free up [%s] resources (unused).", job));
					}
				}
				// dead objects check with min UPDATE_MIN_INTERVAL time between them
				else if (job.getAccessTime() < System.currentTimeMillis()) {
					job.setAccessTime(System.currentTimeMillis() + Configuration.UPDATE_MIN_INTERVAL);
					try {
						// connection to object ok
						Interface obj = new Interface(job.getContact());
						obj.close();
					} catch (Exception e) {
						// manually remove from iterator
						available.add(job);
						iterator.remove();
						LogWriter.writeDebugInfo(String.format("[JM] Free up [%s] resources (dead object).", job));
					}
				}
			}
		} finally {
			// set next update
			nextUpdate = System.currentTimeMillis() + Configuration.UPDATE_MIN_INTERVAL;
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
	
	/**
	 * Propagate application end at the end of an application
	 * NOTE May not the necessary or useful, POP-Java already kill unused objects
	 * @param popAppId
	 * @param initiator 
	 */
	@POPAsyncConc
	public void applicationEnd(int popAppId, boolean initiator) {
		AppResource res = jobs.get(popAppId);
		if (initiator && res != null) {
			SNRequest r = new SNRequest(Util.generateUUID(), null, null, res.getNetwork());
			r.setAsEndRequest();
			r.setPOPAppId(popAppId);
			
			launchDiscovery(r, 1);
		}
		
		// remove job and gain resources bask
		Resource r = jobs.remove(popAppId);
		if (r != null)
			available.add(r);
	}
	
	
	////
	// Utility
	////
	private final Semaphore stayAlive = new Semaphore(0);
	/**
	 * Blocking method until death of this object.
	 */
	@POPSyncConc
	public void stayAlive() {
		stayAlive.acquireUninterruptibly();
	}

	/**
	 * Release waiting processes
	 * @throws Throwable 
	 */
	@Override
	protected void finalize() throws Throwable {
		stayAlive.release(Integer.MAX_VALUE);
		super.finalize();
	}
	
	
	/////
	//		Search Node
	////
	
	/** UID of requests to SN  */
	private final LinkedBlockingDeque<String> SNKnownRequests = new LinkedBlockingDeque<>(Configuration.MAXREQTOSAVE);
	private final Map<String, SNNodesInfo> SNActualRequets = new HashMap<>();
	private final Map<String, Semaphore> SNRequestSemaphore = new HashMap<>();
	
	@POPSyncConc
	public SNNodesInfo launchDiscovery(@POPParameter(Direction.IN) SNRequest request, int timeout) {
		try {
			LogWriter.writeDebugInfo("[PSN] starting research");
			
			// add itself to the nodes visited
			request.getExplorationList().add(getAccessPoint());
			
			if (request.isEndRequest()) {
				timeout = 1;
			}
			else {
				LogWriter.writeDebugInfo(String.format("[PSN] LDISCOVERY;TIMEOUT;%d", timeout));
			}
			
			// create and add request to local map
			SNNodesInfo infos = new SNNodesInfo();
			SNActualRequets.put(request.getUID(), infos);
			
			// start reasearch until timeout is reached
			if (timeout > 0) {
				POPAccessPoint sender = new POPAccessPoint();
				askResourcesDiscovery(request, sender);
				Thread.sleep(timeout);
			}
			
			// not timeout was set, accept first result
			else {
				// semaphore to wait until resource is discovered
				Semaphore reqsem = new Semaphore(0);
				// add it to the map for later async unlocking
				SNRequestSemaphore.put(request.getUID(), reqsem);
				
				// start research
				POPAccessPoint sender = new POPAccessPoint();
				askResourcesDiscovery(request, sender);
				
				// start an automatic unlock to avoid an infinite thread waiting
				new Thread(() -> { 
					try {
						Thread.sleep(Configuration.UNLOCK_TIMEOUT);
						unlockDiscovery(request.getUID());
					} catch(InterruptedException e) {}
				}).start();
				
				// wait to semaphore to let us through
				reqsem.acquireUninterruptibly();
				
				// results acquired, remove semaphore from map
				SNRequestSemaphore.remove(request.getUID());
			}
			
			// get (again) the SN infos
			SNNodesInfo results = SNActualRequets.get(request.getUID());
			// remove request from map
			SNActualRequets.remove(request.getUID());
			
			return results;
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[PSN] Exception caught in launchDiscovery: %s", e.getMessage()));
			return new SNNodesInfo();
		}
	}
	
	@POPAsyncSeq
	public void askResourcesDiscovery(@POPParameter(Direction.IN) SNRequest request,
			@POPParameter(Direction.IN) POPAccessPoint sender) {
		try {
			// previous hops visited
			SNExploration explorationList = request.getExplorationList();
			SNExploration oldExplorationList = new SNExploration(request.getExplorationList());
			// get request network
			POPNetwork network = networks.get(request.getNetworkName());

			// do nothing if we don't have the network
			if (network == null)
				return;

			// decrease the hop we can still do
			if (request.getRemainingHops() != Configuration.UNLIMITED_HOPS)
				request.decreaseHopLimit();
			
			// add all network neighbors to explorations list
			for (POPNetworkNode node : network.getMembers(POPConnectorJobManager.class)) {
				// only JM items and children
				if (node instanceof NodeJobManager) {
					NodeJobManager jmNode = (NodeJobManager) node;
					// add to exploration list
					explorationList.add(jmNode.getJobManagerAccessPoint());
				}
			}
			
			// used to kill the application from all JMs
			if (request.isEndRequest()) {
				// check if we can continue discovering
				if (request.getRemainingHops() >= 0 || request.getRemainingHops() == Configuration.UNLIMITED_HOPS) {
					// propagate to all neighbors
					for (POPNetworkNode node : network.getMembers(POPConnectorJobManager.class)) {
						// only JM items and children
						if (node instanceof NodeJobManager) {
							NodeJobManager jmNode = (NodeJobManager) node;

							// contact if it has not been contacted before by someone else
							if (!oldExplorationList.contains(jmNode.getJobManagerAccessPoint())) {
								// send request to other JM
								POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, jmNode.getJobManagerAccessPoint());
								jm.askResourcesDiscovery(request, getAccessPoint());
								jm.exit();
							}
						}
					}
				}
				
				// and application locally
				applicationEnd(request.getPOPAppId(), false);
				return;
			}
			
			// true research start here
			
			// check if request was already parsed once
			// TODO ?? POPC also remove the current node from the sender since it's already reachable.
			//         This may not a good solution in our case with multiple networks.
			if (SNKnownRequests.contains(request.getUID()))
				return;
			// add it if not, at the beginning to find it more easily 
			SNKnownRequests.push(request.getUID());

			// remove older elements
			if (SNKnownRequests.size() > Configuration.MAXREQTOSAVE) {
				SNKnownRequests.pollLast();
			}

			// check local available resources to see if we can handle the request to the requester
			if (available.canHandle(request.getResourceNeeded()) ||
					available.canHandle(request.getMinResourceNeeded())) {
				// build response and give it back to the original sender
				SNNodesInfo.Node nodeinfo = new SNNodesInfo.Node(nodeId, getAccessPoint(), POPSystem.getPlatform(), available);
				SNResponse response = new SNResponse(request.getUID(), request.getExplorationList(), nodeinfo);
				
				// route response to the original JM
				rerouteResponse(response, new SNWayback(request.getWayback()));
			}
			
			// propagate in the network if we still can
			if (request.getRemainingHops() >= 0 || request.getRemainingHops() == Configuration.UNLIMITED_HOPS) {
				// add current node do wayback
				request.getWayback().push(getAccessPoint());
				// request to all members of the network
				for (POPNetworkNode node : network.getMembers(POPConnectorJobManager.class)) {
					if (node instanceof NodeJobManager) {
						NodeJobManager jmNode = (NodeJobManager) node;
						// contact if it's a new node
						if (!oldExplorationList.contains(jmNode.getJobManagerAccessPoint())) {
							// send request to other JM
							POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, jmNode.getJobManagerAccessPoint());
							jm.askResourcesDiscovery(request, getAccessPoint());
							jm.exit();
						}

					}
				}
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[PSN] Exception caught in askResourcesDiscovery: %s", e.getMessage()));
		}
	}
	
	@POPAsyncConc
	public void callbackResult(@POPParameter(Direction.IN) SNResponse response) {
		try {
			// the result node is stored in the SNNodes
			SNNodesInfo.Node result = response.getResultNode();
			SNNodesInfo nodes = SNActualRequets.get(response.getUID());
			if (nodes == null)
				return;
			// add the node
			nodes.add(result);
			
			// we unlock the senaphore if it was set
			unlockDiscovery(response.getUID());
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[PSN] Exception caught in callbackResult: %s", e.getMessage()));
		}
	}
	
	@POPAsyncConc
	public void rerouteResponse(@POPParameter(Direction.IN) SNResponse response, 
			@POPParameter(Direction.IN) SNWayback wayback) {
		try {
			// we want the call in the network to be between neighbors only
			// so we go back on the way we came with the response for the source
			if (!wayback.isLastNode()) {
				LogWriter.writeDebugInfo(String.format("[PSN] REROUTE;%s;DEST;%s", response.getUID(), wayback.toString()));
				// get next node to contact
				POPAccessPoint jm = wayback.pop();
				POPJavaJobManager njm = PopJava.newActive(POPJavaJobManager.class, jm);
				// route request through it
				njm.rerouteResponse(response, wayback);
				njm.exit();
			}
			// is the last node, give the answer to the original JM who launched the request
			else {
				LogWriter.writeDebugInfo(String.format("[PSN] REROUTE_ORIGIN;%s;", response.getUID()));
				callbackResult(response);
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[PSN] Exception caught in rerouteResponse: %s", e.getMessage()));
			e.printStackTrace();
		}
	}
	
	@POPAsyncConc
	public void unlockDiscovery(String requid) {
		// get and remove the semaphore
		Semaphore sem = SNRequestSemaphore.remove(requid);
		// release if the semaphore was set
		if (sem != null) {
			sem.release();
			LogWriter.writeDebugInfo(String.format("[PSN] UNLOCK SEMAPHORE %s", requid));
		}
	}
}
