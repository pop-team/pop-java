package junit.localtests.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

import junit.localtests.annotations.objects.AbstractChild;
import junit.localtests.annotations.objects.Child;
import junit.localtests.annotations.objects.ConcreteChild;
import junit.localtests.annotations.objects.Integer;
import junit.localtests.annotations.objects.Parent;
import popjava.PopJava;
import popjava.base.MethodInfo;
import popjava.codemanager.POPJavaAppService;
import popjava.system.POPSystem;

public class AnnotationsTest {

	@Test
	public void testAnnotations() {
		POPSystem.initialize();
		Integer i1 = PopJava.newActive(Integer.class /* C */, "localhost");
		assertNotNull(i1);
		Integer i2 = PopJava.newActive(Integer.class /* C */, "localhost");
		assertNotNull(i2);
		
		// Create an array
		Integer[] tab = new Integer[3];
		
		for (int i = 0; i < tab.length; i++) {
			tab[i] = PopJava.newActive(Integer.class /* C */, "localhost");
			assertNotNull(tab[i]);
		}
		
		i1.set(11);
		
		i2.set(14);
		System.out.println("i1=" + i1.get());
		System.out.println("i2=" + i2.get());
		i1.add(i2);
		System.out.println("i1+i2=" + i1.get());
		int[] testArray = new int[] { 1, 2, 3 };
		int temp = i1.arrayChanger(testArray);
		assertEquals(19, temp);
		
		assertEquals(2, testArray[0]);
		assertEquals(4, testArray[1]);
		assertEquals(6, testArray[2]);
		temp = i1.arrayChanger2(testArray);
		assertEquals(19, temp);
		assertEquals(2, testArray[0]);
		assertEquals(4, testArray[1]);
		assertEquals(6, testArray[2]);
		assertTrue(i1.arrayChanger3(testArray));
		
		POPSystem.end();
	}

	@Test
	public void testMethodId() throws NoSuchMethodException{
		POPSystem.initialize();
		
		POPJavaAppService appservice = PopJava.newActive(POPJavaAppService.class);
	    
	    int classId = appservice.getClassId();
	    
	    assertEquals(99923, classId);
	    MethodInfo info = new MethodInfo(classId, 14);
	    Method method = appservice.getMethodByInfo(info);
	    
	    assertNotNull(method);
	    
	    POPSystem.end();
	}
	
	@Test
	public void testInheritedMethodId() throws NoSuchMethodException{
		POPSystem.initialize();
	    Parent parent = PopJava.newActive(Parent.class);
	    assertEquals(1234, parent.getClassId());
	    
	    Child child = PopJava.newActive(Child.class);
	    assertEquals(1235, child.getClassId());
	    
	    MethodInfo info = new MethodInfo(parent.getClassId(), 20);
        Method method = parent.getMethodByInfo(info);
        assertNotNull(method);
        
        info = new MethodInfo(parent.getClassId(), 21);
        method = parent.getMethodByInfo(info);
        assertNotNull(method);
        
        info = new MethodInfo(child.getClassId(), 20);
        method = child.getMethodByInfo(info);
        assertNotNull(method);
        
        System.out.println(method.getName()+" "+method.getDeclaringClass().getName());
        
        /*info = new MethodInfo(parent.getClassId(), 20);
        method = child.getMethodByInfo(info);
        assertNotNull(method);
        
        System.out.println(method.getName()+" "+method.getDeclaringClass().getName());*/
        
        info = new MethodInfo(child.getClassId(), 22);
        method = child.getMethodByInfo(info);
        assertNotNull(method);
        POPSystem.end();
	}
	
	@Test
	public void testInheritedMethodIdBug() throws NoSuchMethodException{
		POPSystem.initialize();
		
	    Child child = PopJava.newActive(Child.class);
	    assertEquals(1235, child.getClassId());
	    
	    MethodInfo info = new MethodInfo(1234, 20);
        Method method = child.getMethodByInfo(info);
        assertNotNull(method);
        
        POPSystem.end();
	}
	
	@Test
    public void testParentClassCall() throws NoSuchMethodException{
		POPSystem.initialize();
		
        Child child = PopJava.newActive(Child.class);
        assertEquals(1235, child.getClassId());
        
        child.parentTest();
        
        POPSystem.end();
    }
	
	@Test
	public void testAbstractClass(){
		POPSystem.initialize();
		
		AbstractChild child = PopJava.newActive(ConcreteChild.class);
		
		assertEquals("C", child.nonInheritedTest());
		assertEquals("B", child.testNonAbstract());
		assertEquals("My String2", child.getStuff2());
		assertEquals("My String", child.getStuff());
		
		POPSystem.end();
	}
}
