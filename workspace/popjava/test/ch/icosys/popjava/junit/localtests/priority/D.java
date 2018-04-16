package ch.icosys.popjava.junit.localtests.priority;

import ch.icosys.popjava.core.annotation.POPAsyncConc;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;

/**
 *
 * @author dosky
 */
@POPClass
public class D extends C {

	@POPObjectDescription(url = "localhost")
	public D() {
	}

	@POPAsyncConc
	@Override
	public void a() {
		super.a();
	}

	@POPAsyncConc
	@Override
	public void b() {
		super.b();
	}
}
