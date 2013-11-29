//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.annotation.*;
import popjava.baseobject.ObjectDescription;
import popjava.system.POPSystem;

public class Main {
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
             long start = System.currentTimeMillis();
            Self self = (Self)PopJava.newActive(Self.class);
            //self.test();
            self.test2();
            //self.test3();
            System.out.println("Finished in " +(System.currentTimeMillis() - start) / 1000 + "s");
            POPSystem.end();
        } catch(POPException e) {
            POPSystem.end();
            System.err.println("POP-Java exception catched :"+e.errorMessage);
        }
    }
}
