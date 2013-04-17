/**
 * This class is a prt of the POP-Java parser and code generator.
 * It's used during the parsing to hold some informations an object description
 */
public class ObjectDescription {
	private int odId;				//Identifier of the OD token
	private String params;	//Parameters of the objects descritpion as a string
	
	/**
   * Create a new object description with an OD identifer and some parameters
   * @param odID		Object descritpion identifer (token id)	
   * @param params	Parameters included in the object description
   */
	public ObjectDescription(int odId, String params){
		this.odId = odId;
		this.params = params;
	}
	
	/**
   * Get the object description identifier
   * @return object descritption identifier as a int value
   */
	public int getOdId(){
		return odId;
	}
	
	/**
   * Get the parameters 
   * @return Parameters of the object description formatted in a string
   */
	public String getParams(){
		return params;
	}

	/**
   * Print the object description as Java code
   * @return a formatted string for this object description
   */
	public String toString(){
		/*String ret="od.";
		if(odId == POPJParserConstants.ODURL)
			ret+="setHostname(";
		else if(odId == POPJParserConstants.ODSEARCH)
			ret+="setSearch(";
		else if(odId == POPJParserConstants.ODMANUAL)
			ret+="manual(";
		else if(odId == POPJParserConstants.ODWALLTIME)
			ret+="setWallTime(";
		else if(odId == POPJParserConstants.ODPOWER)
			ret+="setPower(";
		else if(odId == POPJParserConstants.ODMEMORY)
			ret+="setMemory(";
		else if(odId == POPJParserConstants.ODBANDWIDTH)
			ret+="setBandwidth(";
		else if(odId == POPJParserConstants.ODDIRECTORY)
			ret+="setDirectory(";
		else if(odId == POPJParserConstants.ODJOBURL)
			ret+="setJobUrl(";
		else if(odId == POPJParserConstants.ODEXECUTABLE)
			ret+="setCodeFile(";
		else if(odId == POPJParserConstants.ODPROTOCOL)
			ret+="setProtocol(";
		else if(odId == POPJParserConstants.ODENCODING)
			ret+="setEncoding(";

                if(odId == POPJParserConstants.ODPOWER || odId == POPJParserConstants.ODMEMORY || odId == POPJParserConstants.ODBANDWIDTH){
                    ret+=handleFloatParam()+");";
                }else{
                    ret+=params+");";
                }
                return ret;*/
        return "";
	}

   private String handleFloatParam(){
      String formattedFloat="";
      String param1=params.substring(0, params.indexOf(","));
      String param2=params.substring(params.indexOf(",")+1);
      formattedFloat+="(float)"+param1+","+"(float)"+param2;
      return formattedFloat;
   }
}
