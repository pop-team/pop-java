package popjava.baseobject;

import java.net.UnknownHostException;

import popjava.system.POPSystem;

/**
 * This class represent an access to a broker-side parallel object
 */
public class AccessPoint {
    
	public static final String SOCKET_PROTOCOL = "socket";
	public static final String WEBSERVICE_PROTOCOL = "webservice";
	public static final String HTTP_PROTOCOL = "http";
	public static final int DEFAULT_PORT = 12008;
	public static final String DEFAULT_HOST = "localhost";
	
	protected String protocol;
	protected int port;
	protected String host;

	/**
	 * Create a new AccessPoint
	 */
	public AccessPoint() {
		host = DEFAULT_HOST;
		port = DEFAULT_PORT;
	}

	/**
	 * Create new access point with given values
	 * @param protocol	Protocol of the access point
	 * @param host		Host of the access point
	 * @param port		Port on which the broker is listening to
	 */
	public AccessPoint(String protocol, String host, int port) {
		this.host = normalizeHostname(host);
		this.protocol = protocol;
		this.port = port;
	}

	private String normalizeHostname(String hostname){
		String host = null;
		try {
			host = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return hostname;
		}
		
		if(host != null && hostname.equals(host)){
			return POPSystem.getHostIP();
		}
		
		return hostname;
	}
	
	/**
	 * Create an access point from a formatted string
	 * @param accessString	Formatted access string
	 * @return the new access point created from the string
	 */
	public static AccessPoint create(String accessString) {	    
		accessString = accessString.trim();
		String protocol = "";
		String host = "";
		int port = 0;
		String[] args = accessString.split("://|:");
		String[] datas = new String[3];
		int n = 0;
		for (int i = 0; i < args.length; i++) {
			String tempString = args[i].trim();
			if (tempString.length() > 0){
				datas[n++] = tempString;
			}
		}
		
		if (n == 3) {
			protocol = datas[0].trim();
			host = datas[1].trim();
			port = Integer.parseInt(datas[2]);
		} else if (n == 2) {
		    if(args[0].trim().equalsIgnoreCase("socket")){
		        protocol = SOCKET_PROTOCOL;
	            host = datas[0].trim();
	            port = Integer.parseInt(datas[1]);
		    }else{
		        return null;
		    }
			
		} else {
			protocol = SOCKET_PROTOCOL;
			host = DEFAULT_HOST;
			port = DEFAULT_PORT;
		}
		
		if (protocol.length() > 0 && host.length() > 0){
			return new AccessPoint(protocol, host, port);
		}else{
			return null;
		}
	}

	/**
	 * Get the protocol of this access point
	 * @return protocol as a string value
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Get the host of this access point
	 * @return host as a string value
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Get the port of this access point
	 * @return port as an int value
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the port for this access point
	 * @param port The port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Set the protocol for this access point
	 * @param protocol The protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Set the host form this access point
	 * @param host The host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Check is the access point is empty
	 * @return true if the access point is not set
	 */
	public boolean isEmpty() {
		if (host == null || host.length() == 0)
			return true;
		if (port <= 0)
			return true;
		return false;
	}

	/**
	 * Format the access point as a string value
	 */
	@Override
    public String toString() {
		return String.format("%s://%s:%d", protocol, host, port);
	}
}
