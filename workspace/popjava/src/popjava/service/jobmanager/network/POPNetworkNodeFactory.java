package popjava.service.jobmanager.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import popjava.service.jobmanager.connector.POPConnector;
import popjava.util.Configuration;
import popjava.util.LogWriter;
import popjava.util.Util;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPNetworkNodeFactory {
	
	private static final Configuration conf = Configuration.getInstance();
	
	public static POPNetworkNode makeNode(String... other) {
		return makeNode(new ArrayList<>(Arrays.asList(other)));
	}

	public static POPNetworkNode makeNode(List<String> other) {
		String connectorName = Util.removeStringFromList(other, "connector=");
		// use job manager if nothing is specified
		if (connectorName == null) {
			connectorName = conf.getJobManagerDefaultConnector();
		}

		try {
			POPConnector.Name connector = POPConnector.Name.from(connectorName);
			switch (connector) {
				case JOBMANAGER: return new NodeJobManager(other);
				case DIRECT: return new NodeDirect(other);
				case TFC: return new NodeTFC(other);
			}
		} catch(IllegalArgumentException e) {
			LogWriter.writeDebugInfo("[Node Factory] unknown connector specified '%s'", connectorName);
		}
		
		return null;
	}
}
