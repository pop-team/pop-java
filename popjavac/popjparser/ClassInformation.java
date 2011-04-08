/*
 * This class is a prt of the POP-Java parser and code generator.
 * It's used to hold the informations from the additional informations XML file for the POP-C++ compilation
 */
public class ClassInformation {
	private String fileName;
	private String className;
	private int classuid;
	private boolean destructor;
	
	public ClassInformation(String fileName){
		this.fileName = fileName;
	}
	
	public void setClassName(String className){
		this.className = className;
	}
	
	public void setClassUID(int classuid){
		this.classuid = classuid;
	}
	
	public void setDestructor(boolean destructor){
		this.destructor = destructor;
	}
	
	public String getFilename(){
		return fileName;
	}
	
	public String getClassName(){
		return className;
	}
	
	public int getClassUID(){
		return classuid;
	}
	
	public boolean hasDestructor(){
		return destructor;
	}
}
