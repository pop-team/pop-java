package popjava.baseobject;

/**
 * Connection type to be used if the object has to be created on a remote machine
 * @author Beat Wolf
 *
 */
public enum ConnectionType {
	SSH("SSH"),
	DEAMON("POP-Java deamon"),
	ANY("Any");
	
	private ConnectionType(String name){
		this.name = name;
	}	
	
	private String name;
	
	@Override
	public String toString(){
		return name;
	}
}
