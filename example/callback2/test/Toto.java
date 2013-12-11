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
import popjava.annotation.POPSyncConc;
import popjava.annotation.POPSyncSeq;
@POPClass public class Toto extends POPObject  {
    private int identity;
    public Toto() {
        initializePOPObject();//B
    }
    @ POPSyncConc public void setIdent ( int i )     {
                 identity = i;
    }
    @ POPSyncSeq public int getIdent ( )     {
                 Titi t = (Titi)PopJava.newActive(Titi.class);
        setIdent(222);
        t.computeIdent((Toto)PopJava.newActive(Toto.class, this.getAccessPoint()));
        return identity;
    }
    }
