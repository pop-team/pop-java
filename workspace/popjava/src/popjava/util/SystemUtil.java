package popjava.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 * This glass gives some static method to deal with the system
 */

public class SystemUtil {

	private static final List<Process> processes = new ArrayList<Process>();
	private static final Configuration conf = Configuration.getInstance();
	private static final boolean sshAvailable = commandExists("ssh");
	private static boolean changeUserStatus = false;
	private static String changeUserName = null;
		
	public static void endAllChildren(){
		for(int i = 0; i < processes.size(); i++){
			Process process = processes.get(i);

			if(process != null){
				process.destroy();
			}
		}
	}

	/**
	 * Run a new command in a directory
	 * @param argvs arguments to pass to the new process
	 * @param dir Working directory
	 * @param executeAs Should we change user if possible
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs, String dir, String executeAs) {
		if (executeAs != null && !executeAs.isEmpty() && canChangeUser(executeAs)) {
			argvs = commandAs(executeAs, Util.join(" ", (argvs)));
		}
		
		long startTime = System.currentTimeMillis();
		LogWriter.writeDebugInfo("[System] Run command");
		for(String arg: argvs){
			LogWriter.writeDebugInfo(" %79s", arg);
		}

		ProcessBuilder pb = new ProcessBuilder(argvs);
		if (dir != null && !dir.isEmpty()) {
			pb.directory(new File(dir));
		}

		if(conf.isRedirectOutputToRoot()){
			pb = pb.inheritIO();
		}else{
			pb.redirectErrorStream(true);
			pb.redirectOutput(new File("/dev/null"));
		}

		if (pb != null) {
			try {
				/*String directory = System.getProperty("java.io.tmpdir");
				File currentDirectory = new File(directory);
				if (currentDirectory != null) {
					//pb.directory(currentDirectory);
				}*/
				Process process = pb.start();
				processes.add(process);
				LogWriter.writeDebugInfo("[System] Started command after %s ms", System.currentTimeMillis() - startTime);
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * Run a new command
	 * @param argvs arguments to pass to the new process
	 * @param dir Working directory
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs, String dir) {
		return runCmd(argvs, dir, null);
	}
	
	/**
	 * Run a new command
	 * @param argvs arguments to pass to the new process
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs) {
		return runCmd(argvs, null);
	}

	public static boolean commandExists(String command){
		try {
			Runtime.getRuntime().exec(command);
			return true;
		} catch (Exception e) {
		}

		return false;
	}
	
	/**
	 * Test if a command works.
	 * Timeout 1 second
	 * @param command a 'sh' command
	 * @return true if the command exit value is 0, false otherwise
	 */
	public static boolean commandWork(List<String> command) {
		try {
			ProcessBuilder exec = new ProcessBuilder(command);
			Process start = exec.start();
			boolean wait = start.waitFor(1, TimeUnit.SECONDS);
			return wait && start.exitValue() == 0;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}
	
	/**
	 * Mark if we should use a super user command to start the process.
	 * @param user The user we want to execute 
	 * @return 
	 */
	public static boolean canChangeUser(String user) {
		if (changeUserName == null ? user == null : changeUserName.equals(user)) {
			return changeUserStatus;
		}
		
		changeUserName = user;
		changeUserStatus = commandWork(commandAs(user, "echo 1"));
		return changeUserStatus;
	}
	
	private static List<String> commandAs(String user, String command) {
		Util.OSType os = Util.getOSType();
		List<String> execute = new ArrayList<>();
		switch(os) {
			// TODO test on windows
			case Windows: 
				execute.add("RUNAS");
				execute.add("/USER:" + user);
				execute.add("CMD");
				execute.add("/K");
				execute.add(command);
				break;
			case UNIX:
			default:
				execute.add("sudo");
				execute.add("-snu");
				execute.add(user);
				execute.add("sh");
				execute.add("-c");
				execute.add(command);
				break;
		}
		return execute;
	}

	public static int runRemoteCmdSSHJ(String url, List<String> command){
		int returnValue = -1;
		final SSHClient client = new SSHClient();
		LogWriter.writeDebugInfo("[System] Connect to "+url+" using sshj");
		try {
			client.addHostKeyVerifier(new PromiscuousVerifier());
			client.connect(url);

			LogWriter.writeDebugInfo("[System] Use user "+System.getProperty("user.name")+"for connection");
			client.authPublickey(System.getProperty("user.name"));

            final Session session = client.startSession();
            try{
            	String commandAsString = "";
                for(int i = 0; i < command.size(); i++){
                	commandAsString += command.get(i);
                	if(i < command.size() - 1){
                		commandAsString += " ";
                	}
                }
                LogWriter.writeDebugInfo("[System] Run remote command");
                session.exec(commandAsString);
                returnValue = 0;
            }finally{
            	//session.close();
            }
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				client.disconnect();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return returnValue;
	}

	public static int runRemoteCmd(String url, List<String> command){
		return runRemoteCmd(url, null, command);
	}

	public static int runRemoteCmd(String url, String port, List<String> command){
		if(conf.isUseNativeSSHifPossible() && sshAvailable){
			// it's better to send the command over SSH as a single argument
			// every argument is escaped to avoid expansion
			command = escapeDollar(command);
			command.add(0, url);
			if (port != null && !port.isEmpty()) {
				command.add(0, String.format("-p %s", port));
			}
			command.add(0, "ssh");

			return runCmd(command);
		}

		return runRemoteCmdSSHJ(url, command);
	}
	
	/**
	 * Return the same list with all elements containing $ as \$
	 * @param command
	 * @return 
	 */
	private static List<String> escapeDollar(List<String> command) {
		List<String> newCommand = new ArrayList<>(command);
		for (int i = 0; i < newCommand.size(); i++) {
			String com = newCommand.get(i);
			if (com.contains("$")) {
				newCommand.set(i, com.replace("$", "\\$"));
			}
		}
		
		return newCommand;
	}
}
