package ch.icosys.popjava.testsuite.jobmgr;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.annotation.POPClass;
import ch.icosys.popjava.core.base.POPException;
import ch.icosys.popjava.core.baseobject.ObjectDescription;

@POPClass(isDistributable = false)
public class TestJobMgr {

	public static void main(String... argvs) {
		ObjectDescription od1 = new ObjectDescription();
		od1.setHostname("127.0.0.1");
		@SuppressWarnings("unused")
		ParClass p1 = (ParClass) PopJava.newActive(ParClass.class, od1);

		try {
			ObjectDescription od2 = new ObjectDescription();
			od2.setPower(6000, 5000);
			@SuppressWarnings("unused")
			ParClass p2 = (ParClass) PopJava.newActive(ParClass.class, od2);
		} catch (POPException e) {
			System.out.println("od.power");
		}

		try {
			ObjectDescription od3 = new ObjectDescription();
			od3.setHostname("160.78.90.45");
			// od3.setMemory(6000, 5000);
			@SuppressWarnings("unused")
			ParClass p3 = (ParClass) PopJava.newActive(ParClass.class, od3);
		} catch (POPException e) {
			System.out.println("od.url");
		}

		try {
			ObjectDescription od3 = new ObjectDescription();
			od3.setBandwidth(6000, 5000);
			@SuppressWarnings("unused")
			ParClass p3 = (ParClass) PopJava.newActive(ParClass.class, od3);
		} catch (POPException e) {
			System.out.println("od.bandwidth");
		}

		try {
			ObjectDescription od3 = new ObjectDescription();
			od3.setSearch(10, 1000, 5);
			@SuppressWarnings("unused")
			ParClass p3 = (ParClass) PopJava.newActive(ParClass.class, od3);
		} catch (POPException e) {
			System.out.println("od.search");
		}

		ObjectDescription od4 = new ObjectDescription();
		od4.setPower(100, 80);
		@SuppressWarnings("unused")
		ParClass p4 = (ParClass) PopJava.newActive(ParClass.class, od4);

	}
}
