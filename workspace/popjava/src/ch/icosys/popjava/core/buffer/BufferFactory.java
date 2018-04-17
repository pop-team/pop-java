package ch.icosys.popjava.core.buffer;

/**
 * This abstract class defined all the methods needed by a BufferFactory
 */
public abstract class BufferFactory {

	/**
	 * Creates a new instance of BufferFactory
	 */
	public abstract POPBuffer createBuffer();

	/**
	 * Return buffer's names
	 * 
	 * @return name of the buffers
	 */
	public abstract String getBufferName();

}