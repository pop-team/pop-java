package popjava.service.jobmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import popjava.annotation.POPSyncSeq;
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
import popjava.codemanager.AppService;
import popjava.dataswaper.POPMutableFloat;
import popjava.dataswaper.POPString;
import popjava.util.ssl.SSLUtils;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.network.NodeJobManager;
import popjava.service.jobmanager.network.POPNetwork;
import popjava.service.jobmanager.network.POPNetworkNode;
import popjava.service.jobmanager.network.POPNetworkNodeFactory;
import popjava.service.jobmanager.connector.POPConnectorBase;
import popjava.service.jobmanager.connector.POPConnectorFactory;
import popjava.service.jobmanager.connector.POPConnectorSearchNodeInterface;
import popjava.service.jobmanager.connector.POPConnectorTFC;
import popjava.service.jobmanager.network.AbstractNodeJobManager;
import popjava.service.jobmanager.search.SNExploration;
import popjava.service.jobmanager.search.SNNodesInfo;
import popjava.service.jobmanager.search.SNRequest;
import popjava.service.jobmanager.search.SNResponse;
import popjava.service.jobmanager.search.SNWayback;
import popjava.service.jobmanager.tfc.TFCResource;
import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.Util;
import popjava.util.SystemUtil;

@POPClass
public class POPJavaJobManager extends POPJobService {
	
	private final Configuration conf = Configuration.getInstance();
	
	/** The configuration file location */
	protected String configurationFile;

	/** Currently used resources of a node */
	protected final Resource available = new Resource();
	/** Max available resources of the node */
	protected final Resource total = new Resource();
	/** Total resources a job can have */
	protected final Resource jobLimit = new Resource();

	
	/** Total number of requests received */
	protected AtomicInteger requestCounter = new AtomicInteger(1 + (int) (Math.random() * Integer.MAX_VALUE));

	/** Number of job alive, mapped by {@link AppResource#id} */
	protected Map<Integer,AppResource> jobs = new HashMap<>();
	
	/** Jobs we need to cleanup */
	private final LinkedBlockingDeque<AppResource> cleanupJobs = new LinkedBlockingDeque<>();

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

	/** When to perform the next update of the job list */
	private long nextUpdate = 0;
	/** Self register timer */
	private long nextSelfRegister = 0;
	
	/** Semaphore used to cleanly avoid premature exit of the Job Manager */
	private final Semaphore stayAlive = new Semaphore(0);

	/**
	 * Do not call this directly, way too many methods to this so no init was added.
	 */
	@POPObjectDescription(url = "localhost", jvmParameters = "-Xmx512m")
	public POPJavaJobManager() {
		//init(conf.DEFAULT_JM_CONFIG_FILE);
		configurationFile = conf.getSystemJobManagerConfig().toString();
	}

