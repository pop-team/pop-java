package ch.icosys.popjava.core.service.jobmanager.search;

import java.security.cert.Certificate;

import ch.icosys.popjava.core.buffer.POPBuffer;
import ch.icosys.popjava.core.dataswaper.IPOPBase;
import ch.icosys.popjava.core.util.ssl.SSLUtils;

/**
 * Response of a network request, create and send one for itself.
 *
 * @author Davide Mazzoleni
 */
public class SNResponse implements IPOPBase {

	private String uid;

	private SNExploration explorationList;

	private SNNodesInfo.Node nodeinfo;

	private String networkUUID;

	private byte[] publicCertificate = new byte[0];

	public SNResponse() {
	}

	public SNResponse(String uid, String networkUUID, SNExploration explorationList, SNNodesInfo.Node nodeinfo) {
		this.uid = uid;
		this.explorationList = explorationList;
		this.nodeinfo = nodeinfo;
		this.networkUUID = networkUUID;

		Certificate localPublicCertificate = SSLUtils.getCertificateFromAlias(networkUUID);
		if (localPublicCertificate != null) {
			publicCertificate = SSLUtils.certificateBytes(localPublicCertificate);
			nodeinfo.setCertificate(publicCertificate);
		}
	}

	public String getUID() {
		return uid;
	}

	public SNNodesInfo.Node getResultNode() {
		return nodeinfo;
	}

	public byte[] getPublicCertificate() {
		return publicCertificate;
	}

	public String getNetworkUUID() {
		return networkUUID;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(uid);
		buffer.putValue(explorationList, SNExploration.class);
		buffer.putValue(nodeinfo, SNNodesInfo.Node.class);
		buffer.putByteArray(publicCertificate);
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		uid = buffer.getString();
		explorationList = (SNExploration) buffer.getValue(SNExploration.class);
		nodeinfo = (SNNodesInfo.Node) buffer.getValue(SNNodesInfo.Node.class);
		int buffSize = buffer.getInt();
		publicCertificate = buffer.getByteArray(buffSize);
		return true;
	}
}
