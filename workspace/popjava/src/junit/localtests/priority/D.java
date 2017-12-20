package junit.localtests.priority;

import popjava.annotation.POPAsyncConc;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;

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
