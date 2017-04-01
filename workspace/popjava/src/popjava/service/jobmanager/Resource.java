package popjava.service.jobmanager;

/**
 * This is a generic resource for the Job Manager
 * @author Davide Mazzoleni
 */
public class Resource {
	protected float flops;
	protected float memory;
	protected float bandwidth;

	public Resource() {
	}
	
	public Resource(float flops, float memory, float bandwidth) {
		this.flops = flops;
		this.memory = memory;
		this.bandwidth = bandwidth;
	}
	
	/**
	 * Add another resource to this one, only positive values will be considered
	 * @param r Another resource
	 */
	public void add(Resource r) {
		if(r.flops > 0)
			flops += r.flops;
		if(r.memory > 0)
			memory += r.memory;
		if(r.bandwidth > 0)
			bandwidth += bandwidth;
	}
	
	/**
	 * Subtract another resource to this one, only positive values will be considered
	 * @param r Another resource
	 */
	public void subtract(Resource r) {
		if(r.flops > 0)
			flops -= r.flops;
		if(r.memory > 0)
			memory -= r.memory;
		if(r.bandwidth > 0)
			bandwidth -= bandwidth;
	}

	public float getFlops() {
		return flops;
	}

	public float getMemory() {
		return memory;
	}

	public float getBandwidth() {
		return bandwidth;
	}

	public void setFlops(float flops) {
		this.flops = flops;
	}

	public void setMemory(float memory) {
		this.memory = memory;
	}

	public void setBandwidth(float bandwidth) {
		this.bandwidth = bandwidth;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + Float.floatToIntBits(this.flops);
		hash = 59 * hash + Float.floatToIntBits(this.memory);
		hash = 59 * hash + Float.floatToIntBits(this.bandwidth);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Resource other = (Resource) obj;
		if (Float.floatToIntBits(this.flops) != Float.floatToIntBits(other.flops)) {
			return false;
		}
		if (Float.floatToIntBits(this.memory) != Float.floatToIntBits(other.memory)) {
			return false;
		}
		if (Float.floatToIntBits(this.bandwidth) != Float.floatToIntBits(other.bandwidth)) {
			return false;
		}
		return true;
	}
}
