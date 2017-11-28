package popjava;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.baseobject.POPTracking;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.service.jobmanager.network.POPNetworkDescriptor;
import popjava.service.jobmanager.network.POPNode;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.POPRemoteCaller;
import popjava.util.Util;

/**
 * Accounting API for POP Objects.
 * 
 * @author Davide Mazzoleni
 */
public class POPAccounting {
	
	/**
	 * Cast an object to a POP one, throw an IllegalArgumentException if it isn't one.
	 * 
	 * @param obj
	 * @return 
	 */
	private static POPObject cast(Object obj) {
		if (obj instanceof POPObject) {
			return (POPObject) obj;
		}
		throw new IllegalArgumentException("Given object is not a POPObject.");
	}
	
	/**
	 * Check if a POP Object has tracking and accounting capabilities enabled.
	 * 
	 * @param popObject The object we want to check.
	 * @return 
	 */
	public static boolean isEnabledFor(Object popObject) {
		POPObject obj = cast(popObject);
		return isEnabledFor(obj);
	}
	
	/**
	 * For internal calls, skip casting.
	 * 
	 * @param obj The object we want information about.
	 * @return 
	 * @throws IllegalStateException 
	 */
	private static boolean isEnabledFor(POPObject obj) {
		return obj.isTracking();
	}
	
	/**
	 * Return to the owner of the object (the same machine where the object reside) information on who contacted it.
	 * 
	 * @param popObject The object we want information about.
	 * @return 
	 * @throws IllegalStateException 
	 */
	public static POPRemoteCaller[] getUsers(Object popObject) {
		POPObject obj = cast(popObject);
		if (!isEnabledFor(obj)) {
			throw new IllegalStateException("Tracking is not enabled on this POP Object.");
		}
		return obj.getTrackedUsers();
	}
	
	/**
	 * Get information on a specific caller.
	 * 
	 * @param popObject The object we want information about.
	 * @param caller One of the caller return by {@link #getUsers(java.lang.Object) }
	 * @return A tracking object or null.
	 * @throws IllegalStateException 
	 */
	public static POPTracking getInformation(Object popObject, POPRemoteCaller caller) {
		POPObject obj = cast(popObject);
		if (!isEnabledFor(obj)) {
			throw new IllegalStateException("Tracking is not enabled on this POP Object.");
		}
		return obj.getTracked(caller);
	}
	
	/**
	 * Get information on a specific what was tracked about me.
	 * 
	 * @param popObject The object we want information about.
	 * @return 
	 * @throws IllegalStateException 
	 */
	public static POPTracking getMyInformation(Object popObject) {
		POPObject obj = cast(popObject);
		if (!isEnabledFor(obj)) {
			throw new IllegalStateException("Tracking is not enabled on this POP Object.");
		}
		return obj.getTracked();
	}
	
	/**
	 * With the help of the Job Manager we try to identify who is the node calling us.
	 * 
	 * @param caller One of the caller return by {@link #getUsers(java.lang.Object) }
	 * @return 
	 */
	public static POPNode identifyUser(POPRemoteCaller caller) {
		Configuration conf = Configuration.getInstance();
		String protocol = conf.getJobManagerProtocols()[0];
		int port = conf.getJobManagerPorts()[0];
		String accessString = String.format("%s://%s:%d", protocol, POPSystem.getHostIP(), port);
		POPAccessPoint jma = new POPAccessPoint(accessString);
		POPJavaJobManager jobManager = PopJava.connect(POPJavaJobManager.class, conf.getDefaultNetwork(), jma);
		
		String[][] nodesStrings = jobManager.getNetworkNodes(caller.getNetwork());
		List<POPNode> nodes = new ArrayList<>();
		for (int i = 0; i < nodesStrings.length; i++) {
			List<String> nodeParams = new ArrayList<>(Arrays.asList(nodesStrings[i]));
			String connector = Util.removeStringFromList(nodeParams, "connector=");
			POPNetworkDescriptor descriptor = POPNetworkDescriptor.from(connector);
			if (descriptor != null) {
				nodes.add(descriptor.createNode(nodeParams));
			}
		}
		jobManager.exit();
		return nodes.stream()
			.filter(node -> {
				try {
					InetAddress addr = InetAddress.getByName(node.getHost());
					return addr.equals(caller.getRemote());
				} catch (UnknownHostException e) {
					return false;
				}
			})
			.findFirst().get();
	}
}
