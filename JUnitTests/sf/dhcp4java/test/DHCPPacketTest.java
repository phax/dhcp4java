/*
 * Created on 27 août 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package sf.dhcp4java.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import sf.dhcp4java.DHCPPacket;
import sf.dhcp4java.test.HexUtils;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
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
	private byte[] null256 = new byte[256];
	
	public static junit.framework.Test suite() {
		  return new JUnit4TestAdapter(DHCPPacketTest.class);    
		}
	
    /*
     * @see TestCase#setUp()
     */
    @Before public void setUp() throws Exception {

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

    @Test public void testEquivalence() {
    	//assertEquals(refPacketFromHex, refPacketFromSratch);

//    	is($pac->comment(), undef, "comparing each attribute");
//    	is($pac->op(), BOOTREQUEST());
//    	is($pac->htype(), HTYPE_ETHER());
//    	is($pac->hlen(), 6);
//    	is($pac->hops(), 0);
//    	is($pac->xid(), 0x11223344);
//    	is($pac->flags(), 0x8000);
//    	is($pac->ciaddr(), "10.0.0.1");
//    	is($pac->ciaddrRaw(), "\x0a\x00\x00\x01");
//    	is($pac->yiaddr(), "10.0.0.2");
//    	is($pac->yiaddrRaw(), "\x0a\x00\x00\x02");
//    	is($pac->siaddr(), "10.0.0.3");
//    	is($pac->siaddrRaw(), "\x0a\x00\x00\x03");
//    	is($pac->giaddr(), "10.0.0.4");
//    	is($pac->giaddrRaw(), "\x0a\x00\x00\x04");
//    	is($pac->chaddr(), "00112233445566778899aabbccddeeff");
//    	is($pac->chaddrRaw(), "\x00\x11\x22\x33\x44\x55\x66\x77\x88\x99\xaa\xbb\xcc\xdd\xee\xff");
//    	is($pac->sname(), substr($str200, 0, 63));
//    	is($pac->file(), substr($str200, 0, 127));
//    	is($pac->padding(), "\x00" x 256);
//    	is($pac->isDhcp(), 1);
//
//    	is($pac->getOptionValue(DHO_DHCP_MESSAGE_TYPE()), DHCPDISCOVER());
//    	is($pac->getOptionValue(DHO_DHCP_SERVER_IDENTIFIER()), "12.34.56.68");
//    	is($pac->getOptionValue(DHO_DHCP_LEASE_TIME()), 86400);
//    	is($pac->getOptionValue(DHO_SUBNET_MASK()), "255.255.255.0");
//    	is($pac->getOptionValue(DHO_ROUTERS()), "10.0.0.254");
//    	is($pac->getOptionValue(DHO_STATIC_ROUTES()), "22.33.44.55 10.0.0.254");
//    	is($pac->getOptionValue(DHO_WWW_SERVER()), "10.0.0.6");
//    	is($pac->getOptionValue(DHO_IRC_SERVER()), undef);
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
    	assertTrue(ByteArrayComparator.equalsByteArray(InetAddress.getByName("10.0.0.1").getAddress(), p.getCiaddrRaw()));

//    	is($pac->ciaddr(), "10.0.0.1");
//    	is($pac->ciaddrRaw(), "\x0a\x00\x00\x01");
//    	is($pac->yiaddr(), "10.0.0.2");
//    	is($pac->yiaddrRaw(), "\x0a\x00\x00\x02");
//    	is($pac->siaddr(), "10.0.0.3");
//    	is($pac->siaddrRaw(), "\x0a\x00\x00\x03");
//    	is($pac->giaddr(), "10.0.0.4");
//    	is($pac->giaddrRaw(), "\x0a\x00\x00\x04");
//    	is($pac->chaddr(), "00112233445566778899aabbccddeeff");
//    	is($pac->chaddrRaw(), "\x00\x11\x22\x33\x44\x55\x66\x77\x88\x99\xaa\xbb\xcc\xdd\xee\xff");
//    	is($pac->sname(), substr($str200, 0, 63));
//    	is($pac->file(), substr($str200, 0, 127));
//    	is($pac->padding(), "\x00" x 256);
//    	is($pac->isDhcp(), 1);
//
//    	is($pac->getOptionValue(DHO_DHCP_MESSAGE_TYPE()), DHCPDISCOVER());
//    	is($pac->getOptionValue(DHO_DHCP_SERVER_IDENTIFIER()), "12.34.56.68");
//    	is($pac->getOptionValue(DHO_DHCP_LEASE_TIME()), 86400);
//    	is($pac->getOptionValue(DHO_SUBNET_MASK()), "255.255.255.0");
//    	is($pac->getOptionValue(DHO_ROUTERS()), "10.0.0.254");
//    	is($pac->getOptionValue(DHO_STATIC_ROUTES()), "22.33.44.55 10.0.0.254");
//    	is($pac->getOptionValue(DHO_WWW_SERVER()), "10.0.0.6");
//    	is($pac->getOptionValue(DHO_IRC_SERVER()), undef);
        //TODO Implement marshall().
    }

    @Test public void testSerialize() throws Exception {
        //TODO Implement serialize().
        //throw new Exception("toto");
    }

    @Test public void testHashCode() {
        //TODO Implement hashCode().
    }

    private static final String REF_PACKET = 
    	"0101060011223344000080000a0000010a0000020a0000030a00000400112233"+
    	"445566778899aabbccddeeff3132333435363738393031323334353637383930"+
    	"3132333435363738393031323334353637383930313233343536373839303132"+
    	"3334353637383930313233003132333435363738393031323334353637383930"+
    	"3132333435363738393031323334353637383930313233343536373839303132"+
    	"3334353637383930313233343536373839303132333435363738393031323334"+
    	"3536373839303132333435363738393031323334353637383930313233343536"+
    	"3738393031323334353637006382536335010136040c22384433040001518001"+
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
