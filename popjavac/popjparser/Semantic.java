/**
 * This class is a prt of the POP-Java parser and code generator.
 * It's used during the parsing to hold informations about the semanticS
 */
public class Semantic {
	private String methodName;		//Name of the method associated with this semantic
	private String interfaceSide;	//Semantic of the interface side (sync or async)
	private String brokerSide;		//Semantic of the borker side (seq, conc or mutex)
	
	/**
   * Create a new semantic with the name, the interface side and the broker side semantic
   * @param methodName		Name of the method associated with this semantic
   * @param InterfaceSide	Interface side semantic
   * @param brokerSide		Broker side semantic
   */
	public Semantic(String methodName, String interfaceSide, String brokerSide){
		this.methodName = methodName;
		this.interfaceSide = interfaceSide;
		this.brokerSide = brokerSide;
	}
	
	/**
   * Get the method name associated with this semantic
   * @return method name
   */
	public String getMethodName(){
		return methodName;
	}
	
	/**
   * Get the interface side semantic
   * @return interface side semantic
   */
	public String getInterfaceSide(){
		return interfaceSide;
	}
	
	/**
   * Get the broker side semantic
   * @return broker side semantic
   */
	public String getBrokerSide(){
		return brokerSide;
	}

	/**
   * Give a formatted string of the semantic as a Java code
   * @return semantic formatted as string
   */
	public String toString(){
		String is;
		String bs;
		if(interfaceSide.equals("sync"))
			is = "Semantic.Synchronous";
		else
			is = "Semantic.Asynchronous";
		
		if(brokerSide.equals("mutex"))
			bs = "Semantic.Mutex";
		else if (brokerSide.equals("conc"))
			bs = "Semantic.Concurrent";
		else
			bs = "Semantic.Sequence";

		return "addSemantic(c, \""+methodName+"\", "+is+" | "+bs+");";
	}
}
