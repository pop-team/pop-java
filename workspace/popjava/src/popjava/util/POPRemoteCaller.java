package popjava.util;

import java.net.InetAddress;
import popjava.baseobject.ConnectionProtocol;
import popjava.combox.ssl.POPTrustManager;

/**
 * Information on the remote caller available to a method
 * 
 * @author Davide Mazzoleni
 */
public class POPRemoteCaller {
	
	private final InetAddress remote;
	private final ConnectionProtocol protocol;
	
	private final String fingerprint;
	private final String network;

	public POPRemoteCaller(InetAddress remote, ConnectionProtocol protocol, String fingerprint, String network) {
		this.remote = remote;
		this.protocol = protocol;
		this.fingerprint = fingerprint;
		this.network = network;
	}

	/**
	 * The address the connection is coming from
	 * 
	 * @return 
	 */
	public InetAddress getRemote() {
		return remote;
	}

	/**
	 * The protocol used for this connection
	 * 
	 * @return 
	 */
	public ConnectionProtocol getProtocol() {
		return protocol;
	}

	/**
	 * The fingerprint of the certificate used by the client, if available
	 * 
	 * @return 
	 */
	public String getFingerprint() {
		return fingerprint;
	}

	/**
	 * The network assigned to a certificate, if available
	 * 
	 * @return 
	 */
	public String getNetwork() {
		return network;
	}
	
	/**
	 * `true' if the connection was created using a confidence link
	 * 
	 * @return 
	 */
	public boolean isUsingConfidenceLink() {
		return POPTrustManager.getInstance().isConfidenceLink(fingerprint);
	}
	
	/**
	 * If the call come from a localhost object
	 * 
	 * @return 
	 */
	public boolean isLocalHost() {
		return Util.isLocal(remote.getHostAddress());
	}
}
