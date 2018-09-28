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
import static org.dhcp4java.DHCPConstants.DHCPACK;
import static org.dhcp4java.DHCPConstants.DHCPDISCOVER;
import static org.dhcp4java.DHCPConstants.DHCPINFORM;
import static org.dhcp4java.DHCPConstants.DHCPNAK;
import static org.dhcp4java.DHCPConstants.DHCPOFFER;
import static org.dhcp4java.DHCPConstants.DHCPREQUEST;
import static org.dhcp4java.DHCPConstants.DHO_DHCP_LEASE_TIME;
import static org.dhcp4java.DHCPConstants.DHO_DHCP_MESSAGE;
import static org.dhcp4java.DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER;
import static org.dhcp4java.DHCPConstants.INADDR_ANY;
import static org.dhcp4java.DHCPConstants.INADDR_BROADCAST;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * This class provides some standard factories for DHCP responses.
 * <p>
 * This simplifies DHCP Server development as basic behaviour is already usable
 * as-is.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public final class DHCPResponseFactory
{
  private DHCPResponseFactory ()
  {}

  /**
   * Create a populated DHCPOFFER response.
   * <p>
   * Reponse is populated according to the DHCP request received (must be
   * DHCPDISCOVER), the proposed client address and a set of pre-set options.
   * <p>
   * Note: <code>getDefaultSocketAddress</code> is called internally to populate
   * address and port number to which response should be sent.
   *
   * @param request
   *        request
   * @param offeredAddress
   *        offered address
   * @param leaseTime
   *        lease time
   * @param serverIdentifier
   *        Server identfier
   * @param message
   *        message
   * @param options
   *        options
   * @return the newly created OFFER Packet
   */
  public static final DHCPPacket makeDHCPOffer (final DHCPPacket request,
                                                final InetAddress offeredAddress,
                                                final int leaseTime,
                                                final InetAddress serverIdentifier,
                                                final String message,
                                                final DHCPOption [] options)
  {
    // check request
    if (request == null)
      throw new NullPointerException ("request is null");
    if (!request.isDhcp ())
      throw new DHCPBadPacketException ("request is BOOTP");

    final Byte requestMessageType = request.getDHCPMessageType ();
    if (requestMessageType == null)
      throw new DHCPBadPacketException ("request has no message type");
    if (requestMessageType.byteValue () != DHCPDISCOVER)
      throw new DHCPBadPacketException ("request is not DHCPDISCOVER");

    // check offeredAddress
    if (offeredAddress == null)
      throw new IllegalArgumentException ("offeredAddress must not be null");
    if (!(offeredAddress instanceof Inet4Address))
      throw new IllegalArgumentException ("offeredAddress must be IPv4");

    final DHCPPacket resp = new DHCPPacket ();

    resp.setOp (BOOTREPLY);
    resp.setHtype (request.getHtype ());
    resp.setHlen (request.getHlen ());
    // Hops is left to 0
    resp.setXid (request.getXid ());
    // Secs is left to 0
    resp.setFlags (request.getFlags ());
    // Ciaddr is left to 0.0.0.0
    resp.setYiaddr (offeredAddress);
    // Siaddr ?
    resp.setGiaddrRaw (request.getGiaddrRaw ());
    resp.setChaddr (request.getChaddr ());
    // sname left empty
    // file left empty

    // we set the DHCPOFFER type
    resp.setDHCPMessageType (DHCPOFFER);

    // set standard options
    resp.setOptionAsInt (DHO_DHCP_LEASE_TIME, leaseTime);
    resp.setOptionAsInetAddress (DHO_DHCP_SERVER_IDENTIFIER, serverIdentifier);
    resp.setOptionAsString (DHO_DHCP_MESSAGE, message); // if null, it is
                                                        // removed

    if (options != null)
      for (final DHCPOption opt : options)
        resp.setOption (opt.applyOption (request));

    // we set address/port according to rfc
    resp.setAddrPort (getDefaultSocketAddress (request, DHCPOFFER));

    return resp;
  }

  /**
   * Create a populated DHCPACK response.
   * <p>
   * Response is populated according to the DHCP request received (must be
   * DHCPREQUEST), the proposed client address and a set of pre-set options.
   * <p>
   * Note: <code>getDefaultSocketAddress</code> is called internally to populate
   * address and port number to which response should be sent.
   *
   * @param request
   *        request
   * @param offeredAddress
   *        offered address
   * @param leaseTime
   *        lease time
   * @param serverIdentifier
   *        server identfier
   * @param message
   *        message
   * @param options
   *        options
   * @return the newly created ACK Packet
   */
  public static final DHCPPacket makeDHCPAck (final DHCPPacket request,
                                              final InetAddress offeredAddress,
                                              final int leaseTime,
                                              final InetAddress serverIdentifier,
                                              final String message,
                                              final DHCPOption [] options)
  {
    // check request
    if (request == null)
      throw new NullPointerException ("request is null");
    if (!request.isDhcp ())
      throw new DHCPBadPacketException ("request is BOOTP");

    final Byte requestMessageType = request.getDHCPMessageType ();
    if (requestMessageType == null)
      throw new DHCPBadPacketException ("request has no message type");
    if (requestMessageType.byteValue () != DHCPREQUEST && requestMessageType.byteValue () != DHCPINFORM)
      throw new DHCPBadPacketException ("request is not DHCPREQUEST/DHCPINFORM");

    // check offered address
    if (offeredAddress == null)
      throw new IllegalArgumentException ("offeredAddress must not be null");
    if (!(offeredAddress instanceof Inet4Address))
      throw new IllegalArgumentException ("offeredAddress must be IPv4");

    final DHCPPacket resp = new DHCPPacket ();

    resp.setOp (BOOTREPLY);
    resp.setHtype (request.getHtype ());
    resp.setHlen (request.getHlen ());
    // Hops is left to 0
    resp.setXid (request.getXid ());
    // Secs is left to 0
    resp.setFlags (request.getFlags ());
    resp.setCiaddrRaw (request.getCiaddrRaw ());
    if (requestMessageType.byteValue () != DHCPINFORM)
    {
      resp.setYiaddr (offeredAddress);
    }
    // Siaddr ?
    resp.setGiaddrRaw (request.getGiaddrRaw ());
    resp.setChaddr (request.getChaddr ());
    // sname left empty
    // file left empty

    // we set the DHCPOFFER type
    resp.setDHCPMessageType (DHCPACK);

    // set standard options
    if (requestMessageType.byteValue () == DHCPREQUEST)
    { // rfc 2131
      resp.setOptionAsInt (DHO_DHCP_LEASE_TIME, leaseTime);
    }
    resp.setOptionAsInetAddress (DHO_DHCP_SERVER_IDENTIFIER, serverIdentifier);
    resp.setOptionAsString (DHO_DHCP_MESSAGE, message); // if null, it is
                                                        // removed

    if (options != null)
      for (final DHCPOption opt : options)
        resp.setOption (opt.applyOption (request));

    // we set address/port according to rfc
    resp.setAddrPort (getDefaultSocketAddress (request, DHCPACK));

    return resp;
  }

  /**
   * Create a populated DHCPNAK response.
   * <p>
   * Response is populated according to the DHCP request received (must be
   * DHCPREQUEST), the proposed client address and a set of pre-set options.
   * <p>
   * Note: <code>getDefaultSocketAddress</code> is called internally to populate
   * address and port number to which response should be sent.
   *
   * @param request
   *        request
   * @param serverIdentifier
   *        server identifier
   * @param message
   *        message
   * @return the newly created NAK Packet
   */
  public static final DHCPPacket makeDHCPNak (final DHCPPacket request,
                                              final InetAddress serverIdentifier,
                                              final String message)
  {
    // check request
    if (request == null)
      throw new NullPointerException ("request is null");
    if (!request.isDhcp ())
      throw new DHCPBadPacketException ("request is BOOTP");

    final Byte requestMessageType = request.getDHCPMessageType ();
    if (requestMessageType == null)
      throw new DHCPBadPacketException ("request has no message type");
    if (requestMessageType.byteValue () != DHCPREQUEST)
      throw new DHCPBadPacketException ("request is not DHCPREQUEST");

    final DHCPPacket resp = new DHCPPacket ();

    resp.setOp (BOOTREPLY);
    resp.setHtype (request.getHtype ());
    resp.setHlen (request.getHlen ());
    // Hops is left to 0
    resp.setXid (request.getXid ());
    // Secs is left to 0
    resp.setFlags (request.getFlags ());
    // ciaddr left to 0
    // yiaddr left to 0
    // Siaddr ?
    resp.setGiaddrRaw (request.getGiaddrRaw ());
    resp.setChaddr (request.getChaddr ());
    // sname left empty
    // file left empty

    // we set the DHCPOFFER type
    resp.setDHCPMessageType (DHCPNAK);

    // set standard options
    resp.setOptionAsInetAddress (DHO_DHCP_SERVER_IDENTIFIER, serverIdentifier);
    resp.setOptionAsString (DHO_DHCP_MESSAGE, message); // if null, it is
                                                        // removed

    // we do not set other options for this type of message

    // we set address/port according to rfc
    resp.setAddrPort (getDefaultSocketAddress (request, DHCPNAK));

    return resp;
  }

  /**
   * Calculates the addres/port to which the response must be sent, according to
   * rfc 2131, section 4.1.
   * <p>
   * This is a method ready to use for *standard* behaviour for any RFC
   * compliant DHCP Server.
   * <p>
   * If <code>giaddr</code> is null, it is the client's addres/68, otherwise
   * giaddr/67.
   * <p>
   * Standard behaviour is to set the response packet as follows:
   *
   * <pre>
   * response.setAddrPort (getDefaultSocketAddress (request), response.getOp ());
   * </pre>
   *
   * @param request
   *        the client DHCP request
   * @param responseType
   *        the DHCP Message Type the servers wants to send (DHCPOFFER, DHCPACK,
   *        DHCPNAK)
   * @return the ip/port to send back the response
   * @throws IllegalArgumentException
   *         if request is <code>null</code>.
   * @throws IllegalArgumentException
   *         if responseType is not valid.
   */
  public static InetSocketAddress getDefaultSocketAddress (final DHCPPacket request, final byte responseType)
  {
    if (request == null)
      throw new IllegalArgumentException ("request is null");

    InetSocketAddress sockAdr;
    final InetAddress giaddr = request.getGiaddr ();
    final InetAddress ciaddr = request.getCiaddr ();
    // check whether there is a giaddr

    switch (responseType)
    {
      case DHCPOFFER:
      case DHCPACK:
        if (INADDR_ANY.equals (giaddr))
        {
          if (INADDR_ANY.equals (ciaddr))
          { // broadcast to LAN
            sockAdr = new InetSocketAddress (INADDR_BROADCAST, 68);
          }
          else
          {
            sockAdr = new InetSocketAddress (ciaddr, 68);
          }
        }
        else
        { // unicast to relay
          sockAdr = new InetSocketAddress (giaddr, 67);
        }
        break;
      case DHCPNAK:
        if (INADDR_ANY.equals (giaddr))
        { // always broadcast
          sockAdr = new InetSocketAddress (INADDR_BROADCAST, 68);
        }
        else
        { // unicast to relay
          sockAdr = new InetSocketAddress (giaddr, 67);
        }
        break;
      default:
        throw new IllegalArgumentException ("responseType not valid");
    }
    return sockAdr;
  }

}
