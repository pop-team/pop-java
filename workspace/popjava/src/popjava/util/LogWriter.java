package popjava.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import popjava.system.POPSystem;

/**
 * This class is used to write log file
 */
public class LogWriter {

	/**
	 * Process identifier
	 */
	private static String PID = "";
	
	/**
	 * Log folder where the log files will be written
	 */
	public static String LogFolder = String.format("%s%slogFolder", POPSystem
			.getPopLocation(), POPSystem.getSeparatorString());
	
	/**
	 * Prefix of the log file
	 */
	public static String Prefix = "";

	static {
		String pid = java.lang.management.ManagementFactory.getRuntimeMXBean()
				.getName();
		for (int index = 0; index < pid.length(); index++) {
			if (Character.isLetterOrDigit(pid.charAt(index)))
				PID += Character.toString(pid.charAt(index));
		}
	}

	/**
	 * Default constructor
	 */
	private LogWriter() {

	}

	/**
	 * Write a new log information line in the file
	 * @param info		Information to write
	 * @param filePath	Path of the log file
	 */
	public static void writeLogInfo(String info, String filePath) {
		info = PID + "-" + (new Date()).toString() + "-" + info;
		info += "\r\n";
		writeLogfile(info, filePath);

	}

	/**
	 * Write a new debug information line in the file
	 * @param info	Information to write
	 */
	public static void writeDebugInfo(String info) {
		if (Configuration.Debug) {
			info = PID + "-" + (new Date()).toString()+":"+System.currentTimeMillis() + "-" + info;
			info += "\r\n";
			String path = String.format("%s%s%s%s.txt", LogFolder, POPSystem
					.getSeparatorString(), Prefix, PID);
			writeLogfile(info, path);
		}
	}
	
	public static void writeExceptionLog(Throwable e){
		writeDebugInfo("Exception "+ e.getClass().getName()+" "+e.getMessage());
        for(int i = 0; i < e.getStackTrace().length; i++){
        	writeDebugInfo(e.getStackTrace()[i].getClassName()+" "+e.getStackTrace()[i].getLineNumber());
        }
	}

	/**
	 * Write new log information into a file
	 * @param info	Information to write
	 * @param path	Path of the file
	 */
	public static void writeLogfile(String info, String path) {
		path = path.replace(".txt", PID + ".txt");
		java.io.FileWriter fstream;
		try {
			fstream = new java.io.FileWriter(path, true);
			java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
			out.write(info);

			// Close the output stream
			out.close();
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Remove all file in the log directory
	 * @return	true if the action is succeed
	 */
	public static boolean deleteLogDir() {
		File dir = new File(LogFolder);
		String[] children = dir.list();
		for (int i = 0; i < children.length; i++) {
			new File(dir, children[i]).delete();
		}
		return true;
	}

}
