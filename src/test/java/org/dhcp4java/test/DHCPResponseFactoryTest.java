/**
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
package org.dhcp4java.test;

import static org.dhcp4java.DHCPConstants.BOOTREPLY;
import static org.dhcp4java.DHCPConstants.DHCPACK;
import static org.dhcp4java.DHCPConstants.DHCPDISCOVER;
import static org.dhcp4java.DHCPConstants.DHCPINFORM;
import static org.dhcp4java.DHCPConstants.DHCPNAK;
import static org.dhcp4java.DHCPConstants.DHCPOFFER;
import static org.dhcp4java.DHCPConstants.DHCPREQUEST;
import static org.dhcp4java.DHCPConstants.DHO_DHCP_LEASE_TIME;
import static org.dhcp4java.DHCPConstants.INADDR_ANY;
import static org.dhcp4java.DHCPConstants.INADDR_BROADCAST;
import static org.dhcp4java.DHCPResponseFactory.makeDHCPAck;
import static org.dhcp4java.DHCPResponseFactory.makeDHCPOffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPResponseFactory;
import org.junit.Test;

public class DHCPResponseFactoryTest
{

  // ==============================================================
  // testing makeDHCPOffer
  // ==============================================================
  @Test (expected = IllegalArgumentException.class)
  public void testMakeDHCPOfferNull () throws Exception
  {
    makeDHCPOffer (new DHCPPacket (),
                   null, // this causes the Exception
                   86400,
                   InetAddress.getByName ("10.11.12.13"),
                   null,
                   new DHCPOption [0]);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testMakeDHCPOfferIPv6 () throws Exception
  {
    makeDHCPOffer (new DHCPPacket (),
                   InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"), // Exception
                   86400,
                   InetAddress.getByName ("10.11.12.13"),
                   null,
                   new DHCPOption [0]);
  }

  @Test
  public void testMakeDHCPOffer () throws Exception
  {
    final DHCPPacket req = new DHCPPacket ();
    req.setDHCPMessageType (DHCPDISCOVER);
    final InetAddress offeredAddress = InetAddress.getByName ("10.254.0.1");
    final DHCPOption [] opts = null;
    DHCPPacket resp;

    req.setXid (0x21345678);
    req.setFlags ((short) 0X8000);
    req.setGiaddr ("11.12.156.1");
    req.setChaddrHex ("001122334455");
    resp = makeDHCPOffer (req, offeredAddress, 86400, null, null, opts);

    assertEquals ("", resp.getComment ());
    assertEquals (BOOTREPLY, resp.getOp ());
    assertEquals ((byte) 6, resp.getHlen ());
    assertEquals ((byte) 0, resp.getHops ());
    assertEquals (0x21345678, resp.getXid ());
    assertEquals ((short) 0, resp.getSecs ());
    assertEquals ((short) 0x8000, resp.getFlags ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), resp.getCiaddr ());
    assertEquals (offeredAddress, resp.getYiaddr ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), resp.getSiaddr ());
    assertEquals (InetAddress.getByName ("11.12.156.1"), resp.getGiaddr ());
    assertEquals ("001122334455", resp.getChaddrAsHex ());
    assertEquals ("", resp.getSname ());
    assertEquals ("", resp.getFile ());
    assertEquals (DHCPOFFER, resp.getDHCPMessageType ().byteValue ());
    assertEquals (Integer.valueOf (86400), resp.getOptionAsInteger (DHO_DHCP_LEASE_TIME));
    assertEquals (2, resp.getOptionsArray ().length); // no other options
    assertEquals (InetAddress.getByName ("11.12.156.1"), resp.getAddress ());
    assertEquals (67, resp.getPort ());
  }

  @Test
  public void testMakeDHCPAck () throws Exception
  {
    final DHCPPacket req = new DHCPPacket ();
    req.setDHCPMessageType (DHCPREQUEST);
    final InetAddress offeredAddress = InetAddress.getByName ("10.254.0.1");
    final DHCPOption [] opts = null;
    DHCPPacket resp;

    req.setXid (0x21345678);
    req.setFlags ((short) 0X8000);
    req.setGiaddr ("11.12.156.1");
    req.setChaddrHex ("001122334455");
    resp = makeDHCPAck (req, offeredAddress, 86400, null, null, opts);

    assertEquals ("", resp.getComment ());
    assertEquals (BOOTREPLY, resp.getOp ());
    assertEquals ((byte) 6, resp.getHlen ());
    assertEquals ((byte) 0, resp.getHops ());
    assertEquals (0x21345678, resp.getXid ());
    assertEquals ((short) 0, resp.getSecs ());
    assertEquals ((short) 0x8000, resp.getFlags ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), resp.getCiaddr ());
    assertEquals (offeredAddress, resp.getYiaddr ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), resp.getSiaddr ());
    assertEquals (InetAddress.getByName ("11.12.156.1"), resp.getGiaddr ());
    assertEquals ("001122334455", resp.getChaddrAsHex ());
    assertEquals ("", resp.getSname ());
    assertEquals ("", resp.getFile ());
    assertEquals (DHCPACK, resp.getDHCPMessageType ().byteValue ());
    assertEquals (Integer.valueOf (86400), resp.getOptionAsInteger (DHO_DHCP_LEASE_TIME));
    assertEquals (2, resp.getOptionsArray ().length); // no other options
    assertEquals (InetAddress.getByName ("11.12.156.1"), resp.getAddress ());
    assertEquals (67, resp.getPort ());
  }

  @Test
  public void testMakeDHCPAckForInform () throws Exception
  {
    final DHCPPacket req = new DHCPPacket ();
    req.setDHCPMessageType (DHCPINFORM);
    final InetAddress offeredAddress = InetAddress.getByName ("10.254.0.1");
    final DHCPOption [] opts = null;
    DHCPPacket resp;

    req.setXid (0x21345678);
    req.setFlags ((short) 0X8000);
    req.setGiaddr ("11.12.156.1");
    req.setChaddrHex ("001122334455");
    resp = makeDHCPAck (req, offeredAddress, 86400, null, null, opts);

    assertEquals ("", resp.getComment ());
    assertEquals (BOOTREPLY, resp.getOp ());
    assertEquals ((byte) 6, resp.getHlen ());
    assertEquals ((byte) 0, resp.getHops ());
    assertEquals (0x21345678, resp.getXid ());
    assertEquals ((short) 0, resp.getSecs ());
    assertEquals ((short) 0x8000, resp.getFlags ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), resp.getCiaddr ());
    assertEquals (INADDR_ANY, resp.getYiaddr ());
    assertEquals (InetAddress.getByName ("0.0.0.0"), resp.getSiaddr ());
    assertEquals (InetAddress.getByName ("11.12.156.1"), resp.getGiaddr ());
    assertEquals ("001122334455", resp.getChaddrAsHex ());
    assertEquals ("", resp.getSname ());
    assertEquals ("", resp.getFile ());
    assertEquals (DHCPACK, resp.getDHCPMessageType ().byteValue ());
    assertEquals (null, resp.getOptionAsInteger (DHO_DHCP_LEASE_TIME));
    assertEquals (1, resp.getOptionsArray ().length); // no other options
    assertEquals (InetAddress.getByName ("11.12.156.1"), resp.getAddress ());
    assertEquals (67, resp.getPort ());
  }

  // ==============================================================
  // testing getDefaultSocketAddress
  // ==============================================================
  // test getDefaultSocketAddress
  @Test
  public void testGetDefaultSocketAddress () throws Exception
  {
    final InetAddress adr = InetAddress.getByName ("252.10.0.200");
    // RFC 2131 compliance
    // sorry we ignore the broadcast bit
    // fully broadcast by client
    getDefaultSocketAddressTester (INADDR_ANY, INADDR_ANY, DHCPOFFER, INADDR_BROADCAST, 68);
    getDefaultSocketAddressTester (INADDR_ANY, INADDR_ANY, DHCPACK, INADDR_BROADCAST, 68);
    getDefaultSocketAddressTester (INADDR_ANY, INADDR_ANY, DHCPNAK, INADDR_BROADCAST, 68);
    // unicast from client
    getDefaultSocketAddressTester (adr, INADDR_ANY, DHCPOFFER, adr, 68);
    getDefaultSocketAddressTester (adr, INADDR_ANY, DHCPACK, adr, 68);
    getDefaultSocketAddressTester (adr, INADDR_ANY, DHCPNAK, INADDR_BROADCAST, 68);
    // when though a relay
    getDefaultSocketAddressTester (INADDR_ANY, adr, DHCPOFFER, adr, 67);
    getDefaultSocketAddressTester (INADDR_ANY, adr, DHCPACK, adr, 67);
    getDefaultSocketAddressTester (INADDR_ANY, adr, DHCPNAK, adr, 67);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetDefaultSocketAddressNull ()
  {
    DHCPResponseFactory.getDefaultSocketAddress (null, DHCPOFFER);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetDefaultSocketAddressBadType () throws Exception
  {
    getDefaultSocketAddressTester (INADDR_ANY, INADDR_ANY, (byte) -10, INADDR_ANY, 68);
  }

  private static final void getDefaultSocketAddressTester (final InetAddress ciaddr,
                                                           final InetAddress giaddr,
                                                           final byte responseType,
                                                           final InetAddress expectedAddress,
                                                           final int expectedPort) throws Exception
  {
    final DHCPPacket pac = new DHCPPacket ();
    InetSocketAddress sockAdr;
    pac.setCiaddr (ciaddr);
    pac.setGiaddr (giaddr);
    sockAdr = DHCPResponseFactory.getDefaultSocketAddress (pac, responseType);
    assertNotNull (sockAdr);
    assertEquals (expectedAddress, sockAdr.getAddress ());
    assertEquals (expectedPort, sockAdr.getPort ());
  }
}
