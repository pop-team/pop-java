package popjava.service.jobmanager.network;

import java.util.List;
import java.util.Objects;

/**
 * Describe a connector and gives the means to create it and its nodes.
 * 
 * @author Davide Mazzoleni
 */
public final class POPNetworkDescriptor {
	
	/**
	 * Shorthand for {@link POPNetworkDescriptorFinder#find(java.lang.String) }
	 * 
	 * @param globalName
	 * @return 
	 */
	public static POPNetworkDescriptor from(String globalName) {
		// NOTE do not make this a static attribute, it may create a initialization loop
		return POPNetworkDescriptorFinder.getInstance().find(globalName);
	}
	
	private final String globalName;
	private final POPNetworkDescriptorMethod methods;

	public POPNetworkDescriptor(String globalName, POPNetworkDescriptorMethod methods) {
		this.globalName = globalName;
		this.methods = methods;
	}

	public String getGlobalName() {
		return globalName;
	}
	
	public POPConnector createConnector() {
		return methods.createConnector();
	}
	
	public POPNode createNode(List<String> params) {
		return methods.createNode(params);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 43 * hash + Objects.hashCode(this.globalName);
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
		final POPNetworkDescriptor other = (POPNetworkDescriptor) obj;
		if (!Objects.equals(this.globalName, other.globalName)) {
			return false;
		}
		return true;
	}
}