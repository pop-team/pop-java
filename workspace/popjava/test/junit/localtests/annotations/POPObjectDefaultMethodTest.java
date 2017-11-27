package junit.localtests.annotations;

import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import popjava.PopJava;
import popjava.annotation.POPObjectDescription;
import popjava.annotation.POPSyncConc;
import popjava.base.POPObject;
import popjava.baseobject.POPAccessPoint;
import popjava.system.POPSystem;

/**
 *
 * @author Davide Mazzoleni
 */
public class POPObjectDefaultMethodTest {
	
	private final byte[] certificate = new byte[] {
		'-', '-', '-', '-', '-', 'B', 'E', 'G', 'I', 'N', ' ', 'C', 'E', 'R', 'T', 'I',
		'F', 'I', 'C', 'A', 'T', 'E', '-', '-', '-', '-', '-', '\n', 'M', 'I', 'I', 'C',
		'/', 'j', 'C', 'C', 'A', 'e', 'a', 'g', 'A', 'w', 'I', 'B', 'A', 'g', 'I', 'E',
		'k', 'G', 'E', '9', 'e', 'T', 'A', 'N', 'B', 'g', 'k', 'q', 'h', 'k', 'i', 'G',
		'9', 'w', '0', 'B', 'A', 'Q', 'U', 'F', 'A', 'D', 'B', 'B', 'M', 'S', '0', 'w',
		'K', 'w', 'Y', 'D', 'V', 'Q', 'Q', 'D', 'D', 'C', 'Q', '4', 'Z', 'T', 'I', 'y',
		'Y', '2', 'U', 'w', 'Z', 'i', '0', '3', 'N', 'z', 'l', 'l', 'L', 'T', 'Q', '3',
		'Y', 'j', 'k', 't', 'Y', 'j', 'd', 'm', 'N', 'C', '0', 'y', 'O', 'T', 'N', 'h',
		'Y', 'T', 'k', '5', 'M', 'D', 'E', '1', 'N', 'j', 'U', 'x', 'E', 'D', 'A', 'O',
		'B', 'g', 'N', 'V', 'B', 'A', 's', 'M', 'B', '1', 'B', 'v', 'c', 'E', 'p', 'h',
		'd', 'm', 'E', 'w', 'H', 'h', 'c', 'N', 'M', 'T', 'c', 'x', 'M', 'T', 'I', 'w',
		'M', 'T', 'I', 'y', 'N', 'z', 'M', '5', 'W', 'h', 'c', 'N', 'M', 'T', 'g', 'x',
		'M', 'T', 'I', 'w', 'M', 'T', 'I', 'y', 'N', 'z', 'M', '4', 'W', 'j', 'B', 'B',
		'M', 'S', '0', 'w', 'K', 'w', 'Y', 'D', 'V', 'Q', 'Q', 'D', 'D', 'C', 'Q', '4',
		'Z', 'T', 'I', 'y', 'Y', '2', 'U', 'w', 'Z', 'i', '0', '3', 'N', 'z', 'l', 'l',
		'L', 'T', 'Q', '3', 'Y', 'j', 'k', 't', 'Y', 'j', 'd', 'm', 'N', 'C', '0', 'y',
		'O', 'T', 'N', 'h', 'Y', 'T', 'k', '5', 'M', 'D', 'E', '1', 'N', 'j', 'U', 'x',
		'E', 'D', 'A', 'O', 'B', 'g', 'N', 'V', 'B', 'A', 's', 'M', 'B', '1', 'B', 'v',
		'c', 'E', 'p', 'h', 'd', 'm', 'E', 'w', 'g', 'g', 'E', 'i', 'M', 'A', '0', 'G',
		'C', 'S', 'q', 'G', 'S', 'I', 'b', '3', 'D', 'Q', 'E', 'B', 'A', 'Q', 'U', 'A',
		'A', '4', 'I', 'B', 'D', 'w', 'A', 'w', 'g', 'g', 'E', 'K', 'A', 'o', 'I', 'B',
		'A', 'Q', 'C', 'h', 'f', '6', 'm', '0', 'n', '5', 'e', 'X', '8', 'H', '1', '+',
		'V', '9', 'P', '+', '6', 'j', '1', 'R', '1', 'z', '/', 'A', '9', 'l', 'J', 'q',
		'Q', 'i', '2', 'f', 't', 'p', 't', 'x', 'P', 'T', '0', '3', 'A', 'y', 'F', 'I',
		'b', 'z', 'K', 'z', 'r', 'b', '7', 'w', 'k', 'e', 'u', '1', 'e', 'r', '1', 'b',
		'b', 't', 'V', 'A', '8', 'T', 'T', 'j', 'B', 'f', 'l', 'D', '9', 'a', 'O', 'o',
		'x', 'z', 'F', 'C', '3', 'Y', 'V', '8', 'K', 'x', 'b', 'm', '9', '/', 'Z', 'a',
		'V', 's', 'U', '/', 'p', '4', 'r', 'V', 'b', 'v', '/', 'Z', 'D', 'i', 'g', 'G',
		'J', 'w', 'R', 'v', 'P', 'x', '6', 'X', 'S', 'x', '9', 'm', 'z', '7', 'g', 'H',
		'+', 't', 'y', 'c', 'Q', 'L', 'T', 't', 'I', 'Q', 'd', 'h', 'M', 'A', '3', 'l',
		'Z', 'U', 'q', '6', 'W', 'Y', 'X', 'H', 'D', 'n', 'g', '6', 'b', '3', 'Y', 'x',
		'S', 'm', 'C', 's', 'H', 'M', 'c', 'R', '+', 'G', 'm', 'r', '6', '2', 'c', 'b',
		'0', 'M', 'X', 'U', 'y', 'w', 'U', 'S', 'd', 'u', '0', 'l', 'F', 'd', 'r', 'y',
		'5', 'm', 'p', 'z', 'c', '7', '1', 'Q', 'k', 'd', 'D', '4', 'f', 'l', 's', 'w',
		'I', 'q', 'E', '4', '6', 'c', 'J', 'T', 'Y', 'W', 'p', 'Q', 'E', 'X', 'e', 'h',
		'I', '2', 'I', 'Y', 'Z', 'a', 'e', 'm', 'C', '0', 'B', 'X', 'q', 'G', 'O', '3',
		'n', 'O', 'y', 'P', '6', 'N', 'p', 'o', 'b', 'l', 'S', 'S', 'O', '0', 't', 'y',
		'B', 'v', 'C', 'F', 'c', 'O', 'S', '+', 's', 'G', 'a', 'r', '8', '1', '3', '7',
		'E', 'L', 'r', '0', 'W', 'H', '3', 'V', 'w', '1', 'Q', 'D', 'F', 'K', 's', 'P',
		'Q', '/', 'k', 'U', 'P', '8', 'D', 'H', 'm', 'R', 'i', 'o', 'b', 'T', 'h', 'g',
		'k', 'S', '0', 'j', '4', '5', 'X', 'p', 'w', 'x', 'W', 'Z', 'b', 'x', 'B', 'K',
		'Q', 'W', '4', 'H', '9', 'g', 'N', 'a', 'a', 'H', 'S', 'o', '5', 'b', 'T', 'c',
		'L', 'u', 's', '/', 'C', '3', 'q', 'Z', 'A', 'g', 'M', 'B', 'A', 'A', 'E', 'w',
		'D', 'Q', 'Y', 'J', 'K', 'o', 'Z', 'I', 'h', 'v', 'c', 'N', 'A', 'Q', 'E', 'F',
		'B', 'Q', 'A', 'D', 'g', 'g', 'E', 'B', 'A', 'B', 'N', 'O', 'X', 'C', 'd', 'O',
		'E', 'F', '/', 'S', 'L', '1', 'g', '/', 'T', '/', 'u', 'L', '2', 'a', 'B', 'H',
		'q', '3', '7', 'j', 'K', '6', 'b', 'B', 'y', 'E', 'w', 't', 'J', 'e', 'g', 'F',
		'q', 'n', 'Y', 'N', 'y', 'i', '0', 'p', 'N', 'K', 'y', 'Z', 'Z', 'F', 'E', 'E',
		'y', 'g', 'R', 'M', 'e', 'J', 'Y', '2', '2', 'h', 'p', 'z', 'y', 'h', 'v', 'G',
		'I', 'U', 'L', 'Z', 'm', 'q', 'A', 'Y', 'd', '5', '2', 'J', 'p', 'u', 'q', 'v',
		'P', 'r', 'T', 'b', 'A', '/', 'R', 'F', 'N', '0', 'Q', 'I', 'F', 'A', 'T', 'c',
		'a', 'l', 'E', 'f', 'p', 't', 'u', 't', '8', 'N', 'V', 'f', 'D', 'F', '+', 'e',
		'w', 'y', 'U', 'k', 'd', 'm', 'M', 'D', 'D', 'u', '7', 'N', 'A', '3', 'X', '2',
		'm', '9', '6', '5', '3', 'R', 'B', '5', 'w', 'd', 'H', 'C', 'i', 'W', '3', '5',
		'r', 'k', 'l', 'F', 't', 'G', 'm', '4', 'k', '7', 'm', 'x', 'd', 'a', '5', 'A',
		'2', 'f', '5', 'z', 'n', 'c', 'u', '4', 'Q', 'Z', 'z', 'h', 'k', '4', 'z', 'D',
		'G', 'M', 'b', 'd', 's', 'H', 'Q', 'Z', 'Y', 'w', 'P', '7', 'f', '3', 'a', 't',
		'7', 'F', '7', 'f', 'V', 'r', 'o', 'J', 'D', 'w', 'n', 'l', '+', 'B', 'r', 'g',
		'b', 'x', 'm', '9', 'B', 'D', 'q', 'B', 'f', 'W', 'A', 'R', 'h', 'z', 'c', 'W',
		'r', 'G', 'H', 'D', 'g', 'U', '5', 'F', 'W', 'R', 'E', 'I', '/', 'c', 'j', 'I',
		'5', 'I', 'A', 'c', 'U', 'D', 'n', 's', 'j', 'a', 'P', 'x', 'h', 'Q', 'K', 'H',
		'2', 'F', 'c', '7', 'J', 'E', 'K', '5', 'F', '9', 'X', 'e', 'd', 's', 'W', 'V',
		'a', '5', 'F', '0', 'm', '/', '6', '7', 'w', 'f', 'k', 'n', '6', '9', '7', 's',
		'R', 'y', 'O', 't', 'e', 'v', 'E', 'N', 'B', '2', 'K', 'p', 'z', 'v', '7', 'o',
		'x', 'e', 'f', 'o', 'J', 'N', 'Q', 'N', 'w', 'x', 'P', 'B', 'b', 'q', 'S', '2',
		'e', '7', 'R', 'W', 'X', 'H', 'l', '2', 'd', 'E', 'Q', 'd', 'f', 'V', 'c', '=',
		'\n', '-', '-', '-', '-', '-', 'E', 'N', 'D', ' ', 'C', 'E', 'R', 'T', 'I', 'F',
		'I', 'C', 'A', 'T', 'E', '-', '-', '-', '-', '-',
	};
	
	@Before
	public void b() {
		POPSystem.initialize();
	}
	
	@After
	public void a() {
		POPSystem.end();
	}
	
	@Test
	public void addCertificateToRandomPOPObject() {
		A a = PopJava.newActive(A.class);
		POPAccessPoint ap = a.getAccessPoint();
		
		System.out.println("A available at " + ap);
		System.out.println("Connecting generic POPObject...");
		
		POPObject obj = PopJava.newActive(POPObject.class, ap);
		obj.PopRegisterFutureConnectorCertificate(certificate);
		obj.exit();
	}
	
	@Test
	public void addRandomBytesToPOPObject() {
		byte[] bytes = new byte[certificate.length];
		new Random().nextBytes(bytes);
		
		A a = PopJava.newActive(A.class);
		POPAccessPoint ap = a.getAccessPoint();
		
		System.out.println("A available at " + ap);
		System.out.println("Connecting generic POPObject...");
		
		POPObject obj = PopJava.newActive(POPObject.class, ap);
		obj.PopRegisterFutureConnectorCertificate(bytes);
		obj.exit();
	}
	
	public static class A extends POPObject {

		@POPObjectDescription(url = "localhost")
		public A() {
		}
		
		@POPSyncConc
		public void sync() {
			
		}
	}
}
