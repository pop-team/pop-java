package testsuite.popjavatest;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class POPJParclass extends POPObject {
	
	
	public POPJParclass(){
		Class<?> c = POPJParclass.class;
		initializePOPObject();
		addSemantic(c, "change", Semantic.Synchronous | Semantic.Sequence);		
		addSemantic(c, "changeInt", Semantic.Synchronous | Semantic.Sequence);		
		
		addSemantic(c, "changeByte", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeByteArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeShort", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeShortArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeChar", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeCharArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeInt", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeIntArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeDouble", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeDoubleArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeLong", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeLongArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeFloat", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeFloatArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeBoolean", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeBooleanArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeString", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeStringArray", Semantic.Synchronous | Semantic.Sequence);
	}
	
	public void change(SerialObject so){
		so.i = 222;
		so.test = "Changed in the parclass";
	}
	
	
	
	public void changeByte(byte b){
		b = 1;
	}
	
	public void changeByteArray(byte[] b){
		b[0] = 0;
		b[1] = 1;
	}
	
	public void changeChar(char c){
		c = 'x';
	}
	
	public void changeCharArray(char[] c){
		c[0] = 'a';
		c[1] = 'b';
	}
	
	public void changeShort(short s){
		s = 100;
	}
	
	public void changeShortArray(short[] s){
		s[0] = 1;
		s[1] = 2;
	}
	
	public void changeInt(int i){
		i++;
	}
	
	public void changeIntArray(int[] i){
		i[0] = 1;
		i[1] = 2;
	}
	
	public void changeDouble(double d){
		d = 2.0;
	}
	
	public void changeDoubleArray(double[] d){
		d[0] = 1.1;
		d[1] = 2.2;
	}
	
	public void changeLong(long l){
		l = 100;
	}
	
	public void changeLongArray(long[] l){
		l[0] = 100;
		l[1] = 200;
	}
	
	public void changeFloat(float f){
		f = 100.0f;
	}
	
	public void changeFloatArray(float[] f){
		f[0] = 1.0f;
		f[1] = 2.0f;
	}
	
	public void changeBoolean(boolean b){
		b = true;
	}
	
	public void changeBooleanArray(boolean[] b){
		b[0] = true;
		b[1] = true;
	}
	
	public void changeString(String s){
		s = "ChangedString";
	}
	
	public void changeStringArray(String[] s){
		s[0] = "One";
		s[1] = "Two";
	}
}
