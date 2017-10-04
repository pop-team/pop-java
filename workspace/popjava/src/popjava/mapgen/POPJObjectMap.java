package popjava.mapgen;
import java.io.IOException;
import java.util.ArrayList;


public class POPJObjectMap {
	private static boolean append;
	private static ArrayList<String> files;
	private static String xmlFile;
	private static String cwd;
	private static String strFile;
	
	public static void main(String[] args){
		files = new ArrayList<>();
		if(args.length < 2){
			printUsage();
			System.exit(1);
		}


		for (String arg : args) {
			if (arg.equals("-append")) {
				append = true;
				xmlFile = arg.substring(arg.indexOf("=") + 1);
				if (!xmlFile.endsWith(".xml")) {
					System.err.println(xmlFile + " is not an xml file.");
					System.exit(1);
				}
			} else if (arg.startsWith("-file=")) {
				strFile = arg.substring(arg.indexOf("=") + 1);
			} else if (arg.startsWith("-cwd=")) {
				cwd = arg.substring(arg.indexOf("=") + 1);
				if (!PathWorker.isHandlePath(cwd)) {
					System.err.println("This program does not handle ./ and ../ in path");
					System.exit(1);
				}
			} else {
				System.err.println("Unknown options");
				System.exit(1);
			}
		}
		
		if(cwd.endsWith(Constants.PATH_SEP))
			cwd = cwd.substring(0, cwd.length()-1);
		
		
		if(xmlFile != null && !PathWorker.isAbsoluePath(xmlFile)){
			xmlFile = PathWorker.getAbsolutePath(cwd, xmlFile);
		}
		
		
		while(strFile.indexOf(":")>0){
			String f = strFile.substring(0, strFile.indexOf(":"));
			strFile = strFile.substring(strFile.indexOf(":")+1);
			files.add(PathWorker.setToAbsolute(f, cwd));
		}
		files.add(PathWorker.setToAbsolute(strFile, cwd));
		
	
		
		if(cwd.equals("") || files.size()==0){
			printUsage();
			System.exit(0);
		}
		
		ObjectMapGenerator omg = new ObjectMapGenerator(xmlFile, files, append, cwd);
		try {
			omg.generate();
		} catch (IOException e) {
			System.err.println("Unable to write the file");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Unknown error");
			e.printStackTrace();
		}
	}
	
	/**
	 * Print the usage of this program
	 */
	private static void printUsage(){
		System.out.println("POPJObjectMap [-append=OLD_OBJECTMAP] -file=COMPILED_PARCLASS -cwd=CURRENT_WORKING_DIRECTORY");
	}
}
