package testsuite.popc;

import popjava.annotation.Encoding;
import popjava.annotation.POPClass;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncMutex;
import popjava.annotation.POPSyncSeq;
import popjava.dataswaper.POPString;

@POPClass(classId = 1000, className = "Parameters", deconstructor = true)
public class Parameters {
    
    @POPObjectDescription(encoding = Encoding.XDR)
	public Parameters(){
	}
	
	@POPSyncMutex
	public void setShort(short val)
	{
	}

	@POPSyncConc
	public short getShort()
	{
		return 0;
	}

	@POPSyncMutex
	public void setInt(int val)
	{
		
	}

	@POPSyncConc
	public int getInt()
	{
		return 0;
	}

	@POPSyncMutex
	public void setDouble(double val)
	{
		
	}

	@POPSyncConc
	public double getDouble()
	{
		return 0.0;
	}

	@POPSyncMutex
	public void setFloat(float val)
	{
		
	}

	@POPSyncConc
	public float getFloat()
	{
		return 0.0f;
	}

	@POPSyncMutex
	public void setLong(long val)
	{
		
	}

	@POPSyncConc
	public long getLong()
	{
		return 0;
	}

	@POPSyncMutex
	public void setString(POPString val)
	{
		
	}

	@POPSyncConc
	public POPString getString()
	{
		return new POPString("");
	}

	@POPSyncMutex
	public void setBool(boolean val)
	{
		
	}

	@POPSyncConc
	public boolean getBool()
	{
		return false;
	}

	@POPSyncMutex
	public void setChar(char val)
	{
		
	}

	@POPSyncConc
	public char getChar()
	{
		return '0';
	}
	
	@POPSyncSeq
	public void changeBool(boolean b){}
	@POPSyncSeq
	public void changeBoolArray(int n,boolean[] b){}
	@POPSyncSeq
	public void changeCharArray(int n, char[] c){}
	@POPSyncSeq
	public void changeChar(char c){}
	@POPSyncSeq
	public void changeShort(short s){}
	@POPSyncSeq
	public void changeShortArray(int n,short[] s){}
	@POPSyncSeq
	public void changeInt(int i){}
	@POPSyncSeq
	public void changeLong(long l){}
	@POPSyncSeq
	public void changeLongArray(int n,long[] l){}
	@POPSyncSeq
	public void changeFloat(float f){}
	@POPSyncSeq
	public void changeFloatArray(int n,float[] f){}
	@POPSyncSeq
	public void changeDouble(double d){}
	@POPSyncSeq
	public void changeDoubleArray(int n,double[] d){}
	@POPSyncSeq
	public void changeString(POPString ps){}
	@POPSyncSeq
	public void changeStringArray(int n,POPString ps){}
	@POPSyncSeq
	public void changeIntArray(int n, int[] array){}
	@POPSyncSeq
	public void changeX(SerialObject so){
		so.i = 11;
		so.d = 1.1;
	}
	
	@POPSyncMutex
	public void setX(SerialObject so){}
	
	@POPSyncConc
	public SerialObject getX(){
		return null;
	}

}
