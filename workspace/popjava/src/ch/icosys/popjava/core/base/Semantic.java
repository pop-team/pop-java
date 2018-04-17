package ch.icosys.popjava.core.base;

/**
 * This class class is used to store the different semantics used in the POP
 * model. The different semantics from this class can be combined with the |
 * operator. Synchronous and Asynchronous must not be combined together.
 * Concurrent, Sequence and mutex must not be combined together.
 */
public class Semantic {
	public static final int SYNCHRONOUS = 1;

	public static final int ASYNCHRONOUS = 0;

	public static final int CONSTRUCTOR = 4;

	public static final int CONCURRENT = 8;

	public static final int MUTEX = 16;

	public static final int SEQUENCE = 0;

	public static final int LOCALHOST = 256;
}
