package popjava.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.POPJavaJobManager;
import popjava.util.Configuration;

/**
 *
 * @author Dosky
 */
@POPClass(isDistributable = false)
public class PopjServices {

	public static void main(String[] args) throws InterruptedException {

		Configuration conf = Configuration.getInstance();
		Integer port = null;
		String config = conf.getSystemJobManagerConfig().getAbsolutePath();

		List<String> argl = new ArrayList<>(Arrays.asList(args));
		for (Iterator<String> itr = argl.iterator(); itr.hasNext();) {
			String token = itr.next();

			switch (token) {
				case "-h":
				case "--help":
					System.err.println("usage: PopjServices [-p|--port <port num>] [-c|--config <jm config file>]");
					System.exit(1);
					break;
				case "-p":
				case "--port":
					String portString = itr.next();
					try {
						port = Integer.parseInt(portString);
					} catch (NumberFormatException e) {
					}
					break;
				case "-c":
				case "--config":
					config = itr.next();
					break;
			}
		}

		try {
			POPJavaJobManager jm;
			if (port != null) {
				jm = PopJava.newActive(POPJavaJobManager.class, "localhost:" + port, config);
			} else {
				String[] protocols = conf.getJobManagerProtocols();
				int[] ports = conf.getJobManagerPorts();
				for (int i = 0; i < protocols.length; i++) {
					protocols[i] = protocols[i] + ":" + ports[i];
				}
				jm = PopJava.newActive(POPJavaJobManager.class, "localhost", protocols);
			}
			POPAccessPoint jm_ap = PopJava.getAccessPoint(jm);
			System.out.println("[JM] " + jm_ap.toString());
			jm.start();
			jm.stayAlive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
