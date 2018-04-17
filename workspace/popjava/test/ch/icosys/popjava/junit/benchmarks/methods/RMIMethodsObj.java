package ch.icosys.popjava.junit.benchmarks.methods;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;

public class RMIMethodsObj extends UnicastRemoteObject implements RMIInterface {

	public static final String RMI_NAME = "RMIMethods";

	public static final int RMI_PORT = 9883;

	protected RMIMethodsObj() throws RemoteException {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5741287037705803423L;

	public void noParamNoReturn() {

	}

	private static String[] complexReturn = new String[] { "asdfasdf", "asdfasdf", "asdfasdf" };

	public String[] noParamComplex() {
		return complexReturn;
	}

	@Override
	public int noParamSimple() throws IOException {
		return 100;
	}

	public void simpleParam(int param) {

	}

	public void complexParam(String[] param) {

	}

	private Registry rmiRegistry;

	public void publish() throws RemoteException {

		System.getProperties().setProperty("sun.rmi.transport.connectionTimeout", "10000");
		System.getProperties().setProperty("sun.rmi.transport.tcp.readTimeout", "10000");
		boolean setIp = false;
		try {
			if (InetAddress.getLocalHost().getHostAddress().startsWith("127")) {
				setIp = true;
			}
		} catch (UnknownHostException e1) {
			setIp = true;
		}
		// Workaround for ubuntu and its 127.0.1.1 localhost address
		if (setIp) {
			System.getProperties().setProperty("java.rmi.server.hostname", getLocalIp());
		}

		if (rmiRegistry == null) {
			rmiRegistry = LocateRegistry.createRegistry(RMI_PORT);
		}

		try {
			rmiRegistry.unbind(RMI_NAME);
		} catch (NotBoundException e) {
		} catch (RemoteException e) {
		}

		rmiRegistry.rebind(RMI_NAME, this);
	}

	public void depublish() throws AccessException, RemoteException, NotBoundException {
		if (rmiRegistry != null) {
			rmiRegistry.unbind(RMI_NAME);
		}
	}

	public static RMIInterface getRemote() throws MalformedURLException, RemoteException, NotBoundException {
		String name = "rmi://" + getLocalIp() + ":" + RMI_PORT + "/" + RMI_NAME;
		Remote remote = Naming.lookup(name);
		return (RMIInterface) remote;
	}

	public static String getLocalIp() {
		String ip = "127.0.0.1";
		Enumeration<NetworkInterface> netInterfaces;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return ip;
		}

		while (netInterfaces.hasMoreElements()) {
			NetworkInterface ni = netInterfaces.nextElement();
			try {
				if (ni.isUp()) {
					Enumeration<InetAddress> addresses = ni.getInetAddresses();

					while (addresses.hasMoreElements()) {
						InetAddress addr = addresses.nextElement();
						if (!addr.getHostAddress().startsWith("127.") && !addr.getHostAddress().contains(":")) {
							return addr.getHostAddress();
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}

		}
		return ip;
	}

}
