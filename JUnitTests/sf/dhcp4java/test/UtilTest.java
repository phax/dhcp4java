/*
 *	This file is part of dhcp4java.
 *
 *	dhcp4java is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	dhcp4java is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Foobar; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * (c) 2006 Stephan Hadinger
 */
package sf.dhcp4java.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import sf.dhcp4java.Util;

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
