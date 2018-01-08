package junit.localtests.priority;

import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;

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
