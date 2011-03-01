//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.base.POPObject;
import popjava.base.Semantic;

public class Integer extends POPObject  {
	private int value;
	public Integer() {
		Class<?> c = Integer.class;
		setClassId(1000);
		hasDestructor(true);
		initializePOPObject(c);
		addSemantic(c, "get", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "add", Semantic.Synchronous | Semantic.Mutex);
		addSemantic(c, "set", Semantic.Asynchronous | Semantic.Sequence);
		value = 0;
	}
	public int get ( ) 	{
		 return value;
	}
	public void add ( Integer i) 	throws POPException {
		i=(Integer)PopJava.newActive(Integer.class, i.getAccessPoint());
		 value += i.get();
	}
	public void set ( int val ) 	{
		 value = val;
	}
	}
