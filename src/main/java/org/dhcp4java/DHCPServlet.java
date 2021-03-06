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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General Interface for a "DHCP Servlet"
 * <p>
 * Normal use is to override the <code>doXXX()</code> or <code>service()</code>
 * method to provide your own application logic.
 * <p>
 * For simple servers or test purpose, it as also a good idea to provide a
 * <code>main()</code> method so you can easily launch the server by running the
 * servlet.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPServlet
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPServlet.class);

  /** the server instance running this servlet */
  private DHCPCoreServer m_aServer;

  /**
   * Initialize servlet. Override this method to implement any initialization
   * you may need.
   * <p>
   * This method is called once at startup, before any request is passed to the
   * servlet. A properties is passed to the servlet to read whatever parameters
   * it needs.
   * <p>
   * There is no default behaviour.
   *
   * @param props
   *        a Properties containing parameters, as passed to
   *        <code>DHCPCoreServer</code>
   */
  public void init (final Properties props)
  {
    // read whatever parameters you need
  }

  /**
   * Low-level method for receiving a UDP Daragram and sending one back.
   * <p>
   * This methode normally does not need to be overriden and passes control to
   * <code>service()</code> for DHCP packets handling. Howerever the
   * <code>service()</code> method is not called if the DHCP request is invalid
   * (i.e. could not be parsed). So overriding this method gives you control on
   * every datagram received, not only valid DHCP packets.
   *
   * @param requestDatagram
   *        the datagram received from the client
   * @return response the datagram to send back, or <code>null</code> if no
   *         answer
   */
  public DatagramPacket serviceDatagram (final DatagramPacket requestDatagram)
  {
    if (requestDatagram == null)
      return null;

    try
    {
      // parse DHCP request
      final DHCPPacket request = DHCPPacket.getPacket (requestDatagram);
      if (request == null)
      {
        // nothing much we can do
        return null;
      }

      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug (request.getAsString ());

      // do the real work
      // call service function
      final DHCPPacket aResponse = service (request);
      // done
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("service() done");

      if (aResponse == null)
        return null;

      // check address/port
      final InetAddress aAddress = aResponse.getAddress ();
      if (aAddress == null)
      {
        s_aLogger.warn ("Address needed in response");
        return null;
      }
      final int nPort = aResponse.getPort ();

      // we have something to send back
      final byte [] aResponseBuf = aResponse.serialize ();

      if (s_aLogger.isDebugEnabled ())
      {
        s_aLogger.debug ("Buffer is " + aResponseBuf.length + " bytes long");
      }

      final DatagramPacket aResponseDatagram = new DatagramPacket (aResponseBuf, aResponseBuf.length, aAddress, nPort);
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("Sending back to" + aAddress.getHostAddress () + '(' + nPort + ')');

      postProcess (requestDatagram, aResponseDatagram);
      return aResponseDatagram;
    }
    catch (final DHCPBadPacketException e)
    {
      s_aLogger.info ("Invalid DHCP packet received", e);
    }
    catch (final Exception e)
    {
      s_aLogger.info ("Unexpected Exception", e);
    }

    // general fallback, we do nothing
    return null;
  }

  /**
   * General method for parsing a DHCP request.
   * <p>
   * Returns the DHCPPacket to send back to the client, or null if we silently
   * ignore the request.
   * <p>
   * Default behaviour: ignore BOOTP packets, and dispatch to
   * <code>doXXX()</code> methods.
   *
   * @param request
   *        DHCP request from the client
   * @return response DHCP response to send back to client, <code>null</code> if
   *         no response
   */
  protected DHCPPacket service (final DHCPPacket request)
  {
    if (request == null)
      return null;

    if (!request.isDhcp ())
    {
      s_aLogger.info ("BOOTP packet rejected");
      return null; // skipping old BOOTP
    }

    final Byte dhcpMessageType = request.getDHCPMessageType ();
    if (dhcpMessageType == null)
    {
      s_aLogger.info ("no DHCP message type");
      return null;
    }

    if (request.getOp () == BOOTREQUEST)
    {
      switch (dhcpMessageType.byteValue ())
      {
        case DHCPDISCOVER:
          return doDiscover (request);
        case DHCPREQUEST:
          return doRequest (request);
        case DHCPINFORM:
          return doInform (request);
        case DHCPDECLINE:
          return doDecline (request);
        case DHCPRELEASE:
          return doRelease (request);
        default:
          s_aLogger.info ("Unsupported message type " + dhcpMessageType);
          return null;
      }
    }
    else
      if (request.getOp () == BOOTREPLY)
      {
        // receiving a BOOTREPLY from a client is not normal
        s_aLogger.info ("BOOTREPLY received from client");
        return null;
      }
      else
      {
        s_aLogger.warn ("Unknown Op: " + request.getOp ());
        return null; // ignore
      }
  }

  /**
   * Process DISCOVER request.
   *
   * @param request
   *        DHCP request received from client
   * @return DHCP response to send back, or <code>null</code> if no response.
   */
  protected DHCPPacket doDiscover (final DHCPPacket request)
  {
    s_aLogger.info ("DISCOVER packet received");
    return null;
  }

  /**
   * Process REQUEST request.
   *
   * @param request
   *        DHCP request received from client
   * @return DHCP response to send back, or <code>null</code> if no response.
   */
  protected DHCPPacket doRequest (final DHCPPacket request)
  {
    s_aLogger.info ("REQUEST packet received");
    return null;
  }

  /**
   * Process INFORM request.
   *
   * @param request
   *        DHCP request received from client
   * @return DHCP response to send back, or <code>null</code> if no response.
   */
  protected DHCPPacket doInform (final DHCPPacket request)
  {
    s_aLogger.info ("INFORM packet received");
    return null;
  }

  /**
   * Process DECLINE request.
   *
   * @param request
   *        DHCP request received from client
   * @return DHCP response to send back, or <code>null</code> if no response.
   */
  protected DHCPPacket doDecline (final DHCPPacket request)
  {
    s_aLogger.info ("DECLINE packet received");
    return null;
  }

  /**
   * Process RELEASE request.
   *
   * @param request
   *        DHCP request received from client
   * @return DHCP response to send back, or <code>null</code> if no response.
   */
  protected DHCPPacket doRelease (final DHCPPacket request)
  {
    s_aLogger.info ("RELEASE packet received");
    return null;
  }

  /**
   * You have a chance to catch response before it is sent back to client.
   * <p>
   * This allows for example for last minute modification (who knows?) or for
   * specific logging.
   * <p>
   * Default behaviour is to do nothing.
   * <p>
   * The only way to block the response from being sent is to raise an
   * exception.
   *
   * @param requestDatagram
   *        datagram received from client
   * @param responseDatagram
   *        datagram sent back to client
   */
  protected void postProcess (final DatagramPacket requestDatagram, final DatagramPacket responseDatagram)
  {
    // default is nop
  }

  /**
   * @return Returns the server.
   */
  public final DHCPCoreServer getServer ()
  {
    return m_aServer;
  }

  /**
   * @param server
   *        The server to set.
   */
  final void setServer (final DHCPCoreServer server)
  {
    m_aServer = server;
  }
}
