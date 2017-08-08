package popjava.util;

import java.net.InetAddress;

/**
 * Information on the remote caller available to a method
 * 
 * @author Davide Mazzoleni
 */
public class POPRemoteCaller {
	
	private InetAddress remote;
	private String protocol;
	
	private String method;
	
	private String fingerprint;
	private String network;

	public POPRemoteCaller() {
	}

	public POPRemoteCaller(POPRemoteCaller o) {
		this.remote = o.remote;
		this.protocol = o.protocol;
		this.network = o.network;
		this.method = o.method;
		this.fingerprint = o.fingerprint;
	}
	
	public POPRemoteCaller(InetAddress remote, String protocol, String network) {
		this.remote = remote;
		this.protocol = protocol;
		this.network = network;
	}

	public InetAddress getRemote() {
		return remote;
	}

	public void setRemote(InetAddress remote) {
		this.remote = remote;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	@Override
	public String toString() {
		return String.format("[%s] %s", remote.getHostAddress(), method);
	}
}
