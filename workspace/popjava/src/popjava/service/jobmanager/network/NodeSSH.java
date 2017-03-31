package popjava.service.jobmanager.network;

import java.util.Objects;

/**
 *
 * @author Davide Mazzoleni
 */
public class NodeSSH extends NetworkNode {

	private String host;
	private int port;
	private boolean initialized = true;
	
	NodeSSH(String[] params) {
		// single string, can be <host>:<port> or <ip>
		if (params.length == 1) {
			String[] ip = params[0].split(":");
			
			// simple ip or host
			if (ip.length == 0) {
				host = ip[0];
				port = 22;
			} 
			// port specified
			else {
				host = ip[0];
				try {
					port = Integer.parseInt(ip[1]);
				} catch (NumberFormatException e) {
					// fallback to 22
					port = 22;
				}
			}
		}
		
		// two strings <ip> <port>
		else if (params.length == 2) {
			host = params[0];
			try {
				port = Integer.parseInt(params[1]);
			} catch (NumberFormatException e) {
				// fallback to 22
				port = 22;
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

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.host);
		hash = 37 * hash + this.port;
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
		if (!Objects.equals(this.host, other.host)) {
			return false;
		}
		return true;
	}
}
