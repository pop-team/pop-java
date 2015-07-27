package testsuite.popjavatest;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncSeq;
import popjava.base.POPObject;
import popjava.base.Semantic;

@POPClass
public class POPJParclass{
	
	
	public POPJParclass(){
	}
	
	@POPSyncSeq
	public void change(SerialObject so){
		so.i = 222;
		so.test = "Changed in the parclass";
	}
	
	
	@POPSyncSeq
	public void changeByte(byte b){
		b = 1;
	}
	@POPSyncSeq
	public void changeByteArray(byte[] b){
		b[0] = 0;
		b[1] = 1;
	}
	@POPSyncSeq
	public void changeChar(char c){
		c = 'x';
	}
	@POPSyncSeq
	public void changeCharArray(char[] c){
		c[0] = 'a';
		c[1] = 'b';
	}
	@POPSyncSeq
	public void changeShort(short s){
		s = 100;
	}
	@POPSyncSeq
	public void changeShortArray(short[] s){
		s[0] = 1;
		s[1] = 2;
	}
	@POPSyncSeq
	public void changeInt(int i){
		i++;
	}
	@POPSyncSeq
	public void changeIntArray(int[] i){
		i[0] = 1;
		i[1] = 2;
	}
	@POPSyncSeq
	public void changeDouble(double d){
		d = 2.0;
	}
	@POPSyncSeq
	public void changeDoubleArray(double[] d){
		d[0] = 1.1;
		d[1] = 2.2;
	}
	@POPSyncSeq
	public void changeLong(long l){
		l = 100;
	}
	@POPSyncSeq
	public void changeLongArray(long[] l){
		l[0] = 100;
		l[1] = 200;
	}
	@POPSyncSeq
	public void changeFloat(float f){
		f = 100.0f;
	}
	@POPSyncSeq
	public void changeFloatArray(float[] f){
		f[0] = 1.0f;
		f[1] = 2.0f;
	}
	@POPSyncSeq
	public void changeBoolean(boolean b){
		b = true;
	}
	@POPSyncSeq
	public void changeBooleanArray(boolean[] b){
		b[0] = true;
		b[1] = true;
	}
	@POPSyncSeq
	public void changeString(String s){
		s = "ChangedString";
	}
	@POPSyncSeq
	public void changeStringArray(String[] s){
		s[0] = "One";
		s[1] = "Two";
	}
}
