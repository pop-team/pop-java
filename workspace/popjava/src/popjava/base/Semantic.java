package popjava.base;

/**
 * This class class is used to store the different semantics used in the POP model.
 * The different semantics from this class can be combined with the | operator. 
 * Synchronous and Asynchronous must not be combined together. 
 * Concurrent, Sequence and mutex must not be combined together. 
 */
public class Semantic {
	static public final int Synchronous = 1;
	static public final int Asynchronous = 0;
	static public final int Constructor = 4;
	static public final int Concurrent = 8;
	static public final int Mutex = 16;
	static public final int Sequence = 0;
}
