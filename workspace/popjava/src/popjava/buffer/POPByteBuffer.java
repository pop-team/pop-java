package popjava.buffer;


import java.nio.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Davide Mazzoleni
 */
public final class POPByteBuffer {

	private long size;
	private long capacity;
	private final ByteOrder order;

	private final List<ByteBuffer> buffers = new ArrayList<>();

	public POPByteBuffer() {
		this(ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * Initialize a POP Buffer, if the initialCapacity is bigger than Integer.MAX_INT the buffer will grow of 2GB at time.
	 * @param order
	 */
	public POPByteBuffer(ByteOrder order) {
		// forced as a multiple of two for simplicity sake
		this.capacity = 16384L;
		this.order = order;
		this.size = 0;
		init();
	}

	private void init() {
		if (capacity < Integer.MAX_VALUE) {
			ByteBuffer bb = ByteBuffer.allocateDirect((int) capacity);
			bb.order(order);
			buffers.add(bb);
		} else {
			long nbBuffers = capacity % Integer.MAX_VALUE + 1;
			for (int i = 0; i < nbBuffers; i++) {
				ByteBuffer bb = ByteBuffer.allocateDirect((int) capacity);
				bb.order(order);
				buffers.add(bb);
			}
		}
	}

	public void resize(long extraBytes) {
		size += extraBytes;

		// skip
		if (size <= capacity) {
			return;
		}

		// should we resize something
		ByteBuffer first = buffers.get(0);
		// the first buffer can still be increased in capacity
		if (first.capacity() < Integer.MAX_VALUE) {
			if (size > Integer.MAX_VALUE) {
				capacity = Integer.MAX_VALUE;
			} else {
				do {
					capacity *= 2;
				} while (size > capacity);
				// we can reach the MAX_INT + 1 this way, solve that problem
				if (capacity == Integer.MAX_VALUE + 1L) {
					capacity = Integer.MAX_VALUE;
				}
			}

			ByteBuffer newFirst = ByteBuffer.allocateDirect((int) capacity);
			newFirst.order(order);

			// copy old to new
			newFirst.put(first);
			// replace in global buffers
			buffers.set(0, newFirst);
		}

		if (size > capacity) {
			// increment of MAX_INT each time it's needed
			capacity += Integer.MAX_VALUE;
			long nbBuffers = capacity / Integer.MAX_VALUE;
			// create a new ByteBuffer of max size
			for (int i = activeBuffers(); i < nbBuffers; i++) {
				// always allocate a full buffer when working with more than 2GB
				if (capacity > Integer.MAX_VALUE) {
					ByteBuffer bb = ByteBuffer.allocateDirect(Integer.MAX_VALUE);
					bb.order(order);
					buffers.add(bb);
				}
			}
		}
	}

	private int whichBuffer(long position) {
		return (int) (position % Integer.MAX_VALUE);
	}

	private int activeBuffers() {
		return buffers.size();
	}

	public long capacity() {
		return capacity;
	}

	public long position() {
		return 0L;
	}

	public void position(long i) {

	}

	public long size() {
		return size;
	}

	public byte get() {
		return 0;
	}

	public ByteBuffer put(byte b) {
		return null;
	}

	public byte get(int i) {
		return 0;
	}

	public ByteBuffer put(int i, byte b) {
		return null;
	}

	public char getChar() {
		return 0;
	}

	public ByteBuffer putChar(char c) {
		return null;
	}

	public char getChar(int i) {
		return 0;
	}

	public ByteBuffer putChar(int i, char c) {
		return null;
	}

	public short getShort() {
		return 0;
	}

	public ByteBuffer putShort(short i) {
		return null;
	}

	public short getShort(int i) {
		return 0;
	}

	public ByteBuffer putShort(int i, short i1) {
		return null;
	}

	public int getInt() {
		return 0;
	}

	public ByteBuffer putInt(int i) {
		return null;
	}

	public int getInt(int i) {
		return 0;
	}

	public ByteBuffer putInt(int i, int i1) {
		return null;
	}

	public long getLong() {
		return 0;
	}

	public ByteBuffer putLong(long l) {
		return null;
	}

	public long getLong(int i) {
		return 0;
	}

	public ByteBuffer putLong(int i, long l) {
		return null;
	}

	public float getFloat() {
		return 0;
	}

	public ByteBuffer putFloat(float v) {
		return null;
	}

	public float getFloat(int i) {
		return 0;
	}

	public ByteBuffer putFloat(int i, float v) {
		return null;
	}

	public double getDouble() {
		return 0;
	}

	public ByteBuffer putDouble(double v) {
		return null;
	}

	public double getDouble(int i) {
		return 0;
	}

	public ByteBuffer putDouble(int i, double v) {
		return null;
	}
}
