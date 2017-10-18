package popjava.service.jobmanager.network;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import popjava.PopJava;
import popjava.baseobject.POPAccessPoint;
import popjava.dataswaper.POPString;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.connector.POPConnector;
import popjava.util.Configuration;
import popjava.util.Util;

/**
 * Nodes using the Job Manager should extend this class instead
 * 
 * @author Davide Mazzoleni
 */
public abstract class AbstractNodeJobManager extends POPNetworkNode {

	protected POPAccessPoint jobManagerAccessPoint;
	protected POPJavaJobManager jm;
	protected int port;
	protected String protocol;
	protected boolean initialized = true;

	public AbstractNodeJobManager(POPConnector.Name name, String host, int port, String protocol) {
		super(name);
		this.host = host;
		this.port = port;
		this.protocol = protocol;
		
		init();
	}
	
	public AbstractNodeJobManager(POPConnector.Name name, List<String> params) {
		super(name);
		
		// get potential params
		host = Util.removeStringFromList(params, "host=");
		String portString = Util.removeStringFromList(params, "port=");
		protocol= Util.removeStringFromList(params, "protocol=");
		
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
		jobManagerAccessPoint = new POPAccessPoint(String.format("%s://%s:%d", protocol, host, port));

		Set<String> paramsSet = new HashSet<>();
		paramsSet.add("connector=" + name.getGlobalName());
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

	public final POPJavaJobManager getJobManager() {
		// create connection if it doesn't exists
		if (jm == null) {
			jm = PopJava.newActive(POPJavaJobManager.class, getJobManagerAccessPoint());
		}
		// test connection
		try {
			POPString val = new POPString();
			jm.query("power", val);
		} catch (Exception e) {
			jm = PopJava.newActive(POPJavaJobManager.class, getJobManagerAccessPoint());
		}
		return jm;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.host);
		hash = 67 * hash + Objects.hashCode(this.name);
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
		final AbstractNodeJobManager other = (AbstractNodeJobManager) obj;
		if (this.port != other.port) {
			return false;
		}
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		if (!Objects.equals(this.protocol, other.protocol)) {
			return false;
		}
		if (this.name != other.name) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("host=%s port=%s connector=%s protocol=%s", host, port, name.getGlobalName(), protocol);
	}
}