	// may also want to use  -XX:MaxHeapFreeRatio=?? -XX:MinHeapFreeRatio=??  if fine tuned
	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url) {
		configurationFile = conf.getSystemJobManagerConfig().toString();
		init(conf.getSystemJobManagerConfig().toString());
	}
	
	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url, @POPConfig(Type.PROTOCOLS) String[] protocols) {
		configurationFile = conf.getSystemJobManagerConfig().toString();
		init(conf.getSystemJobManagerConfig().toString());
	}
	
	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url, @POPConfig(Type.PROTOCOLS) String[] protocols, String conf) {
		configurationFile = conf;
		init(conf);
	}

	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url, String conf) {
		configurationFile = conf;
		init(conf);
	}

	/**
	 * Read configuration file and setup system Has some sane defaults
	 *
	 * @param confFile Path to configuration file
	 */
	private void init(String confFile) {
		File config = new File(confFile);

		// early exit
		if (!config.exists()) {
			LogWriter.writeDebugInfo("[JM] Open config file [%s] fail, trying to create", confFile);
			
			try {
				config.createNewFile();
			} catch (IOException e) {
				LogWriter.writeDebugInfo("[JM] can't create job manager file %s", confFile);
			}
		}
		LogWriter.writeDebugInfo("[JM] Using %s as config file", config.getAbsoluteFile().toString());

		// default num of jobs
		maxJobs = 100;

		// set resource by default hoping for the best
		available.add(new Resource(30000, 8192, 1e6f));
		// no restrictions on default limit
		jobLimit.add(available);
		// total is the same as available for now
		total.add(available);
		
		// TODO run benchmark power (maybe memory and bandwidth if needed)

		// config file is read line by line, information is extracted as see fit
		try (BufferedReader br = new BufferedReader(new FileReader(config))) {
			String line;
			String[] token;
			while ((line = br.readLine()) != null) {
				// split line for reading
				token = line.trim().split("\\s+");

				// skip only key or empty lines
				if (token.length < 2) {
					continue;
				}
				// skip commented lines
				if (token[0].startsWith("#")) {
					continue;
				}

				// handle first token
				switch (token[0]) {
					// create network
					// format: network <name> [default] [...]
					case "network":
						String networkName = token[1];
						// not enough elements, [0] = network, [1] = name, [2] = default
						if (token.length < 2) {
							LogWriter.writeDebugInfo(String.format("[JM] Network %s not enough parameters supplied", networkName));
							continue;
						}

						// check if exists
						if (networks.containsKey(networkName)) {
							LogWriter.writeDebugInfo(String.format("[JM] Network %s already exists", networkName));
							continue;
						}

						String[] other = Arrays.copyOfRange(token, 2, token.length);

						// create network
						POPNetwork network = new POPNetwork(networkName, this);

						// set as last added network, this is used when defining nodes
						if (!networkName.equals(lastNetwork)) {
							lastNetwork = networkName;
						}

						// set as default network the first network
						// or change it if other tell us they are the default network
						if (defaultNetwork == null || Arrays.asList(other).contains("default")) {
							defaultNetwork = network;
						}

						// add to map
						LogWriter.writeDebugInfo(String.format("[JM] Network %s created", networkName));
						networks.put(networkName, network);
						break;

					// handle node in network
					// format: node host=<host> <params...>
					case "node":
						// not enough elements
						if (token.length < 2) {
							LogWriter.writeDebugInfo(String.format("[JM] Node not enough parameters supplied: %s", line));
							continue;
						}

						// params for node, at least one
						other = Arrays.copyOfRange(token, 1, token.length);
						// asList only wrap the array making it unmodifiable, we work on the list 
						List<String> params = new ArrayList<>(Arrays.asList(other));

						// get specified network or use default
						String networkString = Util.removeStringFromList(params, "network=");
						if (networkString == null) {
							networkString = lastNetwork;
						}
						// check again and throw error if no network was previously set
						if (networkString == null) {
							throw new RuntimeException(String.format("[JM Config] Setting up `node' before any network. %s", Arrays.toString(token)));
						}

						// get network from know networks
						network = networks.get(networkString);
						// if no network exists, abort
						if (network == null) {
							LogWriter.writeDebugInfo(String.format("[JM] Node, network %s not found for node %s", token[1], Arrays.toString(token)));
							continue;
						}

						// create the node for the network
						POPNetworkNode node = POPNetworkNodeFactory.makeNode(params);
						// add it to the network
						LogWriter.writeDebugInfo(String.format("[JM] Node [%s] added to %s", node.toString(), networkString));
						network.add(node);
						break;

					// set available resources
					// format: resource <power|memory|bandwidth> <value>
					case "resource":
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, not enough parameters: %s", line));
							continue;
						}

						// type of set <power|memory|bandwidth>
						switch (token[1]) {
							case "power":
								try {
									available.setFlops(Float.parseFloat(token[2]));
									total.setFlops(available.getFlops());
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, power value: %s", token[2]));
								}
								break;
							case "memory":
								try {
									available.setMemory(Float.parseFloat(token[2]));
									total.setMemory(available.getMemory());
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, memory value: %s", token[2]));
								}
								break;
							case "bandwidth":
								try {
									available.setBandwidth(Float.parseFloat(token[2]));
									total.setBandwidth(available.getBandwidth());
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, bandwidth value: %s", token[2]));
								}
								break;
							default:
								LogWriter.writeDebugInfo(String.format("[JM] Resource set fail, unknow resource: %s", token[1]));
						}
						break;

					// set jobs limit, resources and number
					// format: job <limit|power|memory|bandwidth> <value>
					case "job":
						if (token.length < 3) {
							LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, not enough parameters: %s", line));
							continue;
						}
						// type of set <limit|power|memory|bandwidth>
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
									jobLimit.setFlops(Float.parseFloat(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, ram value: %s", token[2]));
								}
								break;
							case "memory":
								try {
									jobLimit.setMemory(Float.parseFloat(token[2]));
								} catch (NumberFormatException e) {
									LogWriter.writeDebugInfo(String.format("[JM] Limit set fail, memory value: %s", token[2]));
								}
								break;
							case "bandwidth":
								try {
									jobLimit.setBandwidth(Float.parseFloat(token[2]));
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

	/**
	 * Local request to create an object 
	 * @param localservice The AppService of the application
	 * @param objname Which object we have to create
	 * @param od The OD of the request
	 * @param howmany The size of objcontacts
	 * @param objcontacts How many instances we seek
	 * @param howmany2 
	 * @param remotejobcontacts
	 * @return 
	 */
	@POPSyncConc(id = 12)
	@Override
	public int createObject(POPAccessPoint localservice,
			String objname,
			@POPParameter(Direction.IN) ObjectDescription od,
			int howmany, POPAccessPoint[] objcontacts,
			int howmany2, POPAccessPoint[] remotejobcontacts) {
		try {
			if (howmany <= 0) {
				return 0;
			}

			// get network from od
			String networkString = od.getNetwork();
			// use default if not set
			if (networkString.isEmpty()) {
				networkString = defaultNetwork.getName();
			}
			// get real network
			POPNetwork network = networks.get(networkString);

			// throw exception if no network is found
			if (network == null) {
				throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, networkString);
			}

			// get the job manager connector specified in the od
			Class connectorClass = POPConnectorFactory.getConnectorClass(od.getConnector());
			POPConnectorBase connectorImpl = network.getConnector(connectorClass);
			
			if (connectorImpl == null) {
				throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, networkString);
			}

			// call the protocol specific createObject
			return connectorImpl.createObject(localservice, objname, od, howmany, objcontacts, howmany2, remotejobcontacts);
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[JM] Exception caught in createObject: %s", e.getMessage()));
			throw e;
		}
	}

	/**
	 * Execute an object locally
	 *
	 * @param objname The object to execute
	 * @param howmany How many object will be executed
	 * @param reserveIDs IDs received from {@link #reserve}
	 * @param localservice Application AppService
	 * @param objcontacts Where to answer when the object is created
	 * @return
	 */
	@POPSyncConc
	public int execObj(@POPParameter(Direction.IN) POPString objname, int howmany, int[] reserveIDs,
			String localservice, POPAccessPoint[] objcontacts) {
		try {
			if (howmany < 0) {
				return 1;
			}
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
					od.merge(res.getOd());
					res.addTo(od);

					// force od to localhost
					od.setHostname("localhost");
					// set user to use locally
					String hostuser = conf.getJobmanagerExecutionUser();
					od.setHostuser(hostuser);

					// code file
					POPString codeFile = new POPString();
					String appId = "unknown";
					AppService service;
					try {
						service = PopJava.newActive(POPJavaAppService.class, res.getAppService());
						service.queryCode(objname.getValue(), POPSystem.getPlatform(), codeFile);
						appId = service.getPOPCAppID();
						service.exit();
					} catch (POPException e) {
						service = PopJava.newActive(POPAppService.class, res.getAppService());
						service.queryCode(objname.getValue(), POPSystem.getPlatform(), codeFile);
						appId = service.getPOPCAppID();
						service.exit();
					}
					
					// create directory if it doesn't exists and set OD.cwd
					Path objectAppCwd = Paths.get(conf.getJobManagerExecutionBaseDirectory(), appId).toAbsolutePath();
					// XXX Find a working solution with Files.setOwner(..)?
					String[] cmd = { "mkdir", objectAppCwd.toAbsolutePath().toString() };
					SystemUtil.runCmd(Arrays.asList(cmd), null, hostuser);
					
					od.setDirectory(objectAppCwd.toString());
					res.setAppDirectory(objectAppCwd);
					
					// modify popjava location with local ones
					String[] args = codeFile.getValue().split(" ");
					String jarLocation = POPJavaConfiguration.getPopJavaJar();
					for (int j = 0; j < args.length; j++) {
						if (args[j].startsWith("-javaagent:")) {
							args[j] = "-javaagent:" + jarLocation;
						} else if (args[j].equals("-cp")) {
							args[j + 1] = jarLocation;
						}
					}
					
					od.setCodeFile(Util.join(" ", args));
					od.setOriginAppService(res.getAppService());

					// execute locally, and save status
					status |= Interface.tryLocal(objname.getValue(), objcontacts[i], od);
					// set contact in resource
					res.setContact(objcontacts[i]);
					res.setAccessTime(System.currentTimeMillis());
				}
			} // if any problem occurs, cancel reservation
			catch (Throwable e) {
				LogWriter.writeExceptionLog(e);
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
	 *
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
			if (app == null) {
				return null;
			}
			// resource already in use
			if (app.isUsed()) {
				return null;
			}
			// no problem with the reservation
			app.setAccessTime(System.currentTimeMillis());
			app.setUsed(true);
			reservations.add(app);
		}
		return reservations;
	}

	/**
	 * NOTE: not in parent class, same signature as POPC w/ POPFloat for float transfer Add a resource reservation
	 * Reserve a resource and return a corresponding ID to send in {@link #execObj}
	 *
	 * @param od The request OD
	 * @param iofitness [output] The fitness, compatibility of the request with this node
	 * @param popAppId
	 * @param reqID
	 * @return the reservation ID for this request used in the other methods
	 */
	@POPSyncConc(id = 16)
	public int reserve(@POPParameter(Direction.IN) ObjectDescription od, @POPParameter(Direction.INOUT) POPMutableFloat iofitness, String popAppId, String reqID) {
		//update();

		if (jobs.size() >= maxJobs) {
			return 0;
		}

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

					if (require > available.getFlops() || require > jobLimit.getFlops()) {
						flops = Math.min(available.getFlops(), jobLimit.getFlops());
						fitness = flops / require;
					} else {
						flops = require;
						fitness = Math.min(available.getFlops(), jobLimit.getFlops()) / require;
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
			app.setOd(od);
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
	 *
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
				if (resource == null) {
					continue;
				}

				// free JM resource
				available.add(resource);
				LogWriter.writeDebugInfo(String.format("[JM] Free up [%s] resources (cancellation).", resource));
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo(String.format("[JM] Exception caught in cancelReservation: %s", e.getMessage()));
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * Dump of JobManager information
	 */
	@POPAsyncSeq(localhost = true)
	public void dump() {
		File dumpFile;
		int idx = 0;

		// look for new dump file
		do {
			String location = System.getenv("POPJAVA_LOCATION");
			if (location == null) {
				location = ".";
			}
			
			String f = String.format("%s/dump/JobMgr.%d.dump", location, idx++);
			dumpFile = new File(f);
		} while (dumpFile.exists());
		
		// create file
		try {
			dumpFile.createNewFile();
		} catch (IOException e) {}
		
		if (!dumpFile.canWrite()) {
			LogWriter.writeDebugInfo("[JM] No writable dump location found");
			return;
		}

		// write to file
		try (PrintStream out = new PrintStream(dumpFile)) {
			out.println("[networks]");
			for (Map.Entry<String, POPNetwork> entry : networks.entrySet()) {
				String k = entry.getKey();
				POPNetwork net = entry.getValue();
				POPConnectorBase[] connectors = net.getConnectors();
				out.println(String.format("[%s]", net.getName()));
				out.println(String.format("connectors=%s", Arrays.toString(connectors)));
				out.println(String.format("members=%d", net.size()));
				for (POPConnectorBase connector : connectors) {
					out.println(String.format("[%s.%s]", net.getName(), connector.getClass().getCanonicalName()));
					for (POPNetworkNode node : net.getMembers(connector.getClass())) {
						out.println(String.format("node=%s", node.toString()));
					}
				}
			}
			out.println("[config]");
			out.println(String.format("available power=%f", available.getFlops()));
			out.println(String.format("available memory=%f", available.getMemory()));
			out.println(String.format("available bandwidth=%f", available.getBandwidth()));
			out.println(String.format("total power=%f", total.getFlops()));
			out.println(String.format("total memory=%f", total.getMemory()));
			out.println(String.format("total bandwidth=%f", total.getBandwidth()));
			out.println(String.format("job limit power=%f", jobLimit.getFlops()));
			out.println(String.format("job limit memory=%f", jobLimit.getMemory()));
			out.println(String.format("job limit bandwidth=%f", jobLimit.getBandwidth()));
			out.println(String.format("max jobs=%d", maxJobs));
			out.println("[extra]");
			for (Map.Entry<String, List<String>> entry1 : nodeExtra.entrySet()) {
				String k = entry1.getKey();
				List<String> v = entry1.getValue();
				out.println(String.format("%s=%s", k, v));
			}
		} catch (IOException e) {
			LogWriter.writeDebugInfo("[JM] IO Error while dumping JobManager");
		}
	}

	/**
	 * Start object and parallel thread check for resources death and other timed tasks.
	 */
	@POPAsyncConc(localhost = true)
	@Override
	public void start() {
		while (true) {
			try {
				selfRegister();
				update();
				cleanup();
				Thread.sleep(conf.getJobManagerUpdateInterval());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Query something and return a formatted string
	 *
	 * @param type What to query
	 * @param value A formatted String with some data in it
	 * @return true if the query is successful, false otherwise
	 */
	@POPSyncConc
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
					if (app.getContact().isEmpty() || app.getAppService().isEmpty()) {
						continue;
					}
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
				for (Map.Entry<String, POPNetwork> entry : networks.entrySet()) {
					String k = entry.getKey();
					POPNetwork v = entry.getValue();
					sb.append(String.format("%s=%s\n", k, v.size()));
				}
				value.setValue(sb.toString().trim());
				return true;
			case "power_available":
				update();
				value.setValue(String.valueOf(available.getFlops()));
				return true;
		}

		// other cases
		List<String> vals = nodeExtra.get(type);
		if (vals == null) {
			return false;
		}
		// we can have a list of it
		StringBuilder sb = new StringBuilder();
		for (String s : vals) {
			sb.append(s).append("\n");
		}
		value.setValue(sb.toString().trim());
		return true;
	}
	
	@POPSyncSeq(localhost = true)
	public void changeAvailablePower(float limit) {
		Resource diff = new Resource(total);
		diff.setFlops(limit);
		diff.subtract(total);
		
		total.setFlops(limit);
		available.add(diff);
		writeConfigurationFile();
	}
	
	@POPSyncSeq(localhost = true)
	public void changeAvailableMemory(float limit) {
		Resource diff = new Resource(total);
		diff.setMemory(limit);
		diff.subtract(total);
		
		total.setMemory(limit);
		available.add(diff);
		writeConfigurationFile();
	}
	
	@POPSyncSeq(localhost = true)
	public void changeAvailableBandwidth(float limit) {
		Resource diff = new Resource(total);
		diff.setBandwidth(limit);
		diff.subtract(total);
		
		total.setBandwidth(limit);
		available.add(diff);
		writeConfigurationFile();
	}
	
	@POPSyncSeq(localhost = true)
	public void changeMaxJobLimit(int limit) {
		maxJobs = limit;
		writeConfigurationFile();
	}
	
	@POPSyncSeq(localhost = true)
	public void changeMaxJobPower(float limit) {
		jobLimit.setFlops(limit);
		writeConfigurationFile();
	}
	
	@POPSyncSeq(localhost = true)
	public void changeMaxJobMemory(float limit) {
		jobLimit.setMemory(limit);
		writeConfigurationFile();
	}
	
	@POPSyncSeq(localhost = true)
	public void changeMaxJobBandwidth(float limit) {
		jobLimit.setBandwidth(limit);
		writeConfigurationFile();
	}

	/**
	 * Contact neighbors and tell them we are alive
	 */
	@POPAsyncConc
	protected void selfRegister() {
		// don't register if too close to last one
		if (nextSelfRegister > System.currentTimeMillis()) {
			return;
		}

		try {
			// for all members of each network
			Collection<POPNetwork> nets = networks.values();
			for (POPNetwork network : nets) {
				// all connectors in network
				POPConnectorBase[] connectors = network.getConnectors();

				for (POPConnectorBase connector : connectors) {
					// use connector with nodes who define a job manager
					if (!(connector instanceof POPConnectorSearchNodeInterface)) {
						continue;
					}
					
					// all nodes
					List<POPNetworkNode> nodes = network.getMembers(connector.getClass());

					for (POPNetworkNode node : nodes) {
						// only contact JM types of nodes
						if (node instanceof AbstractNodeJobManager) {
							// connect to remove jm
							AbstractNodeJobManager jmnode = (AbstractNodeJobManager) node;
							registerRemoteAsync(network.getName(), jmnode);
						}
					}
				}
			}
		} finally {
			nextSelfRegister = System.currentTimeMillis() + conf.getJobManagerSelfRegisterInterval();
		}
	}
	
	/**
	 * Contact neighbors and tell them we are shutting down
	 */
	@POPAsyncConc
	protected void selfUnregister() {
		// TODO like selfRegister
	}

	/**
	 * Register to remote network and locally
	 *
	 * @param network
	 * @param node
	 */
	@POPAsyncConc
	private void registerRemoteAsync(String network, AbstractNodeJobManager node) {
		try {
			POPJavaJobManager jm = node.getJobManager();			
			jm.registerNode(network, node.getCreationParams());
		} catch (POPException e) {
		}
	}

	/**
	 * Create a new network and write the Job Manager configuration file anew
	 *
	 * @param name A unique name of the network
	 * @return true if created or already exists, false if already exists but use a different protocol
	 */
	@POPSyncConc(localhost = true)
	public boolean createNetwork(String name) {
		try {
			// check if exists already
			POPNetwork network = networks.get(name);
			if (network != null) {
				return true;
			}

			// create the new network
			POPNetwork newNetwork = new POPNetwork(name, this);

			// add new network
			LogWriter.writeDebugInfo("[JM] Network %s added", name);
			networks.put(name, newNetwork);

			// write all current configurations to a file
			writeConfigurationFile();
			return true;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[JM] Exception caught in createNetwork: %s", e.getMessage());
			return false;
		}
	}

	/**
	 * Remove a network and write the Job Manager configuration file anew
	 *
	 * @param name The unique name of the network
	 */
	@POPAsyncConc(localhost = true)
	public void removeNetwork(String name) {
		if (!networks.containsKey(name)) {
			LogWriter.writeDebugInfo("[JM] Network %s not removed, not found", name);
			return;
		}
		
		LogWriter.writeDebugInfo("[JM] Network %s removed", name);
		networks.remove(name);
		
		// write all current configurations to a file
		writeConfigurationFile();
	}

	/**
	 * Register node to a network by supplying an array of string matching the format in the configuration file
	 *
	 * @param networkName The name of an existing network in this JM
	 * @param params An array of String that will be processed by {@link POPNetwork#makeNode}
	 */
	@POPSyncConc
	public void registerNode(String networkName, String... params) {
		// get network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not registered, network %s not found", Arrays.toString(params), networkName);
			return;
		}

		LogWriter.writeDebugInfo("[JM] Node %s added to %s", Arrays.toString(params), networkName);
		POPNetworkNode node = POPNetworkNodeFactory.makeNode(params);
		node.setTemporary(true);
		network.add(node);
	}

	/**
	 * Remove a node from a network
	 *
	 * @param networkName The name of an existing network in this JM
	 * @param params An array of String that will be processed to {@link POPNetworkNodeFactory#makeNode}
	 */
	@POPAsyncConc
	public void unregisterNode(String networkName, String... params) {
		// get network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not removed, network not found", Arrays.toString(params));
			return;
		}

		LogWriter.writeDebugInfo("[JM] Node %s removed", Arrays.toString(params));
		network.remove(POPNetworkNodeFactory.makeNode(params));
	}

	/**
	 * Register a node in the default POP network
	 *
	 * @param params
	 */
	@POPSyncConc
	public void registerNode(String... params) {
		registerNode(defaultNetwork.getName(), params);
	}

	/**
	 * Unregister a node from the default POP network
	 *
	 * @param params
	 */
	@POPAsyncConc
	public void unregisterNode(String... params) {
		unregisterNode(defaultNetwork.getName(), params);
	}
	
	/**
	 * Register a node and write it in the configuration file
	 * @param networkName
	 * @param params
	 */
	@POPSyncConc(localhost = true)
	public void registerPermanentNode(String networkName, String... params) {
		// get network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not registered, network %s not found", Arrays.toString(params), network);
			return;
		}

		LogWriter.writeDebugInfo("[JM] Node %s added to %s", Arrays.toString(params), network);
		network.add(POPNetworkNodeFactory.makeNode(params));
		writeConfigurationFile();
	}
	
	/**
	 * Unregister a node and write it in the configuration file
	 * @param networkName
	 * @param params
	 */
	@POPSyncConc(localhost = true)
	public void unregisterPermanentNode(String networkName, String... params) {
		// get network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not removed, network not found", Arrays.toString(params));
			return;
		}

		LogWriter.writeDebugInfo("[JM] Node %s removed", Arrays.toString(params));
		network.remove(POPNetworkNodeFactory.makeNode(params));
		writeConfigurationFile();
	}

	/**
	 * Check and remove finished or timed out resources. NOTE: With a lot of job this method need jobs.size connections
	 * to be made before continuing
	 */
	@POPAsyncConc(localhost = true)
	public void update() {
		// don't update if too close to last one
		if (nextUpdate > System.currentTimeMillis()) {
			return;
		}

		try {
			mutex.lock();
			long updateInterval = conf.getJobManagerUpdateInterval();
			for (Iterator<AppResource> iterator = jobs.values().iterator(); iterator.hasNext();) {
				AppResource job = iterator.next();
				// job not started after timeout
				if (!job.isUsed()) {
					if (job.getAccessTime() + conf.getReserveTimeout() > System.currentTimeMillis()) {
						// manually remove from iterator
						available.add(job);
						iterator.remove();
						LogWriter.writeDebugInfo("[JM] Free up [%s] resources (unused).", job);
					}
				} // dead objects check with min UPDATE_MIN_INTERVAL time between them
				else if (job.getAccessTime() < System.currentTimeMillis() - updateInterval) {
					job.setAccessTime(System.currentTimeMillis());
					try {
						// connection to object ok
						Interface obj = new Interface(job.getContact());
						obj.close();
					} catch (Exception e) {
						// manually remove from iterator
						available.add(job);
						iterator.remove();
						// add to cleanup job
						cleanupJobs.add(job);
						LogWriter.writeDebugInfo("[JM] Free up [%s] resources (dead object).", job);
					}
				}
			}
		} finally {
			// set next update
			nextUpdate = System.currentTimeMillis() + conf.getJobManagerUpdateInterval();
			mutex.unlock();
		}
	}

	/**
	 * A copy of the current available resources
	 *
	 * @return A copy Resource object
	 */
	@POPSyncConc(localhost = true)
	public Resource getAvailableResources() {
		return new Resource(available);
	}

	/**
	 * The initial capacity of the node
	 * 
	 * @return 
	 */
	@POPSyncConc(localhost = true)
	public Resource getInitialAvailableResources() {
		return new Resource(total);
	}

	/**
	 * The upper limit for each job
	 * 
	 * @return 
	 */
	@POPSyncConc(localhost = true)
	public Resource getJobResourcesLimit() {
		return new Resource(jobLimit);
	}

	/**
	 * The maximum number of simultaneous object available on the JM machine
	 * 
	 * @return 
	 */
	@POPSyncConc(localhost = true)
	public int getMaxJobs() {
		return maxJobs;
	}

	/**
	 * Unique ID for this node execution
	 *
	 * @return the UUID of the node
	 */
	@POPSyncConc
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * Propagate application end at the end of an application NOTE May not the necessary or useful, POP-Java already
	 * kill unused objects
	 *
	 * @param popAppId
	 * @param initiator
	 */
	@POPAsyncConc
	public void applicationEnd(int popAppId, boolean initiator) {
		AppResource res = jobs.get(popAppId);
		if (initiator && res != null) {
			SNRequest r = new SNRequest(Util.generateUUID(), null, null, res.getOd().getNetwork(), res.getOd().getConnector(), null);
			r.setAsEndRequest();
			r.setPOPAppId(popAppId);

			launchDiscovery(r, 1);
		}

		// remove job and gain resources bask
		Resource r = jobs.remove(popAppId);
		if (r != null) {
			available.add(r);
		}
	}

	////
	//		Utility
	////
	
	/**
	 * Rewrite the configuration file to disk
	 */
	private void writeConfigurationFile() {
		try {
			File config = new File(configurationFile);
			// create a new one if missing
			if (!config.exists()) {
				config.createNewFile();
			}
			
			// lock file
			FileChannel channel = new RandomAccessFile(config, "rw").getChannel();
			FileLock lock = channel.lock();
			
			PrintStream ps = new PrintStream(config);
			
			// resource power|memory|bandwidth
			ps.println("# available resources for this job manager");
			ps.println("resource power " + total.getFlops());
			ps.println("resource memory " + total.getMemory());
			ps.println("resource bandwidth " + total.getBandwidth());
			
			// job limit|power|memory|bandwidth
			ps.println("# limit max for single job");
			ps.println("job limit " + maxJobs);
			ps.println("job power " + jobLimit.getFlops());
			ps.println("job memory " + jobLimit.getMemory());
			ps.println("job bandwidth " + jobLimit.getBandwidth());
			
			// networks
			ps.println("# networks with nodes");
			for (POPNetwork network : networks.values()) {
				// name
				ps.print("network " + network.getName());
				// add default marker if needed
				if (network.equals(defaultNetwork)) {
					ps.print(" default");
				}
				ps.println();
				
				// nodes ordered by connector
				for (POPConnectorBase connector : network.getConnectors()) {
					ps.println("# nodes for connector " + connector.getClass().getCanonicalName());
					
					for (POPNetworkNode node : network.getMembers(connector.getClass())) {
						if (node.isTemporary()) {
							continue;
						}
						// chain creation parameters
						ps.print("node ");
						for (String param : node.getCreationParams()) {
							ps.print(param + " ");
						}
						ps.println();
					}
				}
			}
			
			// extra information present in a manually generated file
			ps.println("# extra information");
			for (String token0 : nodeExtra.keySet()) {
				for (String line : nodeExtra.get(token0)) {
					ps.print(token0);
					ps.println(line);
				}
			}
			
			// write and close
			ps.close();
			lock.release();
		} catch(IOException e) {
			LogWriter.writeDebugInfo("[JM] Failed to write current configuration to disk");
		} finally {
			
		}
	}
	
	/**
	 * Dead AppResources are handled here, cleaning them up.
	 */
	@POPAsyncSeq
	protected void cleanup() {
		for (Iterator<AppResource> iterator = cleanupJobs.iterator(); iterator.hasNext();) {
			try {
				AppResource job = iterator.next();
				Path appDirectory = job.getAppDirectory();
				if (appDirectory == null) {
					continue;
				}
				if (Files.exists(appDirectory) && appDirectory.toFile().list().length == 0) {
					Files.deleteIfExists(appDirectory);
					if (!Files.exists(appDirectory)) {
						iterator.remove();
					}
				}
			} catch(IOException e) {
				LogWriter.writeExceptionLog(e);
			}
		}
	}

	/**
	 * Change configuration file location.
	 * This method will only change the location and try to write in it, it will not delete the old file.
	 * This method is not meant to be used to load a new configuration file.
	 * 
	 * @param configurationFile 
	 */
	@POPAsyncConc(localhost = true)
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
		writeConfigurationFile();
	}	
	
	/**
	 * The available networks in the current Job Manager
	 * 
	 * @return 
	 */
	@POPSyncConc(localhost = true)
	public String[] getAvailableNetworks() {
		int size = networks.keySet().size();
		String[] networksArray = networks.keySet().toArray(new String[size]);
		return networksArray;
	}
	
	/**
	 * All the nodes in a network
	 * 
	 * @param networkName
	 * @return 
	 */
	@POPSyncConc(localhost = true)
	public String[][] getNetworkNodes(String networkName) {
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			return new String[0][0];
		}
		
		String[][] nodes = new String[network.size()][];
		int i = 0;
		for (POPConnectorBase connector : network.getConnectors()) {
			for (POPNetworkNode member : network.getMembers(connector.getClass())) {
				nodes[i++] = member.getCreationParams();
			}
		}
		return nodes;
	}

	/**
	 * Blocking method until death of this object.
	 */
	@POPSyncConc(localhost = true)
	public void stayAlive() {
		stayAlive.acquireUninterruptibly();
	}

	/**
	 * Release waiting processes
	 *
	 * @throws Throwable
	 */
	@Override
	protected void finalize() throws Throwable {
		stayAlive.release(Integer.MAX_VALUE);
		super.finalize();
	}
	
	
	
	////
	//		TFC
	////
	/**
	 * Register a new object that will be available for connection over TFC
	 * @param networkName The network that will contain the object
	 * @param objectName The class of the object
	 * @param accessPoint Where to find the object on the machine
	 * @param secret A secret to remove the object
	 * @return true if we could add the object successfully
	 */
	@POPSyncConc(localhost = true)
	public boolean registerTFCObject(String networkName, String objectName, POPAccessPoint accessPoint, String secret) {
		// get registerer network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			return false;
		}
		
		// get network's TFC connector to add the object
		POPConnectorTFC tfc = network.getConnector(POPConnectorTFC.class);
		if (tfc == null) {
			return false;
		}
		
		// create resource
		TFCResource resource = new TFCResource(objectName, accessPoint, secret);
		// register resource with the connector
		return tfc.registerObject(resource);
	}
	
	/**
	 * Register a new object that will be available for connection over TFC
	 * @param networkName The network that will contain the object
	 * @param objectName The class of the object
	 * @param accessPoint Where to find the object on the machine
	 * @param secret A secret to remove the object
	 */
	@POPSyncConc(localhost = true)
	public void unregisterTFCObject(String networkName, String objectName, POPAccessPoint accessPoint, String secret) {
		// get registerer network
		POPNetwork network = networks.get(networkName);
		if (network == null) {
			return;
		}
		
		// get network's TFC connector to add the object
		POPConnectorTFC tfc = network.getConnector(POPConnectorTFC.class);
		if (tfc == null) {
			return;
		}
		
		// create resource
		TFCResource resource = new TFCResource(objectName, accessPoint, secret);
		// unregister resource with the connector
		tfc.unregisterObject(resource);
	}

	/////
	//		Search Node
	////
	
	/** UID of requests to SN */
	private final LinkedBlockingDeque<String> SNKnownRequests = new LinkedBlockingDeque<>(conf.getSearchNodeMaxRequests());
	/** Answering nodes for a search request */
	private final Map<String, SNNodesInfo> SNActualRequets = new HashMap<>();
	/** Semaphores for non-timed requests */
	private final Map<String, Semaphore> SNRequestSemaphore = new HashMap<>();

	/**
	 * Start a discovery in a POP Network
	 * 
	 * @param request The request generated by {@link POPConnectorBase#createObject}
	 * @param timeout How much time do we wait before proceeding, in case of 0 the first answer is the one we use
	 * @return All the nodes that answered our request
	 */
	@POPSyncConc(localhost = true)
	public SNNodesInfo launchDiscovery(@POPParameter(Direction.IN) final SNRequest request, int timeout) {
		try {
			LogWriter.writeDebugInfo("[PSN] starting research");

			// add itself to the nodes visited
			request.getExplorationList().add(getAccessPoint());

			if (request.isEndRequest()) {
				timeout = 1;
			} else {
				LogWriter.writeDebugInfo("[PSN] LDISCOVERY;TIMEOUT;%d", timeout);
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
				Thread SNunlock = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(conf.getSearchNodeUnlockTimeout());
							unlockDiscovery(request.getUID());
						} catch (InterruptedException e) {
						}
					}
				}, "SearchNode auto-unlock timer");
				SNunlock.setDaemon(true);
				SNunlock.start();
				
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
			LogWriter.writeDebugInfo("[PSN] Exception caught in launchDiscovery: %s", e.getMessage());
			return new SNNodesInfo();
		}
	}

	/**
	 * Recursive part of the Search Node, propagate request and answer if possible
	 * 
	 * @param request The propagating request
	 * @param sender Unused, send empty POPAccessPoint
	 */
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
			if (network == null) {
				return;
			}

			// decrease the hop we can still do
			if (request.getRemainingHops() != conf.getSearchNodeUnlimitedHops()) {
				request.decreaseHopLimit();
			}
			
			// connector we are using
			Class<? extends POPConnectorBase> connectorUsed = POPConnectorFactory.getConnectorClass(request.getConnector());
			POPConnectorBase connector = network.getConnector(connectorUsed);
			
			// connector won't work with the SearchNode
			if (!(connector instanceof POPConnectorSearchNodeInterface)) {
				return;
			}
			POPConnectorSearchNodeInterface snEnableConnector = (POPConnectorSearchNodeInterface) connector;
			
			// add all network neighbors to explorations list
			for (POPNetworkNode node : network.getMembers(connectorUsed)) {
				// only JM items and children
				if (node instanceof AbstractNodeJobManager) {
					AbstractNodeJobManager jmNode = (AbstractNodeJobManager) node;
					// add to exploration list
					explorationList.add(jmNode.getJobManagerAccessPoint());
				}
			}
			
			// XXX not currently in use
			// used to kill the application from all JMs
			if (request.isEndRequest()) {
				// check if we can continue discovering
				if (request.getRemainingHops() >= 0 || request.getRemainingHops() == conf.getSearchNodeUnlimitedHops()) {
					// propagate to all neighbors
					for (POPNetworkNode node : network.getMembers(connectorUsed)) {
						// only JM items and children
						if (node instanceof NodeJobManager) {
							AbstractNodeJobManager jmNode = (AbstractNodeJobManager) node;

							// contact if it has not been contacted before by someone else
							if (!oldExplorationList.contains(jmNode.getJobManagerAccessPoint())) {
								try {
									// send request to other JM
									POPJavaJobManager jm = jmNode.getJobManager();
									jm.askResourcesDiscovery(request, getAccessPoint());		
								} catch(Exception e) {
									LogWriter.writeDebugInfo("[PSN] askResourcesDiscovery can't reach %s: %s", jmNode.getJobManagerAccessPoint(), e.getMessage());
								}
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
			if (SNKnownRequests.contains(request.getUID())) {
				return;
			}
			// add it if not, at the beginning to find it more easily 
			SNKnownRequests.push(request.getUID());

			// remove older elements
			if (SNKnownRequests.size() > conf.getSearchNodeMaxRequests()) {
				SNKnownRequests.pollLast();
			}

			// send request, handled by the different connectors
			snEnableConnector.askResourcesDiscoveryAction(request, sender, oldExplorationList);

			// propagate in the network if we still can
			if (request.getRemainingHops() >= 0 || request.getRemainingHops() == conf.getSearchNodeUnlimitedHops()) {
				// add current node do wayback
				request.getWayback().push(getAccessPoint());
				// request to all members of the network
				for (POPNetworkNode node : network.getMembers(connectorUsed)) {
					if (node instanceof AbstractNodeJobManager) {
						AbstractNodeJobManager jmNode = (AbstractNodeJobManager) node;
						// contact if it's a new node
						if (!oldExplorationList.contains(jmNode.getJobManagerAccessPoint())) {
							try {
								// send request to other JM
								POPJavaJobManager jm = jmNode.getJobManager();
								jm.askResourcesDiscovery(request, getAccessPoint());
							} catch(Exception e) {
								LogWriter.writeDebugInfo("[PSN] askResourcesDiscovery can't reach %s: %s", jmNode.getJobManagerAccessPoint(), e.getMessage());
							}
						}

					}
				}
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[PSN] Exception caught in askResourcesDiscovery: %s", e.getMessage());
		}
	}

	/**
	 * Add a node to the response list
	 * 
	 * @param response 
	 */
	@POPAsyncConc
	public void callbackResult(@POPParameter(Direction.IN) SNResponse response) {
		try {
			// the result node is stored in the SNNodes
			SNNodesInfo.Node result = response.getResultNode();
			SNNodesInfo nodes = SNActualRequets.get(response.getUID());
			// the list doesn't exists
			if (nodes == null) {
				return;
			}
			// add the node to the list
			nodes.add(result);
			
			// save responder certificate
			if (response.getPublicCertificate().length > 0) {
				SSLUtils.addCertToTempStore(response.getPublicCertificate());
			}
			
			// we unlock the senaphore if it was set
			unlockDiscovery(response.getUID());
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[PSN] Exception caught in callbackResult: %s", e.getMessage());
		}
	}

	/**
	 * If we aren't the original node we transport the request back the way it came from
	 * 
	 * @param response Answer to the request
	 * @param wayback The route to go back to the origin
	 */
	@POPAsyncConc
	public void rerouteResponse(@POPParameter(Direction.IN) SNResponse response,
			@POPParameter(Direction.IN) SNWayback wayback) {
		try {
			// we want the call in the network to be between neighbors only
			// so we go back on the way we came with the response for the source
			if (!wayback.isLastNode()) {
				LogWriter.writeDebugInfo("[PSN] REROUTE;%s;DEST;%s", response.getUID(), wayback.toString());
				// get next node to contact
				POPAccessPoint jm = wayback.pop();
				POPJavaJobManager njm = PopJava.newActive(POPJavaJobManager.class, jm);
				// route request through it
				njm.rerouteResponse(response, wayback);
				njm.exit();
			} // is the last node, give the answer to the original JM who launched the request
			else {
				LogWriter.writeDebugInfo("[PSN] REROUTE_ORIGIN;%s;", response.getUID());
				callbackResult(response);
			}
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[PSN] Exception caught in rerouteResponse: %s", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Release the dispatching thread if necessary
	 * 
	 * @param requid 
	 */
	@POPAsyncConc
	public void unlockDiscovery(String requid) {
		// get and remove the semaphore
		Semaphore sem = SNRequestSemaphore.remove(requid);
		// release if the semaphore was set
		if (sem != null) {
			sem.release();
			LogWriter.writeDebugInfo("[PSN] UNLOCK SEMAPHORE %s", requid);
		}
	}
}
