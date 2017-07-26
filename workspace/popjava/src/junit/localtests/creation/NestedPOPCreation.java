package junit.localtests.creation;

import org.junit.Assert;
import org.junit.Test;
import popjava.PopJava;

/**
 *
 * @author dosky
 */
public class NestedPOPCreation {
	
	@Test(expected = Exception.class)
	public void creation() {
		ObjA a = PopJava.newActive(ObjA.class, 10);
		ObjB b = a.getB();
		
		Assert.assertEquals(a.getbVal(), b.getVal());
	}
}
