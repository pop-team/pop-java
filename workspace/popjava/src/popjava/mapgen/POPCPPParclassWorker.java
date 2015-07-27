package popjava.mapgen;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class POPCPPParclassWorker {
	private String path;
	private String parclassName;
	private String arch;
	private String fullpath;

	public POPCPPParclassWorker(String path) {
		this.path = path;
	}

	public void loadExecutableInfo() throws IOException {
		String line; 
		Process p = Runtime.getRuntime().exec(path+" -listlong");
		BufferedReader input = new BufferedReader(new InputStreamReader(p
				.getInputStream()));
		line = input.readLine();
		int fstSpace = line.indexOf(" ");
		int sndSpace = line.indexOf(" ", fstSpace+1);
		parclassName = line.substring(0, fstSpace);
		arch = line.substring(fstSpace+1, sndSpace);
		fullpath = line.substring(sndSpace+1);
	}
	
	public String getParclassName(){
		return parclassName;
	}
	
	public String getArch(){
		return arch;
	}
	
	public String getPath(){
		return fullpath;
	}
}
