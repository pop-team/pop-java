package popjava.service.jobmanager.external;

import java.util.Objects;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;
import popjava.service.jobmanager.network.POPNetwork;

/**
 *
 * @author dosky
 */
public class POPNetworkDetails implements IPOPBase {

	private String uuid;
	private String friendlyName;
	
	public POPNetworkDetails() {
	}
	
	public POPNetworkDetails(POPNetwork network) {
		this.uuid = network.getUUID();
		this.friendlyName = network.getFriendlyName();
	}

	public String getUUID() {
		return uuid;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putString(uuid);
		buffer.putString(friendlyName);
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		uuid = buffer.getString();
		friendlyName = buffer.getString();
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
