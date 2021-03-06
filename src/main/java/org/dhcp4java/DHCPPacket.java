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
import static org.dhcp4java.DHCPConstants.DHO_DHCP_MESSAGE_TYPE;
import static org.dhcp4java.DHCPConstants.DHO_END;
import static org.dhcp4java.DHCPConstants.DHO_PAD;
import static org.dhcp4java.DHCPConstants.HTYPE_ETHER;
import static org.dhcp4java.DHCPConstants._BOOTP_ABSOLUTE_MIN_LEN;
import static org.dhcp4java.DHCPConstants._BOOTP_VEND_SIZE;
import static org.dhcp4java.DHCPConstants._BOOT_NAMES;
import static org.dhcp4java.DHCPConstants._DHCP_DEFAULT_MAX_LEN;
import static org.dhcp4java.DHCPConstants._DHCP_MAX_MTU;
import static org.dhcp4java.DHCPConstants._HTYPE_NAMES;
import static org.dhcp4java.DHCPConstants._MAGIC_COOKIE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic class for manipulating DHCP packets.
 * <p>
 * There are two basic ways to build a new DHCPPacket object.
 * <p>
 * First one is to build an object from scratch using the constructor and
 * setters. If you need to set repeatedly the same set of parameters and
 * options, you can create a "master" object and clone it many times.
 *
 * <pre>
 * DHCPPacket discover = new DHCPPacket();
 * discover.setOp(DHCPPacket.BOOTREQUEST);
 * discover.setHtype(DHCPPacket.HTYPE_ETHER);
 * discover.setHlen((byte) 6);
 * discover.setHops((byte) 0);
 * discover.setXid( (new Random()).nextInt() );
 * ...
 * </pre>
 *
 * Second is to decode a DHCP datagram received from the network. In this case,
 * the object is created through a factory.
 * <p>
 * Example: simple DHCP sniffer
 *
 * <pre>
 * DatagramSocket socket = new DatagramSocket (67);
 * while (true)
 * {
 *   DatagramPacket pac = new DatagramPacket (new byte [1500], 1500);
 *   socket.receive (pac);
 *   DHCPPacket dhcp = DHCPPacket.getPacket (pac);
 *   System.out.println (dhcp.toString ());
 * }
 * </pre>
 *
 * In this second way, beware that a <code>BadPacketExpcetion</code> is thrown
 * if the datagram contains invalid DHCP data.
 * <p>
 * <b>Getters and Setters</b>: methods are provided with high-level data
 * structures wherever it is possible (String, InetAddress...). However there
 * are also low-overhead version (suffix <code>Raw</code>) dealing directly with
 * <code>byte[]</code> for maximum performance. They are useful in servers for
 * copying parameters in a servers from a request to a response without any type
 * conversion. All parameters are copies, you may modify them as you like
 * without any side-effect on the <code>DHCPPacket</code> object.
 * <p>
 * <strong>DHCP datagram format description:</strong>
 * <table summary="DHCP datagram format description">
 * <tr>
 * <th>Field</th>
 * <th>Octets</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td valign=top><code>op</code></td>
 * <td valign=top>1</td>
 * <td>Message op code / message type.<br>
 * use constants <code>BOOTREQUEST</code>, <code>BOOTREPLY</code></td>
 * </tr>
 * <tr>
 * <td valign=top><code>htype</code></td>
 * <td valign=top>1</td>
 * <td>Hardware address type, see ARP section in "Assigned Numbers" RFC<br>
 * use constants <code>HTYPE_ETHER</code>, <code>HTYPE_IEEE802</code>,
 * <code>HTYPE_FDDI</code></td>
 * </tr>
 * <tr>
 * <td valign=top><code>hlen</code></td>
 * <td>1</td>
 * <td>Hardware address length (e.g. '6' for ethernet).</td>
 * </tr>
 * <tr>
 * <td valign=top><code>hops</code></td>
 * <td valign=top>1</td>
 * <td>Client sets to zero, optionally used by relay agents when booting via a
 * relay agent.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>xid</code></td>
 * <td valign=top>4</td>
 * <td>Transaction ID, a random number chosen by the client, used by the client
 * and server to associate messages and responses between a client and a
 * server.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>secs</code></td>
 * <td valign=top>2</td>
 * <td>Filled in by client, seconds elapsed since client began address
 * acquisition or renewal process.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>flags</code></td>
 * <td valign=top>2</td>
 * <td>Flags (see below).</td>
 * </tr>
 * <tr>
 * <td valign=top><code>ciaddr</code></td>
 * <td valign=top>4</td>
 * <td>Client IP address; only filled in if client is in BOUND, RENEW or
 * REBINDING state and can respond to ARP requests.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>yiaddr</code></td>
 * <td valign=top>4</td>
 * <td>'your' (client) IP address.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>siaddr</code></td>
 * <td valign=top>4</td>
 * <td>IP address of next server to use in bootstrap; returned in DHCPOFFER,
 * DHCPACK by server.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>giaddr</code></td>
 * <td valign=top>4</td>
 * <td>Relay agent IP address, used in booting via a relay agent.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>chaddr</code></td>
 * <td valign=top>16</td>
 * <td>Client hardware address.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>sname</code></td>
 * <td valign=top>64</td>
 * <td>Optional server host name, null terminated string.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>file</code></td>
 * <td valign=top>128</td>
 * <td>Boot file name, null terminated string; "generic" name or null in
 * DHCPDISCOVER, fully qualified directory-path name in DHCPOFFER.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>isDhcp</code></td>
 * <td valign=top>4</td>
 * <td>Controls whether the packet is BOOTP or DHCP. DHCP contains the "magic
 * cookie" of 4 bytes. 0x63 0x82 0x53 0x63.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>DHO_*code*</code></td>
 * <td valign=top>*</td>
 * <td>Optional parameters field. See the options documents for a list of
 * defined options. See below.</td>
 * </tr>
 * <tr>
 * <td valign=top><code>padding</code></td>
 * <td valign=top>*</td>
 * <td>Optional padding at the end of the packet.</td>
 * </tr>
 * </table>
 * <div><b>DHCP Option</b></div> The following options are codes are supported:
 *
 * <pre>
 * DHO_SUBNET_MASK(1)
 * DHO_TIME_OFFSET(2)
 * DHO_ROUTERS(3)
 * DHO_TIME_SERVERS(4)
 * DHO_NAME_SERVERS(5)
 * DHO_DOMAIN_NAME_SERVERS(6)
 * DHO_LOG_SERVERS(7)
 * DHO_COOKIE_SERVERS(8)
 * DHO_LPR_SERVERS(9)
 * DHO_IMPRESS_SERVERS(10)
 * DHO_RESOURCE_LOCATION_SERVERS(11)
 * DHO_HOST_NAME(12)
 * DHO_BOOT_SIZE(13)
 * DHO_MERIT_DUMP(14)
 * DHO_DOMAIN_NAME(15)
 * DHO_SWAP_SERVER(16)
 * DHO_ROOT_PATH(17)
 * DHO_EXTENSIONS_PATH(18)
 * DHO_IP_FORWARDING(19)
 * DHO_NON_LOCAL_SOURCE_ROUTING(20)
 * DHO_POLICY_FILTER(21)
 * DHO_MAX_DGRAM_REASSEMBLY(22)
 * DHO_DEFAULT_IP_TTL(23)
 * DHO_PATH_MTU_AGING_TIMEOUT(24)
 * DHO_PATH_MTU_PLATEAU_TABLE(25)
 * DHO_INTERFACE_MTU(26)
 * DHO_ALL_SUBNETS_LOCAL(27)
 * DHO_BROADCAST_ADDRESS(28)
 * DHO_PERFORM_MASK_DISCOVERY(29)
 * DHO_MASK_SUPPLIER(30)
 * DHO_ROUTER_DISCOVERY(31)
 * DHO_ROUTER_SOLICITATION_ADDRESS(32)
 * DHO_STATIC_ROUTES(33)
 * DHO_TRAILER_ENCAPSULATION(34)
 * DHO_ARP_CACHE_TIMEOUT(35)
 * DHO_IEEE802_3_ENCAPSULATION(36)
 * DHO_DEFAULT_TCP_TTL(37)
 * DHO_TCP_KEEPALIVE_INTERVAL(38)
 * DHO_TCP_KEEPALIVE_GARBAGE(39)
 * DHO_NIS_SERVERS(41)
 * DHO_NTP_SERVERS(42)
 * DHO_VENDOR_ENCAPSULATED_OPTIONS(43)
 * DHO_NETBIOS_NAME_SERVERS(44)
 * DHO_NETBIOS_DD_SERVER(45)
 * DHO_NETBIOS_NODE_TYPE(46)
 * DHO_NETBIOS_SCOPE(47)
 * DHO_FONT_SERVERS(48)
 * DHO_X_DISPLAY_MANAGER(49)
 * DHO_DHCP_REQUESTED_ADDRESS(50)
 * DHO_DHCP_LEASE_TIME(51)
 * DHO_DHCP_OPTION_OVERLOAD(52)
 * DHO_DHCP_MESSAGE_TYPE(53)
 * DHO_DHCP_SERVER_IDENTIFIER(54)
 * DHO_DHCP_PARAMETER_REQUEST_LIST(55)
 * DHO_DHCP_MESSAGE(56)
 * DHO_DHCP_MAX_MESSAGE_SIZE(57)
 * DHO_DHCP_RENEWAL_TIME(58)
 * DHO_DHCP_REBINDING_TIME(59)
 * DHO_VENDOR_CLASS_IDENTIFIER(60)
 * DHO_DHCP_CLIENT_IDENTIFIER(61)
 * DHO_NWIP_DOMAIN_NAME(62)
 * DHO_NWIP_SUBOPTIONS(63)
 * DHO_NIS_DOMAIN(64)
 * DHO_NIS_SERVER(65)
 * DHO_TFTP_SERVER(66)
 * DHO_BOOTFILE(67)
 * DHO_MOBILE_IP_HOME_AGENT(68)
 * DHO_SMTP_SERVER(69)
 * DHO_POP3_SERVER(70)
 * DHO_NNTP_SERVER(71)
 * DHO_WWW_SERVER(72)
 * DHO_FINGER_SERVER(73)
 * DHO_IRC_SERVER(74)
 * DHO_STREETTALK_SERVER(75)
 * DHO_STDA_SERVER(76)
 * DHO_USER_CLASS(77)
 * DHO_FQDN(81)
 * DHO_DHCP_AGENT_OPTIONS(82)
 * DHO_NDS_SERVERS(85)
 * DHO_NDS_TREE_NAME(86)
 * DHO_USER_AUTHENTICATION_PROTOCOL(98)
 * DHO_AUTO_CONFIGURE(116)
 * DHO_NAME_SERVICE_SEARCH(117)
 * DHO_SUBNET_SELECTION(118)
 * </pre>
 * <p>
 * These options can be set and get through basic low-level
 * <code>getOptionRaw</code> and <code>setOptionRaw</code> passing
 * <code>byte[]</code> structures. Using these functions, data formats are under
 * your responsibility. Arrays are always passed by copies (clones) so you can
 * modify them freely without side-effects. These functions allow maximum
 * performance, especially when copying options from a request datagram to a
 * response datagram.
 * <p>
 * <strong>Special case: DHO_DHCP_MESSAGE_TYPE</strong>
 * <p>
 * The DHCP Message Type (option 53) is supported for the following values
 *
 * <pre>
 * DHCPDISCOVER(1)
 * DHCPOFFER(2)
 * DHCPREQUEST(3)
 * DHCPDECLINE(4)
 * DHCPACK(5)
 * DHCPNAK(6)
 * DHCPRELEASE(7)
 * DHCPINFORM(8)
 * DHCPFORCERENEW(9)
 * DHCPLEASEQUERY(13)
 * </pre>
 * <p>
 * <strong>DHCP option formats</strong>
 * <p>
 * A limited set of higher level data-structures are supported. Type checking is
 * enforced according to rfc 2132. Check corresponding methods for a list of
 * option codes allowed for each datatype. <blockquote> <br>
 * Inet (4 bytes - IPv4 address) <br>
 * Inets (X*4 bytes - list of IPv4 addresses) <br>
 * Short (2 bytes - short) <br>
 * Shorts (X*2 bytes - list of shorts) <br>
 * Byte (1 byte) <br>
 * Bytes (X bytes - list of 1 byte parameters) <br>
 * String (X bytes - ASCII string) <br>
 * </blockquote>
 * <p>
 * <b>Note</b>: this class is not synchronized for maximum performance. However,
 * it is unlikely that the same <code>DHCPPacket</code> is used in two different
 * threads in real life DHPC servers or clients. Multi-threading acces to an
 * instance of this class is at your own risk.
 * <p>
 * <b>Limitations</b>: this class doesn't support spanned options or options
 * longer than 256 bytes. It does not support options stored in
 * <code>sname</code> or <code>file</code> fields.
 * <p>
 * This API is originally a port from my PERL <code><a href=
 *          "http://search.cpan.org/~shadinger/">Net::DHCP</a></code> api.
 * <p>
 * <b>Future extensions</b>: IPv6 support, extended data structure TODO...
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPPacket implements Cloneable, Serializable
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPPacket.class);

  // user defined comment
  // Free user-defined comment
  private String m_sComment;

  // static structure of the packet
  // Op code
  private byte m_nOp;
  // HW address Type
  private byte m_nHtype;
  // hardware address length
  private byte m_nHlen;
  // Hw options
  private byte m_nHops;
  // transaction id
  private int m_nXid;
  // elapsed time from trying to boot
  private short m_nSecs;
  // flags
  private short m_nFlags;
  // client IP
  private byte [] m_aCiaddr;
  // your client IP
  private byte [] m_aYiaddr;
  // Server IP
  private byte [] m_aSiaddr;
  // relay agent IP
  private byte [] m_aGiaddr;
  // Client HW address
  private byte [] m_aChaddr;
  // Optional server host name
  private byte [] m_aSname;
  // Boot file name
  private byte [] m_aFile;

  // ----------------------------------------------------------------------
  // options part of the packet

  // DHCP options
  // Invariant 1: K is identical to V.getCode()
  // Invariant 2: V.value is never <code>null</code>
  // Invariant 3; K is not 0 (PAD) and not -1 (END)
  private Map <Byte, DHCPOption> m_aOptions;
  // well-formed DHCP Packet ?
  private boolean m_bIsDhcp;
  // are the option truncated
  private boolean m_bTruncated;
  // ----------------------------------------------------------------------
  // extra bytes for padding
  private byte [] m_aPadding;
  // end of packet padding

  // ----------------------------------------------------------------------
  // Address/port address of the machine, which this datagram is being sent to
  // or received from.
  private InetAddress m_aAddress;
  private int m_nPort;

  /**
   * Constructor for the <code>DHCPPacket</code> class.
   * <p>
   * This creates an empty <code>DHCPPacket</code> datagram. All data is default
   * values and the packet is still lacking key data to be sent on the wire.
   */
  public DHCPPacket ()
  {
    m_sComment = "";
    m_nOp = BOOTREPLY;
    m_nHtype = HTYPE_ETHER;
    m_nHlen = 6;
    m_aCiaddr = new byte [4];
    m_aYiaddr = new byte [4];
    m_aSiaddr = new byte [4];
    m_aGiaddr = new byte [4];
    m_aChaddr = new byte [16];
    m_aSname = new byte [64];
    m_aFile = new byte [128];
    m_aPadding = new byte [0];
    m_bIsDhcp = true;
    m_aOptions = new LinkedHashMap <> ();
  }

  /**
   * Factory for creating <code>DHCPPacket</code> objects by parsing a
   * <code>DatagramPacket</code> object.
   *
   * @param datagram
   *        the UDP datagram received to be parsed
   * @return the newly create <code>DHCPPacket</code> instance
   * @throws DHCPBadPacketException
   *         the datagram is malformed and cannot be parsed properly.
   * @throws IllegalArgumentException
   *         datagram is <code>null</code>
   */
  public static DHCPPacket getPacket (final DatagramPacket datagram) throws DHCPBadPacketException
  {
    if (datagram == null)
      throw new IllegalArgumentException ("datagram is null");

    final DHCPPacket packet = new DHCPPacket ();
    // all parameters are checked in marshall()
    packet.marshall (datagram.getData (),
                     datagram.getOffset (),
                     datagram.getLength (),
                     datagram.getAddress (),
                     datagram.getPort (),
                     true); // strict mode by default
    return packet;
  }

  /**
   * Factory for creating <code>DHCPPacket</code> objects by parsing a
   * <code>byte[]</code> e.g. from a datagram.
   * <p>
   * This method allows you to specify non-strict mode which is much more
   * tolerant for packet options. By default, any problem seen during DHCP
   * option parsing causes a DHCPBadPacketException to be thrown.
   *
   * @param buf
   *        buffer for holding the incoming datagram.
   * @param offset
   *        the offset for the buffer.
   * @param length
   *        the number of bytes to read.
   * @param strict
   *        do we parse in strict mode?
   * @return the newly create <code>DHCPPacket</code> instance
   * @throws DHCPBadPacketException
   *         the datagram is malformed.
   */
  public static DHCPPacket getPacket (final byte [] buf,
                                      final int offset,
                                      final int length,
                                      final boolean strict) throws DHCPBadPacketException
  {
    final DHCPPacket packet = new DHCPPacket ();
    // all parameters are checked in marshall()
    packet.marshall (buf, offset, length, null, 0, strict);
    return packet;
  }

  /**
   * Returns a copy of this <code>DHCPPacket</code>.
   * <p>
   * The <code>truncated</code> flag is reset.
   *
   * @return a copy of the <code>DHCPPacket</code> instance.
   */
  @Override
  public DHCPPacket clone ()
  {
    try
    {
      final DHCPPacket p = (DHCPPacket) super.clone ();

      // specifically cloning arrays to avoid side-effects
      p.m_aCiaddr = m_aCiaddr.clone ();
      p.m_aYiaddr = m_aYiaddr.clone ();
      p.m_aSiaddr = m_aSiaddr.clone ();
      p.m_aGiaddr = m_aGiaddr.clone ();
      p.m_aChaddr = m_aChaddr.clone ();
      p.m_aSname = m_aSname.clone ();
      p.m_aFile = m_aFile.clone ();
      // p.options = options.clone();
      p.m_aOptions = new LinkedHashMap <> (m_aOptions);
      p.m_aPadding = m_aPadding.clone ();

      // freshly new object, it is not considered as corrupt
      p.m_bTruncated = false;

      return p;
    }
    catch (final CloneNotSupportedException e)
    {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError ();
    }
  }

  /**
   * Returns true if 2 instances of <code>DHCPPacket</code> represent the same
   * DHCP packet.
   * <p>
   * This is a field by field comparison, except <code>truncated</code> which is
   * ignored.
   */
  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;

    if (o == null || !(o instanceof DHCPPacket))
      return false;

    final DHCPPacket rhs = (DHCPPacket) o;
    // we deliberately ignore "truncated" since it is reset when cloning
    return m_sComment.equals (rhs.m_sComment) &&
           m_nOp == rhs.m_nOp &&
           m_nHtype == rhs.m_nHtype &&
           m_nHlen == rhs.m_nHlen &&
           m_nHops == rhs.m_nHops &&
           m_nXid == rhs.m_nXid &&
           m_nSecs == rhs.m_nSecs &&
           m_nFlags == rhs.m_nFlags &&
           Arrays.equals (m_aCiaddr, rhs.m_aCiaddr) &&
           Arrays.equals (m_aYiaddr, rhs.m_aYiaddr) &&
           Arrays.equals (m_aSiaddr, rhs.m_aSiaddr) &&
           Arrays.equals (m_aGiaddr, rhs.m_aGiaddr) &&
           Arrays.equals (m_aChaddr, rhs.m_aChaddr) &&
           Arrays.equals (m_aSname, rhs.m_aSname) &&
           Arrays.equals (m_aFile, rhs.m_aFile) &&
           m_aOptions.equals (rhs.m_aOptions) &&
           m_bIsDhcp == rhs.m_bIsDhcp &&
           Arrays.equals (m_aPadding, rhs.m_aPadding) &&
           _equalsStatic (m_aAddress, rhs.m_aAddress) &&
           m_nPort == rhs.m_nPort;
  }

  /**
   * Returns a hash code value for the object.
   */
  @Override
  public int hashCode ()
  {
    int h = -1;
    h ^= m_sComment.hashCode ();
    h += m_nOp;
    h += m_nHtype;
    h += m_nHlen;
    h += m_nHops;
    h += m_nXid;
    h += m_nSecs;
    h ^= m_nFlags;
    h ^= Arrays.hashCode (m_aCiaddr);
    h ^= Arrays.hashCode (m_aYiaddr);
    h ^= Arrays.hashCode (m_aSiaddr);
    h ^= Arrays.hashCode (m_aGiaddr);
    h ^= Arrays.hashCode (m_aChaddr);
    h ^= Arrays.hashCode (m_aSname);
    h ^= Arrays.hashCode (m_aFile);
    h ^= m_aOptions.hashCode ();
    h += m_bIsDhcp ? 1 : 0;
    // h += truncated ? 1 : 0;
    h ^= Arrays.hashCode (m_aPadding);
    h ^= (m_aAddress != null) ? m_aAddress.hashCode () : 0;
    h += m_nPort;
    return h;
  }

  private static boolean _equalsStatic (final Object a, final Object b)
  {
    return a == null ? b == null : a.equals (b);
  }

  /**
   * Assert all the invariants of the object. For debug purpose only.
   */
  private void _assertInvariants ()
  {
    assert (m_sComment != null);
    assert (m_aCiaddr != null);
    assert (m_aCiaddr.length == 4);
    assert (m_aYiaddr != null);
    assert (m_aYiaddr.length == 4);
    assert (m_aSiaddr != null);
    assert (m_aSiaddr.length == 4);
    assert (m_aGiaddr != null);
    assert (m_aGiaddr.length == 4);
    // strings
    assert (m_aChaddr != null);
    assert (m_aChaddr.length == 16);
    assert (m_aSname != null);
    assert (m_aSname.length == 64);
    assert (m_aFile != null);
    assert (m_aFile.length == 128);
    assert (m_aPadding != null); // length is free for padding
    // options
    assert (m_aOptions != null);
    for (final Map.Entry <Byte, DHCPOption> mapEntry : m_aOptions.entrySet ())
    {
      final Byte key = mapEntry.getKey ();
      final DHCPOption opt = mapEntry.getValue ();

      assert (key != null);
      assert (key.byteValue () != DHO_PAD);
      assert (key.byteValue () != DHO_END);
      assert (opt != null);
      assert (opt.getCode () == key.byteValue ());
      assert (opt.getValueFast () != null);
    }
  }

  /**
   * Convert a specified byte array containing a DHCP message into a DHCPMessage
   * object.
   *
   * @return a DHCPMessage object with information from byte array.
   * @param buffer
   *        byte array to convert to a DHCPMessage object
   * @param offset
   *        starting offset for the buffer
   * @param length
   *        length of the buffer
   * @param address0
   *        the address from which the packet was sent, or <code>null</code>
   * @param port0
   *        the port from which the packet was sent
   * @param strict
   *        do we read in strict mode?
   * @throws IllegalArgumentException
   *         if buffer is <code>null</code>...
   * @throws IndexOutOfBoundsException
   *         offset..offset+length is out of buffer bounds
   * @throws DHCPBadPacketException
   *         datagram is malformed
   */
  protected DHCPPacket marshall (final byte [] buffer,
                                 final int offset,
                                 final int length,
                                 final InetAddress address0,
                                 final int port0,
                                 final boolean strict)
  {
    // do some basic sanity checks
    // ibuff, offset & length are valid?
    if (buffer == null)
      throw new IllegalArgumentException ("null buffer not allowed");
    if (offset < 0)
      throw new IndexOutOfBoundsException ("negative offset not allowed");
    if (length < 0)
      throw new IllegalArgumentException ("negative length not allowed");
    if (buffer.length < offset + length)
      throw new IndexOutOfBoundsException ("offset+length exceeds buffer length");

    // absolute minimum size for a valid packet
    if (length < _BOOTP_ABSOLUTE_MIN_LEN)
    {
      throw new DHCPBadPacketException ("DHCP Packet too small (" +
                                        length +
                                        ") absolute minimum is " +
                                        _BOOTP_ABSOLUTE_MIN_LEN);
    }
    // maximum size for a valid DHCP packet
    if (length > _DHCP_MAX_MTU)
    {
      throw new DHCPBadPacketException ("DHCP Packet too big (" + length + ") max MTU is " + _DHCP_MAX_MTU);
    }

    // copy address and port
    // no need to clone, InetAddress is immutable
    m_aAddress = address0;
    m_nPort = port0;

    // turn buffer into a readable stream
    try (final ByteArrayInputStream inBStream = new ByteArrayInputStream (buffer, offset, length);
         final DataInputStream inStream = new DataInputStream (inBStream))
    {
      // parse static part of packet
      m_nOp = inStream.readByte ();
      m_nHtype = inStream.readByte ();
      m_nHlen = inStream.readByte ();
      m_nHops = inStream.readByte ();
      m_nXid = inStream.readInt ();
      m_nSecs = inStream.readShort ();
      m_nFlags = inStream.readShort ();
      inStream.readFully (m_aCiaddr, 0, 4);
      inStream.readFully (m_aYiaddr, 0, 4);
      inStream.readFully (m_aSiaddr, 0, 4);
      inStream.readFully (m_aGiaddr, 0, 4);
      inStream.readFully (m_aChaddr, 0, 16);
      inStream.readFully (m_aSname, 0, 64);
      inStream.readFully (m_aFile, 0, 128);

      // check for DHCP MAGIC_COOKIE
      m_bIsDhcp = true;
      // read ahead 4 bytes
      inBStream.mark (4);
      if (inStream.readInt () != _MAGIC_COOKIE)
      {
        m_bIsDhcp = false;
        // re-read the 4 bytes
        inBStream.reset ();
      }

      if (m_bIsDhcp)
      {
        // is it a full DHCP packet or a simple BOOTP?
        // DHCP Packet: parsing options
        int type = 0;

        while (true)
        {
          int r = inBStream.read ();
          if (r < 0)
          {
            // EOF
            break;
          }

          type = (byte) r;

          if (type == DHO_PAD)
          {
            // skip Padding
            continue;
          }
          if (type == DHO_END)
          {
            // break if end of options
            break;
          }

          r = inBStream.read ();
          if (r < 0)
          {
            // EOF
            break;
          }

          final int len = Math.min (r, inBStream.available ());
          final byte [] unit_opt = new byte [len];
          inBStream.read (unit_opt);

          // store option
          setOption (new DHCPOption ((byte) type, unit_opt));
        }
        // truncated options?
        m_bTruncated = (type != DHO_END);
        if (strict && m_bTruncated)
        {
          throw new DHCPBadPacketException ("Packet seams to be truncated");
        }
      }

      // put the remaining in padding
      m_aPadding = new byte [inBStream.available ()];
      inBStream.read (m_aPadding);
      // final verifications (if assertions are activated)
      _assertInvariants ();

      return this;
    }
    catch (final IOException e)
    {
      // unlikely with ByteArrayInputStream
      throw new DHCPBadPacketException ("IOException: " + e.toString (), e);
    }
  }

  /**
   * Converts the object to a byte array ready to be sent on the wire.
   * <p>
   * Default max size of resulting packet is 576, which is the maximum size a
   * client can accept without explicit notice (option XXX)
   *
   * @return a byte array with information from DHCPMessage object.
   * @throws DHCPBadPacketException
   *         the datagram would be malformed (too small, too big...)
   */
  public byte [] serialize ()
  {
    int minLen = _BOOTP_ABSOLUTE_MIN_LEN;

    if (m_bIsDhcp)
    {
      // most other DHCP software seems to ensure that the BOOTP 'vend'
      // field is padded to at least 64 bytes
      minLen += _BOOTP_VEND_SIZE;
    }

    return serialize (minLen, _DHCP_DEFAULT_MAX_LEN);
  }

  /**
   * Converts the object to a byte array ready to be sent on the wire.
   *
   * @param minSize
   *        the minimum buffer size in bytes
   * @param maxSize
   *        the maximum buffer size in bytes
   * @return a byte array with information from DHCPMessage object.
   * @throws DHCPBadPacketException
   *         the datagram would be malformed (too small, too big...)
   */
  public byte [] serialize (final int minSize, final int maxSize)
  {
    _assertInvariants ();
    // prepare output buffer, pre-sized to maximum buffer length
    // default buffer is half the maximum size of possible packet
    // (this seams reasonable for most uses, worst case only doubles the buffer
    // size once
    final ByteArrayOutputStream outBStream = new ByteArrayOutputStream (_DHCP_MAX_MTU / 2);
    final DataOutputStream outStream = new DataOutputStream (outBStream);
    try
    {
      outStream.writeByte (m_nOp);
      outStream.writeByte (m_nHtype);
      outStream.writeByte (m_nHlen);
      outStream.writeByte (m_nHops);
      outStream.writeInt (m_nXid);
      outStream.writeShort (m_nSecs);
      outStream.writeShort (m_nFlags);
      outStream.write (m_aCiaddr, 0, 4);
      outStream.write (m_aYiaddr, 0, 4);
      outStream.write (m_aSiaddr, 0, 4);
      outStream.write (m_aGiaddr, 0, 4);
      outStream.write (m_aChaddr, 0, 16);
      outStream.write (m_aSname, 0, 64);
      outStream.write (m_aFile, 0, 128);

      if (m_bIsDhcp)
      {
        // DHCP and not BOOTP -> magic cookie required
        outStream.writeInt (_MAGIC_COOKIE);

        // parse output options in creation order (LinkedHashMap)
        for (final DHCPOption opt : getOptionsCollection ())
        {
          assert (opt != null);
          assert (opt.getCode () != DHO_PAD);
          assert (opt.getCode () != DHO_END);
          assert (opt.getValueFast () != null);
          final int size = opt.getValueFast ().length;
          assert (size >= 0);
          if (size > 255)
          {
            throw new DHCPBadPacketException ("Options larger than 255 bytes are not yet supported");
          }
          outStream.writeByte (opt.getCode ()); // output option code
          outStream.writeByte (size); // output option length
          outStream.write (opt.getValueFast ()); // output option data
        }
        // mark end of options
        outStream.writeByte (DHO_END);
      }

      // write padding
      outStream.write (m_aPadding);

      // add padding if the packet is too small
      final int min_padding = minSize - outBStream.size ();
      if (min_padding > 0)
      {
        final byte [] add_padding = new byte [min_padding];
        outStream.write (add_padding);
      }

      // final packet is here
      final byte [] data = outBStream.toByteArray ();

      // do some post sanity checks
      if (data.length > _DHCP_MAX_MTU)
      {
        throw new DHCPBadPacketException ("serialize: packet too big (" +
                                          data.length +
                                          " greater than max MAX_MTU (" +
                                          _DHCP_MAX_MTU +
                                          ')');
      }

      return data;
    }
    catch (final IOException e)
    {
      // nomrally impossible with ByteArrayOutputStream
      s_aLogger.error ("Unexpected Exception", e);
      throw new DHCPBadPacketException ("IOException raised: " + e.toString ());
    }
  }

  // ========================================================================
  // debug functions

  /**
   * Returns a detailed string representation of the DHCP datagram.
   * <p>
   * This multi-line string details: the static, options and padding parts of
   * the object. This is useful for debugging, but not efficient.
   *
   * @return a string representation of the object.
   */
  public String getAsString ()
  {
    final StringBuilder buffer = new StringBuilder ();

    try
    {
      buffer.append (m_bIsDhcp ? "DHCP Packet" : "BOOTP Packet")
            .append ("\ncomment=")
            .append (m_sComment)
            .append ("\naddress=")
            .append (m_aAddress != null ? m_aAddress.getHostAddress () : "")
            .append ('(')
            .append (m_nPort)
            .append (')')
            .append ("\nop=");

      final String bootName = _BOOT_NAMES.get (Byte.valueOf (m_nOp));
      if (bootName != null)
      {
        buffer.append (bootName).append ('(').append (m_nOp).append (')');
      }
      else
      {
        buffer.append (m_nOp);
      }

      buffer.append ("\nhtype=");

      final String htypeName = _HTYPE_NAMES.get (Byte.valueOf (m_nHtype));
      if (htypeName != null)
      {
        buffer.append (htypeName).append ('(').append (m_nHtype).append (')');
      }
      else
      {
        buffer.append (m_nHtype);
      }

      buffer.append ("\nhlen=").append (m_nHlen).append ("\nhops=").append (m_nHops).append ("\nxid=0x");
      Util.appendHex (buffer, m_nXid);
      buffer.append ("\nsecs=")
            .append (m_nSecs)
            .append ("\nflags=0x")
            .append (Integer.toHexString (m_nFlags))
            .append ("\nciaddr=");
      Util.appendHostAddress (buffer, InetAddress.getByAddress (m_aCiaddr));
      buffer.append ("\nyiaddr=");
      Util.appendHostAddress (buffer, InetAddress.getByAddress (m_aYiaddr));
      buffer.append ("\nsiaddr=");
      Util.appendHostAddress (buffer, InetAddress.getByAddress (m_aSiaddr));
      buffer.append ("\ngiaddr=");
      Util.appendHostAddress (buffer, InetAddress.getByAddress (m_aGiaddr));
      buffer.append ("\nchaddr=0x");
      _appendChaddrAsHex (buffer);
      buffer.append ("\nsname=").append (getSname ()).append ("\nfile=").append (getFile ());

      if (m_bIsDhcp)
      {
        buffer.append ("\nOptions follows:");

        // parse options in creation order (LinkedHashMap)
        for (final DHCPOption opt : getOptionsCollection ())
        {
          buffer.append ('\n');
          opt.append (buffer);
        }
      }

      // padding
      buffer.append ("\npadding[").append (m_aPadding.length).append ("]=");
      Util.appendHex (buffer, m_aPadding);
    }
    catch (final Exception e)
    {
      // what to do ???
    }

    return buffer.toString ();
  }

  @Override
  @Deprecated
  public String toString ()
  {
    return getAsString ();
  }
  // ========================================================================
  // getters and setters

  /**
   * Returns the comment associated to this packet.
   * <p>
   * This field can be used freely and has no influence on the real network
   * datagram. It can be used to store a transaction number or any other
   * information
   *
   * @return the _comment field.
   */
  public String getComment ()
  {
    return m_sComment;
  }

  /**
   * Sets the comment associated to this packet.
   * <p>
   * This field can be used freely and has no influence on the real network
   * datagram. It can be used to store a transaction number or any other
   * information
   *
   * @param comment
   *        The comment to set.
   */
  public void setComment (final String comment)
  {
    m_sComment = comment;
  }

  /**
   * Returns the chaddr field (Client hardware address - typically MAC address).
   * <p>
   * Returns the byte[16] raw buffer. Only the first <code>hlen</code> bytes are
   * valid.
   *
   * @return the chaddr field.
   */
  public byte [] getChaddr ()
  {
    return m_aChaddr.clone ();
  }

  /**
   * Appends the chaddr field (Client hardware address - typically MAC address)
   * as a hex string to this string buffer.
   * <p>
   * Only first <code>hlen</code> bytes are appended, as uppercase hex string.
   *
   * @param buffer
   *        this string buffer
   * @return the string buffer.
   */
  private StringBuilder _appendChaddrAsHex (final StringBuilder buffer)
  {
    Util.appendHex (buffer, m_aChaddr, 0, m_nHlen & 0xFF);
    return buffer;
  }

  /**
   * Return the hardware address (@MAC) as an <code>HardwareAddress</code>
   * object.
   *
   * @return the <code>HardwareAddress</code> object
   */
  public HardwareAddress getHardwareAddress ()
  {
    int len = m_nHlen & 0xff;
    if (len > 16)
    {
      len = 16;
    }
    final byte [] buf = new byte [len];
    System.arraycopy (m_aChaddr, 0, buf, 0, len);
    return new HardwareAddress (m_nHtype, buf);
  }

  /**
   * Returns the chaddr field (Client hardware address - typically MAC address)
   * as a hex string.
   * <p>
   * Only first <code>hlen</code> bytes are printed, as uppercase hex string.
   *
   * @return the chaddr field as hex string.
   */
  public String getChaddrAsHex ()
  {
    return _appendChaddrAsHex (new StringBuilder (m_nHlen & 0xFF)).toString ();
  }

  /**
   * Sets the chaddr field (Client hardware address - typically MAC address).
   * <p>
   * The buffer length should be between 0 and 16, otherwise an
   * <code>IllegalArgumentException</code> is thrown.
   * <p>
   * If chaddr is null, the field is filled with zeros.
   *
   * @param chaddr
   *        The chaddr to set.
   * @throws IllegalArgumentException
   *         chaddr buffer is longer than 16 bytes.
   */
  public void setChaddr (final byte [] chaddr)
  {
    if (chaddr != null)
    {
      if (chaddr.length > m_aChaddr.length)
      {
        throw new IllegalArgumentException ("chaddr is too long: " + chaddr.length + ", max is: " + m_aChaddr.length);
      }
      Arrays.fill (m_aChaddr, (byte) 0);
      System.arraycopy (chaddr, 0, m_aChaddr, 0, chaddr.length);
    }
    else
    {
      Arrays.fill (m_aChaddr, (byte) 0);
    }
  }

  /**
   * Sets the chaddr field - from an hex String.
   *
   * @param hex
   *        the chaddr in hex format
   */
  public void setChaddrHex (final String hex)
  {
    setChaddr (Util.hex2Bytes (hex));
  }

  /**
   * Returns the ciaddr field (Client IP Address).
   *
   * @return the ciaddr field converted to <code>InetAddress</code> object.
   */
  public InetAddress getCiaddr ()
  {
    try
    {
      return InetAddress.getByAddress (getCiaddrRaw ());
    }
    catch (final UnknownHostException e)
    {
      s_aLogger.error ("Unexpected UnknownHostException", e);
      return null; // normaly impossible
    }
  }

  /**
   * Returns the ciaddr field (Client IP Address).
   * <p>
   * This is the low-level maximum performance getter for this field.
   *
   * @return Returns the ciaddr as raw byte[4].
   */
  public byte [] getCiaddrRaw ()
  {
    return m_aCiaddr.clone ();
  }

  /**
   * Sets the ciaddr field (Client IP Address).
   * <p>
   * Ths <code>ciaddr</code> field must be of <code>Inet4Address</code> class or
   * an <code>IllegalArgumentException</code> is thrown.
   *
   * @param ciaddr
   *        The ciaddr to set.
   */
  public void setCiaddr (final InetAddress ciaddr)
  {
    if (!(ciaddr instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Inet4Address required");
    }
    setCiaddrRaw (ciaddr.getAddress ());
  }

  /**
   * Sets the ciaddr field (Client IP Address).
   *
   * @param ciaddr
   *        The ciaddr to set.
   * @throws UnknownHostException
   *         on error
   */
  public void setCiaddr (final String ciaddr) throws UnknownHostException
  {
    setCiaddr (InetAddress.getByName (ciaddr));
  }

  /**
   * Sets the ciaddr field (Client IP Address).
   * <p>
   * <code>ciaddr</code> must be a 4 bytes array, or an
   * <code>IllegalArgumentException</code> is thrown.
   * <p>
   * This is the low-level maximum performance setter for this field. The array
   * is internally copied so any further modification to <code>ciaddr</code>
   * parameter has no side effect.
   *
   * @param ciaddr
   *        The ciaddr to set.
   */
  public void setCiaddrRaw (final byte [] ciaddr)
  {
    if (ciaddr.length != 4)
    {
      throw new IllegalArgumentException ("4-byte array required");
    }
    System.arraycopy (ciaddr, 0, m_aCiaddr, 0, 4);
  }

  /**
   * Returns the file field (Boot File Name).
   * <p>
   * Returns the raw byte[128] buffer, containing a null terminated string.
   * <p>
   * This is the low-level maximum performance getter for this field.
   *
   * @return the file field.
   */
  public byte [] getFileRaw ()
  {
    return m_aFile.clone ();
  }

  /**
   * Returns the file field (Boot File Name) as String.
   *
   * @return the file converted to a String (transparent encoding).
   */
  public String getFile ()
  {
    return Util.bytesToString (getFileRaw ());
  }

  /**
   * Sets the file field (Boot File Name) as String.
   * <p>
   * The string is first converted to a byte[] array using transparent encoding.
   * If the resulting buffer size is &gt; 128, an
   * <code>IllegalArgumentException</code> is thrown.
   * <p>
   * If <code>file</code> parameter is null, the buffer is filled with zeros.
   *
   * @param file
   *        The file field to set.
   * @throws IllegalArgumentException
   *         string too long
   */
  public void setFile (final String file)
  {
    setFileRaw (Util.stringToBytes (file));
  }

  /**
   * Sets the file field (Boot File Name) as String.
   * <p>
   * If the buffer size is &gt; 128, an <code>IllegalArgumentException</code> is
   * thrown.
   * <p>
   * If <code>file</code> parameter is null, the buffer is filled with zeros.
   * <p>
   * This is the low-level maximum performance setter for this field.
   *
   * @param file
   *        The file field to set.
   * @throws IllegalArgumentException
   *         string too long
   */
  public void setFileRaw (final byte [] file)
  {
    if (file != null)
    {
      if (file.length > m_aFile.length)
      {
        throw new IllegalArgumentException ("File is too long:" + file.length + " max is:" + m_aFile.length);
      }
      Arrays.fill (m_aFile, (byte) 0);
      System.arraycopy (file, 0, m_aFile, 0, file.length);
    }
    else
    {
      Arrays.fill (m_aFile, (byte) 0);
    }
  }

  /**
   * Returns the flags field.
   *
   * @return the flags field.
   */
  public short getFlags ()
  {
    return m_nFlags;
  }

  /**
   * Sets the flags field.
   *
   * @param flags
   *        The flags field to set.
   */
  public void setFlags (final short flags)
  {
    m_nFlags = flags;
  }

  /**
   * Returns the giaddr field (Relay agent IP address).
   *
   * @return the giaddr field converted to <code>InetAddress</code> object.
   */
  public InetAddress getGiaddr ()
  {
    try
    {
      return InetAddress.getByAddress (getGiaddrRaw ());
    }
    catch (final UnknownHostException e)
    {
      s_aLogger.error ("Unexpected UnknownHostException", e);
      return null; // normaly impossible
    }
  }

  /**
   * Returns the giaddr field (Relay agent IP address).
   * <p>
   * This is the low-level maximum performance getter for this field.
   *
   * @return Returns the giaddr as raw byte[4].
   */
  public byte [] getGiaddrRaw ()
  {
    return m_aGiaddr.clone ();
  }

  /**
   * Sets the giaddr field (Relay agent IP address).
   * <p>
   * Ths <code>giaddr</code> field must be of <code>Inet4Address</code> class or
   * an <code>IllegalArgumentException</code> is thrown.
   *
   * @param giaddr
   *        The giaddr to set.
   */
  public void setGiaddr (final InetAddress giaddr)
  {
    if (!(giaddr instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Inet4Address required");
    }
    setGiaddrRaw (giaddr.getAddress ());
  }

  /**
   * Sets the giaddr field (Relay agent IP address).
   *
   * @param giaddr
   *        The giaddr to set.
   * @throws UnknownHostException
   *         on error
   */
  public void setGiaddr (final String giaddr) throws UnknownHostException
  {
    setGiaddr (InetAddress.getByName (giaddr));
  }

  /**
   * Sets the giaddr field (Relay agent IP address).
   * <p>
   * <code>giaddr</code> must be a 4 bytes array, or an
   * <code>IllegalArgumentException</code> is thrown.
   * <p>
   * This is the low-level maximum performance setter for this field. The array
   * is internally copied so any further modification to <code>ciaddr</code>
   * parameter has no side effect.
   *
   * @param giaddr
   *        The giaddr to set.
   */
  public void setGiaddrRaw (final byte [] giaddr)
  {
    if (giaddr.length != 4)
    {
      throw new IllegalArgumentException ("4-byte array required");
    }
    System.arraycopy (giaddr, 0, m_aGiaddr, 0, 4);
  }

  /**
   * Returns the hlen field (Hardware address length).
   * <p>
   * Typical value is 6 for ethernet - 6 bytes MAC address.
   *
   * @return the hlen field.
   */
  public byte getHlen ()
  {
    return m_nHlen;
  }

  /**
   * Sets the hlen field (Hardware address length).
   * <p>
   * Typical value is 6 for ethernet - 6 bytes MAC address.
   * <p>
   * hlen value should be between 0 and 16, but no control is done here.
   *
   * @param hlen
   *        The hlen to set.
   */
  public void setHlen (final byte hlen)
  {
    m_nHlen = hlen;
  }

  /**
   * Returns the hops field.
   *
   * @return the hops field.
   */
  public byte getHops ()
  {
    return m_nHops;
  }

  /**
   * Sets the hops field.
   *
   * @param hops
   *        The hops to set.
   */
  public void setHops (final byte hops)
  {
    m_nHops = hops;
  }

  /**
   * Returns the htype field (Hardware address length).
   * <p>
   * Predefined values are:
   *
   * <pre>
   * HTYPE_ETHER (1)
   * HTYPE_IEEE802 (6)
   * HTYPE_FDDI (8)
   * </pre>
   * <p>
   * Typical value is <code>HTYPE_ETHER</code>.
   *
   * @return the htype field.
   */
  public byte getHtype ()
  {
    return m_nHtype;
  }

  /**
   * Sets the htype field (Hardware address length).
   * <p>
   * Predefined values are:
   *
   * <pre>
   * HTYPE_ETHER (1)
   * HTYPE_IEEE802 (6)
   * HTYPE_FDDI (8)
   * </pre>
   * <p>
   * Typical value is <code>HTYPE_ETHER</code>.
   *
   * @param htype
   *        The htype to set.
   */
  public void setHtype (final byte htype)
  {
    m_nHtype = htype;
  }

  /**
   * Returns whether the packet is DHCP or BOOTP.
   * <p>
   * It indicates the presence of the DHCP Magic Cookie at the end of the BOOTP
   * portion.
   * <p>
   * Default is <code>true</code> for a brand-new object.
   *
   * @return Returns the isDhcp.
   */
  public boolean isDhcp ()
  {
    return m_bIsDhcp;
  }

  /**
   * Sets the isDhcp flag.
   * <p>
   * Indicates whether to generate a DHCP or a BOOTP packet. If
   * <code>true</code> the DHCP Magic Cookie is added after the BOOTP portion
   * and before the DHCP Options.
   * <p>
   * If <code>isDhcp</code> if false, all DHCP options are ignored when calling
   * <code>serialize()</code>.
   * <p>
   * Default value is <code>true</code>.
   *
   * @param isDhcp
   *        The isDhcp to set.
   */
  public void setDhcp (final boolean isDhcp)
  {
    m_bIsDhcp = isDhcp;
  }

  /**
   * Returns the op field (Message op code).
   * <p>
   * Predefined values are:
   *
   * <pre>
   * BOOTREQUEST (1)
   * BOOTREPLY (2)
   * </pre>
   *
   * @return the op field.
   */
  public byte getOp ()
  {
    return m_nOp;
  }

  /**
   * Sets the op field (Message op code).
   * <p>
   * Predefined values are:
   *
   * <pre>
   * BOOTREQUEST (1)
   * BOOTREPLY (2)
   * </pre>
   * <p>
   * Default value is <code>BOOTREPLY</code>, suitable for server replies.
   *
   * @param op
   *        The op to set.
   */
  public void setOp (final byte op)
  {
    m_nOp = op;
  }

  /**
   * Returns the padding portion of the packet.
   * <p>
   * This byte array follows the DHCP Options. Normally, its content is
   * irrelevant.
   *
   * @return Returns the padding.
   */
  public byte [] getPadding ()
  {
    return m_aPadding.clone ();
  }

  /**
   * Sets the padding buffer.
   * <p>
   * This byte array follows the DHCP Options. Normally, its content is
   * irrelevant.
   * <p>
   * If <code>paddig</code> is null, it is set to an empty buffer.
   * <p>
   * Padding is automatically added at the end of the datagram when calling
   * <code>serialize()</code> to match DHCP minimal packet size.
   *
   * @param padding
   *        The padding to set.
   */
  public void setPadding (final byte [] padding)
  {
    m_aPadding = ((padding == null) ? new byte [0] : padding.clone ());
  }

  /**
   * Sets the padding buffer with <code>length</code> zero bytes.
   * <p>
   * This is a short cut for <code>setPadding(new byte[length])</code>.
   *
   * @param nLength
   *        size of the padding buffer
   */
  public void setPaddingWithZeroes (final int nLength)
  {
    int length = nLength;
    if (length < 0)
    {
      length = 0;
    }
    if (length > _DHCP_MAX_MTU)
    {
      throw new IllegalArgumentException ("length is > " + _DHCP_MAX_MTU);
    }
    setPadding (new byte [length]);
  }

  /**
   * Returns the secs field (seconds elapsed).
   *
   * @return the secs field.
   */
  public short getSecs ()
  {
    return m_nSecs;
  }

  /**
   * Sets the secs field (seconds elapsed).
   *
   * @param secs
   *        The secs to set.
   */
  public void setSecs (final short secs)
  {
    m_nSecs = secs;
  }

  /**
   * Returns the siaddr field (IP address of next server).
   *
   * @return the siaddr field converted to <code>InetAddress</code> object.
   */
  public InetAddress getSiaddr ()
  {
    try
    {
      return InetAddress.getByAddress (getSiaddrRaw ());
    }
    catch (final UnknownHostException e)
    {
      s_aLogger.error ("Unexpected UnknownHostException", e);
      return null; // normaly impossible
    }
  }

  /**
   * Returns the siaddr field (IP address of next server).
   * <p>
   * This is the low-level maximum performance getter for this field.
   *
   * @return Returns the siaddr as raw byte[4].
   */
  public byte [] getSiaddrRaw ()
  {
    return m_aSiaddr.clone ();
  }

  /**
   * Sets the siaddr field (IP address of next server).
   * <p>
   * Ths <code>siaddr</code> field must be of <code>Inet4Address</code> class or
   * an <code>IllegalArgumentException</code> is thrown.
   *
   * @param siaddr
   *        The siaddr to set.
   */
  public void setSiaddr (final InetAddress siaddr)
  {
    if (!(siaddr instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Inet4Address required");
    }
    setSiaddrRaw (siaddr.getAddress ());
  }

  /**
   * Sets the siaddr field (IP address of next server).
   *
   * @param siaddr
   *        The siaddr to set.
   * @throws UnknownHostException
   *         on error
   */
  public void setSiaddr (final String siaddr) throws UnknownHostException
  {
    setSiaddr (InetAddress.getByName (siaddr));
  }

  /**
   * Sets the siaddr field (IP address of next server).
   * <p>
   * <code>siaddr</code> must be a 4 bytes array, or an
   * <code>IllegalArgumentException</code> is thrown.
   * <p>
   * This is the low-level maximum performance setter for this field. The array
   * is internally copied so any further modification to <code>ciaddr</code>
   * parameter has no side effect.
   *
   * @param siaddr
   *        The siaddr to set.
   */
  public void setSiaddrRaw (final byte [] siaddr)
  {
    if (siaddr.length != 4)
    {
      throw new IllegalArgumentException ("4-byte array required");
    }
    System.arraycopy (siaddr, 0, m_aSiaddr, 0, 4);
  }

  /**
   * Returns the sname field (Optional server host name).
   * <p>
   * Returns the raw byte[64] buffer, containing a null terminated string.
   * <p>
   * This is the low-level maximum performance getter for this field.
   *
   * @return the sname field.
   */
  public byte [] getSnameRaw ()
  {
    return m_aSname.clone ();
  }

  /**
   * Returns the sname field (Optional server host name) as String.
   *
   * @return the sname converted to a String (transparent encoding).
   */
  public String getSname ()
  {
    return Util.bytesToString (getSnameRaw ());
  }

  /**
   * Sets the sname field (Optional server host name) as String.
   * <p>
   * The string is first converted to a byte[] array using transparent encoding.
   * If the resulting buffer size is &gt; 64, an
   * <code>IllegalArgumentException</code> is thrown.
   * <p>
   * If <code>sname</code> parameter is null, the buffer is filled with zeros.
   *
   * @param sname
   *        The sname field to set.
   * @throws IllegalArgumentException
   *         string too long
   */
  public void setSname (final String sname)
  {
    setSnameRaw (Util.stringToBytes (sname));
  }

  /**
   * Sets the sname field (Optional server host name) as String.
   * <p>
   * If the buffer size is &gt; 64, an <code>IllegalArgumentException</code> is
   * thrown.
   * <p>
   * If <code>sname</code> parameter is null, the buffer is filled with zeros.
   * <p>
   * This is the low-level maximum performance setter for this field.
   *
   * @param sname
   *        The sname field to set.
   * @throws IllegalArgumentException
   *         string too long
   */
  public void setSnameRaw (final byte [] sname)
  {
    if (sname != null)
    {
      if (sname.length > m_aSname.length)
      {
        throw new IllegalArgumentException ("Sname is too long:" + sname.length + " max is:" + m_aSname.length);
      }
      Arrays.fill (m_aSname, (byte) 0);
      System.arraycopy (sname, 0, m_aSname, 0, sname.length);
    }
    else
    {
      Arrays.fill (m_aSname, (byte) 0);
    }
  }

  /**
   * Returns the xid field (Transaction ID).
   *
   * @return Returns the xid.
   */
  public int getXid ()
  {
    return m_nXid;
  }

  /**
   * Sets the xid field (Transaction ID).
   * <p>
   * This field is random generated by the client, and used by the client and
   * server to associate requests and responses for the same transaction.
   *
   * @param xid
   *        The xid to set.
   */
  public void setXid (final int xid)
  {
    m_nXid = xid;
  }

  /**
   * Returns the yiaddr field ('your' IP address).
   *
   * @return the yiaddr field converted to <code>InetAddress</code> object.
   */
  public InetAddress getYiaddr ()
  {
    try
    {
      return InetAddress.getByAddress (getYiaddrRaw ());
    }
    catch (final UnknownHostException e)
    {
      s_aLogger.error ("Unexpected UnknownHostException", e);
      return null; // normaly impossible
    }
  }

  /**
   * Returns the yiaddr field ('your' IP address).
   * <p>
   * This is the low-level maximum performance getter for this field.
   *
   * @return Returns the yiaddr as raw byte[4].
   */
  public byte [] getYiaddrRaw ()
  {
    return m_aYiaddr.clone ();
  }

  /**
   * Sets the yiaddr field ('your' IP address).
   * <p>
   * Ths <code>yiaddr</code> field must be of <code>Inet4Address</code> class or
   * an <code>IllegalArgumentException</code> is thrown.
   *
   * @param yiaddr
   *        The yiaddr to set.
   */
  public void setYiaddr (final InetAddress yiaddr)
  {
    if (!(yiaddr instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Inet4Address required");
    }
    setYiaddrRaw (yiaddr.getAddress ());
  }

  /**
   * Sets the yiaddr field ('your' IP address).
   *
   * @param yiaddr
   *        The yiaddr to set.
   * @throws UnknownHostException
   *         on error
   */
  public void setYiaddr (final String yiaddr) throws UnknownHostException
  {
    setYiaddr (InetAddress.getByName (yiaddr));
  }

  /**
   * Sets the yiaddr field ('your' IP address).
   * <p>
   * <code>yiaddr</code> must be a 4 bytes array, or an
   * <code>IllegalArgumentException</code> is thrown.
   * <p>
   * This is the low-level maximum performance setter for this field. The array
   * is internally copied so any further modification to <code>ciaddr</code>
   * parameter has no side effect.
   *
   * @param yiaddr
   *        The yiaddr to set.
   */
  public void setYiaddrRaw (final byte [] yiaddr)
  {
    if (yiaddr.length != 4)
    {
      throw new IllegalArgumentException ("4-byte array required");
    }
    System.arraycopy (yiaddr, 0, m_aYiaddr, 0, 4);
  }

  /**
   * Return the DHCP Option Type.
   * <p>
   * This is a short-cut for
   * <code>getOptionAsByte(DHO_DHCP_MESSAGE_TYPE)</code>.
   *
   * @return option type, of <code>null</code> if not present.
   */
  public Byte getDHCPMessageType ()
  {
    return getOptionAsByte (DHO_DHCP_MESSAGE_TYPE);
  }

  /**
   * Sets the DHCP Option Type.
   * <p>
   * This is a short-cur for
   * <code>setOptionAsByte(DHO_DHCP_MESSAGE_TYPE, optionType);</code>.
   *
   * @param optionType
   *        option tyoe
   */
  public void setDHCPMessageType (final byte optionType)
  {
    setOptionAsByte (DHO_DHCP_MESSAGE_TYPE, optionType);
  }

  /**
   * Indicates that the DHCP packet has been truncated and did not finished with
   * a 0xFF option. This parameter is set only when parsing packets in
   * non-strict mode (which is not the default behaviour).
   * <p>
   * This field is read-only and can be <code>true</code> only with objects
   * created by parsing a Datagram - getPacket() methods.
   * <p>
   * This field is cleared if the object is cloned.
   *
   * @return the truncated field.
   */
  public boolean isTruncated ()
  {
    return m_bTruncated;
  }

  /**
   * Wrapper function for getValueAsNum() in DHCPOption. Returns a numerical
   * option: int, short or byte.
   *
   * @param code
   *        DHCP option code
   * @return Integer object or <code>null</code>
   */
  public Integer getOptionAsNum (final byte code)
  {
    final DHCPOption opt = getOption (code);
    return (opt != null) ? opt.getValueAsNum () : null;
  }

  /**
   * Returns a DHCP Option as Byte format. This method is only allowed for the
   * following option codes:
   *
   * <pre>
   * DHO_IP_FORWARDING(19)
   * DHO_NON_LOCAL_SOURCE_ROUTING(20)
   * DHO_DEFAULT_IP_TTL(23)
   * DHO_ALL_SUBNETS_LOCAL(27)
   * DHO_PERFORM_MASK_DISCOVERY(29)
   * DHO_MASK_SUPPLIER(30)
   * DHO_ROUTER_DISCOVERY(31)
   * DHO_TRAILER_ENCAPSULATION(34)
   * DHO_IEEE802_3_ENCAPSULATION(36)
   * DHO_DEFAULT_TCP_TTL(37)
   * DHO_TCP_KEEPALIVE_GARBAGE(39)
   * DHO_NETBIOS_NODE_TYPE(46)
   * DHO_DHCP_OPTION_OVERLOAD(52)
   * DHO_DHCP_MESSAGE_TYPE(53)
   * DHO_AUTO_CONFIGURE(116)
   * </pre>
   *
   * @param code
   *        the option code.
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public Byte getOptionAsByte (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return opt == null ? null : Byte.valueOf (opt.getValueAsByte ());
  }

  /**
   * Returns a DHCP Option as Short format.
   * <p>
   * This method is only allowed for the following option codes:
   *
   * <pre>
   * DHO_BOOT_SIZE(13)
   * DHO_MAX_DGRAM_REASSEMBLY(22)
   * DHO_INTERFACE_MTU(26)
   * DHO_DHCP_MAX_MESSAGE_SIZE(57)
   * </pre>
   *
   * @param code
   *        the option code.
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public Short getOptionAsShort (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return opt == null ? null : Short.valueOf (opt.getValueAsShort ());
  }

  /**
   * Returns a DHCP Option as Integer format.
   * <p>
   * This method is only allowed for the following option codes:
   *
   * <pre>
   * DHO_TIME_OFFSET(2)
   * DHO_PATH_MTU_AGING_TIMEOUT(24)
   * DHO_ARP_CACHE_TIMEOUT(35)
   * DHO_TCP_KEEPALIVE_INTERVAL(38)
   * DHO_DHCP_LEASE_TIME(51)
   * DHO_DHCP_RENEWAL_TIME(58)
   * DHO_DHCP_REBINDING_TIME(59)
   * </pre>
   *
   * @param code
   *        the option code.
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public Integer getOptionAsInteger (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return opt == null ? null : Integer.valueOf (opt.getValueAsInt ());
  }

  /**
   * Returns a DHCP Option as InetAddress format.
   * <p>
   * This method is only allowed for the following option codes:
   *
   * <pre>
   * DHO_SUBNET_MASK(1)
   * DHO_SWAP_SERVER(16)
   * DHO_BROADCAST_ADDRESS(28)
   * DHO_ROUTER_SOLICITATION_ADDRESS(32)
   * DHO_DHCP_REQUESTED_ADDRESS(50)
   * DHO_DHCP_SERVER_IDENTIFIER(54)
   * DHO_SUBNET_SELECTION(118)
   * </pre>
   *
   * @param code
   *        the option code.
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public InetAddress getOptionAsInetAddr (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return (opt == null) ? null : opt.getValueAsInetAddr ();
  }

  /**
   * Returns a DHCP Option as String format.
   * <p>
   * This method is only allowed for the following option codes:
   *
   * <pre>
   * DHO_HOST_NAME(12)
   * DHO_MERIT_DUMP(14)
   * DHO_DOMAIN_NAME(15)
   * DHO_ROOT_PATH(17)
   * DHO_EXTENSIONS_PATH(18)
   * DHO_NETBIOS_SCOPE(47)
   * DHO_DHCP_MESSAGE(56)
   * DHO_VENDOR_CLASS_IDENTIFIER(60)
   * DHO_NWIP_DOMAIN_NAME(62)
   * DHO_NIS_DOMAIN(64)
   * DHO_NIS_SERVER(65)
   * DHO_TFTP_SERVER(66)
   * DHO_BOOTFILE(67)
   * DHO_NDS_TREE_NAME(86)
   * DHO_USER_AUTHENTICATION_PROTOCOL(98)
   * </pre>
   *
   * @param code
   *        the option code.
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public String getOptionAsString (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return (opt == null) ? null : opt.getValueAsString ();
  }

  /**
   * Returns a DHCP Option as Short array format.
   * <p>
   * This method is only allowed for the following option codes:
   *
   * <pre>
   * DHO_PATH_MTU_PLATEAU_TABLE(25)
   * DHO_NAME_SERVICE_SEARCH(117)
   * </pre>
   *
   * @param code
   *        the option code.
   * @return the option value array, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public short [] getOptionAsShorts (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return (opt == null) ? null : opt.getValueAsShorts ();
  }

  /**
   * Returns a DHCP Option as InetAddress array format.
   * <p>
   * This method is only allowed for the following option codes:
   *
   * <pre>
   * DHO_ROUTERS(3)
   * DHO_TIME_SERVERS(4)
   * DHO_NAME_SERVERS(5)
   * DHO_DOMAIN_NAME_SERVERS(6)
   * DHO_LOG_SERVERS(7)
   * DHO_COOKIE_SERVERS(8)
   * DHO_LPR_SERVERS(9)
   * DHO_IMPRESS_SERVERS(10)
   * DHO_RESOURCE_LOCATION_SERVERS(11)
   * DHO_POLICY_FILTER(21)
   * DHO_STATIC_ROUTES(33)
   * DHO_NIS_SERVERS(41)
   * DHO_NTP_SERVERS(42)
   * DHO_NETBIOS_NAME_SERVERS(44)
   * DHO_NETBIOS_DD_SERVER(45)
   * DHO_FONT_SERVERS(48)
   * DHO_X_DISPLAY_MANAGER(49)
   * DHO_MOBILE_IP_HOME_AGENT(68)
   * DHO_SMTP_SERVER(69)
   * DHO_POP3_SERVER(70)
   * DHO_NNTP_SERVER(71)
   * DHO_WWW_SERVER(72)
   * DHO_FINGER_SERVER(73)
   * DHO_IRC_SERVER(74)
   * DHO_STREETTALK_SERVER(75)
   * DHO_STDA_SERVER(76)
   * DHO_NDS_SERVERS(85)
   * </pre>
   *
   * @param code
   *        the option code.
   * @return the option value array, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public InetAddress [] getOptionAsInetAddrs (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return (opt == null) ? null : opt.getValueAsInetAddrs ();
  }

  /**
   * Returns a DHCP Option as Byte array format.
   * <p>
   * This method is only allowed for the following option codes:
   *
   * <pre>
   * DHO_DHCP_PARAMETER_REQUEST_LIST (55)
   * </pre>
   * <p>
   * Note: this mehtod is similar to getOptionRaw, only with option type
   * checking.
   *
   * @param code
   *        the option code.
   * @return the option value array, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public byte [] getOptionAsBytes (final byte code) throws IllegalArgumentException
  {
    final DHCPOption opt = getOption (code);
    return (opt == null) ? null : opt.getValueAsBytes ();
  }

  /**
   * Sets a DHCP Option as Byte format.
   * <p>
   * See <code>DHCPOption</code> for allowed option codes.
   *
   * @param code
   *        the option code.
   * @param val
   *        the value
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public void setOptionAsByte (final byte code, final byte val)
  {
    setOption (DHCPOption.newOptionAsByte (code, val));
  }

  /**
   * Sets a DHCP Option as Short format.
   * <p>
   * See <code>DHCPOption</code> for allowed option codes.
   *
   * @param code
   *        the option code.
   * @param val
   *        the value
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public void setOptionAsShort (final byte code, final short val)
  {
    setOption (DHCPOption.newOptionAsShort (code, val));
  }

  /**
   * Sets a DHCP Option as Integer format.
   * <p>
   * See <code>DHCPOption</code> for allowed option codes.
   *
   * @param code
   *        the option code.
   * @param val
   *        the value
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public void setOptionAsInt (final byte code, final int val)
  {
    setOption (DHCPOption.newOptionAsInt (code, val));
  }

  /**
   * Sets a DHCP Option as InetAddress format.
   * <p>
   * See <code>DHCPOption</code> for allowed option codes.
   *
   * @param code
   *        the option code.
   * @param val
   *        the value
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public void setOptionAsInetAddress (final byte code, final InetAddress val)
  {
    setOption (DHCPOption.newOptionAsInetAddress (code, val));
  }

  /**
   * Sets a DHCP Option as InetAddress format.
   * <p>
   * See <code>DHCPOption</code> for allowed option codes.
   *
   * @param code
   *        the option code in String format.
   * @param val
   *        the value
   * @throws UnknownHostException
   *         cannot find the address
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public void setOptionAsInetAddress (final byte code, final String val) throws UnknownHostException
  {
    setOption (DHCPOption.newOptionAsInetAddress (code, InetAddress.getByName (val)));
  }

  /**
   * Sets a DHCP Option as InetAddress array format.
   * <p>
   * See <code>DHCPOption</code> for allowed option codes.
   *
   * @param code
   *        the option code.
   * @param val
   *        the value array
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public void setOptionAsInetAddresses (final byte code, final InetAddress [] val)
  {
    setOption (DHCPOption.newOptionAsInetAddresses (code, val));
  }

  /**
   * Sets a DHCP Option as String format.
   * <p>
   * See <code>DHCPOption</code> for allowed option codes.
   *
   * @param code
   *        the option code.
   * @param val
   *        the value
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public void setOptionAsString (final byte code, final String val)
  {
    setOption (DHCPOption.newOptionAsString (code, val));
  }

  /**
   * Returns the option as raw byte[] buffer.
   * <p>
   * This is the low-level maximum performance getter for options. No byte[]
   * copy is completed to increase performance.
   *
   * @param code
   *        option code
   * @return Returns the option as raw <code>byte[]</code>, or <code>null</code>
   *         if the option is not present.
   */
  public byte [] getOptionRaw (final byte code)
  {
    final DHCPOption opt = getOption (code);
    return ((opt == null) ? null : opt.getValueFast ());
  }

  /**
   * Returns the option as DHCPOption object.
   * <p>
   * This is the low-level maximum performance getter for options. This method
   * is used by every option getter in this object.
   *
   * @param code
   *        option code
   * @return Returns the option as <code>DHCPOption</code>, or <code>null</code>
   *         if the option is not present.
   */
  public DHCPOption getOption (final byte code)
  {
    final DHCPOption opt = m_aOptions.get (Byte.valueOf (code));
    // Sanity checks
    if (opt == null)
    {
      return null;
    }
    assert (opt.getCode () == code);
    assert (opt.getValueFast () != null);
    return opt;
  }

  /**
   * Tests whether an option code is present in the packet.
   *
   * @param code
   *        DHCP option code
   * @return true if option is present
   */
  public boolean containsOption (final byte code)
  {
    return m_aOptions.containsKey (Byte.valueOf (code));
  }

  /**
   * Return an ordered list/collection of all options.
   * <p>
   * The Collection is read-only.
   *
   * @return collection of <code>DHCPOption</code>.
   */
  public Collection <DHCPOption> getOptionsCollection ()
  {
    return Collections.unmodifiableCollection (m_aOptions.values ());
  }

  /**
   * Return an array of all DHCP options.
   *
   * @return the options array
   */
  public DHCPOption [] getOptionsArray ()
  {
    return m_aOptions.values ().toArray (new DHCPOption [m_aOptions.size ()]);
  }

  /**
   * Sets the option specified for the option.
   * <p>
   * If <code>buf</code> is <code>null</code>, the option is cleared.
   * <p>
   * Options are sorted in creation order. Previous values are replaced.
   * <p>
   * This is the low-level maximum performance setter for options.
   *
   * @param code
   *        opt option code, use <code>DHO_*</code> for predefined values.
   * @param buf
   *        raw buffer value (cloned). If null, the option is removed.
   */
  public void setOptionRaw (final byte code, final byte [] buf)
  {
    if (buf == null)
    { // clear parameter
      removeOption (code);
    }
    else
    {
      setOption (new DHCPOption (code, buf)); // exception here if code=0
                                              // or code=-1
    }
  }

  /**
   * Sets the option specified for the option.
   * <p>
   * If <code>buf</code> is <code>null</code>, the option is cleared.
   * <p>
   * Options are sorted in creation order. Previous values are replaced, but
   * their previous position is retained.
   * <p>
   * This is the low-level maximum performance setter for options. This method
   * is called by all setter methods in this class.
   *
   * @param opt
   *        option code, use <code>DHO_*</code> for predefined values.
   */
  public void setOption (final DHCPOption opt)
  {
    if (opt != null)
    {
      if (opt.getValueFast () == null)
      {
        removeOption (opt.getCode ());
      }
      else
      {
        m_aOptions.put (Byte.valueOf (opt.getCode ()), opt);
      }
    }
  }

  /**
   * Sets an array of options. Calles repeatedly setOption on each element of
   * the array.
   *
   * @param opts
   *        array of options.
   */
  public void setOptions (final DHCPOption [] opts)
  {
    if (opts != null)
    {
      for (final DHCPOption opt : opts)
      {
        setOption (opt);
      }
    }
  }

  /**
   * Sets a Collection of options. Calles repeatedly setOption on each element
   * of the List.
   *
   * @param opts
   *        List of options.
   */
  public void setOptions (final Collection <DHCPOption> opts)
  {
    if (opts != null)
    {
      for (final DHCPOption opt : opts)
      {
        setOption (opt);
      }
    }
  }

  /**
   * Remove this option from the options list.
   *
   * @param opt
   *        the option code to remove.
   */
  public void removeOption (final byte opt)
  {
    m_aOptions.remove (Byte.valueOf (opt));
  }

  /**
   * Remove all options.
   */
  public void removeAllOptions ()
  {
    m_aOptions.clear ();
  }

  /**
   * Returns the IP address of the machine to which this datagram is being sent
   * or from which the datagram was received.
   *
   * @return the IP address of the machine to which this datagram is being sent
   *         or from which the datagram was received. <code>null</code> if no
   *         address.
   */
  public InetAddress getAddress ()
  {
    return m_aAddress;
  }

  /**
   * Sets the IP address of the machine to which this datagram is being sent.
   *
   * @param address
   *        the <code>InetAddress</code>.
   * @throws IllegalArgumentException
   *         address is not of <code>Inet4Address</code> class.
   */
  public void setAddress (final InetAddress address)
  {
    if (address == null)
    {
      m_aAddress = null;
    }
    else
      if (!(address instanceof Inet4Address))
      {
        throw new IllegalArgumentException ("only IPv4 addresses accepted");
      }
      else
      {
        m_aAddress = address;
      }
  }

  /**
   * Returns the port number on the remote host to which this datagram is being
   * sent or from which the datagram was received.
   *
   * @return the port number on the remote host to which this datagram is being
   *         sent or from which the datagram was received.
   */
  public int getPort ()
  {
    return m_nPort;
  }

  /**
   * Sets the port number on the remote host to which this datagram is being
   * sent.
   *
   * @param port
   *        the port number.
   */
  public void setPort (final int port)
  {
    m_nPort = port;
  }

  /**
   * Syntactic sugar for getAddress/getPort.
   *
   * @return address + port.
   */
  public InetSocketAddress getAddrPort ()
  {
    return new InetSocketAddress (m_aAddress, m_nPort);
  }

  /**
   * Syntactic sugar for setAddress/setPort.
   *
   * @param addrPort
   *        address and port, if <code>null</code> address is set to null and
   *        port to 0
   */
  public void setAddrPort (final InetSocketAddress addrPort)
  {
    if (addrPort == null)
    {
      setAddress (null);
      setPort (0);
    }
    else
    {
      setAddress (addrPort.getAddress ());
      setPort (addrPort.getPort ());
    }
  }
}
