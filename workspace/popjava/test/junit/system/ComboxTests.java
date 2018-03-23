package junit.system;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import popjava.baseobject.AccessPoint;
import popjava.baseobject.POPAccessPoint;
import popjava.combox.socket.ComboxSocket;
import popjava.combox.socket.raw.ComboxSocketFactory;
import popjava.combox.socket.ssl.ComboxSecureSocketFactory;

public class ComboxTests {

	@Test
	public void testAccessPointSorting() {
		
		POPAccessPoint ap = new POPAccessPoint();
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "1.2.3.4", 1234));
		
		assertEquals(1, ComboxSocket.getSortedAccessPoints("", ap, ComboxSocketFactory.PROTOCOL).size());
		assertEquals(0, ComboxSocket.getSortedAccessPoints("", ap, ComboxSecureSocketFactory.PROTOCOL).size());
		
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "2.4.5.6", 1234));

		List<AccessPoint> sorted = ComboxSocket.getSortedAccessPoints("2.4.4.5", ap, ComboxSocketFactory.PROTOCOL);
		assertEquals(2, sorted.size());
		assertEquals("2.4.5.6", sorted.get(0).getHost());
		
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "www.asdf.com", 1234));
		
		sorted = ComboxSocket.getSortedAccessPoints("9.9.9.9", ap, ComboxSocketFactory.PROTOCOL);
		assertEquals(3, sorted.size());
		assertEquals("www.asdf.com", sorted.get(0).getHost());
		
	}
	
	@Test
	public void testNATIssue() {
		POPAccessPoint ap = new POPAccessPoint();
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "192.168.1.110", 1234));
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "188.63.60.139", 1234));
		
		List<AccessPoint> sorted = ComboxSocket.getSortedAccessPoints("160.98.61.39", ap, ComboxSocketFactory.PROTOCOL);
		assertEquals(2, sorted.size());
		assertEquals("188.63.60.139", sorted.get(0).getHost());
	}
	
}
