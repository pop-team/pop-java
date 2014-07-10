package testsuite.pure.integer;

import popjava.annotation.POPClass;
import testsuite.integer.Integer;

public class TestInteger {

    public static void main(String [] args){
        Integer i1 = new Integer();
        Integer i2 = new Integer();
        i1.set(23);
        i2.set(25);
        System.out.println("i1 = "+i1.get());
        System.out.println("i2 = "+i2.get());
        i1.add(i2);
        int sum =  i1.get();
        System.out.println("i1+i2 = "+sum);
        if(sum==48){
            System.out.println("Test Integer Successful");
        }else{
            System.out.println("Test Integer failed");
        }
    }
    
}
