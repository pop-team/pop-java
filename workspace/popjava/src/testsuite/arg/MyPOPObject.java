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
		addSemantic(c, "setInt", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "changeInt", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getInt", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setDouble", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getDouble", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setLong", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getLong", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setFloat", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getFloat", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setIntArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getIntArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setDoubleArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getDoubleArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setFloatArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getFloatArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setShort", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getShort", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setShortArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getShortArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setChar", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getChar", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setCharArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getCharArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setBoolean", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getBoolean", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setBooleanArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getBooleanArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setString", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getString", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setStringArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getStringArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setLongArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getLongArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setByteArray", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getByteArray", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setByte", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getByte", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "changeString", Semantic.SYNCHRONOUS | Semantic.CONCURRENT);
		addSemantic(c, "setMyType", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getMyType", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "setMyComplexType", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getMyComplexType", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "setComplexType2", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getComplexType2", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "setComplexType3", Semantic.ASYNCHRONOUS | Semantic.SEQUENCE);
		addSemantic(c, "getComplexType3", Semantic.SYNCHRONOUS | Semantic.SEQUENCE);
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
