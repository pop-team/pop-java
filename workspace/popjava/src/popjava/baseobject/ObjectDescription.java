package popjava.baseobject;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * This class represents the object description for a parallel object. The object description is the resource requirements for a specific parallel object.
 */
public class ObjectDescription implements IPOPBase {

	protected boolean isLocalJob;
	protected boolean isManual;
	protected int maxDepth;
	protected int waitTime;
	protected int maxSize;
	protected boolean searchSet;
	protected String hostarch;
	protected String hostcore;
	protected String hostuser;
	protected float powerMin;
	protected float powerReq;
	protected float bandwidthMin;
	protected float bandwidthReq;
	protected float memoryMin;
	protected float memoryReq;
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
	
	protected String remoteAccessPoint = ""; //Used to connect to a remote object at object creation
	
	protected String jvmParamters;
	protected ConnectionType connectionType = ConnectionType.ANY;
	protected String connectionSecret;

	private ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<String, String>();

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
		waitTime=-1;
		hostarch = "";
		hostcore = "";
		hostuser = "";
		powerMin = -1;
		powerReq = -1;
		bandwidthMin = -1;
		bandwidthReq = -1;
		memoryMin = -1;
		memoryReq = -1;
	}
	
	public ObjectDescription(ObjectDescription od) {
//		power = new ODElement();
//		memory = new ODElement();
//		bandwidth = new ODElement();
//		power.set(od.getPower());
//		memory.set(od.getMemory());
//		bandwidth.set(od.getBandwidth());
		powerMin = od.getPowerMin();
		powerReq = od.getPowerReq();
		memoryMin = od.getMemoryMin();
		memoryReq = od.getMemoryReq();
		bandwidthMin = od.getBandwidthMin();
		bandwidthReq = od.getBandwidthReq();
		wallTime = od.getWallTime();
		encoding = od.getEncoding();
		protocol = od.getProtocol();
		platform = od.getPlatform();
		hostName = od.getHostName();
		jobUrl = od.getJobUrl();
		codeFile = od.getCodeFile();
		hostuser = od.getHostuser();
		hostarch = od.getHostarch();
		hostcore = od.getHostcore();
		maxDepth = od.getSearchMaxDepth();
		maxSize =  od.getSearchMaxSize();
		waitTime = od.getSearchWaitTime();
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
		this.powerMin = min;
		this.powerReq = required;
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
		this.memoryMin = min;
		this.memoryReq = required;
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
		this.bandwidthMin = min;
		this.bandwidthReq = required;
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
		maxDepth = maxdepth;
		maxSize = maxsize;
		waitTime = waittime;
	}
	
	/**
	 * Get the OD search maximum depth value
	 * @return maximum depth value set in the OD
	 */
	public int getSearchMaxDepth(){
		return maxDepth;
	}
	
	/**
	 * Get the OD search maximum size value
	 * @return maximum size value set in the OD
	 */
	public int getSearchMaxSize(){
		return maxSize;
	}
	
	/**
	 * Get the OD search waiting time value
	 * @return waiting time value set in the OD
	 */
	public int getSearchWaitTime(){
		return waitTime;
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
	 * Sets the jvm parameters that should be used when creating this object
	 * @param parameters
	 */
	public void setJVMParamters(String parameters){
		this.jvmParamters = parameters;
	}
	
	/**
	 * Sets the connection type to be used if the object has to be created remotely
	 * @param type
	 */
	public void setConnectionType(ConnectionType type){
		connectionType = type;
	}
	
	/**
	 * Sets the secret key to be used to connect to this object.
	 * This option only makes sense when the POP-Java deamon connection type is used
	 * @param secret
	 */
	public void setConnectionSecret(String secret){
		connectionSecret = secret;
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
		return powerMin;
	}
	
	public float getPowerReq(){
		return powerReq;
	}
	
	public float getMemoryMin(){
		return memoryMin;
	}
	
	public float getMemoryReq(){
		return memoryReq;
	}
	
	public float getBandwidthMin(){
		return bandwidthMin;
	}
	
	public float getBandwidthReq(){
		return bandwidthReq;
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
	 * Returns the parameters that should be used when creating the JVM
	 * for this object
	 * @return
	 */
	public String getJVMParameters(){
		return jvmParamters;
	}
	
	/**
	 * Returns the connection type to be used if the object has to be run remotely
	 * @return
	 */
	public ConnectionType getConnectionType(){
		return connectionType;
	}
	
	public String getConnectionSecret(){
		return connectionSecret;
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
	
	public void setRemoteAccessPoint(String accessPoint){
		remoteAccessPoint = accessPoint;
	}
	
	public String getRemoteAccessPoint(){
		return remoteAccessPoint;
	}

	/**
	 * Check if the current object is empty
	 * @return true if empty
	 */
	public boolean isEmpty() {
		if (powerMin <=0 && powerReq <=0 && bandwidthMin <=0 && bandwidthReq <=0 && memoryMin <= 0 && memoryReq <=0
				&& wallTime <= 0 && encoding.length() == 0
				&& protocol.length() == 0 && platform.length() == 0
				&& hostName.length() == 0 && jobUrl.length() == 0
				&& codeFile.length() == 0)
			return true;
		return false;
	}

	/**
	 * Deserialize the object description from the buffer
	 */
	@Override
	public boolean deserialize(POPBuffer buffer) {
		float tmpPowerMin = buffer.getFloat();
		float tmpPowerReq = buffer.getFloat();
		float tmpMemoryMin = buffer.getFloat();
		float tmpMemoryReq = buffer.getFloat();
		float tmpBandwidthMin = buffer.getFloat();
		float tmpBandwidthReq = buffer.getFloat();
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
		this.setPower(tmpPowerReq, tmpPowerMin);
		this.setMemory(tmpMemoryReq, tmpMemoryMin);
		this.setBandwidth(tmpBandwidthReq, tmpBandwidthMin);
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
	public boolean serialize(POPBuffer buffer) {
		buffer.putFloat(powerReq);
		buffer.putFloat(powerMin);
		buffer.putFloat(memoryReq);
		buffer.putFloat(memoryMin);
		buffer.putFloat(bandwidthReq);
		buffer.putFloat(bandwidthMin);
//		power.serialize(buffer);
//		od_memory.serialize(buffer);
//		od_bandwidth.serialize(buffer);
		buffer.putFloat(wallTime);
		buffer.putBoolean(isManual);
		buffer.putString(cwd);
		buffer.putInt(maxDepth);
		buffer.putInt(maxSize);
		buffer.putInt(waitTime);
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
			maxDepth = od.getSearchMaxDepth();
		if(od.getSearchMaxSize() > 0)
			maxSize = od.getSearchMaxSize();
		if(od.getSearchWaitTime() >  0)
			waitTime = od.getSearchWaitTime();
	}

	/**
	 * Format the object description as a string value
	 */
	@Override
    public String toString() {
//		return "Power=" + power.toString() + "Memory=" + od_memory.toString()
//				+ "Bandwidth=" + od_bandwidth.toString() + "WallTime="
//				+ Float.toString(wallTime) + "encoding=" + encoding
//				+ "protocol=" + protocol + "platform=" + platform + "hostName="
//				+ hostName + "jobUrl=" + jobUrl + "codeFile=" + codeFile;
		return "od";
	}
}
