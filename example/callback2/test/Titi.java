package test;
//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.annotation.*;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.serviceadapter.POPAppService;
import popjava.system.POPSystem;

import popjava.annotation.POPClass;
import popjava.annotation.POPSyncSeq;
@POPClass public class Titi extends POPObject  {
    private int identity;
    public Titi() {
        initializePOPObject();//B
        identity = - 1;
    }
    @ POPSyncSeq public void setIdent ( int i )     {
                 identity = i;
    }
    @ POPSyncSeq public void computeIdent ( Toto toto)     {
        toto=(Toto)PopJava.newActive(Toto.class, toto.getAccessPoint());
         toto.setIdent(identity);
    }
    }
