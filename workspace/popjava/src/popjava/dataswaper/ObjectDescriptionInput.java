package popjava.dataswaper;

import java.util.Enumeration;

import popjava.baseobject.ObjectDescription;
import popjava.buffer.*;

/**
 * Compatible implementation of the ObjectDescription POP-Java object for POP-C++
 *
 */
public class ObjectDescriptionInput implements IPOPBaseInput {

//	protected popjava.baseobject.ODElement power;
//	protected ODElement memory;
//	protected ODElement bandwidth;
	protected float power_min;
	protected float power_req;
	protected float bandwidth_min;
	protected float bandwidth_req;
	protected float memory_min;
	protected float memory_req;
	protected float wallTime;
	protected boolean isManual;
	protected String cwd;
	protected int searchMaxDepth;
	protected int searchMaxReq;
	protected int searchWaitingtime;
	protected String url;
	protected String user;
	protected String core;
	protected String batch;
	protected String encoding;
	protected String arch;
	protected String hostName;
	protected String jobUrl;
	protected String codeFile;
	protected String platform;
	protected String protocol;

	private java.util.concurrent.ConcurrentHashMap<String, String> attributes = new java.util.concurrent.ConcurrentHashMap<String, String>();

	/**
	 * Create a new empty instance of ObjectDescriptionInput
	 */
	public ObjectDescriptionInput() {
//		power = new ODElement();
//		memory = new ODElement();
//		bandwidth = new ODElement();
		wallTime = -1;
		encoding = "";
		protocol = "";
		platform = "*-*";
		hostName = "";
		jobUrl = "";
		codeFile = "";
		searchMaxDepth = -1;
		searchMaxReq = -1;
		searchWaitingtime = -1;
		power_min = -1;
		power_req = -1;
		bandwidth_min = -1;
		bandwidth_req = -1;
		memory_min = -1;
		memory_req = -1;
	}

	/**
	 * Create a new instance of ObjectDescriptionInput from an ObjectDescritption
	 * @param od	The base object description
	 */
	public ObjectDescriptionInput(ObjectDescription od) {
//		power = new ODElement();
//		memory = new ODElement();
//		bandwidth = new ODElement();
//		power.set(od.getPower());
//		memory.set(od.getMemory());
//		bandwidth.set(od.getBandwidth());
		power_min = od.getPowerMin();
		power_req = od.getPowerReq();
		memory_min = od.getMemoryMin();
		memory_req = od.getMemoryReq();
		bandwidth_min = od.getBandwidthMin();
		bandwidth_req = od.getBandwidthReq();
		wallTime = od.getWallTime();
		encoding = od.getEncoding();
		protocol = od.getProtocol();
		platform = od.getPlatform();
		hostName = od.getHostName();
		jobUrl = od.getJobUrl();
		codeFile = od.getCodeFile();
		user = od.getHostuser();
		arch = od.getHostarch();
		core = od.getHostcore();
		searchMaxDepth = od.getSearchMaxDepth();
		searchMaxReq =  od.getSearchMaxSize();
		searchWaitingtime = od.getSearchWaitTime();
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
		power_req = required;
		power_min = min;
	}

	/**
	 * Set the memory OD by ODElement
	 * @param memory	ODElement specifying the required and minimum values
	 */
//	public void setMemory(ODElement memory) {
//		this.memory = memory;
//	}

	/**
	 * Set the memory OD by values
	 * @param required	The required memory
	 * @param min		The minimum memory
	 */
	public void setMemory(float required, float min) {
		memory_req = required;
		memory_min = min;
	}

	/**
	 * Set the bandwidth OD by ODELement
	 * @param bandwidth	ODElement specifying the required and minimum values
	 */
//	public void setBandwidth(ODElement bandwidth) {
//		this.bandwidth = bandwidth;
//	}

	/**
	 * Set the bandwidth OD by values
	 * @param required	The required bandwidth
	 * @param min		The minimum bandwidth
	 */
	public void setBandwidth(float required, float min) {
		bandwidth_req = required;
		bandwidth_min = min;
	}

