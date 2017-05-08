package popjava.service.jobmanager.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.util.Configuration;
import popjava.util.LogWriter;

/**
 * Response of a network request, create and send one for itself.
 * @author Davide Mazzoleni
 */
public class SNResponse implements IPOPBase {
	
	private String uid;
	private SNExploration explorationList;
	private SNNodesInfo.Node nodeinfo;
	
	private byte[] publicCertificate = new byte[0];

	public SNResponse() {
	}

	public SNResponse(String uid, SNExploration explorationList, SNNodesInfo.Node nodeinfo) {
		this.uid = uid;
		this.explorationList = explorationList;
		this.nodeinfo = nodeinfo;
		
		File cert = new File(Configuration.PUBLIC_CERTIFICATE);
		if (cert.exists()) {
			try {
				publicCertificate = Files.readAllBytes(cert.toPath());
			} catch (IOException e) {
				LogWriter.writeDebugInfo("[SN] Could not extract certificate bytes");
			}
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
