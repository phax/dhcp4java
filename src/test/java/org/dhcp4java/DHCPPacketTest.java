/**
 *  This file is part of dhcp4java, a DHCP API for the Java language.
 *  (c) 2006 Stephan Hadinger
 *  (c) 2018 Philip Helger
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.dhcp4java;

import static org.dhcp4java.DHCPConstants.*;
import static org.dhcp4java.HexUtils.hexToBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DHCPPacketTest
{
  private static DHCPPacket s_aRefPacketFromHex;
  private static DHCPPacket s_aRefPacketFromScratch;
  private DHCPPacket m_aPac0;

  /*
   * @see TestCase#setUp()
   */
  @BeforeClass
  public static void setUpOnce () throws Exception
  {
    final byte [] refBuf = HexUtils.hexToBytes (REF_PACKET);

    s_aRefPacketFromHex = DHCPPacket.getPacket (refBuf, 0, refBuf.length, true);
    s_aRefPacketFromHex.setComment ("foobar");
    s_aRefPacketFromHex.setAddress (InetAddress.getByName ("10.11.12.13"));
    s_aRefPacketFromHex.setPort (6767);

    final DHCPPacket aPacket = new DHCPPacket ();
    aPacket.setComment ("foobar");
    aPacket.setOp (BOOTREQUEST);
    aPacket.setHtype (HTYPE_ETHER);
    aPacket.setHlen ((byte) 6);
    aPacket.setHops ((byte) 0);
    aPacket.setSecs ((short) 0);
    aPacket.setXid (0x11223344);
    aPacket.setFlags ((short) 0x8000);
    aPacket.setCiaddr ("10.0.0.1");
    aPacket.setYiaddr ("10.0.0.2");
    aPacket.setSiaddr ("10.0.0.3");
    aPacket.setGiaddr ("10.0.0.4");
    aPacket.setChaddr (HexUtils.hexToBytes ("00112233445566778899AABBCCDDEEFF"));
    aPacket.setSname (STR200.substring (0, 64));
    aPacket.setFile (STR200.substring (0, 128));
    aPacket.setDHCPMessageType (DHCPDISCOVER);
    aPacket.setOptionAsInetAddress (DHO_DHCP_SERVER_IDENTIFIER, "12.34.56.68");
    aPacket.setOptionAsInt (DHO_DHCP_LEASE_TIME, 86400);
    aPacket.setOptionAsInetAddress (DHO_SUBNET_MASK, "255.255.255.0");
    aPacket.setOptionAsInetAddress (DHO_ROUTERS, "10.0.0.254");
    final InetAddress [] staticRoutes = new InetAddress [2];
    staticRoutes[0] = InetAddress.getByName ("22.33.44.55");
    staticRoutes[1] = InetAddress.getByName ("10.0.0.254");
    aPacket.setOptionAsInetAddresses (DHO_STATIC_ROUTES, staticRoutes);
    aPacket.setOptionAsInetAddress (DHO_NTP_SERVERS, "10.0.0.5");
    aPacket.setOptionAsInetAddress (DHO_WWW_SERVER, "10.0.0.6");
    aPacket.setPaddingWithZeroes (256);
    aPacket.setAddress (InetAddress.getByName ("10.11.12.13"));
    aPacket.setPort (6767);
    s_aRefPacketFromScratch = aPacket;
  }

  @Before
  public void setUp ()
  {
    m_aPac0 = new DHCPPacket ();
  }

  @Test
  public void testConstrucor () throws Exception
  {
    final DHCPPacket pac = new DHCPPacket ();

    assertEquals ("", pac.getComment ());
    assertEquals (BOOTREPLY, pac.getOp ());
    assertEquals (HTYPE_ETHER, pac.getHtype ());
    assertEquals (6, pac.getHlen ());
    assertEquals ((short) 0, pac.getSecs ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), pac.getCiaddr ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), pac.getYiaddr ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), pac.getSiaddr ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), pac.getGiaddr ());
    assertArrayEquals (new byte [16], pac.getChaddr ());
    assertEquals ("", pac.getSname ());
    assertEquals ("", pac.getFile ());
    assertTrue (Arrays.equals (new byte [0], pac.getPadding ()));
    assertTrue (pac.isDhcp ());
    assertFalse (pac.isTruncated ());
    final DHCPOption [] opts = pac.getOptionsArray ();
    assertNotNull (opts);
    assertEquals (0, opts.length);

    assertNull (pac.getAddress ());
    assertEquals (0, pac.getPort ());

  }

  @Test
  public void testGetPacket () throws Exception
  {
    final byte [] buf = hexToBytes (REF_PACKET);
    final DatagramPacket udp = new DatagramPacket (buf, buf.length);
    udp.setAddress (InetAddress.getByName ("10.11.12.13"));
    udp.setPort (6767);
    final DHCPPacket pac = DHCPPacket.getPacket (udp);
    pac.setComment ("foobar");

    assertEquals (s_aRefPacketFromHex, pac);

  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetPacketNull () throws Exception
  {
    DHCPPacket.getPacket (null);
  }

  // marshall
  @Test (expected = IllegalArgumentException.class)
  public void testMarshallNull ()
  {
    DHCPPacket.getPacket (null, 0, 256, true);
  }

  @Test (expected = IndexOutOfBoundsException.class)
  public void testMarshallNegativeOffset ()
  {
    DHCPPacket.getPacket (new byte [256], -1, 256, true);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testMarshallNegativeLength ()
  {
    DHCPPacket.getPacket (new byte [256], 0, -1, true);
  }

  @Test (expected = IndexOutOfBoundsException.class)
  public void testMarshallLentghTooLong ()
  {
    DHCPPacket.getPacket (new byte [256], 1, 256, true);
  }

  @Test
  public void testMarshallUnderLimits ()
  {
    DHCPPacket.getPacket (new byte [300], 0, 300, true);
    DHCPPacket.getPacket (new byte [1500], 0, 1500, true);
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testMarshallLentghPacketTooSmall ()
  {
    DHCPPacket.getPacket (new byte [235], 0, 235, true);
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testMarshallLentghPacketTooBig ()
  {
    DHCPPacket.getPacket (new byte [1501], 0, 1501, true);
  }

  // serialize
  @Test
  public void testSerializeLimits ()
  {
    byte [] buf;
    final DHCPPacket pac = new DHCPPacket ();
    buf = pac.serialize ();
    assertEquals (300, buf.length);

    pac.setOptionRaw (DHO_HOST_NAME, new byte [255]);
    buf = pac.serialize ();
    assertEquals (498, buf.length);

    buf = pac.serialize (1500, 1500);
    assertEquals (1500, buf.length);
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testSerializeOptionOver256 ()
  {
    final DHCPPacket pac = new DHCPPacket ();
    pac.setOptionRaw (DHO_HOST_NAME, new byte [256]);
    pac.serialize ();
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testSerializePacketTooBig ()
  {
    final DHCPPacket pac = new DHCPPacket ();
    pac.setOptionRaw ((byte) 11, new byte [255]);
    pac.setOptionRaw ((byte) 12, new byte [255]);
    pac.setOptionRaw ((byte) 13, new byte [255]);
    pac.setOptionRaw ((byte) 14, new byte [255]);
    pac.setOptionRaw ((byte) 15, new byte [255]);
    pac.serialize ();
  }

  // Limit tests for setters/getter
  @Test
  public void testSetCHAddrUnderLimit ()
  {
    final String bufs = "FFEEDDCCBBAA9988776655443322110F";
    final DHCPPacket pac = new DHCPPacket ();
    pac.setHlen ((byte) 16);
    pac.setChaddr (hexToBytes (bufs));
    assertEquals (bufs, pac.getChaddrAsHex ());
    pac.setChaddr (null);
    assertEquals ("00000000000000000000000000000000", pac.getChaddrAsHex ());
    pac.setHlen ((byte) 255);
    assertEquals ("00000000000000000000000000000000", pac.getChaddrAsHex ());
    pac.setChaddrHex (bufs);
    assertTrue (Arrays.equals (hexToBytes (bufs), pac.getChaddr ()));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetCHAddrTooLong ()
  {
    final DHCPPacket pac = new DHCPPacket ();
    pac.setHlen ((byte) 16);
    pac.setChaddr (hexToBytes ("FFEEDDCCBBAA9988776655443322110F00"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetCHAddrHexNotEven ()
  {
    m_aPac0.setChaddrHex ("0");
  }

  // ----
  // bad addresses
  @Test (expected = IllegalArgumentException.class)
  public void testSetCIAddrIPv6 () throws Exception
  {
    m_aPac0.setCiaddr (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetCIAddrRaw3 () throws Exception
  {
    m_aPac0.setCiaddrRaw (new byte [3]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetCIAddrRaw5 () throws Exception
  {
    m_aPac0.setCiaddrRaw (new byte [5]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetSIAddrIPv6 () throws Exception
  {
    m_aPac0.setSiaddr (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetSIAddrRaw3 () throws Exception
  {
    m_aPac0.setSiaddrRaw (new byte [3]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetSIAddrRaw5 () throws Exception
  {
    m_aPac0.setSiaddrRaw (new byte [5]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetYIAddrIPv6 () throws Exception
  {
    m_aPac0.setYiaddr (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetYIAddrRaw3 () throws Exception
  {
    m_aPac0.setYiaddrRaw (new byte [3]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetYIAddrRaw5 () throws Exception
  {
    m_aPac0.setYiaddrRaw (new byte [5]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetGIAddrIPv6 () throws Exception
  {
    m_aPac0.setGiaddr (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetGIAddrRaw3 () throws Exception
  {
    m_aPac0.setGiaddrRaw (new byte [3]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetGIAddrRaw5 () throws Exception
  {
    m_aPac0.setGiaddrRaw (new byte [5]);
  }

  // SName
  @Test
  public void testSetSnameRaw ()
  {
    m_aPac0.setSnameRaw (new byte [64]); // maximum size
    assertEquals ("", m_aPac0.getSname ());
    m_aPac0.setSnameRaw (null);
    assertEquals ("", m_aPac0.getSname ());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetSnameRawTooLong ()
  {
    m_aPac0.setSnameRaw (new byte [65]);
  }

  // File
  @Test
  public void testSetFileRaw ()
  {
    m_aPac0.setFileRaw (new byte [128]); // maximum size
    assertEquals ("", m_aPac0.getFile ());
    m_aPac0.setFileRaw (null);
    assertEquals ("", m_aPac0.getFile ());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetFileRawTooLong ()
  {
    m_aPac0.setFileRaw (new byte [129]);
  }

  @Test
  public void testGetHardwareAddress ()
  {
    final HardwareAddress ha = new HardwareAddress (HTYPE_ETHER, "001122334455");
    assertEquals (ha, s_aRefPacketFromScratch.getHardwareAddress ());
    final DHCPPacket pac2 = s_aRefPacketFromScratch.clone ();
    pac2.setHlen ((byte) 17);
    final HardwareAddress ha2 = new HardwareAddress (HTYPE_ETHER, "00112233445566778899AABBCCDDEEFF");
    assertEquals (ha2, pac2.getHardwareAddress ());
  }

  @Test
  public void testGetOptionAsNum ()
  {
    assertEquals (Integer.valueOf (86400), s_aRefPacketFromScratch.getOptionAsNum (DHO_DHCP_LEASE_TIME));
    assertNull (s_aRefPacketFromScratch.getOptionAsNum (DHO_USER_CLASS));
    assertNull (s_aRefPacketFromScratch.getOptionAsNum (DHO_STATIC_ROUTES));
    assertEquals (Integer.valueOf (167772414), s_aRefPacketFromScratch.getOptionAsNum (DHO_ROUTERS));
  }

  @Test
  public void testEqualsTrivial ()
  {
    assertTrue (s_aRefPacketFromHex.equals (s_aRefPacketFromHex));
    assertFalse (s_aRefPacketFromHex.equals ("bla"));
    final DHCPPacket pac = s_aRefPacketFromHex.clone ();
    assertTrue (s_aRefPacketFromHex.equals (pac));
    pac.setHops ((byte) -1);
    assertFalse (s_aRefPacketFromHex.equals (pac));
  }

  @Test
  public void compareRefScratch ()
  {
    // compare packet serialized from packet built from scratch
    // compare DHCPPacket objects
    Assert.assertEquals (s_aRefPacketFromHex, s_aRefPacketFromScratch);
    // compare byte[] datagrams
    Assert.assertTrue (Arrays.equals (HexUtils.hexToBytes (REF_PACKET), s_aRefPacketFromScratch.serialize ()));
  }

  @Test
  public void testMarshall () throws UnknownHostException
  {
    // test if serialized packet has the right parameters
    final DHCPPacket packet = s_aRefPacketFromHex;

    assertEquals ("foobar", packet.getComment ());
    assertEquals (BOOTREQUEST, packet.getOp ());
    assertEquals (HTYPE_ETHER, packet.getHtype ());
    assertEquals ((byte) 6, packet.getHlen ());
    assertEquals ((byte) 0, packet.getHops ());
    assertEquals (0x11223344, packet.getXid ());
    assertEquals ((short) 0x8000, packet.getFlags ());
    assertEquals ((short) 0, packet.getSecs ());
    assertEquals (InetAddress.getByName ("10.0.0.1"), packet.getCiaddr ());
    assertTrue (Arrays.equals (InetAddress.getByName ("10.0.0.1").getAddress (), packet.getCiaddrRaw ()));
    assertEquals (InetAddress.getByName ("10.0.0.2"), packet.getYiaddr ());
    assertTrue (Arrays.equals (InetAddress.getByName ("10.0.0.2").getAddress (), packet.getYiaddrRaw ()));
    assertEquals (InetAddress.getByName ("10.0.0.3"), packet.getSiaddr ());
    assertTrue (Arrays.equals (InetAddress.getByName ("10.0.0.3").getAddress (), packet.getSiaddrRaw ()));
    assertEquals (InetAddress.getByName ("10.0.0.4"), packet.getGiaddr ());
    assertTrue (Arrays.equals (InetAddress.getByName ("10.0.0.4").getAddress (), packet.getGiaddrRaw ()));

    assertEquals ("00112233445566778899aabbccddeeff".substring (0, 2 * packet.getHlen ()), packet.getChaddrAsHex ());
    assertTrue (Arrays.equals (HexUtils.hexToBytes ("00112233445566778899AABBCCDDEEFF"), packet.getChaddr ()));

    assertEquals (STR200.substring (0, 64), packet.getSname ());
    assertEquals (STR200.substring (0, 128), packet.getFile ());
    assertTrue (Arrays.equals (new byte [256], packet.getPadding ()));
    assertTrue (packet.isDhcp ());
    assertFalse (packet.isTruncated ());

    assertEquals (DHCPDISCOVER, packet.getDHCPMessageType ().byteValue ());
    assertEquals ("12.34.56.68", packet.getOptionAsInetAddr (DHO_DHCP_SERVER_IDENTIFIER).getHostAddress ());
    assertEquals (86400, packet.getOptionAsInteger (DHO_DHCP_LEASE_TIME).intValue ());
    assertEquals ("255.255.255.0", packet.getOptionAsInetAddr (DHO_SUBNET_MASK).getHostAddress ());
    assertEquals (1, packet.getOptionAsInetAddrs (DHO_ROUTERS).length);
    assertEquals ("10.0.0.254", packet.getOptionAsInetAddrs (DHO_ROUTERS)[0].getHostAddress ());
    assertEquals (2, packet.getOptionAsInetAddrs (DHO_STATIC_ROUTES).length);
    assertEquals ("22.33.44.55", packet.getOptionAsInetAddrs (DHO_STATIC_ROUTES)[0].getHostAddress ());
    assertEquals ("10.0.0.254", packet.getOptionAsInetAddrs (DHO_STATIC_ROUTES)[1].getHostAddress ());
    assertEquals (1, packet.getOptionAsInetAddrs (DHO_WWW_SERVER).length);
    assertEquals ("10.0.0.6", packet.getOptionAsInetAddrs (DHO_WWW_SERVER)[0].getHostAddress ());
    assertArrayEquals (null, packet.getOptionAsInetAddrs (DHO_IRC_SERVER));

    assertFalse (packet.containsOption (DHO_BOOTFILE));
    assertFalse (packet.containsOption (DHO_PAD));
    assertFalse (packet.containsOption (DHO_END));
    assertTrue (packet.containsOption (DHO_WWW_SERVER));
  }

  @Test
  public void testRemoveAllOptions ()
  {
    final DHCPPacket packet = s_aRefPacketFromHex.clone ();
    assertTrue (packet.containsOption (DHO_WWW_SERVER));
    packet.removeAllOptions ();
    assertFalse (packet.containsOption (DHO_WWW_SERVER));
    assertEquals (0, packet.getOptionsArray ().length);
  }

  @Test
  public void testBootP ()
  {
    m_aPac0.setDhcp (false);
    assertEquals (REF_BOOTP_STRING, m_aPac0.toString ());

    assertEquals (hexToBytes (EMPTY_BOOTP).length, m_aPac0.serialize ().length);
    assertTrue (Arrays.equals (hexToBytes (EMPTY_BOOTP), m_aPac0.serialize ()));
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testMarshallInNonStrictMode ()
  {
    final byte [] buf = hexToBytes (REF_PACKET_WITHOUT_DHO_END);
    DHCPPacket.getPacket (buf, 0, buf.length, true);
  }

  @Test
  public void testMarshallInStrictMode ()
  {
    final byte [] buf = hexToBytes (REF_PACKET_WITHOUT_DHO_END);
    DHCPPacket.getPacket (buf, 0, buf.length, false);
  }

  // padding
  @Test
  public void testSetPaddingWithZeroes ()
  {
    m_aPac0.setPaddingWithZeroes (-1);
    assertTrue (Arrays.equals (new byte [0], m_aPac0.getPadding ()));
    m_aPac0.setPaddingWithZeroes (1500);
    assertTrue (Arrays.equals (new byte [1500], m_aPac0.getPadding ()));
    m_aPac0.setPadding (hexToBytes ("FF0011AA"));
    assertTrue (Arrays.equals (hexToBytes ("FF0011AA"), m_aPac0.getPadding ()));
    m_aPac0.setPaddingWithZeroes (4);
    assertTrue (Arrays.equals (new byte [4], m_aPac0.getPadding ()));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetPaddingWithZeroesTooBig ()
  {
    m_aPac0.setPaddingWithZeroes (1501);
  }

  //
  // option getter and setters
  @Test
  public void testOptionAsByte ()
  {
    assertNull (m_aPac0.getOptionAsByte (DHO_IP_FORWARDING));
    m_aPac0.setOptionAsByte (DHO_IP_FORWARDING, (byte) 0xf0);
    assertEquals ((byte) 0xf0, m_aPac0.getOptionAsByte (DHO_IP_FORWARDING).byteValue ());
  }

  @Test
  public void testOptionAsShort ()
  {
    assertNull (m_aPac0.getOptionAsShort (DHO_INTERFACE_MTU));
    m_aPac0.setOptionAsShort (DHO_INTERFACE_MTU, (short) 1500);
    assertEquals ((short) 1500, m_aPac0.getOptionAsShort (DHO_INTERFACE_MTU).shortValue ());
  }

  @Test
  public void testOptionAsShorts ()
  {
    // TODO missing setOptionAsShorts
    assertNull (m_aPac0.getOptionAsShorts (DHO_PATH_MTU_PLATEAU_TABLE));
    m_aPac0.setOptionRaw (DHO_PATH_MTU_PLATEAU_TABLE, hexToBytes ("FFFF00000010"));
    final short [] shorts = new short [3];
    shorts[0] = (short) -1;
    shorts[1] = (short) 0;
    shorts[2] = (short) 16;
    assertTrue (Arrays.equals (shorts, m_aPac0.getOptionAsShorts (DHO_PATH_MTU_PLATEAU_TABLE)));
  }

  @Test
  public void testOptionAsBytes ()
  {
    assertNull (m_aPac0.getOptionAsShorts (DHO_DHCP_PARAMETER_REQUEST_LIST));
    m_aPac0.setOptionRaw (DHO_DHCP_PARAMETER_REQUEST_LIST, hexToBytes ("FF0180"));
    assertTrue (Arrays.equals (hexToBytes ("FF0180"), m_aPac0.getOptionAsBytes (DHO_DHCP_PARAMETER_REQUEST_LIST)));
  }

  @Test
  public void testOptionAsInetAddress () throws Exception
  {
    assertNull (m_aPac0.getOptionAsInetAddr (DHO_SUBNET_MASK));
    m_aPac0.setOptionAsInetAddress (DHO_SUBNET_MASK, InetAddress.getByName ("10.12.14.16"));
    assertEquals (InetAddress.getByName ("10.12.14.16"), m_aPac0.getOptionAsInetAddr (DHO_SUBNET_MASK));
  }

  @Test
  public void testOptionAsInteger ()
  {
    assertNull (m_aPac0.getOptionAsInteger (DHO_DHCP_LEASE_TIME));
    m_aPac0.setOptionAsInt (DHO_DHCP_LEASE_TIME, -1);
    assertEquals (-1, m_aPac0.getOptionAsInteger (DHO_DHCP_LEASE_TIME).intValue ());
  }

  @Test
  public void testOptionAsString ()
  {
    assertNull (m_aPac0.getOptionAsString (DHO_BOOTFILE));
    m_aPac0.setOptionAsString (DHO_BOOTFILE, "foobar");
    assertEquals ("foobar", m_aPac0.getOptionAsString (DHO_BOOTFILE));
  }

  @Test
  public void testOptionRaw ()
  {
    assertNull (m_aPac0.getOptionRaw (DHO_BOOTFILE));
    m_aPac0.setOptionRaw (DHO_BOOTFILE, hexToBytes ("FF005498"));
    assertTrue (Arrays.equals (hexToBytes ("FF005498"), m_aPac0.getOptionRaw (DHO_BOOTFILE)));
    m_aPac0.setOptionRaw (DHO_BOOTFILE, null); // equivalent to removeOption
    assertNull (m_aPac0.getOptionRaw (DHO_BOOTFILE));
  }

  @Test
  public void testSetOptionNull ()
  {
    m_aPac0.setOptionRaw (DHO_BOOTFILE, hexToBytes ("FF005498"));
    assertNotNull (m_aPac0.getOptionRaw (DHO_BOOTFILE));
    m_aPac0.setOption (new DHCPOption (DHO_BOOTFILE, null));
    assertFalse (m_aPac0.containsOption (DHO_BOOTFILE));
    assertNull (m_aPac0.getOptionRaw (DHO_BOOTFILE));
  }

  @Test
  public void testSetOptions () throws Exception
  {
    final DHCPOption [] opts = new DHCPOption [4];
    opts[0] = DHCPOption.newOptionAsShort (DHO_INTERFACE_MTU, (short) 1500);
    opts[1] = DHCPOption.newOptionAsInt (DHO_DHCP_LEASE_TIME, 0x01FE02FC);
    opts[2] = null;
    opts[3] = DHCPOption.newOptionAsInetAddress (DHO_SUBNET_MASK, InetAddress.getByName ("252.10.224.3"));
    m_aPac0.setOptions (opts);
    final DHCPOption [] pacOpts = m_aPac0.getOptionsArray ();
    assertEquals (3, pacOpts.length);
    assertEquals (opts[0], pacOpts[0]);
    assertEquals (opts[1], pacOpts[1]);
    assertEquals (opts[3], pacOpts[2]);
    // verifying that null setter does not modify packet
    final DHCPPacket pac2 = m_aPac0.clone ();
    pac2.setOptions ((DHCPOption []) null);
    assertEquals (m_aPac0, pac2);
  }

  @Test
  public void testSetOptionsCollection () throws Exception
  {
    final List <DHCPOption> list = new ArrayList <> ();
    list.add (DHCPOption.newOptionAsShort (DHO_INTERFACE_MTU, (short) 1500));
    list.add (DHCPOption.newOptionAsInt (DHO_DHCP_LEASE_TIME, 0x01FE02FC));
    list.add (DHCPOption.newOptionAsInetAddress (DHO_SUBNET_MASK, InetAddress.getByName ("252.10.224.3")));
    m_aPac0.setOptions (list);
    final DHCPOption [] pacOpts = m_aPac0.getOptionsArray ();
    assertEquals (3, pacOpts.length);
    assertEquals (list.get (0), pacOpts[0]);
    assertEquals (list.get (1), pacOpts[1]);
    assertEquals (list.get (2), pacOpts[2]);
    // verifying that null setter does not modify packet
    final DHCPPacket pac2 = m_aPac0.clone ();
    pac2.setOptions ((Collection <DHCPOption>) null);
    assertEquals (m_aPac0, pac2);
  }

  // address/port
  @Test
  public void testSetAddress () throws Exception
  {
    m_aPac0.setAddress (InetAddress.getByName ("10.255.12.254"));
    assertEquals (InetAddress.getByName ("10.255.12.254"), m_aPac0.getAddress ());
    m_aPac0.setAddress (null);
    assertNull (m_aPac0.getAddress ());
  }

  @Test
  public void testAddrPort () throws Exception
  {
    m_aPac0.setAddrPort (new InetSocketAddress (InetAddress.getByName ("10.255.12.254"), 6868));
    assertEquals (InetAddress.getByName ("10.255.12.254"), m_aPac0.getAddress ());
    assertEquals (6868, m_aPac0.getPort ());
    m_aPac0.setAddress (InetAddress.getByName ("255.255.255.255"));
    m_aPac0.setPort (0);
    assertEquals (new InetSocketAddress (InetAddress.getByName ("255.255.255.255"), 0), m_aPac0.getAddrPort ());
    m_aPac0.setAddrPort (null);
    assertNull (m_aPac0.getAddress ());
    assertEquals (0, m_aPac0.getPort ());
    assertEquals (new InetSocketAddress (InetAddress.getByName ("0.0.0.0"), 0), m_aPac0.getAddrPort ());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSetAddressIPv6 () throws Exception
  {
    m_aPac0.setAddress (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testAddrPortIPv6 () throws Exception
  {
    m_aPac0.setAddrPort (new InetSocketAddress (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"), 0));
  }

  @Test
  public void testToString ()
  {
    assertEquals (REF_PACKET_TO_STRING, s_aRefPacketFromHex.toString ());
    final DHCPPacket pac = s_aRefPacketFromHex.clone ();
    pac.setOp ((byte) 129);
    pac.setHtype ((byte) -2);
    assertEquals (REF_PACKET_MOD_TO_STRING, pac.toString ());
  }

  @Test
  public void testSerialize () throws Exception
  {
    // TODO Implement serialize().
    // throw new Exception("toto");
  }

  @Test (expected = IndexOutOfBoundsException.class)
  public void testSerializeBadValues ()
  {
    testPacket (0, -1, 10); // bad values
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testSerializeTooSmall ()
  {
    testPacket (47, 0, 47); // packet too small
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testSerializeTooBig ()
  {
    testPacket (4700, 0, 4700); // packet too big
  }

  private static void testPacket (final int size, final int offset, final int length)
  {
    DHCPPacket.getPacket (new byte [size], offset, length, true);
  }

  // Hashcode
  @Test
  public void testHashCode () throws Exception
  {
    final DHCPPacket pac1 = new DHCPPacket ();
    final DHCPPacket pac2 = new DHCPPacket ();
    pac2.setYiaddr ("10.0.0.1");
    assertTrue (pac1.hashCode () != 0);
    assertTrue (pac2.hashCode () != 0);
    assertTrue (pac1.hashCode () != pac2.hashCode ());

    final DHCPPacket pac3 = pac1.clone ();
    assertTrue (pac1.equals (pac3));
    assertEquals (pac1.hashCode (), pac3.hashCode ());
  }

  private static final String REF_PACKET = "0101060011223344000080000a0000010a0000020a0000030a00000400112233" +
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
  private static final String REF_PACKET_WITHOUT_DHO_END = "0101060011223344000080000a0000010a0000020a0000030a00000400112233" +
                                                           "445566778899aabbccddeeff3132333435363738393031323334353637383930" +
                                                           "3132333435363738393031323334353637383930313233343536373839303132" +
                                                           "3334353637383930313233343132333435363738393031323334353637383930" +
                                                           "3132333435363738393031323334353637383930313233343536373839303132" +
                                                           "3334353637383930313233343536373839303132333435363738393031323334" +
                                                           "3536373839303132333435363738393031323334353637383930313233343536" +
                                                           "3738393031323334353637386382536335010136040c22384433040001518001" +
                                                           "04ffffff0003040a0000fe210816212c370a0000fe2a040a00000548040a0000" +
                                                           "0600000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000000000000000000000000000000000000000000000000000000000000000" +
                                                           "0000";
  private static final String EMPTY_BOOTP = "0201060000000000000000000000000000000000000000000000000000000000" +
                                            "0000000000000000000000000000000000000000000000000000000000000000" +
                                            "0000000000000000000000000000000000000000000000000000000000000000" +
                                            "0000000000000000000000000000000000000000000000000000000000000000" +
                                            "0000000000000000000000000000000000000000000000000000000000000000" +
                                            "0000000000000000000000000000000000000000000000000000000000000000" +
                                            "0000000000000000000000000000000000000000000000000000000000000000" +
                                            "000000000000000000000000";
  private static final String REF_PACKET_TO_STRING = "DHCP Packet\n" +
                                                     "comment=foobar\n" +
                                                     "address=10.11.12.13(6767)\n" +
                                                     "op=BOOTREQUEST(1)\n" +
                                                     "htype=HTYPE_ETHER(1)\n" +
                                                     "hlen=6\n" +
                                                     "hops=0\n" +
                                                     "xid=0x11223344\n" +
                                                     "secs=0\n" +
                                                     "flags=0xffff8000\n" +
                                                     "ciaddr=10.0.0.1\n" +
                                                     "yiaddr=10.0.0.2\n" +
                                                     "siaddr=10.0.0.3\n" +
                                                     "giaddr=10.0.0.4\n" +
                                                     "chaddr=0x001122334455\n" +
                                                     "sname=1234567890123456789012345678901234567890123456789012345678901234\n" +
                                                     "file=12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678\n" +
                                                     "Options follows:\n" +
                                                     "DHO_DHCP_MESSAGE_TYPE(53)=DHCPDISCOVER\n" +
                                                     "DHO_DHCP_SERVER_IDENTIFIER(54)=12.34.56.68\n" +
                                                     "DHO_DHCP_LEASE_TIME(51)=86400\n" +
                                                     "DHO_SUBNET_MASK(1)=255.255.255.0\n" +
                                                     "DHO_ROUTERS(3)=10.0.0.254 \n" +
                                                     "DHO_STATIC_ROUTES(33)=22.33.44.55 10.0.0.254 \n" +
                                                     "DHO_NTP_SERVERS(42)=10.0.0.5 \n" +
                                                     "DHO_WWW_SERVER(72)=10.0.0.6 \n" +
                                                     "padding[256]=00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

  private static final String REF_PACKET_MOD_TO_STRING = "DHCP Packet\n" +
                                                         "comment=foobar\n" +
                                                         "address=10.11.12.13(6767)\n" +
                                                         "op=-127\n" +
                                                         "htype=-2\n" +
                                                         "hlen=6\n" +
                                                         "hops=0\n" +
                                                         "xid=0x11223344\n" +
                                                         "secs=0\n" +
                                                         "flags=0xffff8000\n" +
                                                         "ciaddr=10.0.0.1\n" +
                                                         "yiaddr=10.0.0.2\n" +
                                                         "siaddr=10.0.0.3\n" +
                                                         "giaddr=10.0.0.4\n" +
                                                         "chaddr=0x001122334455\n" +
                                                         "sname=1234567890123456789012345678901234567890123456789012345678901234\n" +
                                                         "file=12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678\n" +
                                                         "Options follows:\n" +
                                                         "DHO_DHCP_MESSAGE_TYPE(53)=DHCPDISCOVER\n" +
                                                         "DHO_DHCP_SERVER_IDENTIFIER(54)=12.34.56.68\n" +
                                                         "DHO_DHCP_LEASE_TIME(51)=86400\n" +
                                                         "DHO_SUBNET_MASK(1)=255.255.255.0\n" +
                                                         "DHO_ROUTERS(3)=10.0.0.254 \n" +
                                                         "DHO_STATIC_ROUTES(33)=22.33.44.55 10.0.0.254 \n" +
                                                         "DHO_NTP_SERVERS(42)=10.0.0.5 \n" +
                                                         "DHO_WWW_SERVER(72)=10.0.0.6 \n" +
                                                         "padding[256]=00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

  private static final String REF_BOOTP_STRING = "BOOTP Packet\n" +
                                                 "comment=\n" +
                                                 "address=(0)\n" +
                                                 "op=BOOTREPLY(2)\n" +
                                                 "htype=HTYPE_ETHER(1)\n" +
                                                 "hlen=6\n" +
                                                 "hops=0\n" +
                                                 "xid=0x00000000\n" +
                                                 "secs=0\n" +
                                                 "flags=0x0\n" +
                                                 "ciaddr=0.0.0.0\n" +
                                                 "yiaddr=0.0.0.0\n" +
                                                 "siaddr=0.0.0.0\n" +
                                                 "giaddr=0.0.0.0\n" +
                                                 "chaddr=0x000000000000\n" +
                                                 "sname=\n" +
                                                 "file=\n" +
                                                 "padding[0]=";
  private static final String STR200 = "12345678901234567890123456789012345678901234567890" + // 50
                                       "12345678901234567890123456789012345678901234567890" + // 50
                                       "12345678901234567890123456789012345678901234567890" + // 50
                                       "12345678901234567890123456789012345678901234567890"; // 50
}
