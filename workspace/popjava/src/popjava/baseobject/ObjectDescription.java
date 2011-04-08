package popjava.baseobject;

import java.util.Enumeration;
import popjava.buffer.*;
import popjava.dataswaper.IPOPBase;

/**
 * This class represents the object description for a parallel object. The object description is the resource requirements for a specific parallel object.
 */
public class ObjectDescription implements IPOPBase {

	protected boolean isLocalJob;
	protected boolean isManual;
	protected int max_depth;
	protected int wait_time;
	protected int max_size;
	protected boolean searchSet;
	protected String hostarch;
	protected String hostcore;
	protected String hostuser;
	protected float power_min;
	protected float power_req;
	protected float bandwidth_min;
	protected float bandwidth_req;
	protected float memory_min;
	protected float memory_req;
//	protected ODElement power;
//	protected ODElement od_memory;
//	protected ODElement od_bandwidth;
	protected float wallTime;
	protected String encoding;
	protected String protocol;
	protected String platform;
	protected String hostName;
	protected String jobUrl;
	protected String codeFile;
	protected String cwd;
	protected String batch;

	private java.util.concurrent.ConcurrentHashMap<String, String> attributes = new java.util.concurrent.ConcurrentHashMap<String, String>();

	/**
	 * Create a new empty instance of ObjectDescription
	 */
	public ObjectDescription() {
//		power = new ODElement();
//		od_memory = new ODElement();
//		od_bandwidth = new ODElement();
		wallTime = -1;
		encoding = "";
		protocol = "";
		platform = "*-*";
		hostName = "";
		jobUrl = "";
		codeFile = "";
		cwd ="";
		batch = "";
		wait_time=-1;
		hostarch = "";
		hostcore = "";
		hostuser = "";
		power_min = -1;
		power_req = -1;
		bandwidth_min = -1;
		bandwidth_req = -1;
		memory_min = -1;
		memory_req = -1;

	}
	
	/**
	 * Set the directory OD
	 * @param d	Specific directory
	 */
	public void setDirectory(String d){
		cwd = d;
	}

	/**
	 * Set the power OD by ODElement
	 * @param power	ODElement specifying the required and minimum values
	 */
//	public void setPower(ODElement power) {
//		this.power = power;
//	}

	/**
	 * Set the power OD by values
	 * @param required	The required power
	 * @param min		The minimum power
	 */
	public void setPower(float required, float min) {
		this.power_min = min;
		this.power_req = required;
//		ODElement tmp = new ODElement(required, min);
//		power = tmp;
//		this.power.requiredValue = required;
//		this.power.minValue = min;
	}

	/**
	 * Set the memory OD by ODElement
	 * @param memory	ODElement specifying the required and minimum values
	 */
//	public void setMemory(ODElement memory) {
//		this.od_memory = memory;
//	}

	/**
	 * Set the memory OD by values
	 * @param required	The required memory
	 * @param min		The minimum memory
	 */
	public void setMemory(float required, float min) {
		this.memory_min = min;
		this.memory_req = required;
//		ODElement tmp = new ODElement(required, min); 
//		
//		od_memory = tmp;
//		this.memory.setMinValue(min);
//		this.memory.setRequiredValue(required);
	}

	/**
	 * Set the bandwidth OD by ODELement
	 * @param bandwidth	ODElement specifying the required and minimum values
	 */
//	public void setBandwidth(ODElement bandwidth) {
//		this.od_bandwidth = bandwidth;
//	}

	/**
	 * Set the bandwidth OD by values
	 * @param required	The required bandwidth
	 * @param min		The minimum bandwidth
	 */
	public void setBandwidth(float required, float min) {
		this.bandwidth_min = min;
		this.bandwidth_req = required;
//		this.od_bandwidth.setMinValue(min);
//		this.od_bandwidth.setRequiredValue(required);
	}

	/**
	 * Set the walltime OD
	 * @param walltime	time allocated for the wall execution
	 */
	public void setWallTime(float walltime) {
		this.wallTime = walltime;
	}
	
	/**
	 * Set the manual OD
	 * @param a	true = manual
	 */
	public void manual(boolean a){
		isManual = a;
	}
	
