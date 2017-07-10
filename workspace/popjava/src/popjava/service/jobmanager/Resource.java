package popjava.service.jobmanager;

import popjava.baseobject.ObjectDescription;
import popjava.buffer.POPBuffer;
import popjava.dataswaper.IPOPBase;

/**
 * This is a generic resource for the Job Manager
 *
 * @author Davide Mazzoleni
 */
public class Resource implements IPOPBase {

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

	Resource(Resource r) {
		this(r.flops, r.memory, r.bandwidth);
	}

	/**
	 * Add another resource to this one, only positive values will be considered
	 *
	 * @param r Another resource
	 */
	public void add(Resource r) {
		if (r.flops > 0) {
			flops += r.flops;
		}
		if (r.memory > 0) {
			memory += r.memory;
		}
		if (r.bandwidth > 0) {
			bandwidth += bandwidth;
		}
	}

	/**
	 * Set this resource in the OD
	 *
	 * @param od A initialized OD
	 */
	public void addTo(ObjectDescription od) {
		od.setBandwidth(bandwidth, bandwidth);
		od.setPower(flops, flops);
		od.setMemory(memory, memory);
	}

	/**
	 * Subtract another resource to this one, only positive values will be considered
	 *
	 * @param r Another resource
	 */
	public void subtract(Resource r) {
		if (r.flops > 0) {
			flops -= r.flops;
		}
		if (r.memory > 0) {
			memory -= r.memory;
		}
		if (r.bandwidth > 0) {
			bandwidth -= bandwidth;
		}
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

	public boolean canHandle(Resource resource) {
		boolean canHandle = true;
		canHandle &= flops >= resource.flops;
		canHandle &= memory >= resource.memory;
		canHandle &= bandwidth >= resource.bandwidth;
		return canHandle;
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

	@Override
	public boolean serialize(POPBuffer buffer) {
		buffer.putFloat(flops);
		buffer.putFloat(memory);
		buffer.putFloat(bandwidth);
		return true;
	}

	@Override
	public boolean deserialize(POPBuffer buffer) {
		flops = buffer.getFloat();
		memory = buffer.getFloat();
		bandwidth = buffer.getFloat();
		return true;
	}

	@Override
	public String toString() {
		return String.format("power=%f memory=%f bandwidth=%f", flops, memory, bandwidth);
	}
}
