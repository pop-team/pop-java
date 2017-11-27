package testsuite.arg;

import popjava.annotation.POPAsyncSeq;
import popjava.annotation.POPClass;
import popjava.annotation.POPSyncConc;

@POPClass
public class MyPOPObject{
	private byte myByte;
	private byte[] byteArray;
	private int myValue;
	private int[] intArray;
	private double myDoubleValue;
	private long myLongValue;
	private long[] longArray;
	private float myFloatValue;
	private double[] doubleArray;
	private float[] floatArray;
	private short[] shortArray;
	private short myShort;
	private char myChar;
	private char[] charArray;
	private boolean myBoolean;
	private boolean[] boolArray;
	private String myString;
	private String[] stringArray;
	private MyType myType;
	private MyComplexType mct;
	private ComplexType2 ct2;
	private ComplexType3 ct3;
		
	@POPAsyncSeq
	public void setInt(int value){
		myValue = value;
	}
	
	@POPAsyncSeq
	public void changeInt(int value){
		value+=value;
	}
	
	@POPSyncConc
	public int getInt(){
		return myValue;
	}
	
	@POPAsyncSeq
	public void setDouble(double value){
		myDoubleValue = value;
	}
	
	@POPSyncConc
	public double getDouble(){
		return myDoubleValue;
	}
	
	@POPAsyncSeq
	public void setLong(long value){
		myLongValue = value;
	}
	
	@POPSyncConc
	public long getLong(){
		return myLongValue;
	}
	
	@POPAsyncSeq
	public void setFloat(float value){
		myFloatValue = value;
	}
	
	@POPSyncConc
	public float getFloat(){
		return myFloatValue;
	}
	
	@POPAsyncSeq
	public void setIntArray(int[] array){
		intArray = array;
	}
	
	@POPSyncConc
	public int[] getIntArray(){
		return intArray;
	}
	
	@POPAsyncSeq
	public void setDoubeArray(double[] array){
		doubleArray = array;
	}
	
	@POPSyncConc
	public double[] getDoubleArray(){
		return doubleArray;
	}
	
	@POPAsyncSeq
	public void setFloatArray(float[] array){
		floatArray = array;
	}
	
	@POPSyncConc
	public float[] getFloatArray(){
		return floatArray;
	}
	
	@POPAsyncSeq
	public void setShort(short value){
		myShort = value;
	}
	
	@POPSyncConc
	public short getShort(){
		return myShort;
	}
	
	@POPAsyncSeq
	public void setShortArray(short[] array){
		shortArray = array;
	}
	
	@POPSyncConc
	public short[] getShortArray(){
		return shortArray;
	}
	
	@POPAsyncSeq
	public void setChar(char c){
		myChar = c;
	}
	
	@POPSyncConc
	public char getChar(){
		return myChar;
	}
	
	@POPAsyncSeq
	public void setCharArray(char[] c){
		charArray = c;
	}
	
	@POPSyncConc
	public char[] getCharArray(){
		return charArray;
	}
	
	@POPAsyncSeq
	public void setBoolean(boolean b){
		myBoolean = b;
	}
	
	@POPSyncConc
	public boolean getBoolean(){
		return myBoolean;
	}
	
	@POPAsyncSeq
	public void setBooleanArray(boolean[] array){
		boolArray = array;
	}
	
	@POPSyncConc
	public boolean[] getBooleanArray(){
		return boolArray;
	}
	
	@POPAsyncSeq
	public void setString(String value){
		myString = value;
	}
	
	@POPSyncConc
	public String getString(){
		return myString;
	}
	
	@POPAsyncSeq
	public void setStringArray(String[] value){
		stringArray = value;
	}
	
	@POPSyncConc
	public String[] getStringArray(){
		return stringArray;
	}
	
	@POPAsyncSeq
	public void changeString(String s){
		s = "theStringhasBeenChanged";
	}
	
	@POPAsyncSeq
	public void setLongArray(long[] array){
		longArray = array;
	}
	
	@POPSyncConc
	public long[] getLongArray(){
		return longArray;
	}
	
	@POPAsyncSeq
	public void setMyType(MyType mt){
		myType = mt;
	}
	
	@POPSyncConc
	public MyType getMyType(){
		return myType;
	}
	
	@POPAsyncSeq
	public void setMyComplexType(MyComplexType mct){
		this.mct = mct;
	}
	
	@POPSyncConc
	public MyComplexType getMyComplexType(){
		return mct;
	}
	
	@POPAsyncSeq
	public void setComplexType2(ComplexType2 ct2){
		this.ct2 = ct2;
	}
	
	@POPSyncConc
	public ComplexType2 getComplexType2(){
		return ct2;
	}
	
	@POPAsyncSeq
	public void setComplexType3(ComplexType3 ct3){
		this.ct3 = ct3;
	}
	
	@POPSyncConc
	public ComplexType3 getComplexType3(){
		return ct3;
	}
	
	@POPAsyncSeq
	public void setByte(byte b){
		myByte = b;
	}
	
	@POPSyncConc
	public byte getByte(){
		return myByte;
	}
	
	@POPAsyncSeq
	public void setByteArray(byte[] array){
		byteArray = array;
	}
	
	@POPSyncConc
	public byte[] getByteArray(){
		return byteArray;
	}
	
}
