package ch.icosys.popjava.core.util.upnp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import ch.icosys.popjava.core.util.LogWriter;

public class UPNPManager {

	private static final GatewayDiscover discover = new GatewayDiscover();

	private static String externalIP = "";

	private static GatewayDevice d = null;

	private static final Set<Integer> mappedPorts = Collections.synchronizedSet(new HashSet<Integer>());

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

	public synchronized static Future<String> registerPort(int port) {
		if (mappedPorts.contains(port)) {
			return CompletableFuture.completedFuture(externalIP);
		}

		Callable<String> mapper = new Callable<String>() {

			@Override
			public String call() throws Exception {
				init();

				if (null != d) {
					LogWriter.writeDebugInfo(
							"Found gateway device.\n" + d.getModelName() + " (" + d.getModelDescription() + ")");
				} else {
					LogWriter.writeDebugInfo("No valid gateway device found.");
					return "";
				}

				InetAddress localAddress = d.getLocalAddress();
				String externalIPAddress = "";
				try {
					externalIPAddress = d.getExternalIPAddress();

					LogWriter.writeDebugInfo("Internal IP " + localAddress);
					LogWriter.writeDebugInfo("External IP " + externalIPAddress);

					PortMappingEntry portMapping = new PortMappingEntry();

					if (d.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
						LogWriter.writeDebugInfo("Port " + port + " is already forwarded");
					} else {
						LogWriter.writeDebugInfo("Sending port mapping request");

						if (!d.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "POP-Java")) {
							LogWriter.writeDebugInfo("Port mapping attempt failed");
						} else {
							mappedPorts.add(port);
						}
					}
				} catch (SAXException e) {
					LogWriter.writeExceptionLog(e);
				} catch (IOException e) {
					LogWriter.writeExceptionLog(e);
				}

				return externalIP;
			}
		};

		FutureTask<String> task = new FutureTask<>(mapper);

		Thread upnpThread = new Thread(task);
		upnpThread.setDaemon(true);
		upnpThread.start();

		return task;
	}

	public static synchronized void close() {

		if (mappedPorts.size() > 0) {
			GatewayDevice d = discover.getValidGateway();

			for (int port : mappedPorts) {
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
