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
package sf.dhcp4javatest;

import java.net.InetAddress;
import java.util.Arrays;

import org.junit.Test;

import sf.dhcp4java.DHCPOption;
import junit.framework.JUnit4TestAdapter;

import static sf.dhcp4java.DHCPConstants.*;
import static junit.framework.Assert.*;

public class DHCPOptionTest {
	
	private static final String testString = "foobar";
	private static final byte[] buf = testString.getBytes();

	public static junit.framework.Test suite() {
	       return new JUnit4TestAdapter(DHCPOptionTest.class);
	    }
	
	@Test (expected=IllegalArgumentException.class)
	public void testConstructorFailPad(){
		new DHCPOption(DHO_PAD, null);
	}
	@Test (expected=IllegalArgumentException.class)
	public void testConstructorFailEnd(){
		new DHCPOption(DHO_END, null);
	}
	@Test (expected=IllegalArgumentException.class)
	public void testConstructorFailTooBig(){
		new DHCPOption(DHO_DHCP_MESSAGE_TYPE, new byte[256]);
	}
	
	@Test
	public void testConstructor() {
		DHCPOption opt = new DHCPOption(DHO_DHCP_MESSAGE, buf);
		
		assertEquals(opt.getCode(), DHO_DHCP_MESSAGE);
		assertTrue(Arrays.equals(opt.getValue(), buf));
		assertTrue(opt.getValue() != buf);		// value should be cloned
		assertTrue(opt.getValueFast() == buf);	// but fast variant should not clone
	}
	
	@Test
	public void testConstructorNull() {
		DHCPOption opt = new DHCPOption(DHO_DHCP_MESSAGE, null);
		
		assertEquals(opt.getCode(), DHO_DHCP_MESSAGE);
		assertEquals(opt.getValue(), null);
	}
	
	@Test
	public void testEquals() {
		DHCPOption opt1 = new DHCPOption(DHO_BOOTFILE, buf);
		DHCPOption opt2 = new DHCPOption(DHO_BOOTFILE, buf.clone());
		
		assertTrue(opt1.equals(opt1));
		assertTrue(opt1.equals(opt2));
		assertFalse(opt1.equals(null));
		assertFalse(opt1.equals(new Integer(1)));
	}
	
	@Test
	public void testHashCode() {
		DHCPOption opt1 = new DHCPOption(DHO_BOOTFILE, buf);
		DHCPOption opt2 = new DHCPOption(DHO_DHCP_MESSAGE, buf);
		assertTrue(opt1.hashCode() != 0);
		assertTrue(opt1.hashCode() != opt2.hashCode());
	}
	
	@Test
	public void testToString() {
		DHCPOption opt1 = new DHCPOption(DHO_BOOTFILE, buf);
		assertEquals(opt1.toString(), "DHO_BOOTFILE(67)=\"foobar\"");
	}
	
	@Test
	public void runMain() throws Exception {
		// there is no real test here, this is just to avoid noise in code coverage tools
		DHCPOption.main(null);
	}
	
	@Test
	public void testUserClassToX() {
		assertNull(DHCPOption.userClassToString(null));
		byte[] userClassBuf1 = "\03foo\06foobar".getBytes();
		assertEquals(DHCPOption.userClassToString(userClassBuf1), "\"foo\",\"foobar\"");
		byte[] userClassBuf2 = "\03foo".getBytes();
		assertEquals(DHCPOption.userClassToString(userClassBuf2), "\"foo\"");
		byte[] userClassBuf3 = "\07foo".getBytes();
		assertEquals(DHCPOption.userClassToString(userClassBuf3), "\"foo\"");
		assertEquals(DHCPOption.userClassToString(new byte[0]), "");
		assertEquals(DHCPOption.userClassToString(new byte[1]), "\"\"");
		assertEquals(DHCPOption.userClassToString(new byte[2]), "\"\",\"\"");
		assertNotNull(DHCPOption.userClassToString(new byte[255]));
		
		// userClassToList are only tested through their string representation
		// last test for null only
		assertNull(DHCPOption.userClassToList(null));
	}
	@Test (expected=IllegalArgumentException.class)
	public void testUserClassToListTooBig() {
		assertNotNull(DHCPOption.userClassToList(new byte[256]));
	}
	@Test (expected=IllegalArgumentException.class)
	public void testUserClassToStringTooBig() {
		assertNotNull(DHCPOption.userClassToString(new byte[256]));
	}
	// ----------------------------------------------------------------------
	// testing type conversion
	@Test
	public void testByte2Bytes() {
		assertTrue(Arrays.equals(DHCPOption.byte2Bytes((byte)0), new byte[1]));
		byte[] buf1 = { (byte) 0xff };
		assertTrue(Arrays.equals(DHCPOption.byte2Bytes((byte)-1), buf1));
	}
	@Test
	public void testShort2Bytes() {
		assertTrue(Arrays.equals(DHCPOption.short2Bytes((short)0), new byte[2]));
		byte[] buf1 = { (byte) 0xff, (byte) 0xff };
		assertTrue(Arrays.equals(DHCPOption.short2Bytes((short)-1), buf1));
		byte[] buf2 = { (byte) 0x11, (byte) 0x22 };
		assertTrue(Arrays.equals(DHCPOption.short2Bytes((short)0x1122), buf2));
	}
	@Test
	public void testIntBytes() {
		assertTrue(Arrays.equals(DHCPOption.int2Bytes(0), new byte[4]));
		byte[] buf1 = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
		assertTrue(Arrays.equals(DHCPOption.int2Bytes(-1), buf1));
		byte[] buf2 = { (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44 };
		assertTrue(Arrays.equals(DHCPOption.int2Bytes(0x11223344), buf2));
	}
	
	@Test
	public void testInetAddress2Bytes() throws Exception {
		assertNull(DHCPOption.inetAddress2Bytes(null));
		byte[] buf = { (byte) 10, (byte) 11, (byte) 12, (byte) 13 };
		InetAddress adr = InetAddress.getByName("10.11.12.13");
		assertTrue(Arrays.equals(DHCPOption.inetAddress2Bytes(adr), buf));
	}
	@Test (expected=IllegalArgumentException.class)
	public void testInetAddress2BytesFailNonIpv4() throws Exception {
		DHCPOption.inetAddress2Bytes(InetAddress.getByName("1080:0:0:0:8:800:200C:417A"));
	}
}
