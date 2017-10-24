package popjava.service.jobmanager.yaml;

/**
 * Describe a resource in the YAML configuration file.
 *
 * @author Davide Mazzoleni
 */
public class YamlResource {

	private float flops;
	private float bandwidth;
	private float memory;

	/**
	 * Power in flops.
	 * 
	 * @return 
	 */
	public float getFlops() {
		return flops;
	}

	/**
	 * Power in flops.
	 * 
	 * @param flops 
	 */
	public void setFlops(float flops) {
		this.flops = flops;
	}

	/**
	 * Transfer speed in kbps.
	 * 
	 * @return 
	 */
	public float getBandwidth() {
		return bandwidth;
	}

	/**
	 * Transfer speed in KiB/s.
	 * 
	 * @param bandwidth 
	 */
	public void setBandwidth(float bandwidth) {
		this.bandwidth = bandwidth;
	}

	/**
	 * Memory in MiB.
	 * 
	 * @return 
	 */
	public float getMemory() {
		return memory;
	}

	/**
	 * Memory in MiB.
	 * 
	 * @param memory 
	 */
	public void setMemory(float memory) {
		this.memory = memory;
	}
}
