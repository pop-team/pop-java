package ch.icosys.popjava.testsuite.popc;

import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.dataswaper.POPString;


@POPClass(isDistributable = false)
public class PopCParameterTest {

	/**
	 * @param args in
	 */
	public static void main(String... args) {
		System.out.println("POP-Java POP-C++ parameters test started...");
		
		Parameters p = new Parameters();
        
        System.out.println("Check char parameter");
        char c = 'x';
        p.setChar(c);
        if(p.getChar()!=c)
            System.out.println("char failed");
        
        System.out.println("Check short parameter");
        short s = 10;
        p.setShort(s);
        if(p.getShort()!=s)
            System.out.println("short failed");
        
        System.out.println("Check int parameter");
        int i = 11;
        p.setInt(i);
        if(p.getInt()!=i)
            System.out.println("int failed");
        
        System.out.println("Check long parameter");
        long l = 6000000000L;
        p.setLong(l);
        if(p.getLong()!=l)
            System.out.println("long failed");
        
        System.out.println("Check double parameter");
        double d = 1.1;
        p.setDouble(d);
        if(p.getDouble()!=d)
            System.out.println("double failed");
        
        System.out.println("Check float parameter");
        float f = 1.1f;
        p.setFloat(f);
        if(p.getFloat()!=f)
            System.out.println("float failed");
        
        System.out.println("Check POPString parameter");
        POPString ps = new POPString("some test");
        p.setString(ps);
        if(!p.getString().getValue().equals("some test"))
            System.out.println("String failed");
        
        System.out.println("Check boolean parameter");
        boolean b = true;
        p.setBool(b);
        if(p.getBool()!=b)
            System.out.println("boolean failed");
        
        
        System.out.println("Change a boolean");
        boolean b1 = true;
        p.changeBool(b1);
        if(!b1)
            System.out.println("boolean changed");
        
        System.out.println("Change a char");
        char c1 = 'z';
        p.changeChar(c1);
        if(c1 == 'x')
            System.out.println("char changed");
        
        System.out.println("Change a short");
        short s1 = 0;
        p.changeShort(s1);
        if(s1 == 10)
            System.out.println("short changed");
        
        System.out.println("Change a int");
        int i1 = 0;
        p.changeInt(i1);
        if(i1==10)
            System.out.println("int changed");
        
        System.out.println("Change a long");
        long l1 = 0;
        p.changeLong(l1);
        if(l1 == 10)
            System.out.println("long changed");
        
        System.out.println("Change a float");
        float f1 = 0.0f;
        p.changeFloat(f1);
        if(f1 == 10.10f)
            System.out.println("float changed");
        
        System.out.println("Change a double");
        double d1 = 0.0;
        p.changeDouble(d1);
        if(d1 == 10.10)
            System.out.println("double changed");
        
        System.out.println("Change a String");
        POPString ps1 = new POPString("test");
        p.changeString(ps1);
        if(ps1.getValue().equals("changed"))
            System.out.println("String changed");
        
        System.out.println("Change IPOPBase");
        SerialObject so = new SerialObject();
        so.i = 111;
        so.d = 1.11;
        System.out.println("Check IPOPBase parameter");
        p.setX(so);
        SerialObject so1 = p.getX();
        
        p.changeX(so);
        
        System.out.println("Test finished");
        
        int[] array = {1,2};
        p.changeIntArray(2, array);
        System.out.println(array[0]+"/"+array[1]);
        
        boolean[] bArray = {false,false};
        p.changeBoolArray(2, bArray);
        System.out.println(bArray[0]+"/"+bArray[1]);
        
        short[] sarray = {1,2};
        p.changeShortArray(2, sarray);
        System.out.println(sarray[0]+"/"+sarray[1]);
        
        long[] larray = {1,2};
        p.changeLongArray(2, larray);
        System.out.println(larray[0]+"/"+larray[1]);
        
        double[] darray = {1,2};
        p.changeDoubleArray(2, darray);
        System.out.println(darray[0]+"/"+darray[1]);
        
        float[] farray = {1,2};
        p.changeFloatArray(2, farray);
        System.out.println(farray[0]+"/"+farray[1]);
        
        char[] carray = {'y','z'};
        p.changeCharArray(carray.length, carray);
        System.out.println(carray[0]+"/"+carray[1]);
	}

}
