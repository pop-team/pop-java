package popjava.util;

import java.net.InetAddress;

/**
 * Information on the remote caller available to a method
 * 
 * @author Davide Mazzoleni
 */
public class POPRemoteCaller {
	
	private final InetAddress remote;
	private final String protocol;
	
	private final String fingerprint;
	private final String network;

	public POPRemoteCaller(InetAddress remote, String protocol, String fingerprint, String network) {
		this.remote = remote;
		this.protocol = protocol;
		this.fingerprint = fingerprint;
		this.network = network;
	}

	public InetAddress getRemote() {
		return remote;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public String getNetwork() {
		return network;
	}
}