	/**
	 * Set the search OD values 
	 * @param maxdepth	The maximum depth for the search algorithm
	 * @param maxsize	The maximum size of a search request
	 * @param waittime	The waiting time of the search algorithm (0 = take the first answer)
	 */
	public void setSearch(int maxdepth, int maxsize, int waittime){
		searchSet = true;
		max_depth = maxdepth;
		max_size = maxsize;
		wait_time = waittime;
	}
	
	/**
	 * Get the OD search maximum depth value
	 * @return maximum depth value set in the OD
	 */
	public int getSearchMaxDepth(){
		return max_depth;
	}
	
	/**
	 * Get the OD search maximum size value
	 * @return maximum size value set in the OD
	 */
	public int getSearchMaxSize(){
		return max_size;
	}
	
	/**
	 * Get the OD search waiting time value
	 * @return waiting time value set in the OD
	 */
	public int getSearchWaitTime(){
		return wait_time;
	}
	
	/**
	 * Say if the OD search is set
	 * @return true if the OD search is set
	 */
	public boolean isSearchSet(){
		return searchSet;
	}
	
	/**
	 * Set the OD host name value
	 * @param hostname	host name to execute the object
	 */
	public void setHostname(String hostname) {
		this.hostName = hostname;
	}
	
	/**
	 * Set the OD host architecture value
	 * @param arch host architecture to execute the object
	 */
	public void setHostarch(String arch){
		hostarch = arch;
	}
	
	/**
	 * Get the OD host architecture value
	 * @return host architecture value set in the OD
	 */
	public String getHostarch(){
		return hostarch;
	}
	
	/**
	 * Set the OD host core value
	 * @param core core value
	 */
	public void setHostcore(String core){
		hostcore = core;
	}
	
	/**
	 * Get the OD host core value
	 * @return host core value set in this OD
	 */
	public String getHostcore(){
		return hostcore;
	}
	
	/**
	 * Set the OD host user value
	 * @param user USer to execute the object
	 */
	public void setHostuser(String user){
		hostuser = user;
	}
	
	/**
	 * Get the OD host user value
	 * @return host user value set in this OD
	 */
	public String getHostuser(){
		return hostuser;
	}
	
	/**
	 * Set the OD batch value
	 * @param batch	batch value
	 */
	public void setBatch(String batch){
		this.batch = batch;
	}
	
	/**
	 * Get the OD batch value
	 * @return batch value set in this OD
	 */
	public String getBatch(){
		return batch;
	}

	/**
	 * Set the OD JobUrl value
	 * @param jobUrl	job manager access point
	 */
	public void setJobUrl(String jobUrl) {
		this.jobUrl = jobUrl;
	}

	/**
	 * Set the OD Code file value
	 * @param codeFile	Get the OD code file value
	 */
	public void setCodeFile(String codeFile) {
		this.codeFile = codeFile;
	}

	/**
	 * Set the OD protocol value
	 * @param protocol	protocol to be used to communicate with the object
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Set the OD encoding value
	 * @param encoding	encoding to be used to communicate with the object
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	/**
	 * Set the OD platform value
	 * @param platform	platform on which the object must be executed
	 */
	public void setPlatform(String platform) {
		this.platform = platform;
	}

	/**
	 * Get the OD power value
	 * @return power value set in this OD
	 */
//	public ODElement getPower() {
//		return power;
//	}

	/**
	 * Get the OD memory value
	 * @return	memory value set in this OD
	 */
//	public ODElement getMemory() {
//		return od_memory;
//	}

	/**
	 * Get the OD bandwith value
	 * @return bandwith value set in this OD
	 */
//	public ODElement getBandwidth() {
//		return od_bandwidth;
//	}
	
	public float getPowerMin(){
		return power_min;
	}
	
	public float getPowerReq(){
		return power_req;
	}
	
	public float getMemoryMin(){
		return memory_min;
	}
	
	public float getMemoryReq(){
		return memory_req;
	}
	
	public float getBandwidthMin(){
		return bandwidth_min;
	}
	
