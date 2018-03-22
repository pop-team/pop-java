package popjava.service.jobmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import popjava.PopJava;
import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPConfig;
import popjava.annotation.POPConfig.Type;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPParameter;
import popjava.annotation.POPParameter.Direction;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPErrorCode;
import popjava.base.POPException;
import popjava.baseobject.AccessPoint;
import popjava.baseobject.ObjectDescription;
import popjava.baseobject.POPAccessPoint;
import popjava.codemanager.AppService;
import popjava.dataswaper.POPMutableFloat;
import popjava.dataswaper.POPString;
import popjava.interfacebase.Interface;
import popjava.service.jobmanager.external.POPNetworkDetails;
import popjava.service.jobmanager.network.POPConnector;
import popjava.service.jobmanager.network.POPConnectorSearchNodeInterface;
import popjava.service.jobmanager.network.POPConnectorTFC;
import popjava.service.jobmanager.network.POPNetwork;
import popjava.service.jobmanager.network.POPNetworkDescriptor;
import popjava.service.jobmanager.network.POPNode;
import popjava.service.jobmanager.network.POPNodeAJobManager;
import popjava.service.jobmanager.network.POPNodeJobManager;
import popjava.service.jobmanager.network.POPNodeTFC;
import popjava.service.jobmanager.search.SNExploration;
import popjava.service.jobmanager.search.SNNodesInfo;
import popjava.service.jobmanager.search.SNRequest;
import popjava.service.jobmanager.search.SNResponse;
import popjava.service.jobmanager.search.SNWayback;
import popjava.service.jobmanager.tfc.TFCResource;
import popjava.service.jobmanager.yaml.PropertyReverser;
import popjava.service.jobmanager.yaml.YamlConnector;
import popjava.service.jobmanager.yaml.YamlJobManager;
import popjava.service.jobmanager.yaml.YamlNetwork;
import popjava.service.jobmanager.yaml.YamlResource;
import popjava.serviceadapter.POPAppService;
import popjava.serviceadapter.POPJobService;
import popjava.system.POPJavaConfiguration;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.POPRemoteCaller;
import popjava.util.SystemUtil;
import popjava.util.Tuple;
import popjava.util.Util;
import popjava.util.ssl.KeyPairDetails;
import popjava.util.ssl.KeyStoreDetails;
import popjava.util.ssl.SSLUtils;



@POPClass
public class POPJavaJobManager extends POPJobService {
	
	private final Configuration conf = Configuration.getInstance();
	
	/** The configuration file location */
	protected File configurationFile;

	/** Currently used resources of a node */
	protected final Resource available = new Resource();
	/** Max available resources of the node */
	protected final Resource total = new Resource();
	/** Total resources a job can have */
	protected final Resource jobLimit = new Resource();
	
	/** Total number of requests received */
	protected final AtomicInteger requestCounter = new AtomicInteger(1 + (int) (Math.random() * Integer.MAX_VALUE));

	/** Number of job alive, mapped by {@link AppResource#id} */
	protected final Map<Integer,AppResource> jobs = new HashMap<>();
	
	/** Jobs we need to cleanup */
	private final LinkedBlockingDeque<AppResource> cleanupJobs = new LinkedBlockingDeque<>();

	/** Networks saved in this JobManager */
	protected final Map<String,POPNetwork> networks = new HashMap<>();

	/** Default network UUID */
	protected String defaultNetwork = null;

	/** Max number of jobs */
	protected int maxJobs;

	/** Node extra information, value as List if multiple are supplied */
	protected final Map<String, List<String>> nodeExtra = new HashMap<>();

	/** Mutex for some operations */
	protected final ReentrantLock mutex = new ReentrantLock(true);

	/** JobManager unique ID */
	protected final String nodeId = Util.generateUUID();

	/** When to perform the next update of the job list */
	private long nextUpdate = 0;
	/** Self register timer */
	private long nextSelfRegister = 0;
	
	/** Semaphore used to cleanly avoid premature exit of the Job Manager */
	private final Semaphore stayAlive = new Semaphore(0);
	
	private final Map<Tuple<String, POPAccessPoint>, POPJavaJobManager> cachedJobManangers = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Do not call this directly, way too many methods to this so no init was added.
	 */
	@POPObjectDescription(url = "localhost", jvmParameters = "-Xmx512m")
	public POPJavaJobManager() {
		//init(conf.DEFAULT_JM_CONFIG_FILE);
		configurationFile = conf.getSystemJobManagerConfig();
	}

