package testsuite.popjavatest;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class POPJParclass extends POPObject {
	
	
	public POPJParclass(){
		Class<?> c = POPJParclass.class;
		initializePOPObject();
		addSemantic(c, "change", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);		
		addSemantic(c, "changeInt", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);		
		
		addSemantic(c, "changeByte", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeByteArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeShort", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeShortArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeChar", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeCharArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeInt", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeIntArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeDouble", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeDoubleArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeLong", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeLongArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeFloat", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeFloatArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeBoolean", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeBooleanArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeString", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeStringArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
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
