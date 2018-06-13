package ch.icosys.popjava.core.service.jobmanager.network;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.dataswaper.POPString;
import ch.icosys.popjava.core.service.jobmanager.POPJavaJobManager;
import ch.icosys.popjava.core.system.POPSystem;
import ch.icosys.popjava.core.util.Configuration;
import ch.icosys.popjava.core.util.LogWriter;
import ch.icosys.popjava.core.util.Util;

/**
 * Nodes using the Job Manager should extend this class instead
 * 
 * @author Davide Mazzoleni
 */
public abstract class POPNodeAJobManager extends POPNode {

	protected POPAccessPoint jobManagerAccessPoint;

	protected POPJavaJobManager jm;

	protected int port;

	protected String protocol;

	protected boolean initialized = true;

	public POPNodeAJobManager(POPNetworkDescriptor descriptor, String host, int port, String protocol) {
		super(descriptor);
		this.host = host;
		this.port = port;
		this.protocol = protocol;

		init();
	}

	public POPNodeAJobManager(POPNetworkDescriptor descriptor, List<String> params) {
		super(descriptor);

		// get potential params
		host = Util.removeStringFromList(params, "host=");
		
		String portString = Util.removeStringFromList(params, "port=");
		protocol = Util.removeStringFromList(params, "protocol=");

		// stop if we have no host
		if (host == null) {
			initialized = false;
			return;
		}

		Configuration conf = Configuration.getInstance();
		if (protocol == null) {
			protocol = conf.getDefaultProtocol();
		}

		// some sane defaults
		port = conf.getJobManagerPorts()[0];
		if (portString != null) {
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				// we assume the initialization failed in this case
				initialized = false;
			}
		}

		init();
	}

	private void init() {
		// set access point
		jobManagerAccessPoint = new POPAccessPoint();

		jobManagerAccessPoint.addAccessPoint(new AccessPoint(protocol, host, port));
		
		Set<String> paramsSet = new HashSet<>();
		paramsSet.add("connector=" + descriptor.getGlobalName());
		paramsSet.add("host=" + host);
		paramsSet.add("port=" + port);
		paramsSet.add("protocol=" + protocol);
		creationParams = paramsSet.toArray(new String[paramsSet.size()]);
	}

	public final POPAccessPoint getJobManagerAccessPoint() {
		return jobManagerAccessPoint;
	}

	public final boolean isInitialized() {
		return initialized;
	}

	public final POPJavaJobManager getJobManager(POPJavaJobManager localJM, String networkUUID) {

		if (localJM != null) {
			try {
				return localJM.connectToJobmanager(getJobManagerAccessPoint(), networkUUID);
			}catch (InterruptedException e) {
			}
			return null;
		}

		// create connection if it doesn't exists
		if (jm == null) {
			jm = PopJava.connect(localJM, POPJavaJobManager.class, networkUUID, getJobManagerAccessPoint());
		}
		// test connection
		try {
			POPString val = new POPString();
			jm.query("power", val);
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[NodeJM] Connection lost with [%s], opening new one", getJobManagerAccessPoint());
			jm = PopJava.connect(localJM, POPJavaJobManager.class, networkUUID, getJobManagerAccessPoint());
		}
		return jm;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.host);
		hash = 67 * hash + Objects.hashCode(this.descriptor);
		hash = 67 * hash + this.port;
		hash = 67 * hash + Objects.hashCode(this.protocol);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final POPNodeAJobManager other = (POPNodeAJobManager) obj;
		if (this.port != other.port) {
			return false;
		}
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		if (!Objects.equals(this.protocol, other.protocol)) {
			return false;
		}
		if (this.descriptor != other.descriptor) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("host=%s port=%s connector=%s protocol=%s", host, port, descriptor.getGlobalName(),
				protocol);
	}
}