	// may also want to use  -XX:MaxHeapFreeRatio=?? -XX:MinHeapFreeRatio=??  if fine tuned
	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url) {
		configurationFile = conf.getSystemJobManagerConfig();
		init(conf.getSystemJobManagerConfig());
	}
	
	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url, @POPConfig(Type.PROTOCOLS) String[] protocols) {
		configurationFile = conf.getSystemJobManagerConfig();
		init(conf.getSystemJobManagerConfig());
	}
	
	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url, @POPConfig(Type.PROTOCOLS) String[] protocols, String conf) {
		configurationFile = new File(conf);
		init(configurationFile);
	}
	
   @POPObjectDescription(jvmParameters = "-Xmx512m")
    public POPJavaJobManager(@POPConfig(Type.URL) String url, @POPConfig(Type.PROTOCOLS) String[] protocols, String conf,
            @POPConfig(Type.LOCAL_JVM) boolean localJVM, @POPConfig(Type.UPNP) boolean upnp) {
        configurationFile = new File(conf);
        init(configurationFile);
    }

	@POPObjectDescription(jvmParameters = "-Xmx512m")
	public POPJavaJobManager(@POPConfig(Type.URL) String url, String conf) {
		configurationFile = new File(conf);
		init(configurationFile);
	}

	/**
	 * Read configuration file and setup system Has some sane defaults
	 *
	 * @param configFile Configuration file
	 */
	private void init(File configFile) {

		// early exit
		if (!configFile.exists()) {
			LogWriter.writeDebugInfo("[JM] Open config file [%s] fail, trying to create", configFile);
			
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				LogWriter.writeDebugInfo("[JM] can't create job manager file %s", configFile);
			}
		}
		LogWriter.writeDebugInfo("[JM] Using %s as config file", configFile.getAbsoluteFile().toString());

		// default num of jobs
		maxJobs = 200;

		// set resource by default hoping for the best
		available.add(new Resource(30000, 8192, 102400f));
		// no restrictions on default limit
		jobLimit.add(available);
		// total is the same as available for now
		total.add(available);
		
		// TODO run benchmark power (maybe memory and bandwidth if needed)

		Yaml yaml = new Yaml();
		YamlJobManager config;
		// config file is read line by line, information is extracted as see fit
		try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
			config = yaml.loadAs(br, YamlJobManager.class);
		} catch (IOException e) {
			LogWriter.writeExceptionLog(e);
			throw new POPException(0, e.getMessage());
		}
		
		// abort init early, nothing to configure
		if (config == null) {
			return;
		}
	
		// root single params
		// number of job running at the same time
		maxJobs = config.getJobLimit() > 0 ? config.getJobLimit() : maxJobs;
		// default network UUID
		defaultNetwork = config.getDefaultNetwork();



		// set machine max resource usage
		YamlResource machineResources = config.getMachineResources();
		if (machineResources != null && machineResources.getFlops() > 0f) {
				available.setFlops(machineResources.getFlops());
				total.setFlops(machineResources.getFlops());
				LogWriter.writeDebugInfo("[JM] setting maximim flops to [%f]", machineResources.getFlops());
		}
		if (machineResources != null && machineResources.getBandwidth() > 0f) {
				available.setBandwidth(machineResources.getBandwidth());
				total.setBandwidth(machineResources.getBandwidth());
				LogWriter.writeDebugInfo("[JM] setting maximim bandwidth to [%f]", machineResources.getBandwidth());
		}
		if (machineResources != null && machineResources.getMemory() > 0f) {
				available.setMemory(machineResources.getMemory());
				total.setMemory(machineResources.getMemory());
				LogWriter.writeDebugInfo("[JM] setting maximim memory to [%f]", machineResources.getMemory());
		}

		// set single job limit
		YamlResource jobResources = config.getJobResources();
		if (jobResources != null && jobResources.getFlops() > 0f) {
				jobLimit.setFlops(jobResources.getFlops());
				LogWriter.writeDebugInfo("[JM] setting job limit flops to [%f]", jobResources.getFlops());
		}
		if (jobResources != null && jobResources.getBandwidth() > 0f) {
				jobLimit.setBandwidth(jobResources.getBandwidth());
				LogWriter.writeDebugInfo("[JM] setting job limit bandwidth to [%f]", jobResources.getBandwidth());
		}
		if (jobResources != null && jobResources.getMemory() > 0f) {
				jobLimit.setMemory(jobResources.getMemory());
				LogWriter.writeDebugInfo("[JM] setting job limit memory to [%f]", jobResources.getMemory());
		}



		// networks
		for (YamlNetwork ymlNetwork : config.getNetworks()) {
			String uuid = ymlNetwork.getUuid();
			String friendlyName = ymlNetwork.getFriendlyName();
			
			// not default set, use first
			if (defaultNetwork == null) {
				defaultNetwork = uuid;
			}

			// create the network
			POPNetwork network = new POPNetwork(uuid, friendlyName, this);
			networks.put(uuid, network);
			LogWriter.writeDebugInfo("[JM] added network [%s] (%s)", friendlyName, uuid);

			for (YamlConnector ymlConnector : ymlNetwork.getConnectors()) {
				String connectorType = ymlConnector.getType();
				// get connector descriptor from name
				POPNetworkDescriptor descriptor = POPNetworkDescriptor.from(connectorType);
				// skip if we don't have the connector
				if (descriptor == null) {
					LogWriter.writeDebugInfo("[JM] couldn't find implementation of [%s] POPConnector", connectorType);
					continue;
				}

				for (List<String> listNode : ymlConnector.asPOPNodeParams()) {
					POPNode node = descriptor.createNode(listNode);						
					// add to network
					network.add(node);
					LogWriter.writeDebugInfo("[JM] node [%s] added to [%s] (%s)", node.toString(), friendlyName, uuid);

				}
			}
		}
	}

	/**
	 * Local request to create an object 
	 * @param localservice The AppService of the application
	 * @param objname Which object we have to create
	 * @param od The OD of the request
	 * @param howmany The size of objcontacts
	 * @param objcontacts How many instances we seek
	 * @param howmany2 number of remote access points (we think)
	 * @param remotejobcontacts actual access points (we think)
	 * @return 0 or an exception, normally
	 */
	@POPSyncConc(id = 12)
	@Override
	@SuppressWarnings("unchecked")
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
				networkString = defaultNetwork;
				od.setNetwork(networkString);
			}
			// get real network
			POPNetwork network = networks.get(networkString);

			// throw exception if no network is found
			if (network == null) {
				throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, networkString + " not found.");
			}

			// get the job manager connector specified in the od
			POPNetworkDescriptor connector = null;
			try {
				connector = POPNetworkDescriptor.from(od.getConnector());
			} catch(IllegalArgumentException e) {
				throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, 
					od.getConnector() + " in " + networkString + " unknown.");
			}
			POPConnector connectorImpl = network.getConnector(connector);
			
			if (connectorImpl == null) {
				throw new POPException(POPErrorCode.POP_JOBSERVICE_FAIL, 
					od.getConnector() + " in " + networkString + " not found.");
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
	 * @return 0 when successful, any other number when not
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
					// TODO need to save the working network at the start of the request
					try {
						service = PopJava.newActiveConnect(this, POPJavaAppService.class, res.getAppService());
						service.queryCode(objname.getValue(), POPSystem.getPlatform(), codeFile);
						appId = service.getPOPCAppID();
						service.exit();
					} catch (POPException e) {
						service = PopJava.newActiveConnect(this, POPAppService.class, res.getAppService());
						service.queryCode(objname.getValue(), POPSystem.getPlatform(), codeFile);
						appId = service.getPOPCAppID();
						service.exit();
					}
					
					// create directory if it doesn't exists and set OD.cwd
					Path objectAppCwd = Paths.get(conf.getJobManagerExecutionBaseDirectory(), appId).toAbsolutePath();
					SystemUtil.mkdir(objectAppCwd, hostuser);
					// ensure the directory is there, 200ms 
					for (int j = 0; !Files.exists(objectAppCwd) && j < 20; j++) {
						Thread.sleep(10);
					}
					
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
		for (int id : ids) {
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
	 * @param popAppId the application id
	 * @param reqID the request id
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
			if (!od.isEmpty()) 

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
				POPConnector[] connectors = net.getConnectors();
				out.println(String.format("[%s]", net.getUUID()));
				out.println(String.format("connectors=%s", Arrays.toString(connectors)));
				out.println(String.format("members=%d", net.size()));
				for (POPConnector connector : connectors) {
					out.println(String.format("[%s.%s]", net.getUUID(), connector.getClass().getCanonicalName()));
					for (POPNode node : net.getMembers(connector.getDescriptor())) {
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
			case "power":
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
				POPConnector[] connectors = network.getConnectors();

				for (POPConnector connector : connectors) {
					// use connector with nodes who define a job manager
					if (!(connector instanceof POPConnectorSearchNodeInterface)) {
						continue;
					}
					
					// skip we the connector is configured not to broadcast
					POPConnectorSearchNodeInterface jmConnector = (POPConnectorSearchNodeInterface) connector;
					if (!jmConnector.broadcastPresence()) {
						continue;
					}
					
					// all nodes
					List<POPNode> nodes = network.getMembers(connector.getDescriptor());

					for (POPNode node : nodes) {
						// only contact JM types of nodes
						if (node instanceof POPNodeAJobManager) {
							// connect to remove jm
							POPNodeAJobManager jmnode = (POPNodeAJobManager) node;
							registerRemoteAsync(network.getUUID(), jmnode);
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
	 * @param networkUUID the network id
	 * @param node the node we are registering
	 */
	@POPAsyncConc
	private void registerRemoteAsync(String networkUUID, POPNodeAJobManager node) {
		try {
			// TODO send self, we should in some way generate a POPNetworkNode of ourselves in the right format
			// a way could be by modifying the parameters we get from node.getParameters(), we know host=??, 
			// port=?? and protocol=?? should be there
			/*POPJavaJobManager jm = node.getJobManager();			
			jm.registerNode(network, );*/
		} catch (POPException e) {
		}
	}

	/**
	 * Create a new network and write the Job Manager configuration file anew
	 *
	 * @param friendlyName A name for this network
	 * @return true if created or already exists, false if already exists but use a different protocol
	 */
	@POPSyncConc(localhost = true)
	public POPNetworkDetails createNetwork(String friendlyName) {
		try {
			// create the new network
			POPNetwork network = new POPNetwork(friendlyName, this);

			// add new network
			LogWriter.writeDebugInfo("[JM] Network %s added", friendlyName);
			networks.put(network.getUUID(), network);
			
			// generate network key
			KeyStoreDetails keyStoreDetails = conf.getSSLKeyStoreOptions();
			if (keyStoreDetails.getKeyStoreFile() != null) {
				try {
					KeyPairDetails keyPairDetails = new KeyPairDetails(network.getUUID());
					KeyStore.PrivateKeyEntry generateKeyPair = SSLUtils.ensureKeyPairGeneration(keyPairDetails);

					SSLUtils.addKeyEntryToKeyStore(keyStoreDetails, keyPairDetails, generateKeyPair);
				} catch(Exception e) {
					LogWriter.writeDebugInfo("[JM] Failed to generate Key add it to KeyStore with message: %s", e.getMessage());
				}
			}

			// write all current configurations to a file
			POPNetworkDetails d = new POPNetworkDetails(network);
			if (defaultNetwork == null || defaultNetwork.isEmpty()) {
				defaultNetwork = d.getUUID();
			}
			
			writeConfigurationFile();
			return d;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[JM] Exception caught in createNetwork: %s", e.getMessage());
			LogWriter.writeExceptionLog(e);
			return null;
		}
	}

	/**
	 * Create a new network and write the Job Manager configuration file anew.
	 * The network is create with the specified UUID.
	 *
	 * @param networkUUID A machine unique identifier for this network
	 * @param friendlyName A name for this network
	 * @return true if created or already exists, false if already exists but use a different protocol
	 */
	@POPSyncConc(localhost = true)
	public POPNetworkDetails createNetwork(String networkUUID, String friendlyName) {
		try {
			// check if exists already
			POPNetwork network = networks.get(networkUUID);
			if (network != null) {
				return new POPNetworkDetails(network);
			}

			// create the new network
			POPNetwork newNetwork = new POPNetwork(networkUUID, friendlyName, this);

			// add new network
			LogWriter.writeDebugInfo("[JM] Network %s added", friendlyName);
			networks.put(newNetwork.getUUID(), newNetwork);
			
			// generate network key
			KeyStoreDetails keyStoreDetails = conf.getSSLKeyStoreOptions();
			if (keyStoreDetails.getKeyStoreFile() != null) {
				try {
					KeyPairDetails keyPairDetails = new KeyPairDetails(newNetwork.getUUID());
					KeyStore.PrivateKeyEntry generateKeyPair = SSLUtils.ensureKeyPairGeneration(keyPairDetails);

					SSLUtils.addKeyEntryToKeyStore(keyStoreDetails, keyPairDetails, generateKeyPair);
				} catch(Exception e) {
					LogWriter.writeDebugInfo("[JM] Failed to generate Key add it to KeyStore with message: %s", e.getMessage());
				}
			}

			// write all current configurations to a file
			POPNetworkDetails d = new POPNetworkDetails(newNetwork);
			if (defaultNetwork == null || defaultNetwork.isEmpty()) {
				defaultNetwork = d.getUUID();
			}
			
			writeConfigurationFile();
			return d;
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[JM] Exception caught in createNetwork: %s", e.getMessage());
			LogWriter.writeExceptionLog(e);
			return null;
		}
	}

	/**
	 * Remove a network and write the Job Manager configuration file anew
	 *
	 * @param networkUUID The unique name of the network
	 */
	@POPAsyncConc(localhost = true)
	public void removeNetwork(String networkUUID) {
		if (!networks.containsKey(networkUUID)) {
			LogWriter.writeDebugInfo("[JM] Network %s not removed, not found", networkUUID);
			return;
		}
		
		POPNetwork network = networks.get(networkUUID);
		for (POPConnector connector : network.getConnectors()) {
			List<POPNode> copy = new ArrayList<>(network.getMembers(connector.getDescriptor()));
			for (POPNode member : copy) {
				unregisterNode(networkUUID, member.getCreationParams());
			}
		}
		
		try {
			SSLUtils.removeAlias(networkUUID);
		} catch(IOException e) {
			
		}
		
		LogWriter.writeDebugInfo("[JM] Network %s removed", networkUUID);
		networks.remove(networkUUID);
		
		// write all current configurations to a file
		writeConfigurationFile();
	}

	/**
	 * Register node to a network by supplying an array of string matching the format in the configuration file
	 *
	 * @param networkUUID The name of an existing network in this JM
	 * @param params An array of String that will be processed by {@link POPNetworkDescriptor#createNode }
	 */
	@POPSyncConc
	public void registerNode(String networkUUID, String... params) {
		// get network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not registered, network %s not found", Arrays.toString(params), networkUUID);
			return;
		}

		List<String> listparams = new ArrayList<>(Arrays.asList(params));
		String connector = Util.removeStringFromList(listparams, "connector=");
		POPNode node = POPNetworkDescriptor.from(connector).createNode(listparams);
		node.setTemporary(true);
		network.add(node);
		LogWriter.writeDebugInfo("[JM] Node %s added to %s", Arrays.toString(params), networkUUID);
	}

	/**
	 * Remove a node from a network
	 *
	 * @param networkUUID The name of an existing network in this JM
	 * @param params An array of String that will be processed to {@link POPNetworkDescriptor#createNode }
	 */
	@POPAsyncConc
	public void unregisterNode(String networkUUID, String... params) {
		// get network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not removed, network not found", Arrays.toString(params));
			return;
		}

		List<String> listparams = new ArrayList<>(Arrays.asList(params));
		String connector = Util.removeStringFromList(listparams, "connector=");
		POPNode node = POPNetworkDescriptor.from(connector).createNode(listparams);
		network.remove(node);
		
		try {
			SSLUtils.removeConfidenceLink(node, networkUUID);
		} catch(Exception e) {
			
		}
		LogWriter.writeDebugInfo("[JM] Node %s removed", Arrays.toString(params));
	}

	/**
	 * Register a node in the default POP network
	 *
	 * @param params the parameters needed to create a  new node, via {@link POPNode#getCreationParams()}
	 */
	@POPSyncConc
	public void registerNode(String... params) {
		registerNode(defaultNetwork, params);
	}

	/**
	 * Unregister a node from the default POP network
	 *
	 * @param params the parameters needed to create a  new node, via {@link POPNode#getCreationParams()}
	 */
	@POPAsyncConc
	public void unregisterNode(String... params) {
		unregisterNode(defaultNetwork, params);
	}
	
	/**
	 * Register a node and write it in the configuration file
	 * @param networkUUID the network id
	 * @param params the parameters needed to create a  new node, via {@link POPNode#getCreationParams()}
	 */
	@POPSyncConc(localhost = true)
	public void registerPermanentNode(String networkUUID, String... params) {
		// get network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not registered, network %s not found", Arrays.toString(params), networkUUID);
			return;
		}

		List<String> listparams = new ArrayList<>(Arrays.asList(params));
		String connector = Util.removeStringFromList(listparams, "connector=");
		network.add(POPNetworkDescriptor.from(connector).createNode(listparams));
		writeConfigurationFile();
		LogWriter.writeDebugInfo("[JM] Node %s added to %s", Arrays.toString(params), network);
	}
	
	/**
	 * Register a new node and add its certificate to the Key Store
	 * @param networkUUID the network id
	 * @param certificate the certificate associated with the node
	 * @param params the parameters needed to create a  new node, via {@link POPNode#getCreationParams()}
	 */
	@POPSyncConc(localhost = true)
	public void registerPermanentNode(String networkUUID, byte[] certificate, String... params) {
		// get network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not registered, network %s not found", Arrays.toString(params), networkUUID);
			return;
		}

		List<String> listparams = new ArrayList<>(Arrays.asList(params));
		String connector = Util.removeStringFromList(listparams, "connector=");
		POPNode node = POPNetworkDescriptor.from(connector).createNode(listparams);
		
		try {
			SSLUtils.addConfidenceLink(node, SSLUtils.certificateFromBytes(certificate), networkUUID);
		} catch(Exception e) {
			throw new POPException(20, "Job Manager couldn't add certificate to Key Store");
		}
		
		network.add(node);
		writeConfigurationFile();
		LogWriter.writeDebugInfo("[JM] Node %s added to %s", Arrays.toString(params), network);
	}
	
	/**
	 * Unregister a node and write it in the configuration file
	 * @param networkUUID the network id
	 * @param params the parameters needed to create a  new node, via {@link POPNode#getCreationParams()}
	 */
	@POPSyncConc(localhost = true)
	public void unregisterPermanentNode(String networkUUID, String... params) {
		// get network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			LogWriter.writeDebugInfo("[JM] Node %s not removed, network not found", Arrays.toString(params));
			return;
		}

		List<String> listparams = new ArrayList<>(Arrays.asList(params));
		String connector = Util.removeStringFromList(listparams, "connector=");
		POPNode node = POPNetworkDescriptor.from(connector).createNode(listparams);
		
		try {
			SSLUtils.removeConfidenceLink(node, networkUUID);
		} catch(IOException e) {
		}
		
		network.remove(node);
		LogWriter.writeDebugInfo("[JM] Node %s removed", Arrays.toString(params));
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
						Interface obj = new Interface(null, job.getContact());
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
	 * @return the current available resources
	 */
	@POPSyncConc(localhost = true)
	public Resource getInitialAvailableResources() {
		return new Resource(total);
	}

	/**
	 * The upper limit for each job
	 * 
	 * @return a single job's available resource
	 */
	@POPSyncConc(localhost = true)
	public Resource getJobResourcesLimit() {
		return new Resource(jobLimit);
	}

	/**
	 * The maximum number of simultaneous object available on the JM machine
	 * 
	 * @return the maximum number of resources available on the machine
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
	 * @param popAppId the application id
	 * @param initiator who started the application end request
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
			// create a new one if missing
			if (!configurationFile.exists()) {
				configurationFile.createNewFile();
			}
			
			YamlJobManager yamlJobManager = new YamlJobManager();
			yamlJobManager.setDefaultNetwork(defaultNetwork);
			yamlJobManager.setJobLimit(maxJobs);
			yamlJobManager.setMachineResources(total.toYamlResource());
			yamlJobManager.setJobResources(jobLimit.toYamlResource());
			
			List<YamlNetwork> yamlNetworks = new ArrayList<>(networks.size());
			yamlJobManager.setNetworks(yamlNetworks);
			for (POPNetwork network : networks.values()) {
				yamlNetworks.add(network.toYamlResource());
			}
			
			Representer representer = new Representer();
			representer.setPropertyUtils(new PropertyReverser());
			Yaml yaml = new Yaml(representer);
			String output = yaml.dumpAs(yamlJobManager, Tag.MAP, DumperOptions.FlowStyle.AUTO);

			// write updated configuration file
			try (FileOutputStream fos = new FileOutputStream(configurationFile)) {
				fos.write(output.getBytes(StandardCharsets.UTF_8));
			}
		} catch(IOException e) {
			LogWriter.writeDebugInfo("[JM] Failed to write current configuration to disk");
		}
	}
	
	/**
	 * Dead AppResources are handled here, cleaning them up.
	 */
	@POPAsyncSeq
	protected void cleanup() {
		for (Iterator<AppResource> iterator = cleanupJobs.iterator(); iterator.hasNext();) {
			AppResource job = iterator.next();
			Path appDirectory = job.getAppDirectory();
			if (appDirectory == null) {
				continue;
			}
			if (Files.exists(appDirectory) && appDirectory.toFile().list().length == 0) {
				SystemUtil.rmdir(appDirectory, conf.getJobmanagerExecutionUser());
				if (!Files.exists(appDirectory)) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Change configuration file location.
	 * This method will only change the location and try to write in it, it will not delete the old file.
	 * This method is not meant to be used to load a new configuration file.
	 * 
	 * @param configurationFile the job manager configuration file
	 */
	@POPAsyncConc(localhost = true)
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = new File(configurationFile);
		writeConfigurationFile();
	}	
	
	/**
	 * The available networks in the current Job Manager
	 * 
	 * @return all the available locally on this machine (job manager)
	 */
	@POPSyncConc(localhost = true)
	public POPNetworkDetails[] getAvailableNetworks() {
		List<POPNetwork> nets = new ArrayList<>(networks.values());
		int size = nets.size();
		POPNetworkDetails[] netsDetails = new POPNetworkDetails[size];
		int i = 0;
		for (POPNetwork net : nets) {
			netsDetails[i++] = new POPNetworkDetails(net);
		}
		return netsDetails;
	}
	
	/**
	 * All the nodes in a network
	 * 
	 * @param networkUUID the network id
	 * @return the parameters needed to create multiple new nodes, via {@link POPNode#getCreationParams()}
	 */
	@POPSyncConc(localhost = true)
	public String[][] getNetworkNodes(String networkUUID) {
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			return new String[0][0];
		}
		
		String[][] nodes = new String[network.size()][];
		int i = 0;
		for (POPConnector connector : network.getConnectors()) {
			for (POPNode node : network.getMembers(connector.getDescriptor())) {
				nodes[i++] = node.getCreationParams();
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
	 * @throws Throwable anything
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
	 * @param networkUUID The network that will contain the object
	 * @param objectName The class of the object
	 * @param accessPoint Where to find the object on the machine
	 * @param secret A secret to remove the object
	 * @return true if we could add the object successfully
	 */
	@POPSyncConc(localhost = true)
	public boolean registerTFCObject(String networkUUID, String objectName, POPAccessPoint accessPoint, String secret) {
		// get registerer network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			return false;
		}
		
		// get network's TFC connector to add the object or add a local one
		POPConnectorTFC tfc = network.getConnector(POPNetworkDescriptor.from("tfc"));
		if (tfc == null) {
			// add a local node, this will also add the connector
			AccessPoint myself = getAccessPoint().get(0);
			String protocol = myself.getProtocol();
			int port = myself.getPort();
			POPNode newNode = new POPNodeTFC("localhost", port, protocol);
			this.registerNode(networkUUID, newNode.getCreationParams());
			writeConfigurationFile();
			
			tfc = network.getConnector(POPNetworkDescriptor.from("tfc"));
		}
		
		// create resource
		TFCResource resource = new TFCResource(objectName, accessPoint, secret);
		// register resource with the connector
		return tfc.registerObject(resource);
	}
	
	/**
	 * Register a new object that will be available for connection over TFC
	 * @param networkUUID The network that will contain the object
	 * @param objectName The class of the object
	 * @param accessPoint Where to find the object on the machine
	 * @param secret A secret to remove the object
	 */
	@POPSyncConc(localhost = true)
	public void unregisterTFCObject(String networkUUID, String objectName, POPAccessPoint accessPoint, String secret) {
		// get registerer network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			return;
		}
		
		// get network's TFC connector to add the object
		POPConnectorTFC tfc = network.getConnector(POPNetworkDescriptor.from("tfc"));
		if (tfc == null) {
			return;
		}
		
		// create resource
		TFCResource resource = new TFCResource(objectName, accessPoint, secret);
		// unregister resource with the connector
		tfc.unregisterObject(resource);
	}
	
	/**
	 * Looks for live TFC objects in a POP Network on this node.
	 * @param networkUUID the network id
	 * @param objectName the kind of object we are looking for
	 * @return The POPAccessPoint(s) of lives TFC Objects registered on this Job Manager.
	 */
	@POPSyncConc
	public POPAccessPoint[] localTFCSearch(String networkUUID, String objectName) {
		POPAccessPoint[] aps = new POPAccessPoint[0];
		// get registerer network
		POPNetwork network = networks.get(networkUUID);
		if (network == null) {
			return aps;
		}
		
		// get network's TFC connector to add the object
		POPConnectorTFC tfc = network.getConnector(POPNetworkDescriptor.from("tfc"));
		if (tfc == null) {
			return aps;
		}
		
		// get remote certificate
		POPRemoteCaller remote = PopJava.getRemoteCaller();
		Certificate cert = null;
		if (remote.isSecure() && !remote.isUsingConfidenceLink()) {
			cert = SSLUtils.getCertificate(remote.getFingerprint());
		}
		
		// research in TFC Connector, only alive
		List<TFCResource> resources = tfc.getObjects(objectName, cert);
		if (resources == null || resources.isEmpty()) {
			return aps;
		}
		
		// create result to send
		aps = new POPAccessPoint[resources.size()];
		for (int i = 0; i < aps.length; i++) {
			aps[i] = resources.get(i).getAccessPoint();
		}
		
		return aps;
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
	 * @param request The request generated by {@link POPConnector#createObject}
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
			LogWriter.writeExceptionLog(e);
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
			POPNetwork network = networks.get(request.getNetworkUUID());

			// do nothing if we don't have the network
			if (network == null) {
				LogWriter.writeDebugInfo("[JM] Network [%s] not found", request.getNetworkUUID());
				return;
			}

			// decrease the hop we can still do
			if (request.getRemainingHops() != conf.getSearchNodeUnlimitedHops()) {
				request.decreaseHopLimit();
			}
			
			// connector we are using
			POPNetworkDescriptor descriptor = null;
			try {
				descriptor = POPNetworkDescriptor.from(request.getConnector());
			} catch(IllegalArgumentException e) {
				LogWriter.writeDebugInfo("[JM] Connector descriptor [%s] not found", request.getConnector());
				return;
			}
			POPConnector connectorImpl = network.getConnector(descriptor);
			
			// connector won't work with the SearchNode
			if (!(connectorImpl instanceof POPConnectorSearchNodeInterface)) {
				LogWriter.writeDebugInfo("[JM] Connector [%s] is not Job Manager enabed", request.getConnector());
				return;
			}
			POPConnectorSearchNodeInterface snEnableConnector = (POPConnectorSearchNodeInterface) connectorImpl;
			
			// add all network neighbors to explorations list
			for (POPNode node : network.getMembers(connectorImpl.getDescriptor())) {
				// only JM items and children
				if (node instanceof POPNodeAJobManager) {
					POPNodeAJobManager jmNode = (POPNodeAJobManager) node;
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
					for (POPNode node : network.getMembers(connectorImpl.getDescriptor())) {
						// only JM items and children
						if (node instanceof POPNodeJobManager) {
							POPNodeAJobManager jmNode = (POPNodeAJobManager) node;

							// contact if it has not been contacted before by someone else
							if (!oldExplorationList.contains(jmNode.getJobManagerAccessPoint())) {
								try {
									// send request to other JM
									POPJavaJobManager jm = connectToJobmanager(jmNode.getJobManagerAccessPoint(), request.getNetworkUUID());
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
			
			// check for host if they are specified
			boolean answer = request.getHosts().length == 0;
			if (!answer) {
				InetAddress myself = InetAddress.getByName(POPSystem.getHostIP());
				for (String host : request.getHosts()) {
					InetAddress addr = InetAddress.getByName(host);
					if (myself.equals(addr)) {
						answer = true;
						break;
					}
				}
			}
			
			if (answer) {
				LogWriter.writeDebugInfo("[PSN] Looking for local answer");
				// send request, handled by the different connectors
				snEnableConnector.askResourcesDiscoveryAction(request, sender, oldExplorationList);
			} else {
				LogWriter.writeDebugInfo("[PSN] Node not in request answer list, skipping and propagating request");
			}

			// propagate in the network if we still can
			if (request.getRemainingHops() >= 0 || request.getRemainingHops() == conf.getSearchNodeUnlimitedHops()) {
				// add current node do wayback
				request.getWayback().push(getAccessPoint());
				// request to all members of the network
				for (POPNode node : network.getMembers(connectorImpl.getDescriptor())) {
					if (node instanceof POPNodeAJobManager) {
						POPNodeAJobManager jmNode = (POPNodeAJobManager) node;
						// contact if it's a new node
						if (!oldExplorationList.contains(jmNode.getJobManagerAccessPoint())) {
							try {
								// send request to other JM
								POPJavaJobManager jm = connectToJobmanager(jmNode.getJobManagerAccessPoint(), request.getNetworkUUID());
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
			LogWriter.writeExceptionLog(e);
		}
	}

	/**
	 * Add a node to the response list
	 * 
	 * @param response the response from the remote node
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
			LogWriter.writeExceptionLog(e);
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
				POPJavaJobManager njm =connectToJobmanager(jm, response.getNetworkUUID());
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
			LogWriter.writeExceptionLog(e);
		}
	}

	/**
	 * Release the dispatching thread if necessary
	 * 
	 * @param requid the request id to unlock a potential semaphore
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
	
	public synchronized POPJavaJobManager connectToJobmanager(POPAccessPoint ap, String network) {
		Tuple<String, POPAccessPoint> key = new Tuple<String, POPAccessPoint>(network, ap);
		
		if(!cachedJobManangers.containsKey(key)) {
			System.out.println("######No JM found for "+ap+" # "+network);
			
			for(Tuple<String, POPAccessPoint> tmpKey : cachedJobManangers.keySet()) {
				System.out.println("#####Cached JM : "+tmpKey.a+" "+tmpKey.b);
			}			
			
			POPJavaJobManager jm = PopJava.connect(this, POPJavaJobManager.class, network, ap);
			
			cachedJobManangers.put(key, jm);
		}else {
			System.out.println("######Reuse JM "+ap+" # "+network);
		}
		
		POPJavaJobManager jm = cachedJobManangers.get(key);
		
		try {
			POPString val = new POPString();
			jm.query("power", val);
			
			jm.registerNeighbourJobmanager(getAccessPoint(), network, this);
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[NodeJM] Connection lost with [%s], opening new one");
			jm = PopJava.connect(this, POPJavaJobManager.class, network, ap);
		
			cachedJobManangers.put(key, jm);
		}
		
		return jm;
	}
	
	@POPSyncConc
	public void registerNeighbourJobmanager(POPAccessPoint ap, String network, POPJavaJobManager jm) {
		Tuple<String, POPAccessPoint> key = new Tuple<String, POPAccessPoint>(network, ap);
				
		if(!cachedJobManangers.containsKey(key)) {
			System.out.println("######Register new neighbour JM "+ap+" # "+network);
			jm.makePermanent();
			cachedJobManangers.put(key, jm);
		}else {
			System.out.println("######We already know about JM "+ap+" # "+network);
		}
	}
}
