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

import static org.dhcp4java.DHCPConstants.BOOTREPLY;
import static org.dhcp4java.DHCPConstants.BOOTREQUEST;
import static org.dhcp4java.DHCPConstants.DHCPDECLINE;
import static org.dhcp4java.DHCPConstants.DHCPDISCOVER;
import static org.dhcp4java.DHCPConstants.DHCPINFORM;
import static org.dhcp4java.DHCPConstants.DHCPRELEASE;
import static org.dhcp4java.DHCPConstants.DHCPREQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;
import java.net.InetAddress;

import org.dhcp4java.DHCPBadPacketException;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPServlet;
import org.junit.BeforeClass;
import org.junit.Test;

public class DHCPServletTest
{
  private static DHCPServletTestServlet s_aServlet;

  @BeforeClass
  public static void initServlet ()
  {
    s_aServlet = new DHCPServletTestServlet ();
    s_aServlet.init (null); // not much to test here
  }

  @Test
  public void testServiceDatagram ()
  {
    assertNull (s_aServlet.serviceDatagram (null));

    DHCPPacket pac = new DHCPPacket ();
    pac.setDhcp (false); // BOOTP
    assertNull (_servicePacket (pac)); // reject BOOTP

    pac = new DHCPPacket ();
    assertNull (_servicePacket (pac)); // reject if DHCP_MESSAGE_TYPE is empty

    pac = new DHCPPacket ();
    pac.setOp (BOOTREPLY);
    pac.setDHCPMessageType (DHCPDISCOVER);
    assertNull (_servicePacket (pac)); // reject if BOOTREPLY

    pac = new DHCPPacket ();
    pac.setOp ((byte) -1);
    pac.setDHCPMessageType (DHCPDISCOVER);
    assertNull (_servicePacket (pac)); // reject if bad Op

    assertNull (s_aServlet.getServer ());
    s_aServlet.setServer (null);
  }

  // test all messages
  @Test
  public void testDoXXX ()
  {
    _messageTypeTester (DHCPDISCOVER);
    _messageTypeTester (DHCPREQUEST);
    _messageTypeTester (DHCPINFORM);
    _messageTypeTester (DHCPDECLINE);
    _messageTypeTester (DHCPRELEASE);
  }

  @Test
  public void testInvalidMessageType ()
  {
    final DHCPPacket pac = new DHCPPacket ();
    pac.setDHCPMessageType ((byte) -2);
    pac.setOp (BOOTREQUEST);
    s_aServlet.lastMessageType = -1;
    assertNull (_servicePacket (pac));
    assertEquals ((byte) -1, s_aServlet.lastMessageType);
  }

  private static final DatagramPacket _servicePacket (final DHCPPacket pac) throws DHCPBadPacketException
  {
    final byte [] buf = pac.serialize ();
    final DatagramPacket udp = new DatagramPacket (buf, buf.length);
    return s_aServlet.serviceDatagram (udp);
  }

  private static final void _messageTypeTester (final byte messageType)
  {
    final DHCPPacket pac = new DHCPPacket ();
    pac.setDHCPMessageType (messageType);
    pac.setOp (BOOTREQUEST);
    s_aServlet.lastMessageType = -1;
    assertNull (_servicePacket (pac));
    assertEquals (messageType, s_aServlet.lastMessageType);
  }

  // test response addresses
  @Test
  public void testResponseAddresses () throws Exception
  {
    final DHCPServletTestServletWithGoodResponse servlet2 = new DHCPServletTestServletWithGoodResponse ();
    final DHCPPacket pac = new DHCPPacket ();
    pac.setDHCPMessageType (DHCPDISCOVER);
    pac.setOp (BOOTREQUEST);
    servlet2.postProcessPassed = false;
    final byte [] buf = pac.serialize ();
    final DatagramPacket udp = new DatagramPacket (buf, buf.length);

    servlet2.addressToReturn = null;
    servlet2.portToReturn = 0;
    assertNull (servlet2.serviceDatagram (udp)); // reject is address returned
                                                 // is null

    servlet2.postProcessPassed = false;
    servlet2.addressToReturn = InetAddress.getByName ("10.11.12.13");
    servlet2.portToReturn = 67;
    assertNotNull (servlet2.serviceDatagram (udp));
    assertTrue (servlet2.postProcessPassed);
  }
}

class DHCPServletTestServlet extends DHCPServlet
{
  public byte lastMessageType = -1;

  @Override
  protected DHCPPacket doDiscover (final DHCPPacket request)
  {
    lastMessageType = DHCPDISCOVER;
    return super.doDiscover (request);
  }

  @Override
  protected DHCPPacket doRequest (final DHCPPacket request)
  {
    lastMessageType = DHCPREQUEST;
    return super.doRequest (request);
  }

  @Override
  protected DHCPPacket doInform (final DHCPPacket request)
  {
    lastMessageType = DHCPINFORM;
    return super.doInform (request);
  }

  @Override
  protected DHCPPacket doDecline (final DHCPPacket request)
  {
    lastMessageType = DHCPDECLINE;
    return super.doDecline (request);
  }

  @Override
  protected DHCPPacket doRelease (final DHCPPacket request)
  {
    lastMessageType = DHCPRELEASE;
    return super.doRelease (request);
  }
}

class DHCPServletTestServletWithGoodResponse extends DHCPServlet
{

  public boolean postProcessPassed = false;
  public InetAddress addressToReturn = null;
  public int portToReturn = 0;

  @Override
  protected DHCPPacket doDiscover (final DHCPPacket request)
  {
    final DHCPPacket response = new DHCPPacket ();
    response.setAddress (addressToReturn);
    response.setPort (portToReturn);
    return response;
  }

  @Override
  protected void postProcess (final DatagramPacket requestDatagram, final DatagramPacket responseDatagram)
  {
    super.postProcess (requestDatagram, responseDatagram);
    postProcessPassed = true;
  }
}
