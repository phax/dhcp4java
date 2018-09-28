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

import static org.dhcp4java.DHCPConstants.BOOTREQUEST;
import static org.dhcp4java.DHCPConstants.DHCPDISCOVER;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephan Hadinger
 */
@Ignore
public class DHCPEndToEndTest
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPEndToEndTest.class);
  static final String SERVER_ADDR = "127.0.0.1";
  private static final int SERVER_PORT = 6767;
  private static final int CLIENT_PORT = 6768;

  private static DHCPCoreServer m_aServer;
  private static DatagramSocket m_aClientSocket;

  /**
   * Start Server.
   *
   * @throws Exception
   *         on error
   */
  @BeforeClass
  public static void startServer () throws Exception
  {
    final Properties localProperties = new Properties ();
    localProperties.put (DHCPCoreServer.SERVER_ADDRESS, SERVER_ADDR + ':' + SERVER_PORT);
    localProperties.put (DHCPCoreServer.SERVER_THREADS, "1");

    m_aServer = DHCPCoreServer.initServer (new DHCPEndToEndTestServlet (), localProperties);

    s_aLogger.info ("Starting server at " + SERVER_ADDR + ':' + SERVER_PORT);
    final Thread t = new Thread (m_aServer);
    t.start ();

    m_aClientSocket = new DatagramSocket (CLIENT_PORT);
  }

  @Test (timeout = 1000)
  public void testDiscover () throws Exception
  {
    final DHCPPacket pac = new DHCPPacket ();
    pac.setOp (BOOTREQUEST);
    pac.setDHCPMessageType (DHCPDISCOVER);

    s_aLogger.info ("Sending: " + pac.getAsString ());

    final byte [] buf = pac.serialize ();
    DatagramPacket udp = new DatagramPacket (buf, buf.length);
    udp.setAddress (InetAddress.getByName (SERVER_ADDR));
    udp.setPort (SERVER_PORT);
    m_aClientSocket.send (udp);
    udp = new DatagramPacket (new byte [1500], 1500);
    m_aClientSocket.receive (udp);
    final DHCPPacket aReceived = DHCPPacket.getPacket (udp);
    s_aLogger.info ("Received: " + aReceived.getAsString ());
  }

  @AfterClass
  public static void shutdownServer ()
  {
    if (m_aClientSocket != null)
    {
      m_aClientSocket.close ();
      m_aClientSocket = null;
    }
    if (m_aServer != null)
    {
      // do some cleanup
      m_aServer.stopServer ();
      m_aServer = null;
    }
  }
}

class DHCPEndToEndTestServlet extends DHCPServlet
{
  // TODO to be completed
  @Override
  protected DHCPPacket doDiscover (final DHCPPacket request)
  {
    try
    {
      return DHCPResponseFactory.makeDHCPOffer (request,
                                                Util.int2InetAddress (0x0A000001),
                                                86400,
                                                InetAddress.getByName (DHCPEndToEndTest.SERVER_ADDR),
                                                null,
                                                null);
    }
    catch (final UnknownHostException ex)
    {
      throw new IllegalStateException (ex);
    }
  }
}
