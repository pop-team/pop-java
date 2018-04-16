package testsuite.demo;

import popjava.PopJava;
import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class DemoMain {

	public static void main(String... argvs) {
			Demopop[] objs;
			int nbObject;
			if (argvs.length < 1 || Integer.parseInt(argvs[0]) == 0) {
				System.out.println("Usage: ");
				return;
			} else {
				nbObject = Integer.parseInt(argvs[0]);
				objs = new Demopop[nbObject];

				System.out.println("START of DemoMain program with " + nbObject
						+ " objects");

			}

			for (int i = 0; (i < nbObject); i++) {
				if (i < argvs.length - 1){
					objs[i] = new Demopop(i + 1, argvs[i + 1]);
				} else{
					objs[i] = new Demopop(i + 1, 60, 40);
				}
				System.out.println("Demopop with ID=" + objs[i].getID()
						+ " created with access point : "
						+ PopJava.getAccessPoint(objs[i]));
			}

			for (int i = 0; i < objs.length - 1; i++) {
				System.out
						.println("Demopop:" + objs[i].getID()
								+ " with access point "
								+ PopJava.getAccessPoint(objs[i])
								+ " is sending his ID to object:"
								+ objs[i + 1].getID());
				objs[i].sendIDto(objs[i + 1]);
				System.out.println("Demopop:" + objs[i + 1].getID()
						+ " receiving id=" + objs[i].getID());
			}
			System.out.println("Demopop:" + objs[nbObject - 1].getID()
					+ " with access point "
					+ PopJava.getAccessPoint(objs[nbObject - 1])
					+ " is sending his ID to object:" + objs[0].getID());
			objs[nbObject - 1].sendIDto(objs[0]);
			System.out.println("Demopop:" + objs[0].getID() + " receiving id="
					+ objs[nbObject - 1].getID());
			objs[nbObject - 1].wait(2);

			System.out.println("END of DemoMain program");

	}
}
