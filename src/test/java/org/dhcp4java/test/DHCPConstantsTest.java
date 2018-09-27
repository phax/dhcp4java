package org.dhcp4java.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.dhcp4java.DHCPConstants;
import org.junit.Test;

/**
 * Test of DHCPConstants internals.
 *
 * @author Stephan Hadinger
 */
public class DHCPConstantsTest
{
  @Test
  public void testConstants () throws UnknownHostException
  {
    assertEquals (InetAddress.getByName ("0.0.0.0"), DHCPConstants.INADDR_ANY);
    assertEquals (InetAddress.getByName ("255.255.255.255"), DHCPConstants.INADDR_BROADCAST);
  }

  @Test
  public void testGetBootNamesMap ()
  {
    final Map <Byte, String> map = DHCPConstants.getBootNamesMap ();
    assertNotNull (map);
    assertEquals ("BOOTREQUEST", map.get (Byte.valueOf (DHCPConstants.BOOTREQUEST)));
    assertEquals ("BOOTREPLY", map.get (Byte.valueOf (DHCPConstants.BOOTREPLY)));
    assertNull (map.get (Byte.valueOf ((byte) 3)));
  }

  @Test
  public void testGetHtypesMap ()
  {
    final Map <Byte, String> map = DHCPConstants.getHtypesMap ();
    assertNotNull (map);
    assertEquals ("HTYPE_ETHER", map.get (Byte.valueOf (DHCPConstants.HTYPE_ETHER)));
    assertNull (map.get (Byte.valueOf ((byte) 64)));
  }

  @Test
  public void testGetDhcpCodesMap ()
  {
    final Map <Byte, String> map = DHCPConstants.getDhcpCodesMap ();
    assertNotNull (map);
    assertEquals ("DHCPDISCOVER", map.get (Byte.valueOf (DHCPConstants.DHCPDISCOVER)));
    assertNull (map.get (Byte.valueOf ((byte) 127)));
  }

  @Test
  public void testGetDhoNamesMap ()
  {
    final Map <Byte, String> map = DHCPConstants.getDhoNamesMap ();
    assertNotNull (map);
    assertEquals ("DHO_SUBNET_MASK", map.get (Byte.valueOf (DHCPConstants.DHO_SUBNET_MASK)));
    assertEquals ("DHO_DHCP_LEASE_TIME", map.get (Byte.valueOf (DHCPConstants.DHO_DHCP_LEASE_TIME)));
    assertNull (map.get (Byte.valueOf ((byte) 145)));
  }

  @Test
  public void testGetDhoNamesReverseMap ()
  {
    final Map <String, Byte> map = DHCPConstants.getDhoNamesReverseMap ();
    assertNotNull (map);
    assertEquals (DHCPConstants.DHO_SUBNET_MASK, map.get ("DHO_SUBNET_MASK").byteValue ());
    assertEquals (DHCPConstants.DHO_DHCP_LEASE_TIME, map.get ("DHO_DHCP_LEASE_TIME").byteValue ());
    assertNull (map.get (""));
  }

  @Test
  public void testGetDhoNamesReverse ()
  {
    assertEquals (DHCPConstants.DHO_SUBNET_MASK, DHCPConstants.getDhoNamesReverse ("DHO_SUBNET_MASK").byteValue ());
    assertEquals (DHCPConstants.DHO_DHCP_LEASE_TIME,
                  DHCPConstants.getDhoNamesReverse ("DHO_DHCP_LEASE_TIME").byteValue ());
    assertNull (DHCPConstants.getDhoNamesReverse (""));
  }

  @Test (expected = NullPointerException.class)
  public void testGetDhoNamesReverseNull ()
  {
    DHCPConstants.getDhoNamesReverse (null);
  }

  @Test
  public void testGetDhoName ()
  {
    assertEquals ("DHO_SUBNET_MASK", DHCPConstants.getDhoName (DHCPConstants.DHO_SUBNET_MASK));
    assertEquals ("DHO_DHCP_LEASE_TIME", DHCPConstants.getDhoName (DHCPConstants.DHO_DHCP_LEASE_TIME));
    assertNull (DHCPConstants.getDhoName ((byte) 145));
  }
}
