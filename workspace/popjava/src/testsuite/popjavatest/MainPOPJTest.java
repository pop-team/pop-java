package testsuite.popjavatest;

import popjava.annotation.POPClass;

@POPClass(isDistributable = false)
public class MainPOPJTest {
	
	public static void main(String... argvs){
		
	    POPJParclass pj = new POPJParclass();
        SerialObject so = new SerialObject(10, "test");
        pj.change(so);
        System.out.println("Value of so.i = "+so.i+", value of so.test = "+so.test);
        
        byte b = 0;
        pj.changeByte(b);
        if (b == 1)
            System.out.println("Java change the byte passed as arguments");
        else
            System.out.println("Java does not change the byte passed as arguments " + b);
        
        byte[] ba = {0,0};
        pj.changeByteArray(ba);
        if (ba[0] == 0 && ba[1]==1)
            System.out.println("Java change the byte[] passed as arguments " + ba[0] + " " + ba[1]);
        else
            System.out.println("Java does not change the byte[] passed as arguments " + ba[0] + " " + ba[1]);
        
        char c = 'y';
        pj.changeChar(c);
        if (c == 'x')
            System.out.println("Java changes the char passes as arguments" + ba);
        else
            System.out.println("Java does not change char --> " + c);
        
        char[] ca = {'y', 'z'};
        pj.changeCharArray(ca);
        if (ca[0] == 'a' && ca[1]=='b')
            System.out.println("Java changes the char[] passes as arguments " + ca[0]+" "+ca[1]);
        else
            System.out.println("Java does not change char[] --> ");
        
        short s = 10;
        pj.changeShort(s);
        if (s == 100)
            System.out.println("Java changes the short passes as arguments");
        else
            System.out.println("Java does not change short --> " + s);
        
        short[] sa = {100,200};
        pj.changeShortArray(sa);
        if (sa[0] == 1 && sa[1]==2)
            System.out.println("Java changes the short[] passes as arguments "+sa[0] +" " + sa[1]);
        else
            System.out.println("Java does not change short[] --> "+sa[0] +" " + sa[1]);
        
        long l = 10;
        pj.changeLong(l);
        if (l == 100)
            System.out.println("Java changes the long passes as arguments");
        else
            System.out.println("Java does not change long --> " + l);
        
        long[] la = {1,2};
        pj.changeLongArray(la);
        if (la[0] == 100 && la[1]==200)
            System.out.println("Java changes the long[] passes as arguments");
        else
            System.out.println("Java does not change long --> ");
        
        float f = 10.0f;
        pj.changeFloat(f);
        if (f == 100.0f)
            System.out.println("Java changes the float passes as arguments");
        else
            System.out.println("Java does not change float --> " + f);
        
        float[] fa = {100.0f, 200.0f};
        pj.changeFloatArray(fa);
        if (fa[0] == 1.0f && fa[1]==2.0f)
            System.out.println("Java changes the float[] passes as arguments");
        else
            System.out.println("Java does not change float[] --> ");
        
        boolean bo = false;
        pj.changeBoolean(bo);
        if (bo)
            System.out.println("Java changes the boolean passes as arguments");
        else
            System.out.println("Java does not change boolean --> " + bo);
        
        boolean[] boa = {false, false};
        pj.changeBooleanArray(boa);
        if(boa[0] && boa[1])
            System.out.println("Java changes the boolean[] passes as arguments");
        else
            System.out.println("Java does not change boolean[] --> ");
        

        int i = 0;
        pj.changeInt(i);
        if (i == 1)
            System.out.println("Java change the int passed as arguments");
        else
            System.out
                    .println("Java doesn't change the int passed as arguments "
                            + i);

        
        
        int[] ia = {100,200};
        pj.changeIntArray(ia);
        if(ia[0]==1 && ia[1]==2)
            System.out.println("Java change the int[] passed as arguments");
        else
            System.out
                    .println("Java doesn't change the int[] passed as arguments ");

        
        String st = "Test";
        pj.changeString(st);
        if (st.equals("ChangedString"))
            System.out.println("Java changes the String passes as arguments");
        else
            System.out.println("Java does not change String --> " + st);

        double d1 = 1.0;
        pj.changeDouble(d1);
        if (d1 == 2.0)
            System.out.println("Java changes the double passes as arguments");
        else
            System.out.println("Java does not change double --> " + d1);
        
        double[] da = {1.0, 2.0};
        pj.changeDoubleArray(da);
        if (da[0]==1.1 && da[1] == 2.2)
            System.out.println("Java changes the double[] passes as arguments");
        else
            System.out.println("Java does not change double[]");

        String[] sta = {"un", "deux"};
        pj.changeStringArray(sta);
        if (sta[0].equals("One") && sta[1].equals("Two"))
            System.out.println("Java changes the String[] passes as arguments");
        else
            System.out.println("Java does not change String[]");
	}

}
