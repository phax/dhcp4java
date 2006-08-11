/*
 *	This file is part of dhcp4java, a DHCP API for the Java language.
 *	(c) 2006 Stephan Hadinger
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package sf.dhcp4java.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.dhcp4java.Util;
import org.junit.Test;


import junit.framework.JUnit4TestAdapter;

import static org.junit.Assert.*;

public class UtilTest {

	public static junit.framework.Test suite() {
       return new JUnit4TestAdapter(UtilTest.class);
    }
	
	@Test
	public void testInt2InetAddress() {
		InetAddress ip;
		
		try {
			ip = InetAddress.getByName("0.0.0.0");
			assertEquals(ip, Util.int2InetAddress(0));
			ip = InetAddress.getByName("255.255.255.255");
			assertEquals(ip, Util.int2InetAddress(-1));
			ip = InetAddress.getByName("10.0.0.1");
			assertEquals(ip, Util.int2InetAddress(0x0A000001));
		} catch (UnknownHostException e) {
			assertTrue("UnknownHostException raised", false);
		}
	}
	
	@Test
	public void testInetAddress2Int() {
		InetAddress ip;
		
		try {
			ip = InetAddress.getByName("0.0.0.0");
			assertEquals(Util.inetAddress2Int(ip), 0);
			ip = InetAddress.getByName("255.255.255.255");
			assertEquals(Util.inetAddress2Int(ip), -1);
			ip = InetAddress.getByName("10.0.0.1");
			assertEquals(Util.inetAddress2Int(ip), 0x0A000001);
		} catch (UnknownHostException e) {
			assertTrue("UnknownHostException raised", false);
		}
	}
}
