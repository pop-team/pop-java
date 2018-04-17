package ch.icosys.popjava.core.dataswaper;

import ch.icosys.popjava.core.buffer.POPBuffer;

/**
 * This type is used for communicate with the pop-c++ only. It is compatible
 * with the in type Be careful when use this type
 */
public interface IPOPBaseConst {
	boolean serialize(POPBuffer buffer);
}
