import java.util.ArrayList;

/**
 * This class is a prt of the POP-Java parser and code generator.
 * It's used during the parsing to hold all informations about the parallel class
 */
public class Parclass {
    private String parclassName;							//Parallel class name
    private ArrayList<Constructor> con;				//All constructors
    private ArrayList<Semantic> sem;					//All semanctics
    private boolean defaultConstructor=false;	//True if the parallel class get a default constructor

	/**
   * Create a new parclass object with the parallel class name
   * @param name 
   */
    public Parclass(String name){
        parclassName = name;
        con = new ArrayList<Constructor>();
        sem = new ArrayList<Semantic>();
    }
	
	/**
   * Get the default constructor value
   * @return true if the parallel class has a default constructor
   */
    public boolean hasDefaultConstructor(){
        return defaultConstructor;
    }

  /**
   * Get the parallel class name
   * @return Parallel class name as a string value
   */
    public String getParclassName(){
        return parclassName;
    }
    
	/**
   * Add a new constructor in the parallel class
   * @param id			Unique identifier of the constructor in the class
   * @param nbParam	Number of parameters in this constructor
   */
    public void addConstructor(int id, int nbParam){
        Constructor c = new Constructor(id, nbParam);
        con.add(c);
        if(nbParam==0){
            defaultConstructor=true;
        }
    }
	
	/**
   * Add a new object description associated with a constructor
   * @param constructorID	Unique constructor identifier
   * @param odId					Object description identifier (token kind)
   * @param params				Parameters included in the object description
   */
    public void addOD(int constructorID, int odId, String params){
        for (int i = 0; i < con.size(); i++) {
            Constructor c = con.get(i);
            if(c.getID() == constructorID){
                c.addOD(odId, params);
                return;
            }
        }
    }
	
	/**
   * Add a new semantic in the parallel class
   * @param methodName		Name of the method
   * @param interfaceSide	
   * @param brokerSide		
   */
    public void addSemantic(String methodName, String interfaceSide, String brokerSide){
        Semantic s = new Semantic(methodName, interfaceSide, brokerSide);
        sem.add(s);
    }
	
	/**
   * Get all the semantics in this parallel class
   * @return  array list of semantics
   */
    public ArrayList<Semantic> getSemantics(){
        return sem;
    }
	
	/**
   * Get all the constructors in this parallel class
   * @return array list of Constructor
   */
    public ArrayList<Constructor> getConstructors(){
        return con;
    }

	/**
    * Get a constructor by its identifier
    * @param id	Unique identifier of the constructor
    * @return The constructor associated with the given ID or null
    */
    public Constructor getConstructor(int id){
        for (int i = 0; i < con.size(); i++) {
            Constructor c = con.get(i);
            if(c.getID() == id){
                    return c;
            }
        }
        return null;
    }
	
	
}
