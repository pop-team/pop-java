package ch.icosys.popjava.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.icosys.popjava.core.broker.Broker;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 * This glass gives some static method to deal with the system
 */

public class SystemUtil {

	private static final List<Process> processes = new ArrayList<>();

	private static final List<Broker> localJVM = new ArrayList<>();

	private static final Configuration conf = Configuration.getInstance();

	private static final boolean sshAvailable = commandExists("ssh");

	private static boolean changeUserStatus = false;

	private static String changeUserName = null;

	/**
	 * Kill all objects created by this JVM
	 */
	public static void endAllChildren() {
		for (Process process : processes) {
			if (process != null) {
				process.destroy();
			}
		}
		processes.clear();
		for (Broker broker : localJVM) {
			if (broker != null) {
				broker.kill();
			}
		}
		localJVM.clear();
	}

	/**
	 * Run a new command in a directory
	 * 
	 * @param argvs
	 *            arguments to pass to the new process
	 * @param dir
	 *            Working directory
	 * @param executeAs
	 *            Should we change user if possible
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs, String dir, String executeAs) {
		if (executeAs != null && !executeAs.isEmpty() && canChangeUser(executeAs)) {
			argvs = commandAs(executeAs, Util.join(" ", escapeDollar(argvs)));
		}

		long startTime = System.currentTimeMillis();
		LogWriter.writeDebugInfo("[System] Run command");
		for (String arg : argvs) {
			LogWriter.writeDebugInfo(" %79s", arg);
		}

		ProcessBuilder pb = new ProcessBuilder(argvs);
		if (dir != null && !dir.isEmpty()) {
			pb.directory(new File(dir));
		}

		if (conf.isRedirectOutputToRoot()) {
			pb = pb.inheritIO();
		} else {
			pb.redirectErrorStream(true);
			pb.redirectOutput(new File("/dev/null"));
		}

		if (pb != null) {
			try {
				/*
				 * String directory = System.getProperty("java.io.tmpdir"); File
				 * currentDirectory = new File(directory); if (currentDirectory != null) {
				 * //pb.directory(currentDirectory); }
				 */
				Process process = pb.start();
				processes.add(process);
				LogWriter.writeDebugInfo("[System] Started command after %s ms",
						System.currentTimeMillis() - startTime);
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * Create a new directory via command line, it's possible of doing this using a
	 * different user if preconfigured.
	 * 
	 * @param dir
	 *            directory we want to create
	 * @param executeAs
	 *            the user we want the directory to be created with, can be null
	 * @return true if create correctly, false otherwise
	 */
	public static boolean mkdir(Path dir, String executeAs) {
		Util.OSType os = Util.getOSType();
		List<String> cmd = new ArrayList<>();
		cmd.add("mkdir");
		switch (os) {
		case Windows:
			cmd.add("/Q");
			break;
		case UNIX:
		default:
			cmd.add("-m=755");
			cmd.add("-p");
			break;
		}
		cmd.add(dir.toAbsolutePath().toString());
		return runCmd(cmd, null, executeAs) == 0;
	}

	/**
	 * Delete a file/directory via command line, it's possible of doing this using a
	 * different user if preconfigured. To delete a directory it must be empty.
	 * 
	 * @param path
	 *            file/directory we want to remove
	 * @param executeAs
	 *            the user we want the directory to be delete with, can be null
	 * @return true if create correctly, false otherwise
	 */
	public static boolean rm(Path path, String executeAs) {
		Util.OSType os = Util.getOSType();
		List<String> cmd = new ArrayList<>();
		switch (os) {
		case Windows:
			cmd.add("del");
			cmd.add("/q");
			break;
		case UNIX:
		default:
			cmd.add("rm");
			break;
		}
		cmd.add(path.toAbsolutePath().toString());
		return runCmd(cmd, null, executeAs) == 0;
	}