	/**
	 * Set the walltime OD
	 * @param walltime	time allocated for the wall execution
	 */
	public void setWallTime(float walltime) {
		this.wallTime = walltime;
	}

	/**
	 * Set the OD host name value
	 * @param hostname	host name to execute the object
	 */
	public void setHostname(String hostname) {
		this.hostName = hostname;
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
//		return memory;
//	}

	/**
	 * Get the OD bandwith value
	 * @return bandwith value set in this OD
	 */
//	public ODElement getBandwidth() {
//		return bandwidth;
//	}

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
	 * Set the search OD values 
	 * @param depth		The maximum depth for the search algorithm
	 * @param size		The maximum size of a search request
	 * @param waittime	The waiting time of the search algorithm (0 = take the first answer)
	 */
	public void setSearch(int depth, int size, int waittime){
		searchMaxDepth = depth;
		searchMaxReq = size;
		searchWaitingtime = waittime;
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
	 * Serialize the object description into the buffer
	 */
	@Override
	public boolean serialize(Buffer buffer) {
//		power.serialize(buffer);
//		memory.serialize(buffer);
//		bandwidth.serialize(buffer);
		buffer.putFloat(power_req);
		buffer.putFloat(power_min);
		buffer.putFloat(memory_req);
		buffer.putFloat(memory_min);
		buffer.putFloat(bandwidth_req);
		buffer.putFloat(bandwidth_min);
		buffer.putFloat(wallTime);
		buffer.putBoolean(isManual);
		buffer.putString(cwd);
		buffer.putInt(searchMaxDepth);
		buffer.putInt(searchMaxReq);
		buffer.putInt(searchWaitingtime);
		buffer.putString(hostName);
		buffer.putString(user);
		buffer.putString(core);
		buffer.putString(arch);
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
	}
	
	/**
	 * Format the object description as a string value
	 */
	public String toString() {
//		return "Power=" + power.toString() + "Memory=" + memory.toString()
//				+ "Bandwidth=" + bandwidth.toString() + "WallTime="
//				+ Float.toString(wallTime) + "encoding=" + encoding
//				+ "protocol=" + protocol + "platform=" + platform + "hostName="
//				+ hostName + "jobUrl=" + jobUrl + "codeFile=" + codeFile;
		return "od";
	}

	/**
	 * Deserialize the object description from the buffer
	 */
	@Override
	public boolean deserialize(Buffer buffer) {
//		ODElement power = ODElement.deserialize(buffer);
//		ODElement memory = ODElement.deserialize(buffer);
//		ODElement bandwidth = ODElement.deserialize(buffer);
//		float walltime = buffer.getFloat();
//		boolean isManual = buffer.getBoolean();
//		String cwd = buffer.getString();
//		int searchMaxDepth = buffer.getInt();
//		int searchMacReq = buffer.getInt();
//		int waitingtime = buffer.getInt();
//		String hostName = buffer.getString();
//		String user = buffer.getString();
//		String core = buffer.getString();
//		String arch = buffer.getString();
//		String batch = buffer.getString();
//		String jobUrl = buffer.getString();
//		String codeFile = buffer.getString();
//		String platform = buffer.getString();
//		String protocol = buffer.getString();
//		String encoding = buffer.getString();
//		this.setPower(power);
//		this.setMemory(memory);
//		this.setBandwidth(bandwidth);
//		this.setWallTime(walltime);
//		this.setHostname(hostName);
//		this.setJobUrl(jobUrl);
//		this.setCodeFile(codeFile);
//		this.setPlatform(platform);
//		this.setProtocol(protocol);
//		this.setEncoding(encoding);
//		this.setHostname(hostName);
//
//		// put the attributes
//		this.attributes.clear();
//		int attributeCount = buffer.getInt();
//		for (int i = 0; i < attributeCount; i++) {
//			String key = buffer.getString();
//			String value = buffer.getString();
//			setValue(key, value);
//		}
		return true;
	}
}