package popjava.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

	private static final String JAR_FOLDER = "/JarFile/";
	private static final String JAR_OBJMAPGEN = JAR_FOLDER+"popjobjectmapgen.jar";
	private static final String JAR_POPJAVA = JAR_FOLDER+"popjava.jar";
	private static final String JAR_JAVASSIST = JAR_FOLDER+"javassist.jar";
	private static final String DEFAULT_POP_JAVA_LOCATION = "/usr/local/popj";
	
	private static boolean verbose = false;
	
	private static void printHelp() {
		System.out.println(HELP_MESSAGE);
	}
	
	//http://stackoverflow.com/questions/6356340/killing-a-process-using-java
	//http://stackoverflow.com/questions/81902/how-to-find-and-kill-running-win-processes-from-within-java
	private static void killAll(){
		if(System.getProperty("os.name").toLowerCase().indexOf("windows") > -1){
			
		}else{
			
		}
	}
	
	private static String getOption(ArrayList<String> parameters, String option, String defaultValue){
        for(int i = 0; i < parameters.size(); i++){
            if(parameters.get(i).equals(option)){
                if(parameters.size() > i + 1){
                	String value = parameters.get(i+1);
                	parameters.remove(i);
                	parameters.remove(i);
                    return value;
                }
            }
        }
        
        return defaultValue;
    }
	
	private static boolean containsOption(String [] args, String option){
		for(String argument: args){
			if(argument.equals(option)){
				return true;
			}
		}
		
		return false;
	}
	
	private static String getClassPath(ArrayList<String> arguments){
		String classPath = getOption(arguments, "-c", "");
		if(classPath.isEmpty()){
			classPath = getOption(arguments, "--classpath", "");
		}
		
		String popJavaLocation = System.getenv("POPJAVA_LOCATION");
		
		if(popJavaLocation == null || popJavaLocation.isEmpty()){
			popJavaLocation = DEFAULT_POP_JAVA_LOCATION;
		}
		
		String popJavaClassPath = DEFAULT_POP_JAVA_LOCATION+JAR_POPJAVA+":"+
				DEFAULT_POP_JAVA_LOCATION+JAR_JAVASSIST;
		
		if(classPath.isEmpty()){
			classPath = popJavaClassPath;
		}else{
			classPath += ":"+popJavaClassPath;
		}
		
		return classPath;
	}
	
	public static void main(String[] args) {
		
		if(args.length == 0 ||
				containsOption(args, "-h") ||
				containsOption(args, "--help")){
			printHelp();
			return;
		}
		
		verbose = containsOption(args, "-v") || containsOption(args, "--verbose");
		
		if(containsOption(args, "-k") ||
				containsOption(args, "--killall")){
			killAll();
			return;
		}
		
		ArrayList<String> arguments = new ArrayList<String>();
		
		for(String arg: args){
			arguments.add(arg);
		}
		
		String classPath = getClassPath(arguments);
		
		String objectMap = "";
		
		if(arguments.size() >= 2){
			objectMap = arguments.get(0);
			arguments.remove(0);
		}
		
		String main = arguments.get(0);
		arguments.remove(0);
		
		String java = System.getProperty("java.home") + "/bin/java";
		
		arguments.add(0, "-codeconf="+objectMap);
		arguments.add(0, main);
		arguments.add(0, classPath);
		arguments.add(0, "-cp");
		arguments.add(0, java);
		
		runPopApplication(arguments);
	}
	
	private static int runPopApplication(ArrayList<String> arguments){
		String argArray[] = new String[arguments.size()];
		arguments.toArray(argArray);
		
		if(verbose){
			for(String arg: argArray){
				System.out.print(arg+" ");
			}
			System.out.println();
		}
		
		ProcessBuilder builder = new ProcessBuilder(arguments);

		try{
			Process process = builder.start();
			
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			
			BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while((line = out.readLine()) != null){
				System.out.println(line);
			}
			
			while((line = error.readLine()) != null){
				System.out.println(line);
			}
			
	        process.waitFor();
	        return process.exitValue();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return -1;
	}

}
