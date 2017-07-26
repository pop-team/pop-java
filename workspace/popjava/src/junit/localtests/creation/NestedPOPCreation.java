package junit.localtests.creation;

import org.junit.Assert;
import org.junit.Test;
import popjava.annotation.POPClass;

/**
 *
 * @author dosky
 */
@POPClass(isDistributable = false)
public class NestedPOPCreation {
	
	@Test
	public void creation() {
		ObjA a = new ObjA(10);
		ObjB b = a.getB();
		
		Assert.assertEquals(a.getbVal(), b.getVal());
	}
}
