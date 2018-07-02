package ch.icosys.popjava.core.util.upnp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.util.LogWriter;
import ch.icosys.popjava.core.util.Tuple;

public class UPNPManager {

	private static final GatewayDiscover discover = new GatewayDiscover();

	private static String externalIP = "";

	private static GatewayDevice d = null;

	private static final Map<Integer, Integer> mappedPorts = Collections.synchronizedMap(new HashMap<>());
	private static final Map<Integer, Future<Tuple<String, Integer>>> mappingTasks = Collections.synchronizedMap(new HashMap<>());

	private static boolean inited = false;

	private synchronized static void init() {
		if (!inited) {
			try {
				discover.discover();
				d = discover.getValidGateway();

				if(d != null) {
					externalIP = d.getExternalIPAddress();
				}else {
					System.out.println("UPNP could not be initialized correctly, no gateway found");
				}				
				
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			inited = true;
		}
	}

	public synchronized static String getExternalIP() {
		init();

		return externalIP;
	}

	public synchronized static Future<Tuple<String, Integer>> registerPort(int port) {
		if (mappedPorts.containsKey(port)) {
			return CompletableFuture.completedFuture(new Tuple<String, Integer>(externalIP, mappedPorts.get(port)));
		}

		Callable<Tuple<String, Integer>> mapper = new Callable<Tuple<String, Integer>>() {

			@Override
			public Tuple<String, Integer> call() throws Exception {
				init();

				if (null != d) {
					LogWriter.writeDebugInfo(
							"Found gateway device.\n" + d.getModelName() + " (" + d.getModelDescription() + ")");
				} else {
					LogWriter.writeDebugInfo("No valid gateway device found.");
					return new Tuple<String, Integer>("", -1);
				}

				int newPort = port;
				
				InetAddress localAddress = d.getLocalAddress();
				String externalIPAddress = "";
				try {
					externalIPAddress = d.getExternalIPAddress();

					LogWriter.writeDebugInfo("Internal IP " + localAddress);
					LogWriter.writeDebugInfo("External IP " + externalIPAddress);

					PortMappingEntry portMapping = new PortMappingEntry();

					boolean directMapping = false;
					if (d.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
						if(portMapping.getInternalClient().equals(localAddress.getHostAddress())) {
							directMapping = true;
							LogWriter.writeDebugInfo("Port " + port + " is already forwarded to ourself");
						}else {
							LogWriter.writeDebugInfo("Port " + port + " is already forwarded to "+portMapping.getInternalClient());
							newPort = getFreeNATPort(localAddress, port);
							if(newPort < 0) {
								newPort *= -1;
								LogWriter.writeDebugInfo("Remap of " + port + " to "+newPort+" is already in place");
								directMapping = true;
							}else {
								LogWriter.writeDebugInfo("Remap " + port + " to "+newPort);
							}
						}
					}
					
					if(!directMapping) {
						LogWriter.writeDebugInfo("Sending port mapping request");

						if (!d.addPortMapping(newPort, port, localAddress.getHostAddress(), "TCP", "POP-Java")) {
							LogWriter.writeDebugInfo("Port mapping attempt failed");
							newPort = -1;
						}
					}
				} catch (SAXException e) {
					LogWriter.writeExceptionLog(e);
				} catch (IOException e) {
					LogWriter.writeExceptionLog(e);
				}
				
				if(newPort != -1) {
					mappedPorts.put(port, newPort);
				}
				
				mappingTasks.remove(port);
				
				return new Tuple<String, Integer>(externalIP, newPort);
			}
		};

		FutureTask<Tuple<String, Integer>> task = new FutureTask<>(mapper);

		Thread upnpThread = new Thread(task);
		upnpThread.setDaemon(true);
		upnpThread.start();
		
		mappingTasks.put(port, task);

		return task;
	}
	
	public static void mapAccessPoint(final AccessPoint ap) {
		Future<Tuple<String, Integer>> futurePort = mappingTasks.get(ap.getPort());
		
		if(mappedPorts.containsKey(ap.getPort())) {
			ap.setPort(mappedPorts.get(ap.getPort()));
		}
		
		if(futurePort != null) {
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						ap.setPort(futurePort.get(2, TimeUnit.SECONDS).getB());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					}
				}
			});
			thread.setDaemon(true);
			thread.start();
			
		}
	}
	
	private static int getFreeNATPort(InetAddress localAddress, int port) throws IOException, SAXException {

		int counter = 0;
		do {
			port++;
			PortMappingEntry portMapping = new PortMappingEntry();
			if (!d.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
				return port;
			}
			
			if(portMapping.getInternalClient().equals(localAddress.getHostAddress())) {
				return -port;
			}
			
			//Abort after 1000 ports
			if(counter++ > 1000) {
				return 0;
			}
		}while(true);
		
	}

	public static synchronized void close() {

		if (mappedPorts.size() > 0) {
			GatewayDevice d = discover.getValidGateway();

			for (int port : mappedPorts.keySet()) {
				try {
					d.deletePortMapping(port, "TCP");
				} catch (IOException e) {
					LogWriter.writeExceptionLog(e);
				} catch (SAXException e) {
					LogWriter.writeExceptionLog(e);
				}
			}
		}

	}

}
