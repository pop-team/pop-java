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

	/**
	 * Run a new command
	 * @param argvs arguments to pass to the new process
	 * @return 0 if the command launch is a success
	 */
	public static int runCmd(ArrayList<String> argvs) {
		for(String arg: argvs){
			LogWriter.writeDebugInfo(arg);
		}
		ProcessBuilder pb = new ProcessBuilder(argvs);
		if (pb != null) {
			try {
				String directory = System.getProperty("java.io.tmpdir");
				File currentDirectory = new File(directory);
				if (currentDirectory != null) {
					//pb.directory(currentDirectory);
				}
				
				pb.start();
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public static int runRemoteCmd(String url, List<String> command){
		int returnValue = -1;
		final SSHClient client = new SSHClient();
		try {
			//client.loadKnownHosts();
			client.addHostKeyVerifier(new PromiscuousVerifier());
			client.connect(url);
			
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return returnValue;
	}
}
