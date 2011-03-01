package popjava.buffer;

/**
 * Implementation of the abstract BufferFactory for the RAW encoding
 */
public class BufferXDRFactory extends BufferFactory {

	/**
	 * Identifier of this buffer
	 */
	public final String BufferName = "xdr";

	/**
	 * Create a new XDR Buffer
	 */
	@Override
	public Buffer createBuffer() {
		return new BufferXDR();
	}

	/**
	 * Get the identifier of this buffer factory
	 */
	@Override
	public String getBufferName() {
		return BufferName;
	}

}
