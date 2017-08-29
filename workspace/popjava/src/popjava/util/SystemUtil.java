package popjava.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 * This glass gives some static method to deal with the system
 */

public class SystemUtil {
	
	private static List<Process> processes = new ArrayList<Process>();
	private static final Configuration conf = Configuration.getInstance();

	public static void endAllChildren(){
		for(int i = 0; i < processes.size(); i++){
			Process process = processes.get(i);
			
			if(process != null){
				process.destroy();
			}
		}
	}
	
	/**
	 * Run a new command
	 * @param argvs arguments to pass to the new process
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(List<String> argvs) {
		long startTime = System.currentTimeMillis();
		LogWriter.writeDebugInfo("Run command");
		for(String arg: argvs){
			LogWriter.writeDebugInfo(arg);
		}
		
		ProcessBuilder pb = new ProcessBuilder(argvs);
		if(conf.isRedirectOutputToRoot()){
			pb = pb.inheritIO();
		}else{
			pb.redirectErrorStream(true);
			pb.redirectOutput(new File("/dev/null"));
		}
		
		if (pb != null) {
			try {
				String directory = System.getProperty("java.io.tmpdir");
				File currentDirectory = new File(directory);
				if (currentDirectory != null) {
					//pb.directory(currentDirectory);
				}
				Process process = pb.start();
				processes.add(process);
				LogWriter.writeDebugInfo("Started command after "+(System.currentTimeMillis() - startTime));
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public static boolean commandExists(String command){
		try {
			Runtime.getRuntime().exec("ssh");
			return true;
		} catch (Exception e) {
		}
		
		return false;
	}
	
	public static int runRemoteCmdSSHJ(String url, List<String> command){
		int returnValue = -1;
		final SSHClient client = new SSHClient();
		LogWriter.writeDebugInfo("Connect to "+url+" using sshj");
		try {
			client.addHostKeyVerifier(new PromiscuousVerifier());
			client.connect(url);
			
			LogWriter.writeDebugInfo("Use user "+System.getProperty("user.name")+"for connection");
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
                LogWriter.writeDebugInfo("Run remote command");
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
		if(conf.isUseNativeSSHifPossible() && commandExists("ssh")){
			command.add(0, url);
			command.add(0, "ssh");
			
			return runCmd(command);
		}
		
		return runRemoteCmdSSHJ(url, command);
	}
	
	public static int runRemoteCmd(String url, String port, List<String> command){
		if(conf.isUseNativeSSHifPossible() && commandExists("ssh")){
			command.add(0, url);
			command.add(0, "-p " + port);
			command.add(0, "ssh");
			
			return runCmd(command);
		}
		
		return runRemoteCmdSSHJ(url, command);
	}
}
