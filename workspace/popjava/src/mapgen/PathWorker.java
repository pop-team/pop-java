package mapgen;

public class PathWorker {
	
	public static String getFile(String path){
		if(path.contains(Constants.PATH_SEP)){
			int index = 0;
			while (path.indexOf(Constants.PATH_SEP, index+1) > 0)
				index = path.indexOf(Constants.PATH_SEP, index+1);
			String file = path.substring(index+1);
			return file;
		} else {
			return path;
		}
	}
	
	public static String getFileWithoutExt(String path){
		String file = getFile(path);
		if(file.contains("."))
			return file.substring(0, file.indexOf("."));
		else
			return file;
	}
	
	public static String getCleanPath(String path){
		int index = 0;
		while (path.indexOf(Constants.PATH_SEP, index+1) >= 0)
			index = path.indexOf(Constants.PATH_SEP, index+1);
		String cleanPath = path.substring(0, index+1);
		return cleanPath;
	}
	
	public static boolean isAbsoluePath(String path){
		return path.startsWith(Constants.PATH_SEP);
	}
	
	public static String setToAbsolute(String file, String cwd){
		if(isAbsoluePath(file))
			return file;
		else 
			return cwd+Constants.PATH_SEP+file;
	}
	
	public static String getAbsolutePath(String cwd, String relativePath){
		return cwd+Constants.PATH_SEP+relativePath;
	}
	
	public static boolean isHandlePath(String path){
		if(path.contains("./") || path.contains("../"))
			return false;
		return true;
	}
}
