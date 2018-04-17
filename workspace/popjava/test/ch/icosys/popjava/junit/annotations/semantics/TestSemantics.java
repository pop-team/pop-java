package ch.icosys.popjava.junit.annotations.semantics;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.icosys.popjava.core.base.Semantic;

public class TestSemantics {

	@Test
	public void testSemantics() throws NoSuchMethodException, SecurityException {
		SemanticObject obj = new SemanticObject();
		obj.loadPOPAnnotations(obj.getClass().getConstructor());

		assertEquals(Semantic.SYNCHRONOUS | Semantic.CONCURRENT,
				obj.getSemantic(SemanticObject.class.getMethod("testSyncConc")));
	}

}
