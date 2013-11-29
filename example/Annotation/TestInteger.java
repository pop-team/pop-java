//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.baseobject.ObjectDescription;
import popjava.system.POPSystem;

public class TestInteger {
    public static void main(String... args){
        try{
            POPSystem.initialize(args);
            if(args.length > 0 && args[0].startsWith("-codeconf=")){
                String[] tmpArg = new String[args.length-1];
                for (int i = 0; i < tmpArg.length; i++) {
                    tmpArg[i] = args[i+1];
                }
                args = tmpArg;
            }
             Integer i1 = (Integer)PopJava.newActive(Integer.class,"localhost");
            Integer i2 = (Integer)PopJava.newActive(Integer.class,"localhost");
            //Create an array 
            Integer [ ] tab = new Integer [ 10 ] ;
            for(int i = 0;
            i < tab.length;
            i ++) {
                tab [ i ] = (Integer)PopJava.newActive(Integer.class,"localhost");
            }
            i1.set(11);
            i2.set(14);
            System.out.println("i1=" + i1.get());
            System.out.println("i2=" + i2.get());
            i1.add(i2);
            System.out.println("i1+i2=" + i1.get());
            int [ ] testArray = new int [ ] {
                1 , 2 , 3             }
            ;
            int temp = i1.arrayChanger(testArray);
            System.out.println("19 == " + temp);
            System.out.println("First, should change! " + testArray [ 0 ] + ", " + testArray [ 1 ] + ", " + testArray [ 2 ]);
            temp = i1.arrayChanger2(testArray);
            System.out.println("19 == " + temp);
            System.out.println("Second, should not change! " + testArray [ 0 ] + ", " + testArray [ 1 ] + ", " + testArray [ 2 ]);
            System.out.println("Should be true " + i1.arrayChanger3(testArray));
            POPSystem.end();
        } catch(POPException e) {
            POPSystem.end();
            System.err.println("POP-Java exception catched :"+e.errorMessage);
        }
    }
}
