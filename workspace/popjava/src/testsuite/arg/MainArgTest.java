package testsuite.arg;

import popjava.PopJava;
import popjava.base.POPException;
import popjava.system.POPSystem;

public class MainArgTest {
	
	
	public static void main(String... argvs){
		try{
			POPSystem.initialize(argvs);
			
			
			MyPOPObject pop = (MyPOPObject)PopJava.newActive(MyPOPObject.class);
			
			String s1 = "mystring";
			pop.setString(s1);
			String s2 = pop.getString();
			if(!s1.equals(s2)){
				System.out.println("Test failed because of String arg");
				return;
			}
			
			byte b = 1;
			pop.setByte(b);
			byte bb = pop.getByte();
			if(bb!=b){
				System.out.println("Test failed because of byte arg");
				return;
			}
			
			byte[] ba = {1,2,3,4};
			pop.setByteArray(ba);
			byte[] bba = pop.getByteArray();
			for (int i = 0; i < bba.length; i++) {
				if(ba[i]!=bba[i]){
					System.out.println("Test failed because of byte[] arg");
					return;
				}
			}
			
			short s = 12;
			pop.setShort(s);
			short sback = pop.getShort();
			if(s!=sback){
				System.out.println("Test failed because of short arg");
				return;
			}
			
			pop.setInt(222);
			int val = pop.getInt();
			if(val!=222){
				System.out.println("Test failed because of int arg");
				return;
			}
			
			pop.setDouble(2.5);
			double valD = pop.getDouble();
			if(valD != 2.5){
				System.out.println("Test failed because of double arg");
				return;
			}
						
			pop.setLong(125000);
			long valL = pop.getLong();
			if(valL != 125000){
				System.out.println("Test failed because of int arg");
				return;
			}
			
			pop.setFloat(3.44f);
			double valF = pop.getFloat();
			if(valF!=3.44f){
				System.out.println("Test failed because of float arg");
				return;
			}
			
			int[] aInt = {1,2,3,4,5,6,7,8,9,10};
			pop.setIntArray(aInt);
			int[] aaInt = pop.getIntArray();
			for (int i = 0; i < aaInt.length; i++) {
				if(aaInt[i]!= i+1){
					System.out.println("Test failed because of int[] arg");
					return;
				}
			}
			
			
			double[] aDouble = {1.1,2.2,3.3,4.4,5.5,6.6,7.7,8.8,9.9,10.10};
			pop.setDoubeArray(aDouble);
			
			double[] aaDouble = pop.getDoubleArray();
			for (int i = 0; i < aaDouble.length; i++) {
				if(aDouble[i]!=aaDouble[i]){
					System.out.println("Test failed because of double[] arg");
					return;
				}
			}
			
			
			float[] aFloat = {1.1f, 2.2f, 3.3f, 4.4f, 5.5f, 6.6f, 7.7f, 8.8f, 9.9f, 10.1f};
			pop.setFloatArray(aFloat);
			
			float[] aaFloat = pop.getFloatArray();
			for (int i = 0; i < aaFloat.length; i++) {
				if(aFloat[i]!=aaFloat[i]){
					System.out.println("Test failed because of float[] arg");
					return;
				}
			}
			
			short[] aShort = {1,2,3,4,5,6,7,8,9,10};
			pop.setShortArray(aShort);
			short[] aaShort = pop.getShortArray();
			for (int i = 0; i < aaFloat.length; i++) {
				if(aShort[i]!=aaShort[i]){
					System.out.println("Test failed because of short[] arg");
					return;
				}
			}
			
			char[] aChar = {'a','b','c','d','e'};
			pop.setCharArray(aChar);
			char[] aaChar = pop.getCharArray();
			for (int i = 0; i < aChar.length; i++) {
				if(aChar[i]!=aaChar[i]){
					System.out.println("Test failed because of char[] arg");
					return;
				}
			}
						
			
			char c = 'c';
			pop.setChar(c);
			char d = pop.getChar();
			if(c!=d){
				System.out.println("Test failed because of char arg");
				return;
			}
			
			
			pop.setBoolean(true);
			if(!pop.getBoolean()){
				System.out.println("Test failed because of boolean arg");
				return;
			}
			
			boolean[] boola = {true, false, true};
			pop.setBooleanArray(boola);
			boolean[] baa = pop.getBooleanArray();
			if(!(baa[0] && !baa[1] && baa[0])){
				System.out.println("Test failed because of boolean[] arg");
				return;
			}
			
			String myString = "testString";
			pop.setString(myString);
			if(!pop.getString().equals(myString)){
				System.out.println("Test failed because of String arg");
				return;
			}
		
			
			String[] sArray = {"one", "two", "three"};
			pop.setStringArray(sArray);
			String[] backArray = pop.getStringArray();
			if(!(backArray[0].equals(sArray[0]) && backArray[1].equals(sArray[1]) && backArray[2].equals(sArray[2]))){
				System.out.println("Test failed because of String[] arg");
				return;
			}
			
			long[] lArray = {100,200,300,400,500,600,700,800,900,1000};
			pop.setLongArray(lArray);
			long[] blArray = pop.getLongArray();
			for (int i = 0; i < blArray.length; i++) {
				if(blArray[i] != lArray[i]){
					System.out.println("Test failed because of long[] arg");
					return;
				}
			}
			
			String test = "oneSimpleString";
			pop.changeString(test);
			
			MyType mt = new MyType();
			mt.setInt(10);
			mt.setString("ten");
			
			pop.setMyType(mt);
			MyType mt2 = pop.getMyType();
			if(!((mt2.getInt() == mt.getInt()) && (mt2.getString().equals(mt.getString()))))
				System.out.println("Test failed for IPOPBase arguments");
			
			
			int[] tab = {1,2,3,4,5};
			MyComplexType mct = new MyComplexType(tab.length, 22.2, tab);
			
			pop.setMyComplexType(mct);
			MyComplexType mct2 = pop.getMyComplexType();
			
			
			ComplexType2 ct2 = new ComplexType2();
			double[] testValue = {1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 10.1};
			ct2.setDouble(testValue);
			ct2.setInt(testValue.length);
			
			pop.setComplexType2(ct2);
			ComplexType2 backct2 = pop.getComplexType2();
			
			ComplexType3 ct3 = new ComplexType3();
			
			pop.setComplexType3(ct3);
			
			ComplexType3 backct3 = pop.getComplexType3();
			
			System.out.println("Arguments test successful");
			
		} catch (POPException e){
			System.out.println("Exception catched during the test");
			System.out.println("Message: "+e.errorMessage);
		}
	}

}
