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
		addSemantic(c, "changeBool", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeBoolArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeChar", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeCharArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeDouble", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeDoubleArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeFloat", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeFloatArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeInt", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeIntArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeLong", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeLongArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeShort", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeShortArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeString", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeStringArray", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "changeX", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "getBool", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getChar", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getDouble", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getFloat", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getInt", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getLong", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getShort", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getString", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "getX", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setBool", Semantic.Synchronous | Semantic.Mutex);	
		addSemantic(c, "setChar", Semantic.Synchronous | Semantic.Mutex);	
		addSemantic(c, "setDouble", Semantic.Synchronous | Semantic.Mutex);	
		addSemantic(c, "setFloat", Semantic.Synchronous | Semantic.Mutex);	
		addSemantic(c, "setInt", Semantic.Synchronous | Semantic.Mutex);	
		addSemantic(c, "setLong", Semantic.Synchronous | Semantic.Mutex);	
		addSemantic(c, "setShort", Semantic.Synchronous | Semantic.Mutex);	
		addSemantic(c, "setString", Semantic.Synchronous | Semantic.Mutex);
		addSemantic(c, "setX", Semantic.Synchronous | Semantic.Mutex);	
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