	public float getBandwidthReq(){
		return bandwidth_req;
	}

	/**
	 * Get the OD walltime value
	 * @return walltime value set in this OD
	 */
	public float getWallTime() {
		return wallTime;
	}

	/**
	 * Get the OD hostname value
	 * @return hostname set in this OD
	 */
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * Get the OD JobUrl value
	 * @return joburl set in this OD
	 */
	public String getJobUrl() {
		return jobUrl;
	}

	/**
	 * Get the OD protocol value
	 * @return protocol set in this OD
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Get the OD encoding value
	 * @return encoding set in this OD
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Get he OD platform value
	 * @return platform set in this OD
	 */
	public String getPlatform() {
		return platform;
	}
	
	/**
	 * Get the OD code file value
	 * @return codefile set in this OD
	 */
	public String getCodeFile() {
		return codeFile;
	}

	/**
	 * Set a specific attribute in the list
	 * @param key	Key for this attribute
	 * @param value	value for this attribute
	 */
	public void setValue(String key, String value) {
		if (attributes.containsKey(key))
			attributes.replace(key, value);
		else
			attributes.put(key, value);
	}

	/**
	 * Get a specific attribute from the list
	 * @param key	Key of the specific attribute
	 * @return	Value of the attribute or an empty string
	 */
	public String getValue(String key) {
		if (attributes.containsKey(key))
			return attributes.get(key);
		else
			return "";
	}

	/**
	 * Remove a specific attribute from the list
	 * @param key	Key of the attribute to be removed
	 */
	public void removeValue(String key) {
		if (attributes.containsKey(key))
			attributes.remove(key);
	}

	/**
	 * Remove all attributes from the list
	 */
	public void removeAllAttributes() {
		attributes.clear();
	}

	/**
	 * Check if the current object is empty
	 * @return true if empty
	 */
	public boolean isEmpty() {
		if (power_min <=0 && power_req <=0 && bandwidth_min <=0 && bandwidth_req <=0 && memory_min <= 0 && memory_req <=0
				&& wallTime <= 0 && encoding.length() == 0
				&& protocol.length() == 0 && platform.length() == 0
				&& hostName.length() == 0 && jobUrl.length() == 0
				&& codeFile.length() == 0)
			return true;
		return false;
	}

	/**
	 * Method called before destruction
	 */
	@Override
	protected void finalize() throws Throwable {
		try {

		} finally {
			super.finalize();
		}
	}

	/**
	 * Deserialize the object description from the buffer
	 */
	@Override
	public boolean deserialize(Buffer buffer) {
		float tmp_power_min = buffer.getFloat();
		float tmp_power_req = buffer.getFloat();
		float tmp_memory_min = buffer.getFloat();
		float tmp_memory_req = buffer.getFloat();
		float tmp_bandwidth_min = buffer.getFloat();
		float tmp_bandwidth_req = buffer.getFloat();
//		ODElement power = ODElement.deserialize(buffer);
//		ODElement memory = ODElement.deserialize(buffer);
//		ODElement bandwidth = ODElement.deserialize(buffer);
		float walltime = buffer.getFloat();
		boolean isManual = buffer.getBoolean();
		String cwd = buffer.getString();
		int maxDepth = buffer.getInt();
		int maxSize = buffer.getInt();
		int waitTime = buffer.getInt();
		String hostName = buffer.getString();
		String hostuser = buffer.getString();
		String hostCore = buffer.getString();
		String hostArch = buffer.getString();
		String batch = buffer.getString();
		String jobUrl = buffer.getString();
		String codeFile = buffer.getString();
		String platform = buffer.getString();
		String protocol = buffer.getString();
		String encoding = buffer.getString();
		this.setPower(tmp_power_req, tmp_power_min);
		this.setMemory(tmp_memory_req, tmp_memory_min);
		this.setBandwidth(tmp_bandwidth_req, tmp_bandwidth_min);
		this.setWallTime(walltime);
		this.manual(isManual);
		this.setDirectory(cwd);
		this.setSearch(maxDepth, maxSize, waitTime);
		this.setHostname(hostName);
		this.setHostuser(hostuser);
		this.setHostcore(hostCore);
		this.setHostarch(hostArch);
		this.setBatch(batch);
		this.setJobUrl(jobUrl);
		this.setCodeFile(codeFile);
		this.setPlatform(platform);
		this.setProtocol(protocol);
		this.setEncoding(encoding);

		// put the attributes
		this.attributes.clear();
		int attributeCount = buffer.getInt();
		for (int i = 0; i < attributeCount; i++) {
			String key = buffer.getString();
			String value = buffer.getString();
			setValue(key, value);
		}
		return true;
	}

