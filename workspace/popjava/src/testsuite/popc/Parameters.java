package testsuite.popc;

import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.dataswaper.POPString;

public class Parameters  extends POPObject {
	public Parameters(){
		Class<?> c = Parameters.class;
		setClassId(1000);
		setClassName("Parameters");
		hasDestructor(true);
		od.setEncoding("xdr");
		initializePOPObject();
		addSemantic(c, "changeBool", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeBoolArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeChar", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeCharArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeDouble", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeDoubleArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeFloat", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeFloatArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeInt", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeIntArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeLong", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeLongArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeShort", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeShortArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeString", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeStringArray", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeX", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getBool", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getChar", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getDouble", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getFloat", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getInt", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getLong", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getShort", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getString", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "getX", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setBool", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
		addSemantic(c, "setChar", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
		addSemantic(c, "setDouble", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
		addSemantic(c, "setFloat", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
		addSemantic(c, "setInt", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
		addSemantic(c, "setLong", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
		addSemantic(c, "setShort", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
		addSemantic(c, "setString", Semantic.SYNCHRONOUS | Semantic.MUTEX);
		addSemantic(c, "setX", Semantic.SYNCHRONOUS | Semantic.MUTEX);	
	}
	
	
	public void setShort(short val)
	{
		
	}

	public short getShort()
	{
		return 0;
	}

	public void setInt(int val)
	{
		
	}

	public int getInt()
	{
		return 0;
	}

	public void setDouble(double val)
	{
		
	}

	public double getDouble()
	{
		return 0.0;
	}

	public void setFloat(float val)
	{
		
	}

	public float getFloat()
	{
		return 0.0f;
	}

	public void setLong(long val)
	{
		
	}

	public long getLong()
	{
		return 0;
	}

	public void setString(POPString val)
	{
		
	}

	public POPString getString()
	{
		return new POPString("");
	}

	public void setBool(boolean val)
	{
		
	}

	public boolean getBool()
	{
		return false;
	}

	public void setChar(char val)
	{
		
	}

	public char getChar()
	{
		return '0';
	}
	
	public void changeBool(boolean b){}
	public void changeBoolArray(int n,boolean[] b){}
	public void changeCharArray(int n, char[] c){}
	public void changeChar(char c){}
	public void changeShort(short s){}
	public void changeShortArray(int n,short[] s){}
	public void changeInt(int i){}
	public void changeLong(long l){}
	public void changeLongArray(int n,long[] l){}
	public void changeFloat(float f){}
	public void changeFloatArray(int n,float[] f){}
	public void changeDouble(double d){}
	public void changeDoubleArray(int n,double[] d){}
	public void changeString(POPString ps){}
	public void changeStringArray(int n,POPString ps){}
	public void changeIntArray(int n, int[] array){}
	public void changeX(SerialObject so){
		so.i = 11;
		so.d = 1.1;
	}
	
	public void setX(SerialObject so){}
	
	public SerialObject getX(){
		return null;
	}

}
