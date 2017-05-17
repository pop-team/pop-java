package popjava.scripts;

import popjava.PopJava;
import popjava.annotation.POPClass;
import popjava.baseobject.POPAccessPoint;
import popjava.service.jobmanager.POPJavaJobManager;


/**
 *
 * @author Dosky
 */
@POPClass(isDistributable = false)
public class PopjServices {
	public static void main(String[] args) throws InterruptedException {
		
		if (args.length < 1) {
			System.err.println("No config file supplied.\n");
			System.err.println("usage: PopjServices <jm cofig file>");
			System.exit(1);
		}
		
		try {
			final POPJavaJobManager jm = PopJava.newActive(POPJavaJobManager.class, "localhost:2711", args[0]);
			final POPAccessPoint jm_ap = PopJava.getAccessPoint(jm);
			System.out.println("[JM] " + jm_ap.toString());
			jm.start();
			jm.stayAlive();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
