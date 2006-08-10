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
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import sf.dhcp4java.DHCPBadPacketException;
import sf.dhcp4java.DHCPPacket;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import static sf.dhcp4java.DHCPConstants.*;

/**
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

        this.refPacketFromHex = DHCPPacket.getPacket(refBuf, 0, refBuf.length);
    	
    	DHCPPacket packet = new DHCPPacket();
    	packet.setOp(BOOTREQUEST);
    	packet.setHtype(HTYPE_ETHER);
    	packet.setHlen((byte) 6);
    	packet.setHops((byte) 0);
    	packet.setXid(0x11223344);
    	packet.setFlags((short) 0x8000);
    	packet.setCiaddr("10.0.0.1");
    	packet.setYiaddr("10.0.0.2");
    	packet.setSiaddr("10.0.0.3");
    	packet.setGiaddr("10.0.0.4");
    	packet.setChaddr(HexUtils.hexToBytes("00112233445566778899AABBCCDDEEFF"));
    	packet.setSname(STR200.substring(0,  64));
    	packet.setFile (STR200.substring(0, 128));
    	packet.setDHCPMessageType(DHCPDISCOVER);
    	packet.setOptionAsInetAddress(DHO_DHCP_SERVER_IDENTIFIER, "12.34.56.68");
    	packet.setOptionAsInt(DHO_DHCP_LEASE_TIME, 86400);
    	packet.setOptionAsInetAddress(DHO_SUBNET_MASK, "255.255.255.0");
    	packet.setOptionAsInetAddress(DHO_ROUTERS, "10.0.0.254");
    	InetAddress[] staticRoutes = new InetAddress[2];
    	staticRoutes[0] = InetAddress.getByName("22.33.44.55");
    	staticRoutes[1] = InetAddress.getByName("10.0.0.254");
    	packet.setOptionAsInetAddresses(DHO_STATIC_ROUTES, staticRoutes);
    	packet.setOptionAsInetAddress(DHO_NTP_SERVERS, "10.0.0.5");
    	packet.setOptionAsInetAddress(DHO_WWW_SERVER, "10.0.0.6");
    	packet.setPaddingWithZeroes(256);

        this.refPacketFromSratch = packet;
    }

    /**
     * Constructor for PacketTest.
     */
    public DHCPPacketTest() {
    }

    
    @Test
    public void compareRefScratch() {
    	// compare packet serialized from packet built from scratch
    	// compare DHCPPacket objects
    	Assert.assertEquals(this.refPacketFromHex, this.refPacketFromSratch);
    	// compare byte[] datagrams
    	Assert.assertTrue(Arrays.equals(HexUtils.hexToBytes(REF_PACKET),
                                 this.refPacketFromSratch.serialize()));
    }
    
    @Test
    public void testMarshall() throws UnknownHostException {
    	// test if serialized packet has the right parameters
    	DHCPPacket packet = this.refPacketFromHex;
    	
    	Assert.assertEquals("", packet.getComment());
    	Assert.assertEquals(BOOTREQUEST, packet.getOp());
    	Assert.assertEquals(HTYPE_ETHER, packet.getHtype());
    	Assert.assertEquals((byte) 6, packet.getHlen());
    	Assert.assertEquals((byte)0, packet.getHops());
    	Assert.assertEquals(0x11223344, packet.getXid());
    	Assert.assertEquals((short) 0x8000, packet.getFlags());
    	Assert.assertEquals(InetAddress.getByName("10.0.0.1"), packet.getCiaddr());
    	Assert.assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.1").getAddress(), packet.getCiaddrRaw()));
    	Assert.assertEquals(InetAddress.getByName("10.0.0.2"), packet.getYiaddr());
    	Assert.assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.2").getAddress(), packet.getYiaddrRaw()));
    	Assert.assertEquals(InetAddress.getByName("10.0.0.3"), packet.getSiaddr());
    	Assert.assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.3").getAddress(), packet.getSiaddrRaw()));
    	Assert.assertEquals(InetAddress.getByName("10.0.0.4"), packet.getGiaddr());
    	Assert.assertTrue(Arrays.equals(InetAddress.getByName("10.0.0.4").getAddress(), packet.getGiaddrRaw()));
    	
    	Assert.assertEquals("00112233445566778899aabbccddeeff".substring(0, 2*packet.getHlen()), packet.getChaddrAsHex());
    	Assert.assertTrue(Arrays.equals(HexUtils.hexToBytes("00112233445566778899AABBCCDDEEFF"), packet.getChaddr()));
    	
    	Assert.assertEquals(STR200.substring(0,  64), packet.getSname());
    	Assert.assertEquals(STR200.substring(0, 128), packet.getFile());
    	Assert.assertTrue(Arrays.equals(new byte[256], packet.getPadding()));
    	Assert.assertTrue(packet.isDhcp());
    	Assert.assertFalse(packet.isTruncated());
    	
    	Assert.assertEquals(DHCPDISCOVER, packet.getDHCPMessageType().byteValue());
    	Assert.assertEquals("12.34.56.68", packet.getOptionAsInetAddr(DHO_DHCP_SERVER_IDENTIFIER).getHostAddress());
    	Assert.assertEquals(86400, packet.getOptionAsInteger(DHO_DHCP_LEASE_TIME).intValue());
    	Assert.assertEquals("255.255.255.0", packet.getOptionAsInetAddr(DHO_SUBNET_MASK).getHostAddress());
    	Assert.assertEquals(1, packet.getOptionAsInetAddrs(DHO_ROUTERS).length);
    	Assert.assertEquals("10.0.0.254", packet.getOptionAsInetAddrs(DHO_ROUTERS)[0].getHostAddress());
    	Assert.assertEquals(2, packet.getOptionAsInetAddrs(DHO_STATIC_ROUTES).length);
    	Assert.assertEquals("22.33.44.55", packet.getOptionAsInetAddrs(DHO_STATIC_ROUTES)[0].getHostAddress());
    	Assert.assertEquals("10.0.0.254", packet.getOptionAsInetAddrs(DHO_STATIC_ROUTES)[1].getHostAddress());
    	Assert.assertEquals(1, packet.getOptionAsInetAddrs(DHO_WWW_SERVER).length);
    	Assert.assertEquals("10.0.0.6", packet.getOptionAsInetAddrs(DHO_WWW_SERVER)[0].getHostAddress());
    	Assert.assertEquals(null, packet.getOptionAsInetAddrs(DHO_IRC_SERVER));
    }

    @Test public void testSerialize() throws Exception {
        //TODO Implement serialize().
        //throw new Exception("toto");
    }

    @Test (expected=IndexOutOfBoundsException.class)
    public void testSerializeBadValues() {
        testPacket(   0, -1,   10);    // bad values
    }
    
    @Test (expected=DHCPBadPacketException.class)
    public void testSerializeTooSmall() {
        testPacket(  47,  0,   47);    // packet too small
    }

    @Test (expected=DHCPBadPacketException.class)
    public void testSerializeTooBig() {
        testPacket(4700,  0, 4700);    // packet too big
    }
    
    private static void testPacket(int size, int offset, int length) {
    	DHCPPacket.getPacket(new byte[size], offset, length);
    }

    private static final String REF_PACKET =
        "0101060011223344000080000a0000010a0000020a0000030a00000400112233" +
        "445566778899aabbccddeeff3132333435363738393031323334353637383930" +
        "3132333435363738393031323334353637383930313233343536373839303132" +
        "3334353637383930313233343132333435363738393031323334353637383930" +
        "3132333435363738393031323334353637383930313233343536373839303132" +
        "3334353637383930313233343536373839303132333435363738393031323334" +
        "3536373839303132333435363738393031323334353637383930313233343536" +
        "3738393031323334353637386382536335010136040c22384433040001518001" +
        "04ffffff0003040a0000fe210816212c370a0000fe2a040a00000548040a0000" +
        "06ff000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000000" +
        "0000";
    private static final String STR200 = 
    	"12345678901234567890123456789012345678901234567890" + //50
    	"12345678901234567890123456789012345678901234567890" + //50
    	"12345678901234567890123456789012345678901234567890" + //50
    	"12345678901234567890123456789012345678901234567890";  //50
}
