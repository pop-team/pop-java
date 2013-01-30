package popjava.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Popjrun {

	private static final String HELP_MESSAGE = "POP-Java Application Runner v1.0\n\n"
			+
			"This program is used to run a POP-Java application or to generate object map\n\n"
			+
			"Usage: popjrun <options> <objectmap> <mainclass>\n\n"
			+
			"OPTIONS:\n"
			+ "   -h, --help                Show this message\n"
			+ "   -v, --verbose             Verbose mode\n"
			+ "   -k, --killall             Kill all parallel object (zombie)\n"
			+ "   -c, --classpath <files>   Include JAR or compiled Java class needed to run the application. Files must be separated by a semicolon \":\"\n\n"
			+
			"OPTIONS FOR OBJECT MAP GENERATION:\n"
			+ "   -l, --listlong <parclass> Generate the object map for the given parclasses. Parclasses can be a .class, .jar, .obj or .module file. Parclasses must be separated by : ";

	private static final String JAR_FOLDER = "JarFile";
	private static final String JAR_OBJMAPGEN = JAR_FOLDER+File.separatorChar+"popjobjectmapgen.jar";
	private static final String JAR_POPJAVA = JAR_FOLDER+File.separatorChar+"popjava.jar";
	private static final String DEFAULT_POP_JAVA_LOCATION = "C:\\Users\\asraniel\\workspace\\PopJava\\release\\";
	
	private static boolean verbose = false;
	
	private static void printHelp() {
		System.out.println(HELP_MESSAGE);
	}
	
	//http://stackoverflow.com/questions/6356340/killing-a-process-using-java
	//http://stackoverflow.com/questions/81902/how-to-find-and-kill-running-win-processes-from-within-java
	private static void killAll(){
		if(ScriptUtils.isWindows()){
			
		}else{
			
		}
	}
	
	private static String getClassPath(List<String> arguments){
		String classPath = ScriptUtils.getOption(arguments, "", "-c", "--classpath");
		
		String popJavaLocation = System.getenv("POPJAVA_LOCATION");
		
		if(popJavaLocation == null || popJavaLocation.isEmpty()){
			popJavaLocation = DEFAULT_POP_JAVA_LOCATION;
		}
		
		String popJavaClassPath = DEFAULT_POP_JAVA_LOCATION+JAR_POPJAVA;
		
		if(classPath.isEmpty()){
			classPath = popJavaClassPath;
		}else{
			classPath += File.pathSeparatorChar+popJavaClassPath;
		}
		
		return classPath;
	}
	
	public static void main(String[] args) {
		
		if(args.length == 0 ||
				ScriptUtils.containsOption(args, "-h") ||
				ScriptUtils.containsOption(args, "--help")){
			printHelp();
			return;
		}
		
		if(ScriptUtils.containsOption(args, "-k") ||
				ScriptUtils.containsOption(args, "--killall")){
			killAll();
			return;
		}
		
		List<String> arguments = ScriptUtils.arrayToList(args);
		
		verbose = ScriptUtils.removeOption(arguments, "-v", "--verbose");
		
		String classPath = getClassPath(arguments);
		
		String objectMap = "";
		
		if(arguments.size() >= 2){
			objectMap = arguments.get(0);
			arguments.remove(0);
		}
		
		String main = arguments.get(0);
		arguments.remove(0);
		
		//String java = System.getProperty("java.home") + "/bin/java";
		String java = "java";
		
		arguments.add(0, "-codeconf="+objectMap);
		arguments.add(0, main);
		arguments.add(0, classPath);
		arguments.add(0, "-cp");
		arguments.add(0, java);
		
		runPopApplication(arguments);
	}
	
	private static class StreamReader implements Runnable{
		
		private InputStream in;
		
		public StreamReader(InputStream in){
			this.in = in;
		}

		@Override
		public void run() {
			BufferedReader out = new BufferedReader(new InputStreamReader(in));
			String line;
			try {
				while(!Thread.currentThread().isInterrupted() && (line = out.readLine()) != null){
					System.out.println(line);
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	private static int runPopApplication(List<String> arguments){
		String argArray[] = new String[arguments.size()];
		arguments.toArray(argArray);
		
		if(verbose){
			for(String arg: argArray){
				System.out.print(arg+" ");
			}
			System.out.println();
		}
		
		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.redirectErrorStream(true);
		try{
			Process process = builder.start();
			
			InputStream in = process.getInputStream();
			Thread reader =  new Thread(new StreamReader(in));
			reader.start();
			
			process.waitFor();
			
	        return process.exitValue();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return -1;
	}

}