	/**
	 * Serialize the object description into the buffer
	 */
	@Override
	public boolean serialize(Buffer buffer) {
		buffer.putFloat(power_req);
		buffer.putFloat(power_min);
		buffer.putFloat(memory_req);
		buffer.putFloat(memory_min);
		buffer.putFloat(bandwidth_req);
		buffer.putFloat(bandwidth_min);
//		power.serialize(buffer);
//		od_memory.serialize(buffer);
//		od_bandwidth.serialize(buffer);
		buffer.putFloat(wallTime);
		buffer.putBoolean(isManual);
		buffer.putString(cwd);
		buffer.putInt(max_depth);
		buffer.putInt(max_size);
		buffer.putInt(wait_time);
		buffer.putString(hostName);
		buffer.putString(hostuser);
		buffer.putString(hostcore);
		buffer.putString(hostarch);
		buffer.putString(batch);
		buffer.putString(jobUrl);
		buffer.putString(codeFile);
		buffer.putString(platform);
		buffer.putString(protocol);
		buffer.putString(encoding);
		// put the attributes
		buffer.putInt(attributes.size());
		Enumeration<String> keys = attributes.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			buffer.putString(key);
			buffer.putString(attributes.get(key));
		}
		return true;
	}

	/**
	 * Merge another object description with this object description
	 * @param od	The object description to be merged with this one
	 */
	public void merge(ObjectDescription od) {
		if (od.getPowerMin() > 0 && od.getPowerReq() > 0)
			setPower(od.getPowerMin(), od.getPowerReq());
		if(od.getBandwidthMin() > 0 && od.getBandwidthReq() > 0)
			setBandwidth(od.getBandwidthReq(), od.getBandwidthMin());
		if(od.getMemoryMin() > 0 && od.getMemoryReq() > 0)
			setMemory(od.getMemoryReq(), od.getMemoryMin());
//		if (!od.getPower().isEmpty())
//			power.set(od.getPower());
//		if (!od.getMemory().isEmpty())
//			od_memory.set(od.getMemory());
//		if (!od.getBandwidth().isEmpty())
//			od_bandwidth.set(od.getBandwidth());
		if (od.getWallTime() > 0)
			wallTime = od.getWallTime();
		if (od.getEncoding().length() > 0)
			encoding = od.getEncoding();
		if (od.getProtocol().length() > 0)
			protocol = od.getProtocol();
		if (od.getPlatform().length() > 0)
			platform = od.getPlatform();
		if (od.getHostName().length() > 0)
			hostName = od.getHostName();
		if (od.getJobUrl().length() > 0)
			jobUrl = od.getJobUrl();
		if (od.getCodeFile().length() > 0)
			codeFile = od.getCodeFile();
		if(od.getSearchMaxDepth() > 0)
			max_depth = od.getSearchMaxDepth();
		if(od.getSearchMaxSize() > 0)
			max_size = od.getSearchMaxSize();
		if(od.getSearchWaitTime() >  0)
			wait_time = od.getSearchWaitTime();
	}

	/**
	 * Format the object description as a string value
	 */
	public String toString() {
//		return "Power=" + power.toString() + "Memory=" + od_memory.toString()
//				+ "Bandwidth=" + od_bandwidth.toString() + "WallTime="
//				+ Float.toString(wallTime) + "encoding=" + encoding
//				+ "protocol=" + protocol + "platform=" + platform + "hostName="
//				+ hostName + "jobUrl=" + jobUrl + "codeFile=" + codeFile;
		return "od";
	}
}
