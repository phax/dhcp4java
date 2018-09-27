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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for manipulating DHCP options (used internally).
 *
 * @author Stephan Hadinger
 * @version 1.00 Immutable object.
 */
public class DHCPOption implements Serializable
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPOption.class);

  /**
   * The code of the option. 0 is reserved for padding, -1 for end of options.
   */
  private final byte m_nCode;

  /**
   * Raw bytes value of the option. Some methods are provided for higher level
   * of data structures, depending on the <code>code</code>.
   */
  private final byte [] m_aValue;

  /**
   * Used to mark an option as having a mirroring behaviour. This means that
   * this option if used by a server will first mirror the option the client
   * sent then provide a default value if this option was not present in the
   * request.
   * <p>
   * This is only meant to be used by servers through the
   * <code>getMirrorValue</code> method.
   */
  private final boolean m_bMirror;

  /**
   * Constructor for <code>DHCPOption</code>.
   * <p>
   * Note: you must not prefix the value by a length-byte. The length prefix
   * will be added automatically by the API.
   * <p>
   * If value is <code>null</code> it is considered as an empty option. If you
   * add an empty option to a DHCPPacket, it removes the option from the packet.
   * <p>
   * This constructor adds a parameter to mark the option as "mirror". See
   * comments above.
   *
   * @param code
   *        DHCP option code
   * @param value
   *        DHCP option value as a byte array.
   * @param mirror
   *        having a mirroring behaviour.
   */
  public DHCPOption (final byte code, final byte [] value, final boolean mirror)
  {
    if (code == DHO_PAD)
    {
      throw new IllegalArgumentException ("code=0 is not allowed (reserved for padding");
    }
    if (code == DHO_END)
    {
      throw new IllegalArgumentException ("code=-1 is not allowed (reserved for End Of Options)");
    }

    m_nCode = code;
    m_aValue = value != null ? value.clone () : null;
    m_bMirror = mirror;
  }

  /**
   * Constructor for <code>DHCPOption</code>. This is the default constructor.
   * <p>
   * Note: you must not prefix the value by a length-byte. The length prefix
   * will be added automatically by the API.
   * <p>
   * If value is <code>null</code> it is considered as an empty option. If you
   * add an empty option to a DHCPPacket, it removes the option from the packet.
   *
   * @param code
   *        DHCP option code
   * @param value
   *        DHCP option value as a byte array.
   */
  public DHCPOption (final byte code, final byte [] value)
  {
    this (code, value, false);
  }

  /**
   * Return the <code>code</code> field (byte).
   *
   * @return code field
   */
  public byte getCode ()
  {
    return m_nCode;
  }

  /**
   * returns true if two <code>DHCPOption</code> objects are equal, i.e. have
   * same <code>code</code> and same <code>value</code>.
   */
  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !(o instanceof DHCPOption))
      return false;

    final DHCPOption rhs = (DHCPOption) o;
    return rhs.m_nCode == m_nCode && rhs.m_bMirror == m_bMirror && Arrays.equals (rhs.m_aValue, m_aValue);
  }

  /**
   * Returns hashcode.
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode ()
  {
    return m_nCode ^ Arrays.hashCode (m_aValue) ^ (m_bMirror ? 0x80000000 : 0);
  }

  /**
   * @return option value, can be null.
   */
  public byte [] getValue ()
  {
    return ((m_aValue == null) ? null : m_aValue.clone ());
  }

  /**
   * @return option value, never <code>null</code>. Minimal value is
   *         <code>byte[0]</code>.
   */
  public byte [] getValueFast ()
  {
    return m_aValue;
  }

  /**
   * Returns whether the option is marked as "mirror", meaning it should mirror
   * the option value in the client request.
   * <p>
   * To be used only in servers.
   *
   * @return is the option marked is mirror?
   */
  public boolean isMirror ()
  {
    return m_bMirror;
  }

  public static final boolean isOptionAsByte (final byte code)
  {
    return OptionFormat.BYTE.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
  }

  /**
   * Creates a DHCP Option as Byte format.
   * <p>
   * This method is only allowed for the following option codes:
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
   * @param val
   *        the value
   * @return New option
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public static DHCPOption newOptionAsByte (final byte code, final byte val)
  {
    if (!isOptionAsByte (code))
    {
      throw new IllegalArgumentException ("DHCP option type (" + code + ") is not byte");
    }
    return new DHCPOption (code, byte2Bytes (val));
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
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public byte getValueAsByte () throws IllegalArgumentException
  {
    if (!isOptionAsByte (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not byte");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    if (m_aValue.length != 1)
    {
      throw new DHCPBadPacketException ("option " + m_nCode + " is wrong size:" + m_aValue.length + " should be 1");
    }
    return m_aValue[0];
  }

  public static final boolean isOptionAsShort (final byte code)
  {
    return OptionFormat.SHORT.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
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
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public short getValueAsShort () throws IllegalArgumentException
  {
    if (!isOptionAsShort (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not short");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    if (m_aValue.length != 2)
    {
      throw new DHCPBadPacketException ("option " + m_nCode + " is wrong size:" + m_aValue.length + " should be 2");
    }

    return (short) ((m_aValue[0] & 0xff) << 8 | (m_aValue[1] & 0xFF));
  }

  public static final boolean isOptionAsInt (final byte code)
  {
    return OptionFormat.INT.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
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
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public int getValueAsInt () throws IllegalArgumentException
  {
    if (!isOptionAsInt (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not int");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    if (m_aValue.length != 4)
    {
      throw new DHCPBadPacketException ("option " + m_nCode + " is wrong size:" + m_aValue.length + " should be 4");
    }
    return ((m_aValue[0] & 0xFF) << 24 | (m_aValue[1] & 0xFF) << 16 | (m_aValue[2] & 0xFF) << 8 | (m_aValue[3] & 0xFF));
  }

  // TODO
  /**
   * Returns a DHCP Option as Integer format, but is usable for any numerical
   * type: int, short or byte.
   * <p>
   * There is no check on the option
   *
   * @return the option value <code>null</code> if option is not present, or
   *         wrong number of bytes.
   */
  public Integer getValueAsNum ()
  {
    if (m_aValue == null)
    {
      return null;
    }
    if (m_aValue.length == 1)
    {
      // byte
      return Integer.valueOf (m_aValue[0] & 0xFF);
    }
    if (m_aValue.length == 2)
    {
      // short
      return Integer.valueOf (((m_aValue[0] & 0xff) << 8 | (m_aValue[1] & 0xFF)));
    }
    if (m_aValue.length == 4)
    {
      return Integer.valueOf ((m_aValue[0] & 0xFF) << 24 |
                              (m_aValue[1] & 0xFF) << 16 |
                              (m_aValue[2] & 0xFF) << 8 |
                              (m_aValue[3] & 0xFF));
    }
    return null;
  }

  public static final boolean isOptionAsInetAddr (final byte code)
  {
    return OptionFormat.INET.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
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
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public InetAddress getValueAsInetAddr () throws IllegalArgumentException
  {
    if (!isOptionAsInetAddr (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not InetAddr");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    if (m_aValue.length != 4)
    {
      throw new DHCPBadPacketException ("option " + m_nCode + " is wrong size:" + m_aValue.length + " should be 4");
    }
    try
    {
      return InetAddress.getByAddress (m_aValue);
    }
    catch (final UnknownHostException e)
    {
      s_aLogger.error ("Unexpected UnknownHostException", e);
      return null; // normally impossible
    }
  }

  public static final boolean isOptionAsString (final byte code)
  {
    return OptionFormat.STRING.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
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
   * @return the option value, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public String getValueAsString () throws IllegalArgumentException
  {
    if (!isOptionAsString (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not String");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    return DHCPPacket.bytesToString (m_aValue);
  }

  public static final boolean isOptionAsShorts (final byte code)
  {
    return OptionFormat.SHORTS.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
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
   * @return the option value array, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public short [] getValueAsShorts () throws IllegalArgumentException
  {
    if (!isOptionAsShorts (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not short[]");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    if ((m_aValue.length % 2) != 0)
    {
      // multiple of 2
      throw new DHCPBadPacketException ("option " + m_nCode + " is wrong size:" + m_aValue.length + " should be 2*X");
    }

    final short [] shorts = new short [m_aValue.length / 2];
    for (int i = 0, a = 0; a < m_aValue.length; i++, a += 2)
    {
      shorts[i] = (short) (((m_aValue[a] & 0xFF) << 8) | (m_aValue[a + 1] & 0xFF));
    }
    return shorts;
  }

  public static final boolean isOptionAsInetAddrs (final byte code)
  {
    return OptionFormat.INETS.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
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
   * @return the option value array, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   * @throws DHCPBadPacketException
   *         the option value in packet is of wrong size.
   */
  public InetAddress [] getValueAsInetAddrs () throws IllegalArgumentException
  {
    if (!isOptionAsInetAddrs (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not InetAddr[]");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    if ((m_aValue.length % 4) != 0) // multiple of 4
    {
      throw new DHCPBadPacketException ("option " + m_nCode + " is wrong size:" + m_aValue.length + " should be 4*X");
    }
    try
    {
      final byte [] addr = new byte [4];
      final InetAddress [] addrs = new InetAddress [m_aValue.length / 4];
      for (int i = 0, a = 0; a < m_aValue.length; i++, a += 4)
      {
        addr[0] = m_aValue[a];
        addr[1] = m_aValue[a + 1];
        addr[2] = m_aValue[a + 2];
        addr[3] = m_aValue[a + 3];
        addrs[i] = InetAddress.getByAddress (addr);
      }
      return addrs;
    }
    catch (final UnknownHostException e)
    {
      s_aLogger.error ("Unexpected UnknownHostException", e);
      return null; // normally impossible
    }
  }

  public static final boolean isOptionAsBytes (final byte code)
  {
    return OptionFormat.BYTES.equals (_DHO_FORMATS.get (Byte.valueOf (code)));
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
   * @return the option value array, <code>null</code> if option is not present.
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public byte [] getValueAsBytes () throws IllegalArgumentException
  {
    if (!isOptionAsBytes (m_nCode))
    {
      throw new IllegalArgumentException ("DHCP option type (" + m_nCode + ") is not bytes");
    }
    if (m_aValue == null)
    {
      throw new IllegalStateException ("value is null");
    }
    return getValue ();
  }

  /**
   * Creates a DHCP Option as Short format.
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
   * @param val
   *        the value
   * @return New option. Never <code>null</code>
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public static DHCPOption newOptionAsShort (final byte code, final short val)
  {
    if (!isOptionAsShort (code))
    {
      throw new IllegalArgumentException ("DHCP option type (" + code + ") is not short");
    }
    return new DHCPOption (code, short2Bytes (val));
  }

  /**
   * Creates a DHCP Options as Short[] format.
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
   * @param arr
   *        the array of shorts
   * @return New option. Never <code>null</code>
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public static DHCPOption newOptionAsShorts (final byte code, final short [] arr)
  {
    if (!isOptionAsShorts (code))
    {
      throw new IllegalArgumentException ("DHCP option type (" + code + ") is not shorts");
    }
    byte [] buf = null;
    if (arr != null)
    {
      buf = new byte [arr.length * 2];
      for (int i = 0; i < arr.length; i++)
      {
        final short val = arr[i];
        buf[i * 2] = (byte) ((val & 0xFF00) >>> 8);
        buf[i * 2 + 1] = (byte) (val & 0XFF);
      }
    }
    return new DHCPOption (code, buf);
  }

  /**
   * Creates a DHCP Option as Integer format.
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
   * @param val
   *        the value
   * @return New option. Never <code>null</code>
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public static DHCPOption newOptionAsInt (final byte code, final int val)
  {
    if (!isOptionAsInt (code))
    {
      throw new IllegalArgumentException ("DHCP option type (" + code + ") is not int");
    }
    return new DHCPOption (code, int2Bytes (val));
  }

  /**
   * Sets a DHCP Option as InetAddress format.
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
   * and also as a simplified version for setOptionAsInetAddresses
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
   * @param val
   *        the value
   * @return New option. Never <code>null</code>
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public static DHCPOption newOptionAsInetAddress (final byte code, final InetAddress val)
  {
    if ((!isOptionAsInetAddr (code)) && (!isOptionAsInetAddrs (code)))
    {
      throw new IllegalArgumentException ("DHCP option type (" + code + ") is not InetAddress");
    }
    return new DHCPOption (code, inetAddress2Bytes (val));
  }

  /**
   * Creates a DHCP Option as InetAddress array format.
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
   * @param val
   *        the value array
   * @return New option. Never <code>null</code>
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public static DHCPOption newOptionAsInetAddresses (final byte code, final InetAddress [] val)
  {
    if (!isOptionAsInetAddrs (code))
    {
      throw new IllegalArgumentException ("DHCP option type (" + code + ") is not InetAddresses");
    }
    return new DHCPOption (code, inetAddresses2Bytes (val));
  }

  /**
   * Creates a DHCP Option as String format.
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
   * @param val
   *        the value
   * @return New option. Never <code>null</code>
   * @throws IllegalArgumentException
   *         the option code is not in the list above.
   */
  public static DHCPOption newOptionAsString (final byte code, final String val)
  {
    if (!isOptionAsString (code))
    {
      throw new IllegalArgumentException ("DHCP option type (" + code + ") is not string");
    }
    return new DHCPOption (code, DHCPPacket.stringToBytes (val));
  }

  /**
   * Get the option value based on the context, i.e. the client's request.
   * <p>
   * This should be the only method used with this class to get relevant values.
   *
   * @param request
   *        the client's DHCP requets
   * @return the value of the specific option in the client request
   * @throws NullPointerException
   *         if <code>request</code> is <code>null</code>.
   */
  public DHCPOption applyOption (final DHCPPacket request)
  {
    if (request == null)
      throw new NullPointerException ("request is null");
    if (m_bMirror)
    {
      final DHCPOption res = request.getOption (getCode ());
      return res != null ? res : this;
    }
    return this;
  }

  /**
   * Appends to this string builder a detailed string representation of the DHCP
   * datagram.
   * <p>
   * This multi-line string details: the static, options and padding parts of
   * the object. This is useful for debugging, but not efficient.
   *
   * @param buffer
   *        the string builder the string representation of this object should
   *        be appended.
   */
  public void append (final StringBuilder buffer)
  {
    // check for readable option name
    if (_DHO_NAMES.containsKey (Byte.valueOf (m_nCode)))
    {
      buffer.append (_DHO_NAMES.get (Byte.valueOf (m_nCode)));
    }
    buffer.append ('(').append (unsignedByte (m_nCode)).append (")=");

    if (m_bMirror)
    {
      buffer.append ("<mirror>");
    }

    // check for value printing
    if (m_aValue == null)
    {
      buffer.append ("<null>");
    }
    else
      if (m_nCode == DHO_DHCP_MESSAGE_TYPE)
      {
        final Byte cmd = Byte.valueOf (getValueAsByte ());
        if (_DHCP_CODES.containsKey (cmd))
        {
          buffer.append (_DHCP_CODES.get (cmd));
        }
        else
        {
          buffer.append (cmd);
        }
      }
      else
        if (m_nCode == DHO_USER_CLASS)
        {
          buffer.append (userClassToString (m_aValue));
        }
        else
          if (m_nCode == DHO_DHCP_AGENT_OPTIONS)
          {
            buffer.append (agentOptionsToString (m_aValue));
          }
          else
            if (_DHO_FORMATS.containsKey (Byte.valueOf (m_nCode)))
            {
              // formatted output
              try
              { // catch malformed values
                switch (_DHO_FORMATS.get (Byte.valueOf (m_nCode)))
                {
                  case INET:
                    DHCPPacket.appendHostAddress (buffer, getValueAsInetAddr ());
                    break;
                  case INETS:
                    for (final InetAddress addr : getValueAsInetAddrs ())
                    {
                      DHCPPacket.appendHostAddress (buffer, addr);
                      buffer.append (' ');
                    }
                    break;
                  case INT:
                    buffer.append (getValueAsInt ());
                    break;
                  case SHORT:
                    buffer.append (getValueAsShort ());
                    break;
                  case SHORTS:
                    for (final short aShort : getValueAsShorts ())
                    {
                      buffer.append (aShort).append (' ');
                    }
                    break;
                  case BYTE:
                    buffer.append (getValueAsByte ());
                    break;
                  case STRING:
                    buffer.append ('"').append (getValueAsString ()).append ('"');
                    break;
                  case BYTES:
                    if (m_aValue != null)
                    {
                      for (final byte aValue : m_aValue)
                      {
                        buffer.append (unsignedByte (aValue)).append (' ');
                      }
                    }
                    break;
                  default:
                    buffer.append ("0x");
                    DHCPPacket.appendHex (buffer, m_aValue);
                    break;
                }
              }
              catch (final IllegalArgumentException e)
              {
                // fallback to bytes
                buffer.append ("0x");
                DHCPPacket.appendHex (buffer, m_aValue);
              }
            }
            else
            {
              // unformatted raw output
              buffer.append ("0x");
              DHCPPacket.appendHex (buffer, m_aValue);
            }
  }

  /**
   * Returns a detailed string representation of the DHCP datagram.
   * <p>
   * This multi-line string details: the static, options and padding parts of
   * the object. This is useful for debugging, but not efficient.
   *
   * @return a string representation of the object.
   */
  @Override
  public String toString ()
  {
    final StringBuilder s = new StringBuilder ();
    append (s);
    return s.toString ();
  }

  /**
   * Convert unsigned byte to int
   */
  private static int unsignedByte (final byte b)
  {
    return (b & 0xFF);
  }

  public static byte [] byte2Bytes (final byte val)
  {
    final byte [] raw = { val };
    return raw;
  }

  public static byte [] short2Bytes (final short val)
  {
    final byte [] raw = { (byte) ((val & 0xFF00) >>> 8), (byte) (val & 0XFF) };
    return raw;
  }

  public static byte [] int2Bytes (final int val)
  {
    final byte [] raw = { (byte) ((val & 0xFF000000) >>> 24),
                          (byte) ((val & 0X00FF0000) >>> 16),
                          (byte) ((val & 0x0000FF00) >>> 8),
                          (byte) ((val & 0x000000FF)) };
    return raw;
  }

  public static byte [] inetAddress2Bytes (final InetAddress val)
  {
    if (val == null)
    {
      return null;
    }
    if (!(val instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Adress must be of subclass Inet4Address");
    }
    return val.getAddress ();
  }

  public static byte [] inetAddresses2Bytes (final InetAddress [] val)
  {
    if (val == null)
    {
      return null;
    }

    final byte [] buf = new byte [val.length * 4];
    for (int i = 0; i < val.length; i++)
    {
      final InetAddress addr = val[i];
      if (!(addr instanceof Inet4Address))
      {
        throw new IllegalArgumentException ("Adress must be of subclass Inet4Address");
      }
      System.arraycopy (addr.getAddress (), 0, buf, i * 4, 4);
    }
    return buf;
  }

  /**
   * Convert DHO_USER_CLASS (77) option to a List.
   *
   * @param buf
   *        option value of type User Class.
   * @return List of String values.
   */
  public static List <String> userClassToList (final byte [] buf)
  {
    if (buf == null)
    {
      return null;
    }

    final LinkedList <String> list = new LinkedList <> ();
    int i = 0;
    while (i < buf.length)
    {
      int size = unsignedByte (buf[i++]);
      final int instock = buf.length - i;
      if (size > instock)
      {
        size = instock;
      }
      list.add (DHCPPacket.bytesToString (buf, i, size));
      i += size;
    }
    return list;
  }

  /**
   * Converts DHO_USER_CLASS (77) option to a printable string
   *
   * @param buf
   *        option value of type User Class.
   * @return printable string.
   */
  public static String userClassToString (final byte [] buf)
  {
    if (buf == null)
    {
      return null;
    }

    final StringBuilder s = new StringBuilder ();
    final List <String> list = userClassToList (buf);
    for (final String sStr : list)
    {
      if (s.length () > 0)
        s.append (',');
      s.append ('"').append (sStr).append ('"');
    }
    return s.toString ();
  }

  /**
   * Converts this list of strings to a DHO_USER_CLASS (77) option.
   *
   * @param list
   *        the list of strings
   * @return byte[] buffer to use with <code>setOptionRaw</code>,
   *         <code>null</code> if list is null
   * @throws IllegalArgumentException
   *         if List contains anything else than String
   */
  public static byte [] stringListToUserClass (final List <String> list)
  {
    if (list == null)
    {
      return null;
    }

    final ByteArrayOutputStream buf = new ByteArrayOutputStream (32);
    final DataOutputStream out = new DataOutputStream (buf);

    try
    {
      for (final String s : list)
      {
        final byte [] bytes = DHCPPacket.stringToBytes (s);
        int size = bytes.length;

        if (size > 255)
        {
          size = 255;
        }
        out.writeByte (size);
        out.write (bytes, 0, size);
      }
      return buf.toByteArray ();
    }
    catch (final IOException e)
    {
      s_aLogger.error ("Unexpected IOException", e);
      return buf.toByteArray ();
    }
  }

  /**
   * Converts DHO_DHCP_AGENT_OPTIONS (82) option type to a printable string
   *
   * @param buf
   *        option value of type Agent Option.
   * @return printable string.
   */
  public static String agentOptionsToString (final byte [] buf)
  {
    if (buf == null)
    {
      return null;
    }

    final Map <Byte, String> map = agentOptionsToMap (buf);
    final StringBuffer s = new StringBuffer ();
    for (final Entry <Byte, String> entry : map.entrySet ())
    {
      s.append ('{').append (unsignedByte (entry.getKey ().byteValue ())).append ("}\"");
      s.append (entry.getValue ()).append ('\"');
      s.append (',');
    }
    if (s.length () > 0)
    {
      s.setLength (s.length () - 1);
    }

    return s.toString ();
  }

  /**
   * Converts Map&lt;Byte,String&gt; to DHO_DHCP_AGENT_OPTIONS (82) option.
   * <p>
   * LinkedHashMap are preferred as they preserve insertion order. Regular
   * HashMap order is random.
   *
   * @param map
   *        Map&lt;Byte,String&gt; couples
   * @return byte[] buffer to use with <code>setOptionRaw</code>
   * @throws IllegalArgumentException
   *         if List contains anything else than String
   */
  public static byte [] agentOptionToRaw (final Map <Byte, String> map)
  {
    if (map == null)
    {
      return null;
    }
    final ByteArrayOutputStream buf = new ByteArrayOutputStream (64);
    final DataOutputStream out = new DataOutputStream (buf);
    try
    {
      for (final Entry <Byte, String> entry : map.entrySet ())
      {
        final byte [] bufTemp = DHCPPacket.stringToBytes (entry.getValue ());
        final int size = bufTemp.length;
        assert (size >= 0);
        if (size > 255)
        {
          throw new IllegalArgumentException ("Value size is greater then 255 bytes");
        }
        out.writeByte (entry.getKey ().byteValue ());
        out.writeByte (size);
        out.write (bufTemp, 0, size);
      }
      return buf.toByteArray ();
    }
    catch (final IOException e)
    {
      s_aLogger.error ("Unexpected IOException", e);
      return buf.toByteArray ();
    }
  }

  /**
   * Converts DHO_DHCP_AGENT_OPTIONS (82) option type to a LinkedMap.
   * <p>
   * Order of parameters is preserved (use avc <code>LinkedHashmap</code>). Keys
   * are of type <code>Byte</code>, values are of type <code>String</code>.
   *
   * @param buf
   *        byte[] buffer returned by <code>getOptionRaw</code>
   * @return the LinkedHashmap of values, <code>null</code> if buf is
   *         <code>null</code>
   */
  public static final Map <Byte, String> agentOptionsToMap (final byte [] buf)
  {
    if (buf == null)
    {
      return null;
    }

    final Map <Byte, String> map = new LinkedHashMap <> ();
    int i = 0;

    while (i < buf.length)
    {
      if (buf.length - i < 2)
      {
        break; // not enough data left
      }
      final Byte key = Byte.valueOf (buf[i++]);
      int size = unsignedByte (buf[i++]);
      final int instock = buf.length - i;

      if (size > instock)
      {
        size = instock;
      }
      map.put (key, DHCPPacket.bytesToString (buf, i, size));
      i += size;
    }
    return map;
  }

  /**
   * Returns the type of the option based on the option code.
   * <p>
   * The type is returned as a <code>Class</code> object:
   * <ul>
   * <li><code>InetAddress.class</code></li>
   * <li><code>InetAddress[].class</code></li>
   * <li><code>int.class</code></li>
   * <li><code>short.class</code></li>
   * <li><code>short[].class</code></li>
   * <li><code>byte.class</code></li>
   * <li><code>byte[].class</code></li>
   * <li><code>String.class</code></li>
   * </ul>
   * <p>
   * Please use <code>getSimpleName()</code> method of <code>Class</code> object
   * for the String representation.
   *
   * @param code
   *        the DHCP option code
   * @return the Class object representing accepted types
   */
  public static Class <?> getOptionFormat (final byte code)
  {
    final OptionFormat format = _DHO_FORMATS.get (Byte.valueOf (code));
    if (format == null)
    {
      return null;
    }
    switch (format)
    {
      case INET:
        return InetAddress.class;
      case INETS:
        return InetAddress [].class;
      case INT:
        return int.class;
      case SHORT:
        return short.class;
      case SHORTS:
        return short [].class;
      case BYTE:
        return byte.class;
      case BYTES:
        return byte [].class;
      case STRING:
        return String.class;
      default:
        return null;
    }
  }

  /**
   * Simple method for converting from string to supported class format.
   * <p>
   * Support values are:
   * <ul>
   * <li>InetAddress, inet</li>
   * <li>InetAddress[], inets</li>
   * <li>int</li>
   * <li>short</li>
   * <li>short[], shorts</li>
   * <li>byte</li>
   * <li>byte[], bytes</li>
   * <li>String, string</li>
   * </ul>
   *
   * @param className
   *        name of the data format (see above)
   * @return <code>Class</code> or <code>null</code> if not supported
   */
  public static Class <?> string2Class (final String className)
  {
    if ("InetAddress".equals (className))
      return InetAddress.class;
    if ("inet".equals (className))
      return InetAddress.class;
    if ("InetAddress[]".equals (className))
      return InetAddress [].class;
    if ("inets".equals (className))
      return InetAddress [].class;
    if ("int".equals (className))
      return int.class;
    if ("short".equals (className))
      return short.class;
    if ("short[]".equals (className))
      return short [].class;
    if ("shorts".equals (className))
      return short [].class;
    if ("byte".equals (className))
      return byte.class;
    if ("byte[]".equals (className))
      return byte [].class;
    if ("bytes".equals (className))
      return byte [].class;
    if ("String".equals (className))
      return String.class;
    if ("string".equals (className))
      return String.class;
    return null;
  }

  /**
   * Parse an option from a pure string representation.
   * <P>
   * The expected class is passed as a parameter, and can be provided by the
   * <code>string2Class()</code> method from a string representation of the
   * class.
   * <P>
   * TODO examples
   *
   * @param code
   *        DHCP option code
   * @param format
   *        expected Java Class after conversion
   * @param sValue
   *        string representation of the value
   * @return the DHCPOption object
   */
  public static DHCPOption parseNewOption (final byte code, final Class <?> format, final String sValue)
  {
    if (format == null || sValue == null)
    {
      throw new NullPointerException ();
    }
    String value = sValue;

    if (short.class.equals (format))
    { // short
      return newOptionAsShort (code, (short) Integer.parseInt (value));
    }
    else
      if (short [].class.equals (format))
      { // short[]
        final String [] listVal = value.split (" ");
        final short [] listShort = new short [listVal.length];
        for (int i = 0; i < listVal.length; i++)
        {
          listShort[i] = (short) Integer.parseInt (listVal[i]);
        }
        return newOptionAsShorts (code, listShort);
      }
      else
        if (int.class.equals (format))
        { // int
          return newOptionAsInt (code, Integer.parseInt (value));
        }
        else
          if (String.class.equals (format))
          { // String
            return newOptionAsString (code, value);
          }
          else
            if (byte.class.equals (format))
            { // byte
              return newOptionAsByte (code, (byte) Integer.parseInt (value));
              // TODO be explicit about BYTE allowed from -128 to 255 (unsigned
              // int support)
            }
            else
              if (byte [].class.equals (format))
              { // byte[]
                value = value.replace (".", " ");
                final String [] listVal = value.split (" ");
                final byte [] listBytes = new byte [listVal.length];
                for (int i = 0; i < listVal.length; i++)
                {
                  listBytes[i] = (byte) Integer.parseInt (listVal[i]);
                }
                return new DHCPOption (code, listBytes);
              }
              else
                if (InetAddress.class.equals (format))
                { // InetAddress
                  try
                  {
                    return newOptionAsInetAddress (code, InetAddress.getByName (value));
                  }
                  catch (final UnknownHostException e)
                  {
                    s_aLogger.error ("Invalid address:" + value, e);
                    return null;
                  }
                }
                else
                  if (InetAddress [].class.equals (format))
                  { // InetAddress[]
                    final String [] listVal = value.split (" ");
                    final InetAddress [] listInet = new InetAddress [listVal.length];
                    try
                    {
                      for (int i = 0; i < listVal.length; i++)
                      {
                        listInet[i] = InetAddress.getByName (listVal[i]);
                      }
                    }
                    catch (final UnknownHostException e)
                    {
                      s_aLogger.error ("Invalid address", e);
                      return null;
                    }
                    return newOptionAsInetAddresses (code, listInet);
                  }
    return null;
  }

  // ----------------------------------------------------------------------
  // Internal constants for high-level option type conversions.
  //
  // formats of options
  //
  enum OptionFormat
  {
    INET, // 4 bytes IP, size = 4
    INETS, // list of 4 bytes IP, size = 4*n
    INT, // 4 bytes integer, size = 4
    SHORT, // 2 bytes short, size = 2
    SHORTS, // list of 2 bytes shorts, size = 2*n
    BYTE, // 1 byte, size = 1
    BYTES, // list of bytes, size = n
    STRING, // string, size = n
    // RELAYS = 9; // DHCP sub-options (rfc 3046)
    // ID = 10; // client identifier : byte (htype) + string (chaddr)

  }

  //
  // list of formats by options
  //
  private static final Object [] _OPTION_FORMATS = { Byte.valueOf (DHO_SUBNET_MASK),
                                                     OptionFormat.INET,
                                                     Byte.valueOf (DHO_TIME_OFFSET),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_ROUTERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_TIME_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NAME_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_DOMAIN_NAME_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_LOG_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_COOKIE_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_LPR_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_IMPRESS_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_RESOURCE_LOCATION_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_HOST_NAME),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_BOOT_SIZE),
                                                     OptionFormat.SHORT,
                                                     Byte.valueOf (DHO_MERIT_DUMP),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_DOMAIN_NAME),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_SWAP_SERVER),
                                                     OptionFormat.INET,
                                                     Byte.valueOf (DHO_ROOT_PATH),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_EXTENSIONS_PATH),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_IP_FORWARDING),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_NON_LOCAL_SOURCE_ROUTING),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_POLICY_FILTER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_MAX_DGRAM_REASSEMBLY),
                                                     OptionFormat.SHORT,
                                                     Byte.valueOf (DHO_DEFAULT_IP_TTL),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_PATH_MTU_AGING_TIMEOUT),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_PATH_MTU_PLATEAU_TABLE),
                                                     OptionFormat.SHORTS,
                                                     Byte.valueOf (DHO_INTERFACE_MTU),
                                                     OptionFormat.SHORT,
                                                     Byte.valueOf (DHO_ALL_SUBNETS_LOCAL),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_BROADCAST_ADDRESS),
                                                     OptionFormat.INET,
                                                     Byte.valueOf (DHO_PERFORM_MASK_DISCOVERY),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_MASK_SUPPLIER),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_ROUTER_DISCOVERY),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_ROUTER_SOLICITATION_ADDRESS),
                                                     OptionFormat.INET,
                                                     Byte.valueOf (DHO_STATIC_ROUTES),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_TRAILER_ENCAPSULATION),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_ARP_CACHE_TIMEOUT),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_IEEE802_3_ENCAPSULATION),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_DEFAULT_TCP_TTL),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_TCP_KEEPALIVE_INTERVAL),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_TCP_KEEPALIVE_GARBAGE),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_NIS_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NTP_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NETBIOS_NAME_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NETBIOS_DD_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NETBIOS_NODE_TYPE),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_NETBIOS_SCOPE),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_FONT_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_X_DISPLAY_MANAGER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_DHCP_REQUESTED_ADDRESS),
                                                     OptionFormat.INET,
                                                     Byte.valueOf (DHO_DHCP_LEASE_TIME),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_DHCP_OPTION_OVERLOAD),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_DHCP_MESSAGE_TYPE),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_DHCP_SERVER_IDENTIFIER),
                                                     OptionFormat.INET,
                                                     Byte.valueOf (DHO_DHCP_PARAMETER_REQUEST_LIST),
                                                     OptionFormat.BYTES,
                                                     Byte.valueOf (DHO_DHCP_MESSAGE),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_DHCP_MAX_MESSAGE_SIZE),
                                                     OptionFormat.SHORT,
                                                     Byte.valueOf (DHO_DHCP_RENEWAL_TIME),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_DHCP_REBINDING_TIME),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_VENDOR_CLASS_IDENTIFIER),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_NWIP_DOMAIN_NAME),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_NISPLUS_DOMAIN),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_NISPLUS_SERVER),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_TFTP_SERVER),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_BOOTFILE),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_MOBILE_IP_HOME_AGENT),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_SMTP_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_POP3_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NNTP_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_WWW_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_FINGER_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_IRC_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_STREETTALK_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_STDA_SERVER),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NDS_SERVERS),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_NDS_TREE_NAME),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_NDS_CONTEXT),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_CLIENT_LAST_TRANSACTION_TIME),
                                                     OptionFormat.INT,
                                                     Byte.valueOf (DHO_ASSOCIATED_IP),
                                                     OptionFormat.INETS,
                                                     Byte.valueOf (DHO_USER_AUTHENTICATION_PROTOCOL),
                                                     OptionFormat.STRING,
                                                     Byte.valueOf (DHO_AUTO_CONFIGURE),
                                                     OptionFormat.BYTE,
                                                     Byte.valueOf (DHO_NAME_SERVICE_SEARCH),
                                                     OptionFormat.SHORTS,
                                                     Byte.valueOf (DHO_SUBNET_SELECTION),
                                                     OptionFormat.INET,
                                                     Byte.valueOf (DHO_DOMAIN_SEARCH),
                                                     OptionFormat.STRING,

  };
  static final Map <Byte, OptionFormat> _DHO_FORMATS = new LinkedHashMap <> ();

  /*
   * preload at startup Maps with constants allowing reverse lookup
   */
  static
  {
    // construct map of formats
    for (int i = 0; i < _OPTION_FORMATS.length / 2; i++)
    {
      _DHO_FORMATS.put ((Byte) _OPTION_FORMATS[i * 2], (OptionFormat) _OPTION_FORMATS[i * 2 + 1]);
    }
  }

  // ========================================================================
  // main: print DHCP options for Javadoc
  public static void main (final String [] args)
  {
    String all = "";
    String inet1 = "";
    String inets = "";
    String int1 = "";
    String short1 = "";
    String shorts = "";
    String byte1 = "";
    String bytes = "";
    String string1 = "";

    for (final Byte codeByte : _DHO_NAMES.keySet ())
    {
      final byte code = codeByte.byteValue ();
      String s = "";
      if (code != DHO_PAD && code != DHO_END)
      {
        s = " * " + _DHO_NAMES.get (codeByte) + '(' + (code & 0xFF) + ")\n";
      }

      all += s;
      if (_DHO_FORMATS.containsKey (codeByte))
      {
        switch (_DHO_FORMATS.get (codeByte))
        {
          case INET:
            inet1 += s;
            break;
          case INETS:
            inets += s;
            break;
          case INT:
            int1 += s;
            break;
          case SHORT:
            short1 += s;
            break;
          case SHORTS:
            shorts += s;
            break;
          case BYTE:
            byte1 += s;
            break;
          case BYTES:
            bytes += s;
            break;
          case STRING:
            string1 += s;
            break;
          default:
        }
      }
    }

    s_aLogger.info ("---All codes---");
    s_aLogger.info (all);
    s_aLogger.info ("---INET---");
    s_aLogger.info (inet1);
    s_aLogger.info ("---INETS---");
    s_aLogger.info (inets);
    s_aLogger.info ("---INT---");
    s_aLogger.info (int1);
    s_aLogger.info ("---SHORT---");
    s_aLogger.info (short1);
    s_aLogger.info ("---SHORTS---");
    s_aLogger.info (shorts);
    s_aLogger.info ("---BYTE---");
    s_aLogger.info (byte1);
    s_aLogger.info ("---BYTES---");
    s_aLogger.info (bytes);
    s_aLogger.info ("---STRING---");
    s_aLogger.info (string1);
  }
}
