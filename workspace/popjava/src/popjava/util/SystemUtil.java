package popjava.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This glass gives some static method to deal with the system
 */

public class SystemUtil {

	/**
	 * Run a new command
	 * @param argvs arguments to pass to the new process
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(ArrayList<String> argvs) {
		/*for(String arg: argvs){
			System.out.println(arg);
		}*/
		ProcessBuilder pb = new ProcessBuilder(argvs);
		if (pb != null) {
			try {
				String directory = System.getProperty("java.io.tmpdir");
				File currentDirectory = new File(directory);
				if (currentDirectory != null) {
					//pb.directory(currentDirectory);
					pb.start();
					return 0;

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
}
