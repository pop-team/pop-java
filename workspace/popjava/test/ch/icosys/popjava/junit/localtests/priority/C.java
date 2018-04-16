package ch.icosys.popjava.junit.localtests.priority;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.annotation.POPObjectDescription;

/**
 *
 * @author dosky
 */
@POPClass
public class C extends B {

	@POPObjectDescription(url = "localhost")
	public C() {
	}

	@Override
	public void a() {
		super.a();
	}

	@Override
	public void b() {
		super.b();
	}
	
}
