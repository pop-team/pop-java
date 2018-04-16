package testsuite.javatest;

public class TestObject {
	
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
	
	public void changeBoolean(boolean[] b){
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
	
	public void changeInteger(Integer i){
		i++;
	}
	
	public void changeDouble(Double d){
		d = new Double(3.0);
	}
	
	public void changeInsideClass(InsideClass ic){
		ic.i = 10;
		ic.test = "ChangedString";
	}

}
