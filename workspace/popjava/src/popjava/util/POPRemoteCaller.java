package popjava.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.util.ssl.SSLUtils;

/**
 * Information on the remote caller available to a method
 * 
 * @author Davide Mazzoleni
 */
public class POPRemoteCaller implements IPOPBase {
	
	private InetAddress remote;
	private String protocol;
	private String network;
	private boolean secure;
	
	private String fingerprint;

	public POPRemoteCaller() {
	}

	public POPRemoteCaller(InetAddress remote, String protocol, String network, boolean secure, String fingerprint) {
		this.remote = remote;
		this.protocol = protocol;
		this.secure = secure;
		this.fingerprint = fingerprint;
		this.network = network;
	}

	public POPRemoteCaller(InetAddress remote, String protocol, String network, boolean secure) {
		this(remote, protocol, network, secure, null);
	}

	/**
	 * The address the connection is coming from
	 * 
	 * @return the address of the remote connection (IPv4 or IPv6)
	 */
	public InetAddress getRemote() {
		return remote;
	}

	/**
	 * The protocol used for this connection
	 * 
	 * @return the protocol used for this connection
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Is {@link #getProtocol() } secure or not.
	 * 
	 * @return if the connection is secure or not
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * The fingerprint of the certificate used by the client, if available
	 * 
	 * @return the fingerprint if we are using certificated, null otherwise
	 */
	public String getFingerprint() {
		return fingerprint;
	}

	/**
	 * The network assigned to a certificate, if available
	 * 
	 * @return the network we are working into, signaled by the client
	 */
	public String getNetwork() {
		return network;
	}
	
	/**
	 * `true' if the connection was created using a confidence link
	 * 
	 * @return if we are connected directly to one of our direct link
	 */
	public boolean isUsingConfidenceLink() {
		return SSLUtils.isConfidenceLink(fingerprint);
	}
	
	/**
	 * If the call come from a localhost object
	 * 
	 * @return true if the address is our own machine
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
		hash = 67 * hash + Objects.hashCode(this.network);
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

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putByteArray(remote.getAddress());
		buffer.putString(protocol);
		buffer.putBoolean(secure);
		boolean net = network != null;
		buffer.putBoolean(net);
		if (net) {
			buffer.putString(network);
		}
		boolean fig = fingerprint != null;
		buffer.putBoolean(fig);
		if (fig) {
			buffer.putString(fingerprint);
		}
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		try {
			int size = buffer.getInt();
			remote = InetAddress.getByAddress(buffer.getByteArray(size));
		} catch(UnknownHostException e) {
			LogWriter.writeDebugInfo("[POPRemoteCaller] can't decode received InetAddress");
		}
		protocol = buffer.getString();
		secure = buffer.getBoolean();
		if (buffer.getBoolean()) {
			network = buffer.getString();
		}
		if (buffer.getBoolean()) {
			fingerprint = buffer.getString();
		}
		return true;
	}
}
