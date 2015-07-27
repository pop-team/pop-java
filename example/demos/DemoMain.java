import popjava.annotation.POPClass;


@POPClass(isDistributable = false)
public class DemoMain {

	public static void main(String[] args) {
		int n = Integer.parseInt(args[0]);
		DemoPOP[] objects = new DemoPOP[n];
		for (int i = 0; i < n; i++) {
			objects[i] = new DemoPOP(i);
		}

		for (int i = 0; i < (n - 1); i++) {
			objects[i].sendIdTo(objects[i + 1]);
		}

		objects[n - 1].sendIdTo(objects[0]);

		objects[n - 1].wait(2);

	}

}
