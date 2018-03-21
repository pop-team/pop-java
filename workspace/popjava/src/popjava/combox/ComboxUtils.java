package popjava.combox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Future;

import javax.net.ServerSocketFactory;
import popjava.system.POPSystem;
import popjava.util.Configuration;
import popjava.util.upnp.UPNPManager;

/**
 * Some utility method used by multiple Comboxes
 * @author Davide Mazzoleni
 */
public class ComboxUtils {
	
	private static final PreOperation EMPTY = (ServerSocket ss) -> {};
	private static final ServerSocketFactory SS_FACTORY = ServerSocketFactory.getDefault();
		
	/**
	 * Try to create a ServerSocket on the specified port.
	 * @param port if 0 the port will be choose randomly or sequentially from {@link Configuration#allocatePortRange}
	 * @param op an operation to perform on the server socket before its binding. ex: {@link ServerSocket#setReceiveBufferSize(int) }
	 * @return A server already binded
	 * @throws IOException If we specify a port but we can't bind the address
	 */
	public static ServerSocket createServerSocket(int port, PreOperation op, boolean upnp) throws IOException {
		if (op == null) {
			op = EMPTY;
		}
		return createServerSocket(port, op, port == 0, upnp);
	}
	
	/**
	 * Try to create a ServerSocket on the specified port.
	 * @param port if 0 the port will be choose randomly or sequentially from {@link Configuration#allocatePortRange}
	 * @param op an operation to perform on the server socket before its binding. ex: {@link ServerSocket#setReceiveBufferSize(int) }
	 * @param sequential Continue looking for new port if we fail to bind
	 * @return A server already binded
	 * @throws IOException If we specify a port but we can't bind the address
	 */
	private static ServerSocket createServerSocket(int port, PreOperation op, boolean sequential, boolean upnp) throws IOException {
		ServerSocket server = SS_FACTORY.createServerSocket();
		boolean working = false;
		if (port == 0) {
			port = Configuration.getInstance().getAllocatePortRange();
		}
		do {
			try {
				op.preBind(server);
				server.bind(new InetSocketAddress(port));
				working = true;
			} catch (IOException ex) {
				// close and dispose of old server, create new one
				server.close();
				server = new ServerSocket();
				// propagate the exception if we wanted a specific port
				if (!sequential) {
					server.close();
					throw ex;
				}
				// otherwise we continue sequentially
				port++;
				if (port > 65535) {
					// something really wrong here
					throw new IOException("[ComboxUtils] can't find an available port");
				}
			}
		} while (!working);
		
		if(upnp) {
			Future<String> externalIP = UPNPManager.registerPort(server.getLocalPort());
		}
		
		return server;
	}
	
	/**
	 * Should be used as parameter for {@link #createServerSocket(int, PreOperation)}  }
	 */
	public interface PreOperation {
		void preBind(ServerSocket ss) throws IOException;
	}
}
