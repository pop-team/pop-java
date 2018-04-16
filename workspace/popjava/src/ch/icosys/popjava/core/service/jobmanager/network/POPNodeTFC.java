package ch.icosys.popjava.core.service.jobmanager.network;

import java.util.List;

/**
 * A TFC node
 * 
 * @author Davide Mazzoleni
 */
public class POPNodeTFC extends POPNodeAJobManager {

	public POPNodeTFC(String host, int port, String protocol) {
		super(POPConnectorTFC.DESCRIPTOR, host, port, protocol);
	}

	public POPNodeTFC(List<String> params) {
		super(POPConnectorTFC.DESCRIPTOR, params);
	}

}