	/**
	 * Delete a directory via command line, it's possible of doing this using a
	 * different user if preconfigured. The directory must be empty.
	 * 
	 * @param dir
	 *            directory we want to remove
	 * @param executeAs
	 *            the user we want the directory to be delete with, can be null
	 * @return true if create correctly, false otherwise
	 */
	public static boolean rmdir(Path dir, String executeAs) {
		Util.OSType os = Util.getOSType();
		List<String> cmd = new ArrayList<>();
		cmd.add("rmdir");
		switch (os) {
		case Windows:
			cmd.add("/Q");
			break;
		case UNIX:
		default:
			break;
		}
		cmd.add(dir.toAbsolutePath().toString());
		return runCmd(cmd, null, executeAs) == 0;
	}

	/**
	 * Run a new command
	 * 
	 * @param argvs
	 *            arguments to pass to the new process
	 * @param dir
	 *            Working directory
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs, String dir) {
		return runCmd(argvs, dir, null);
	}

	/**
	 * Run a new command
	 * 
	 * @param argvs
	 *            arguments to pass to the new process
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs) {
		return runCmd(argvs, null);
	}

	public static boolean commandExists(String command) {
		try {
			Runtime.getRuntime().exec(command);
			return true;
		} catch (Exception e) {
		}

		return false;
	}

	/**
	 * Test if a command works. Timeout 1 second
	 * 
	 * @param command
	 *            a 'sh' command
	 * @return true if the command exit value is 0, false otherwise
	 */
	public static boolean commandWork(List<String> command) {
		try {
			ProcessBuilder exec = new ProcessBuilder(command);
			Process start = exec.start();
			return start.waitFor() == 0;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	/**
	 * Mark if we should use a super user command to start the process. Defined in
	 * {@link Configuration#setJobmanagerExecutionUser(String)}.
	 * 
	 * @param user
	 *            The user we want to execute
	 * @return true if we have to switch user
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
		switch (os) {
		// TODO test on windows
		case Windows:
			execute.add("RUNAS");
			execute.add("/USER:" + user);
			execute.add("/PROFILE");
			execute.add("/SAVECRED");
			execute.add("CMD");
			execute.add("/K");
			execute.add(command);
			break;
		case UNIX:
		default:
			execute.add("sudo");
			execute.add("-inu"); // --login --non-interactive --user
			execute.add(user);
			execute.add("sh");
			execute.add("-c");
			execute.add(command);
			break;
		}
		return execute;
	}

	public static int runRemoteCmdSSHJ(String url, List<String> command) {
		int returnValue = -1;
		final SSHClient client = new SSHClient();
		LogWriter.writeDebugInfo("[System] Connect to " + url + " using sshj");
		try {
			client.addHostKeyVerifier(new PromiscuousVerifier());
			client.connect(url);

			LogWriter.writeDebugInfo("[System] Use user " + System.getProperty("user.name") + "for connection");
			client.authPublickey(System.getProperty("user.name"));

			final Session session = client.startSession();
			try {
				StringBuilder commandAsString = new StringBuilder();
				for (int i = 0; i < command.size(); i++) {
					commandAsString.append(command.get(i));
					if (i < command.size() - 1) {
						commandAsString.append(" ");
					}
				}
				LogWriter.writeDebugInfo("[System] Run remote command");
				session.exec(commandAsString.toString());
				returnValue = 0;
			} finally {
				// session.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				client.disconnect();
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return returnValue;
	}

	public static int runRemoteCmd(String url, List<String> command) {
		return runRemoteCmd(url, null, command);
	}

	public static int runRemoteCmd(String url, String port, List<String> command) {
		if (conf.isUseNativeSSHifPossible() && sshAvailable) {
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
	 * 
	 * @param command
	 *            the command to escape the dollar
	 * @return a new list with the escaped $ character
	 */
	private static List<String> escapeDollar(List<String> command) {
		List<String> newCommand = new ArrayList<>(command);
		for (int i = 0; i < newCommand.size(); i++) {
			String com = newCommand.get(i);
			if (com.contains("$")) {
				// XXX escaping AFTER the $ works, why though is rather a
				// mystery
				newCommand.set(i, com.replace("$", "$\\"));
			}
		}

		return newCommand;
	}

	/**
	 * Register local JVM object so we can kill them
	 * 
	 * @param broker
	 *            the broker of the local JVM object
	 */
	public static synchronized void registerLocalJVM(Broker broker) {
		Objects.requireNonNull(broker);
		localJVM.add(broker);
	}
}
