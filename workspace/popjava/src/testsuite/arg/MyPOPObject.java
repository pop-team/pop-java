package testsuite.arg;

import popjava.base.POPObject;
import popjava.base.Semantic;

public class MyPOPObject extends POPObject {
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
	
	public MyPOPObject(){
		Class<?> c = MyPOPObject.class;
		initializePOPObject();
		od.setPower(100, 80);
		addSemantic(c, "setInt", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "changeInt", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getInt", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setDouble", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getDouble", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setLong", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getLong", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setFloat", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getFloat", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setIntArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getIntArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setDoubleArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getDoubleArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setFloatArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getFloatArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setShort", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getShort", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setShortArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getShortArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setChar", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getChar", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setCharArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getCharArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setBoolean", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getBoolean", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setBooleanArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getBooleanArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setString", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getString", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setStringArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getStringArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setLongArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getLongArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setByteArray", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getByteArray", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setByte", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getByte", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "changeString", Semantic.Synchronous | Semantic.Concurrent);
		addSemantic(c, "setMyType", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getMyType", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "setMyComplexType", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getMyComplexType", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "setComplexType2", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getComplexType2", Semantic.Synchronous | Semantic.Sequence);
		addSemantic(c, "setComplexType3", Semantic.Asynchronous | Semantic.Sequence);
		addSemantic(c, "getComplexType3", Semantic.Synchronous | Semantic.Sequence);
	}
	
	public void setInt(int value){
		myValue = value;
	}
	
	public void changeInt(int value){
		value+=value;
	}
	
	public int getInt(){
		return myValue;
	}
	
	public void setDouble(double value){
		myDoubleValue = value;
	}
	
	public double getDouble(){
		return myDoubleValue;
	}
	
	public void setLong(long value){
		myLongValue = value;
	}
	
	public long getLong(){
		return myLongValue;
	}
	
	public void setFloat(float value){
		myFloatValue = value;
	}
	
	public float getFloat(){
		return myFloatValue;
	}
	
	public void setIntArray(int[] array){
		intArray = array;
	}
	
	public int[] getIntArray(){
		return intArray;
	}
	
	public void setDoubeArray(double[] array){
		doubleArray = array;
	}
	
	public double[] getDoubleArray(){
		return doubleArray;
	}
	
	public void setFloatArray(float[] array){
		floatArray = array;
	}
	
	public float[] getFloatArray(){
		return floatArray;
	}
	
	public void setShort(short value){
		myShort = value;
	}
	
	public short getShort(){
		return myShort;
	}
	
	public void setShortArray(short[] array){
		shortArray = array;
	}
	
	public short[] getShortArray(){
		return shortArray;
	}
	
	public void setChar(char c){
		myChar = c;
	}
	
	public char getChar(){
		return myChar;
	}
	
	public void setCharArray(char[] c){
		charArray = c;
	}
	
	public char[] getCharArray(){
		return charArray;
	}
	
	public void setBoolean(boolean b){
		myBoolean = b;
	}
	
	public boolean getBoolean(){
		return myBoolean;
	}
	
	public void setBooleanArray(boolean[] array){
		boolArray = array;
	}
	
	public boolean[] getBooleanArray(){
		return boolArray;
	}
	
	public void setString(String value){
		myString = value;
	}
	
	public String getString(){
		return myString;
	}
	
	public void setStringArray(String[] value){
		stringArray = value;
	}
	
	public String[] getStringArray(){
		return stringArray;
	}
	
	public void changeString(String s){
		s = "theStringhasBeenChanged";
	}
	
	public void setLongArray(long[] array){
		longArray = array;
	}
	
	public long[] getLongArray(){
		return longArray;
	}
	
	public void setMyType(MyType mt){
		myType = mt;
	}
	
	public MyType getMyType(){
		return myType;
	}
	
	public void setMyComplexType(MyComplexType mct){
		this.mct = mct;
	}
	
	public MyComplexType getMyComplexType(){
		return mct;
	}
	
	public void setComplexType2(ComplexType2 ct2){
		this.ct2 = ct2;
	}
	
	public ComplexType2 getComplexType2(){
		return ct2;
	}
	
	public void setComplexType3(ComplexType3 ct3){
		this.ct3 = ct3;
	}
	
	public ComplexType3 getComplexType3(){
		return ct3;
	}
	
	public void setByte(byte b){
		myByte = b;
	}
	
	public byte getByte(){
		return myByte;
	}
	
	public void setByteArray(byte[] array){
		byteArray = array;
	}
	
	public byte[] getByteArray(){
		return byteArray;
	}
	
}
