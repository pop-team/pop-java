package ch.icosys.popjava.junit.system;

import static org.junit.Assert.assertEquals;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import ch.icosys.popjava.core.baseobject.AccessPoint;
import ch.icosys.popjava.core.baseobject.POPAccessPoint;
import ch.icosys.popjava.core.combox.socket.ComboxSocket;
import ch.icosys.popjava.core.combox.socket.raw.ComboxSocketFactory;
import ch.icosys.popjava.core.combox.socket.ssl.ComboxSecureSocketFactory;

public class ComboxTests {

	@Test
	public void testAccessPointSorting() throws UnknownHostException {

		POPAccessPoint ap = new POPAccessPoint();
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "1.2.3.4", 1234));

		assertEquals(1, ComboxSocket.getSortedAccessPoints(null, ap, ComboxSocketFactory.PROTOCOL).size());
		assertEquals(0, ComboxSocket.getSortedAccessPoints(null, ap, ComboxSecureSocketFactory.PROTOCOL).size());

		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "2.4.5.6", 1234));

		List<AccessPoint> sorted = ComboxSocket.getSortedAccessPoints(mockAddress("2.4.4.5", (short)16), ap, ComboxSocketFactory.PROTOCOL);
		assertEquals(2, sorted.size());
		assertEquals("2.4.5.6", sorted.get(0).getHost());

		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "www.asdf.com", 1234));

		sorted = ComboxSocket.getSortedAccessPoints(mockAddress("9.9.9.9", (short)16), ap, ComboxSocketFactory.PROTOCOL);
		assertEquals(3, sorted.size());
		assertEquals("www.asdf.com", sorted.get(0).getHost());
	}

	@Test
	public void testNATIssue() throws UnknownHostException {
		POPAccessPoint ap = new POPAccessPoint();
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "192.168.1.110", 1234));
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "188.63.60.139", 1234));

		List<AccessPoint> sorted = ComboxSocket.getSortedAccessPoints(mockAddress("160.98.61.39", (short)16), ap, ComboxSocketFactory.PROTOCOL);
		assertEquals(2, sorted.size());
		assertEquals("188.63.60.139", sorted.get(0).getHost());
	}
	
	@Test
	public void testNATIssue2() throws UnknownHostException {
		POPAccessPoint ap = new POPAccessPoint();
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "192.168.1.110", 1234));
		ap.addAccessPoint(new AccessPoint(ComboxSocketFactory.PROTOCOL, "188.63.60.139", 1234));

		List<AccessPoint> sorted = ComboxSocket.getSortedAccessPoints(mockAddress("192.168.110.23", (short)24), ap, ComboxSocketFactory.PROTOCOL);
		assertEquals(2, sorted.size());
		assertEquals("188.63.60.139", sorted.get(0).getHost());
	}
	
	private InterfaceAddress mockAddress(String ip, short maskLength) throws UnknownHostException {
		InterfaceAddress addr = Mockito.mock(InterfaceAddress.class);
		
		Mockito.when(addr.getNetworkPrefixLength()).thenReturn(maskLength);
		
		InetAddress inetAddr = Inet4Address.getByName(ip);
		
		Mockito.when(addr.getAddress()).thenReturn(inetAddr);
		
		return addr;
	}

}
