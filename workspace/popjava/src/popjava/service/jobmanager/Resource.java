package popjava.service.jobmanager;

/**
 * This is a generic resource for the Job Manager
 * @author Davide Mazzoleni
 */
public class Resource {
	protected int id;
	protected float flops;
	protected float memory;
	protected float bandwidth;
	
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
}
