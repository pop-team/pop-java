package test;
//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.annotation.*;
import popjava.baseobject.ObjectDescription;
import popjava.system.POPSystem;

public class CallBackMain {
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
             System.out.println("Callback test started ...");
            Toto t = (Toto)PopJava.newActive(Toto.class /*C*/);
            t.setIdent(1234);
            int value = t.getIdent();
            System.out.println("Identity callback is " + value);
            if(value == - 1) {
                System.out.println("Callback test successful");
            }
            else {
                System.out.println("Callback test failed");
            }
                        POPSystem.end();
        } catch(POPException e) {
            POPSystem.end();
            System.err.println("POP-Java exception catched :"+e.errorMessage);
        }
    }
}
