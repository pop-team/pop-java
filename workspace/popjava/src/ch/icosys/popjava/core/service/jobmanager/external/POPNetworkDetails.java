package popjava.service.jobmanager.external;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Objects;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.service.jobmanager.network.POPNetwork;
import popjava.util.LogWriter;
import popjava.util.ssl.SSLUtils;

/**
 * Details on a network.
 * Global UUID, local friendly name, network 
 * 
 * @author Davide Mazzoleni
 */
public class POPNetworkDetails implements IPOPBase {

	private String uuid;
	private String friendlyName;
	private Certificate certificate;
	
	public POPNetworkDetails() {
	}
	
	public POPNetworkDetails(POPNetwork network) {
		this.uuid = network.getUUID();
		this.friendlyName = network.getFriendlyName();
		try {
			this.certificate = SSLUtils.getCertificateFromAlias(uuid);
		} catch (Exception e) {
			LogWriter.writeDebugInfo("[NetworkDetails] No certificate found for network [%s].", uuid);
		}
	}

	public String getUUID() {
		return uuid;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public Certificate getCertificate() {
		return certificate;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(uuid);
		buffer.putString(friendlyName);
		boolean hasCertificate = certificate != null;
		buffer.putBoolean(hasCertificate);
		if (hasCertificate) {
			buffer.putByteArray(SSLUtils.certificateBytes(certificate));
		}
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		uuid = buffer.getString();
		friendlyName = buffer.getString();
		boolean hasCertificate = buffer.getBoolean();
		if (hasCertificate) {
			int certLength = buffer.getInt();
			try {
				certificate = SSLUtils.certificateFromBytes(buffer.getByteArray(certLength));
			} catch(CertificateException e) {
			}
		}
 		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59 * hash + Objects.hashCode(this.uuid);
		hash = 59 * hash + Objects.hashCode(this.friendlyName);
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
		final POPNetworkDetails other = (POPNetworkDetails) obj;
		if (!Objects.equals(this.uuid, other.uuid)) {
			return false;
		}
		if (!Objects.equals(this.friendlyName, other.friendlyName)) {
			return false;
		}
		return true;
	}
	
}
