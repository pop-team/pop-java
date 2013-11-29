//Import added by the POP-Java compiler
import popjava.PopJava;
import popjava.base.POPException;
import popjava.annotation.*;
import popjava.base.POPObject;
import popjava.base.Semantic;
import popjava.serviceadapter.POPAppService;
import popjava.system.POPSystem;

import java.util.concurrent.Semaphore;
public class Self extends POPObject  {
    int loopCount = 10;
    private Semaphore locker = new Semaphore(4);
    @ POPObjectDescription(url = "localhost") public Self() {
        initializePOPObject();//B
    }
    @ POPSyncConc public void test ( )     throws POPException {
                 Self test = (Self)PopJava.newActive(Self.class, this.getAccessPoint());
        for(int i = 0;
        i < loopCount;
        i ++) {
            test.pause();
        }
        for(int i = 0;
        i < loopCount;
        i ++) {
            try {
                locker.acquire();
            }
            catch(Exception e) {
            }
                    }
            }
    @ POPSyncConc public void test2 ( )     throws POPException {
                 for(int i = 0;
        i < loopCount;
        i ++) {
            ((Self)PopJava.newActive(Self.class, this.getAccessPoint())).pause();
        }
        for(int i = 0;
        i < loopCount;
        i ++) {
            try {
                locker.acquire();
            }
            catch(Exception e) {
            }
                    }
            }
    @ POPSyncConc public void test3 ( )     throws POPException {
                 for(int i = 0;
        i < loopCount;
        i ++) {
            ((Self)(Self)PopJava.newActive(Self.class, this.getAccessPoint())).pause();
        }
        for(int i = 0;
        i < loopCount;
        i ++) {
            try {
                locker.acquire();
            }
            catch(Exception e) {
            }
                    }
            }
    @ POPAsyncConc public void pause ( )     throws POPException {
                 try {
            Thread.sleep(1000);
        }
        catch(Exception e) {
        }
        locker.release();
    }
    }
