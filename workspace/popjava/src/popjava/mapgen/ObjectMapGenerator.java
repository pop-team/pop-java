package popjava.mapgen;
import java.util.ArrayList;

/**
 * 
 * @author clementval
 *
 */
public class ObjectMapGenerator {
	private String file;
	private ArrayList<String> files;
	private boolean append;
	private String cwd;

	/**
	 * 
	 * @param file
	 * @param files
	 * @param append
	 * @param cwd
	 */
	public ObjectMapGenerator(String file, ArrayList<String> files,
			boolean append, String cwd) {
		this.file = file;
		this.files = files;
		this.append = append;
		this.cwd = cwd;
	}

	/**
	 * This method handle the different files and generate the entries for them
	 * @throws Exception	thrown if anything is wrong during the generation process
	 */
	public void generate() throws Exception {
		ObjectMapWriter omw = new ObjectMapWriter(file, append);
		for (int i = 0; i < files.size(); i++) {
			String crtFile = files.get(i);
			
			if (crtFile.endsWith(Constants.JAR_EXT)) {
				JARReader jr = new JARReader(crtFile);
				ArrayList<String> parclasses = jr.getParclassFromJar();
				omw.writePOPJavaEntries(parclasses, crtFile);
			} else if (crtFile.endsWith(Constants.CLASS_EXT)) {
				ClassReader cr = new ClassReader(crtFile);
				if (cr.isParclass()){
					omw.writePOPJavaEntry(cr.getClassFullName(), cr.getCleanPath());
				}
			} else if (crtFile.endsWith(Constants.POPC_OBJ) || crtFile.endsWith(Constants.POPC_MOD)) {
				POPCPPParclassWorker ppw = new POPCPPParclassWorker(crtFile);
				ppw.loadExecutableInfo();
				omw.writePOPCPPEntry(ppw.getParclassName(), ppw.getPath(), ppw.getArch());
			} else {
				
			}
		}
//		omw.writeToFile();
		omw.writeToConsole();
	}
}
