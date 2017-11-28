package popjava.service.jobmanager.network;

import java.util.HashMap;
import java.util.Map;
import popjava.util.Configuration;

/**
 * Find and register customs POP Network Descriptor (factories).
 * 
 * @author Davide Mazzoleni
 */
public class POPNetworkDescriptorFinder {
	
	private static POPNetworkDescriptorFinder instance;
	
	private final Map<String, POPNetworkDescriptor> descriptors = new HashMap<>();

	private POPNetworkDescriptorFinder() {
		POPNetworkDescriptor jm = POPConnectorJobManager.DESCRIPTOR;
		POPNetworkDescriptor direct = POPConnectorDirect.DESCRIPTOR;
		POPNetworkDescriptor tfc = POPConnectorTFC.DESCRIPTOR;
		
		descriptors.put(jm.getGlobalName(), jm);
		descriptors.put(direct.getGlobalName(), direct);
		descriptors.put(tfc.getGlobalName(), tfc);
		
		loadCustomDescriptors();
	}

	private void loadCustomDescriptors() {
		// TODO from some file read and add class
	}
	
	/**
	 * Add a new descriptor description
	 * 
	 * @param descriptor 
	 */
	public void register(POPNetworkDescriptor descriptor) {
		if (!descriptors.containsKey(descriptor.getGlobalName())) {
			descriptors.put(descriptor.getGlobalName(), descriptor);
		}
	}

	public static POPNetworkDescriptorFinder getInstance() {
		if (instance == null) {
			instance = new POPNetworkDescriptorFinder();
		}
		return instance;
	}
	
	/**
	 * Find a descriptor based on its global name.
	 * 
	 * @param globalName
	 * @return 
	 */
	public POPNetworkDescriptor find(String globalName) {
		if (globalName == null || globalName.isEmpty()) {
			globalName = Configuration.getInstance().getJobManagerDefaultConnector();
		}
		return descriptors.get(globalName);
	}
	
	/**
	 * All available descriptors
	 * @return 
	 */
	public POPNetworkDescriptor[] all() {
		return descriptors.values().toArray(new POPNetworkDescriptor[descriptors.size()]);
	}
}
