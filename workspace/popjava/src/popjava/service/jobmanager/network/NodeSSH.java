package popjava.service.jobmanager.network;

import java.util.Objects;

/**
 * A SSH node for direct IP connections
 * XXX: port is not used ATM it will always default to port 22
 * @author Davide Mazzoleni
 */
public class NodeSSH extends NetworkNode {
	
	private String host;
	private int port;
	private boolean daemon;
	private String daemonSecret;
	private boolean initialized = true;
	
	NodeSSH(String[] params) {
		if (params.length == 3 && params[1].equals("daemon")) {
			daemon = true;
			daemonSecret = params[2];
		}
		
		// single string, can be <host>[:<port>] [<deamon> <secret>]
		if (params.length <= 3) {
			String[] ip = params[0].split(":");
			host = ip[0];
			port = 22;
			
			// simple ip or host
			if (ip.length > 1) {
				try {
					port = Integer.parseInt(ip[1]);
				} catch (NumberFormatException e) {	}
			}
		}
		
		else {
			// fail
			initialized = false;
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public boolean isDaemon() {
		return daemon;
	}

	public String getDaemonSecret() {
		return daemonSecret;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + Objects.hashCode(this.host);
		hash = 59 * hash + this.port;
		hash = 59 * hash + (this.daemon ? 1 : 0);
		hash = 59 * hash + Objects.hashCode(this.daemonSecret);
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
		final NodeSSH other = (NodeSSH) obj;
		if (this.port != other.port) {
			return false;
		}
		if (this.daemon != other.daemon) {
			return false;
		}
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		if (!Objects.equals(this.daemonSecret, other.daemonSecret)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s:%d %s %s", host, port, 
				daemon ? "daemon" : "", 
				daemonSecret == null ? "" : daemonSecret);
	}
}
