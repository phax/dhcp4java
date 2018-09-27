package org.dhcp4java.test;

import static org.dhcp4java.DHCPConstants.DHO_DHCP_LEASE_TIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings ("unused")
public class DHCPOptionMirrorTest
{
  private DHCPOption m_aOpt;

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorPad ()
  {
    // 0 is reserved for padding
    new DHCPOption ((byte) 0, null, true);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorEnd ()
  {
    // 0xFF is reserved for "end of options"
    new DHCPOption ((byte) 0xFF, null, true);
  }

  @Before
  public void setupOpt ()
  {
    m_aOpt = new DHCPOption (DHO_DHCP_LEASE_TIME, null, true);
  }

  @Test
  public void testConstructor ()
  {
    assertEquals (DHO_DHCP_LEASE_TIME, m_aOpt.getCode ());
    assertNull (m_aOpt.getValue ());
  }

  @Test
  public void testGetMirrorValue ()
  {
    DHCPOption mirrorOpt;
    final DHCPPacket pac = new DHCPPacket ();
    assertEquals (m_aOpt, m_aOpt.applyOption (pac));

    pac.setOptionAsInt (DHO_DHCP_LEASE_TIME, 86400);

    mirrorOpt = m_aOpt.applyOption (pac);
    assertEquals (DHO_DHCP_LEASE_TIME, mirrorOpt.getCode ());
    assertTrue (Arrays.equals (DHCPOption.int2Bytes (86400), mirrorOpt.getValue ()));

    pac.setOptionRaw (DHO_DHCP_LEASE_TIME, new byte [0]);
    mirrorOpt = m_aOpt.applyOption (pac);
    assertEquals (DHO_DHCP_LEASE_TIME, mirrorOpt.getCode ());
    assertTrue (Arrays.equals (new byte [0], mirrorOpt.getValue ()));
  }

  @Test (expected = NullPointerException.class)
  public void testGetMirrorValueNull ()
  {
    m_aOpt.applyOption (null);
  }

  @Test
  public void testGetMirrorValueIfMirrorIsFalse ()
  {
    final DHCPOption opt2 = new DHCPOption (DHO_DHCP_LEASE_TIME, null, false);
    DHCPOption mirrorOpt;
    final DHCPPacket pac = new DHCPPacket ();
    assertEquals (opt2, opt2.applyOption (pac));
    pac.setOptionAsInt (DHO_DHCP_LEASE_TIME, 86400);

    mirrorOpt = opt2.applyOption (pac);
    assertEquals (DHO_DHCP_LEASE_TIME, mirrorOpt.getCode ());
    assertEquals (opt2, mirrorOpt); // not mirrored here since isMirror is false
  }

  @Test
  public void testToString ()
  {
    assertEquals ("DHO_DHCP_LEASE_TIME(51)=<mirror><null>", m_aOpt.toString ());
  }

}
