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
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import sf.dhcp4java.DHCPBadPacketException;
import sf.dhcp4java.DHCPPacket;
import sf.dhcp4java.test.HexUtils;
import junit.framework.JUnit4TestAdapter;
import static sf.dhcp4java.DHCPConstants.*;

/**
 * @author yshi7355
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DHCPPacketTest {

	private DHCPPacket refPacketFromHex;
	private DHCPPacket refPacketFromSratch;
	
	public static junit.framework.Test suite() {
		  return new JUnit4TestAdapter(DHCPPacketTest.class);    
		}
	
    /*
     * @see TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {

    	byte[] refBuf = HexUtils.hexToBytes(REF_PACKET);
    	refPacketFromHex = DHCPPacket.getPacket(refBuf, 0, refBuf.length);
    	
    	DHCPPacket p = new DHCPPacket();
    	p.setOp(BOOTREQUEST);
    	p.setHtype(HTYPE_ETHER);
    	p.setHlen((byte) 6);
    	p.setHops((byte) 0);
    	p.setXid(0x11223344);
    	p.setFlags((short) 0x8000);
    	p.setCiaddr("10.0.0.1");
    	p.setYiaddr("10.0.0.2");
    	p.setSiaddr("10.0.0.3");
    	p.setGiaddr("10.0.0.4");
    	p.setChaddr(HexUtils.hexToBytes("00112233445566778899AABBCCDDEEFF"));
    	p.setSname(STR200.substring(0,64));
    	p.setFile(STR200.substring(0,128));
    	p.setDHCPMessageType(DHCPDISCOVER);
    	p.setOptionAsInetAddress(DHO_DHCP_SERVER_IDENTIFIER, "12.34.56.68");
    	p.setOptionAsInt(DHO_DHCP_LEASE_TIME, 86400);
    	p.setOptionAsInetAddress(DHO_SUBNET_MASK, "255.255.255.0");
    	p.setOptionAsInetAddress(DHO_ROUTERS, "10.0.0.254");
    	InetAddress[] staticRoutes = new InetAddress[2];
    	staticRoutes[0] = InetAddress.getByName("22.33.44.55");
    	staticRoutes[1] = InetAddress.getByName("10.0.0.254");
    	p.setOptionAsInetAddresses(DHO_STATIC_ROUTES, staticRoutes);
    	p.setOptionAsInetAddress(DHO_NTP_SERVERS, "10.0.0.5");
    	p.setOptionAsInetAddress(DHO_WWW_SERVER, "10.0.0.6");
    	p.setPaddingWithZeroes(256);
    	
    	refPacketFromSratch = p;
    	
    }

    /**
     * Constructor for PacketTest.
     * @param arg0
     */
    public DHCPPacketTest() {
    }

    
    @Test public void compareRefScratch() {
    	// compare packet serialized from packet built from scratch
    	// compare DHCPPacket objects
    	assertEquals(refPacketFromHex, refPacketFromSratch);
    	// compare byte[] datagrams
    	assertTrue(Arrays.equals(HexUtils.hexToBytes(REF_PACKET),
    			refPacketFromSratch.serialize()));
    }
    
    @Test public void testMarshall() throws UnknownHostException {
    	// test if serialized packet has the right parameters
    	DHCPPacket p = refPacketFromHex;
    	
    	assertEquals("", p.getComment());
    	assertEquals(BOOTREQUEST, p.getOp());
    	assertEquals(HTYPE_ETHER, p.getHtype());
    	assertEquals((byte) 6, p.getHlen());
    	assertEquals((byte)0, p.getHops());
    	assertEquals(0x11223344, p.getXid());
    	assertEquals((short) 0x8000, p.getFlags());
    	assertEquals(InetAddress.getByName("10.0.0.1"), p.getCiaddr());
    	assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.1").getAddress(), p.getCiaddrRaw()));
    	assertEquals(InetAddress.getByName("10.0.0.2"), p.getYiaddr());
    	assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.2").getAddress(), p.getYiaddrRaw()));
    	assertEquals(InetAddress.getByName("10.0.0.3"), p.getSiaddr());
    	assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.3").getAddress(), p.getSiaddrRaw()));
    	assertEquals(InetAddress.getByName("10.0.0.4"), p.getGiaddr());
    	assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.4").getAddress(), p.getGiaddrRaw()));
    	
    	assertEquals("00112233445566778899aabbccddeeff".substring(0, 2*p.getHlen()), p.getChaddrAsHex());
    	assertTrue(Arrays.equals(HexUtils.hexToBytes("00112233445566778899AABBCCDDEEFF"), p.getChaddr()));
    	
    	assertEquals(STR200.substring(0,64), p.getSname());
    	assertEquals(STR200.substring(0, 128), p.getFile());
    	assertTrue(Arrays.equals(new byte[256], p.getPadding()));
    	assertTrue(p.isDhcp());
    	assertFalse(p.isTruncated());
    	
    	assertEquals(DHCPDISCOVER, p.getDHCPMessageType());
    	assertEquals("12.34.56.68", p.getOptionAsInetAddr(DHO_DHCP_SERVER_IDENTIFIER).getHostAddress());
    	assertEquals(86400, p.getOptionAsInteger(DHO_DHCP_LEASE_TIME));
    	assertEquals("255.255.255.0", p.getOptionAsInetAddr(DHO_SUBNET_MASK).getHostAddress());
    	assertEquals(1, p.getOptionAsInetAddrs(DHO_ROUTERS).length);
    	assertEquals("10.0.0.254", p.getOptionAsInetAddrs(DHO_ROUTERS)[0].getHostAddress());
    	assertEquals(2, p.getOptionAsInetAddrs(DHO_STATIC_ROUTES).length);
    	assertEquals("22.33.44.55", p.getOptionAsInetAddrs(DHO_STATIC_ROUTES)[0].getHostAddress());
    	assertEquals("10.0.0.254", p.getOptionAsInetAddrs(DHO_STATIC_ROUTES)[1].getHostAddress());
    	assertEquals(1, p.getOptionAsInetAddrs(DHO_WWW_SERVER).length);
    	assertEquals("10.0.0.6", p.getOptionAsInetAddrs(DHO_WWW_SERVER)[0].getHostAddress());
    	assertEquals(null, p.getOptionAsInetAddrs(DHO_IRC_SERVER));
    }

    @Test public void testSerialize() throws Exception {
        //TODO Implement serialize().
        //throw new Exception("toto");
    }
    
    @Test public void testSerializeExtremeValues() {
    	// bad values
    	try {
    		byte[] buf = new byte[0];
    		
    		DHCPPacket.getPacket(buf, -1, 10);
    		assertTrue(false);
    	} catch (IndexOutOfBoundsException e) {
    		// good
    	}
    	
    	// packet too small
    	try {
    		byte[] buf = new byte[47];
    		DHCPPacket.getPacket(buf, 0, buf.length);
    		assertTrue(false);
    	} catch (DHCPBadPacketException e) {
    		// good
    	}
    	
    	// packet too big
    	try {
    		byte[] buf = new byte[4700];
    		DHCPPacket.getPacket(buf, 0, buf.length);
    		assertTrue(false);
    	} catch (DHCPBadPacketException e) {
    		// good
    	}
    }

    private static final String REF_PACKET = 
    	"0101060011223344000080000a0000010a0000020a0000030a00000400112233"+
    	"445566778899aabbccddeeff3132333435363738393031323334353637383930"+
    	"3132333435363738393031323334353637383930313233343536373839303132"+
    	"3334353637383930313233343132333435363738393031323334353637383930"+
    	"3132333435363738393031323334353637383930313233343536373839303132"+
    	"3334353637383930313233343536373839303132333435363738393031323334"+
    	"3536373839303132333435363738393031323334353637383930313233343536"+
    	"3738393031323334353637386382536335010136040c22384433040001518001"+
    	"04ffffff0003040a0000fe210816212c370a0000fe2a040a00000548040a0000"+
    	"06ff000000000000000000000000000000000000000000000000000000000000"+
    	"0000000000000000000000000000000000000000000000000000000000000000"+
    	"0000000000000000000000000000000000000000000000000000000000000000"+
    	"0000000000000000000000000000000000000000000000000000000000000000"+
    	"0000000000000000000000000000000000000000000000000000000000000000"+
    	"0000000000000000000000000000000000000000000000000000000000000000"+
    	"0000000000000000000000000000000000000000000000000000000000000000"+
    	"0000000000000000000000000000000000000000000000000000000000000000"+
    	"0000";
    private static final String STR200 = 
    	"12345678901234567890123456789012345678901234567890"+ //50
    	"12345678901234567890123456789012345678901234567890"+ //50
    	"12345678901234567890123456789012345678901234567890"+ //50
    	"12345678901234567890123456789012345678901234567890"; //50
}
