/**
 * This class is a prt of the POP-Java parser and code generator.
 * It's used during the parsing to hold some informations about constrcutors of the parclass
 * 
 */

import java.util.ArrayList;

public class Constructor {

	private int id;	//Unique identifier for the constrcutor
	private int nbParam;	//Number of parameters
	private ArrayList<ObjectDescription> od;	//Object descirption associated with this constructor

	/**
   * Create a new constructor with a unique ID and a number of parameters
   *
   */
	public Constructor(int id, int nbParam){
		this.id = id;
		this.nbParam = nbParam;
		od = new ArrayList<ObjectDescription>();
	}
	
	/**
   * Add an object description to this constrcutor
   *
   */
	public void addOD(int odId, String params){
		ObjectDescription o = new ObjectDescription(odId, params);
		od.add(o);
	}
	
	/**
   * Get the unique identifier
   * @return	The unique identifier of this constrcutor
   */
	public int getID(){
		return id;
	}

	/**
   * Get the number of parameters
   * @return	The number of parameters for this constrcutor
   */
	public int getNbParam(){
		return nbParam;	
	}
	
	/**
   * Get the object description associated with this constructor
   * @return	An array list of object description
   */
	public ArrayList<ObjectDescription> getOD(){
		return od;
	}
	
	
}
