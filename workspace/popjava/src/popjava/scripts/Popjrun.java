package popjava.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import popjava.system.POPJavaClassloader;
import popjava.util.Configuration;

public class Popjrun {

	private static final String HELP_MESSAGE = "POP-Java Application Runner v1.0\n\n"
			+ "This program is used to run a POP-Java application or to generate object map\n\n"
			+ "Usage: popjrun <options> <objectmap> <mainclass>\n\n"
			+ "OPTIONS:\n"
			+ "   -h, --help                Show this message\n"
			+ "   -v, --verbose             Verbose mode\n"
			+ "   -k, --killall             Kill all parallel object (zombie) (not implemented)\n"
			+ "   -c, --classpath <files>   Include JAR or compiled Java class needed to run the application. Files must be separated by a "
			+ File.pathSeparatorChar
			+ "\n\n"
			+ "OPTIONS FOR OBJECT MAP GENERATION:\n"
			+ "   -l, --listlong <parclass> Generate the object map for the given parclasses. Parclasses can be a .class, .jar, .obj or .module file. Parclasses must be separated by "
			+ File.pathSeparatorChar;

	private static final String JAR_FOLDER = "JarFile";
	private static final String JAR_OBJMAPGEN = JAR_FOLDER + File.separatorChar
			+ "popjobjectmapgen.jar";
	private static final String JAR_POPJAVA = JAR_FOLDER + File.separatorChar
			+ "popjava.jar";
	private static final String DEFAULT_POP_JAVA_LOCATION;

	private static final boolean USE_SEPARATE_JVM = true;

	static {
		if (ScriptUtils.isWindows()) {
			DEFAULT_POP_JAVA_LOCATION = "C:\\Users\\asraniel\\workspace\\PopJava\\release\\";
		} else {
			DEFAULT_POP_JAVA_LOCATION = "/usr/local/popj/";
		}
	}

	private static boolean verbose = false;

	private static void printHelp() {
		System.out.println(HELP_MESSAGE);
	}

	// http://stackoverflow.com/questions/6356340/killing-a-process-using-java
	// http://stackoverflow.com/questions/81902/how-to-find-and-kill-running-win-processes-from-within-java
	private static void killAll() {
		if (ScriptUtils.isWindows()) {

		} else {

		}
	}

	/**
	 * Returns the path to POPJava (ex: /usr/local/popj/)
	 * 
	 * @return
	 */
	private static String getPopJavaLocation() {
		String popJavaLocation = System.getenv("POPJAVA_LOCATION");

		if (popJavaLocation == null || popJavaLocation.isEmpty()) {
			popJavaLocation = DEFAULT_POP_JAVA_LOCATION;
		} else {
			popJavaLocation += File.separatorChar;
		}

		return popJavaLocation;
	}

	private static String createClassPath(String classPath) {

		if (USE_SEPARATE_JVM) {
			String popJavaLocation = getPopJavaLocation();

			String popJavaClassPath = popJavaLocation + JAR_POPJAVA;

			if (classPath.isEmpty()) {
				classPath = popJavaClassPath + File.pathSeparatorChar + ".";
			} else {
				classPath += File.pathSeparatorChar + popJavaClassPath;
			}
		}

		return classPath;
	}

	private static boolean help = false;
	private static boolean killAll = false;
	private static String listLong = "";
	private static String classPath = "";

	private static List<String> parseArguments(String[] args) {
		List<String> arguments = new ArrayList<String>();
		int i = 0;
		for (; i < args.length; i++) {
			if (args[i].equals("-h") || args[i].equals("--help")) {
				help = true;
			} else if (args[i].equals("-k") || args[i].equals("--killall")) {
				killAll = true;
			} else if (args[i].equals("-v") || args[i].equals("--verbose")) {
				verbose = true;
			} else if (args[i].equals("-l") || args[i].equals("--listlong")) {
				if (args.length > i + 1) {
					listLong = args[i + 1];
					i++;
				} else {
					System.err
							.println("Listlong command needs a parameter following it");
					System.exit(0);
				}
			} else if (args[i].equals("-c") || args[i].equals("--classpath")) {
				if (args.length > i + 1) {
					classPath = createClassPath(args[i + 1]);
					i++;
				} else {
					System.err
							.println("Classpath parameter needs a parameter following it");
					System.exit(0);
				}
			} else {
				break;
			}
		}

		for (; i < args.length; i++) {
			arguments.add(args[i]);
		}

		return arguments;
	}

