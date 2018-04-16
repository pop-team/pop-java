package ch.icosys.popjava.core.buffer;

/**
 * Implementation of the abstract BufferFactory for the RAW encoding
 */
public class BufferRawFactory extends BufferFactory {

	/**
	 * Identifier of this buffer
	 */
	public final String BufferName = "raw";

	/**
	 * Create a new RAW factory
	 */
	@Override
	public POPBuffer createBuffer() {
		return new BufferRaw();
	}

	/**
	 * Get the identifier of this factory
	 */
	@Override
	public String getBufferName() {
		return BufferName;
	}

}
