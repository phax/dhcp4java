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

import org.junit.Test;

import sf.dhcp4java.InetCidr;
import sf.dhcp4java.Util;

import junit.framework.JUnit4TestAdapter;

import static org.junit.Assert.*;

public class InetCidrTest {

	public static junit.framework.Test suite() {
       return new JUnit4TestAdapter(InetCidrTest.class);
    }

	@Test
	public void testAddrmask2CidrGood() {
		InetAddress ip = Util.int2InetAddress(0x12345678);
		InetCidr cidr1 = new InetCidr(ip, 30);
		InetCidr cidr2 = InetCidr.addrmask2Cidr(ip, Util.int2InetAddress(0xFFFFFFFC));
		assertEquals(cidr1, cidr2);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testAddrmask2CidrFail() {
		InetAddress ip = Util.int2InetAddress(0x12345678);
		InetCidr.addrmask2Cidr(ip, ip);		// exception should be raised here
	}
	
	@Test
	public void testBasic() {
		try {
			new InetCidr(null, 0);
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			// good
		}
		try {
			new InetCidr(Util.int2InetAddress(0), 34);
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			// good
		}
		InetCidr cidr = new InetCidr(Util.int2InetAddress(0x12345678), 10);
		assertEquals(cidr.getAddr(), Util.int2InetAddress(0x12000000));
		assertEquals(cidr.getMask(), 10);
	}
	
	@Test
	public void testAddr2Cidr() {
		int ip = 0xFFFFFFFF;
		int mask = 32;
		InetCidr[] addrs = InetCidr.addr2Cidr(Util.int2InetAddress(ip));
		
		assertEquals(32, addrs.length);
		for (int i=0; i<32; i++) {
			InetCidr refValue =  new InetCidr(Util.int2InetAddress(ip), mask);
			assertEquals(addrs[i], refValue);
			assertEquals(addrs[i].getAddr(), Util.int2InetAddress(ip));
			assertEquals(addrs[i].getMask(), mask);
			ip = ip << 1;
			mask--;
		}
	}
}
