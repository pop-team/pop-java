package popjava.util;

import java.net.InetAddress;
import java.util.Objects;
import popjava.combox.ssl.POPTrustManager;
import popjava.util.ssl.SSLUtils;

/**
 * Information on the remote caller available to a method
 * 
 * @author Davide Mazzoleni
 */
public class POPRemoteCaller {
	
	private final InetAddress remote;
	private final String protocol;
	private final boolean secure;
	
	private final String fingerprint;
	private final String network;

	public POPRemoteCaller(InetAddress remote, String protocol, boolean secure, String fingerprint, String network) {
		this.remote = remote;
		this.protocol = protocol;
		this.secure = secure;
		this.fingerprint = fingerprint;
		this.network = network;
	}

	public POPRemoteCaller(InetAddress remote, String protocol, boolean secure) {
		this(remote, protocol, secure, null, null);
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
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Is {@link #getProtocol() } secure or not.
	 * 
	 * @return 
	 */
	public boolean isSecure() {
		return secure;
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
		return SSLUtils.isConfidenceLink(fingerprint);
	}
	
	/**
	 * If the call come from a localhost object
	 * 
	 * @return 
	 */
	public boolean isLocalHost() {
		return Util.isLocal(remote.getHostAddress());
	}

	@Override
	public String toString() {
		return String.format("%s://%s [%s]", protocol, remote.getHostAddress(), network);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + Objects.hashCode(this.remote);
		hash = 67 * hash + Objects.hashCode(this.protocol);
		hash = 67 * hash + Objects.hashCode(this.fingerprint);
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
		final POPRemoteCaller other = (POPRemoteCaller) obj;
		if (!Objects.equals(this.protocol, other.protocol)) {
			return false;
		}
		if (!Objects.equals(this.fingerprint, other.fingerprint)) {
			return false;
		}
		if (!Objects.equals(this.network, other.network)) {
			return false;
		}
		if (!Objects.equals(this.remote, other.remote)) {
			return false;
		}
		return true;
	}
}