	public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {

		List<String> arguments = parseArguments(args);

		if (args.length == 0 || help) {
			printHelp();
			return;
		}

		if (killAll) {
			killAll();
			return;
		}

		if (!listLong.isEmpty()) {
			listLong(listLong);
			return;
		}

		String objectMap = "";

		if (arguments.size() >= 2) {
			objectMap = arguments.get(0);
			arguments.remove(0);
		}

		if (arguments.size() == 0) {
			System.err.println("No arguments where specified to run POP-Java application");
			return;
		}
		
		String main = arguments.get(0);
		arguments.remove(0);

		if (USE_SEPARATE_JVM) {
			runForkedApplication(main, objectMap, arguments);
		}else{
			launchApplication(main, objectMap, arguments);
		}
	}
	
	private static void launchApplication(String main, String objectMap, List<String> arguments) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException{
		List<String> popJavaConf = new ArrayList<String>();
		popJavaConf.add("-codeconf="+objectMap);
		//POPSystem.initialize(popJavaConf);
		
		String [] urlStrings = classPath.split(""+File.pathSeparatorChar);
		URL [] urls = new URL[urlStrings.length];
		for(int i = 0;i < urls.length; i++){
			System.out.println("Load "+urlStrings[i]);
			urls[i] = new URL("file:"+urlStrings[i]);
		}
		
		POPJavaClassloader classLoader = new POPJavaClassloader(urls);
		
		Class<?> mainClass = classLoader.loadClass(main);
		try {
			Method mainMethod = mainClass.getMethod("main", String[].class);
			
			String [] args = arguments.toArray(new String[0]);
			mainMethod.invoke(null, (Object)args);
		} catch (NoSuchMethodException e) {
			System.err.println("Could not find method main in "+main);
		}
		
		classLoader.close();
		
		//POPSystem.end();
	}

	private static void runForkedApplication(String main, String objectMap, List<String> arguments) {
		// String java = System.getProperty("java.home") + "/bin/java";
		String java = "java";

		if (classPath.isEmpty()) {
			classPath = createClassPath("");
		}

		arguments.add(0, "-codeconf=" + objectMap);
		arguments.add(0, main);
		//arguments.add(0, "-Djava.system.class.loader="+POPJavaClassloader.class.getName());
		arguments.add(0, classPath);
		arguments.add(0, "-cp");
		if (Configuration.ACTIVATE_JMX) {
			arguments.add(0, "-Dcom.sun.management.jmxremote.port=3333");
			arguments.add(0, "-Dcom.sun.management.jmxremote.ssl=false");
			arguments.add(0,
					"-Dcom.sun.management.jmxremote.authenticate=false");
		}

		arguments.add(0, java);

		runPopApplication(arguments);
	}

	private static void listLong(String files) {
		// java -cp
		// $POPJAVA_LOCATION$JAR_OBJMAPGEN:$POPJAVA_LOCATION$JAR_POPJAVA:$POPJAVA_LOCATION$JAR_JAVASSIST
		// $CLASS_OBJMAPGEN -cwd=$PWD $APPEND$FILE -file=$FILES
		String[] command = new String[6];
		command[0] = "java";
		command[1] = "-cp";
		command[2] = getPopJavaLocation() + JAR_OBJMAPGEN
				+ File.pathSeparatorChar + getPopJavaLocation() + JAR_POPJAVA;
		command[3] = "POPJObjectMap";
		command[4] = "-cwd=" + System.getProperty("user.dir");
		command[5] = "-file=" + files;
		// command[7] = "-file="+files;

		ScriptUtils.runNativeApplication(command, "Java not installed", null,
				verbose);
	}

	private static class StreamReader implements Runnable {

		private InputStream in;

		public StreamReader(InputStream in) {
			this.in = in;
		}

		@Override
		public void run() {
			BufferedReader out = new BufferedReader(new InputStreamReader(in));
			String line;
			try {
				while (!Thread.currentThread().isInterrupted()
						&& (line = out.readLine()) != null) {
					System.out.println(line);
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static int runPopApplication(List<String> arguments) {
		String argArray[] = new String[arguments.size()];
		arguments.toArray(argArray);

		if (verbose) {
			for (String arg : argArray) {
				System.out.print(arg + " ");
			}
			System.out.println();
		}

		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.redirectErrorStream(true);
		try {
			Process process = builder.start();

			InputStream in = process.getInputStream();
			Thread reader = new Thread(new StreamReader(in));
			reader.start();

			process.waitFor();

			return process.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}
}
