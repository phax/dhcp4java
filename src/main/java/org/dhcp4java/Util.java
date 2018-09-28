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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Stephan Hadinger
 * @version 1.00
 */
public final class Util
{
  private Util ()
  {}

  private static final char [] HEX_CHARS = { '0',
                                             '1',
                                             '2',
                                             '3',
                                             '4',
                                             '5',
                                             '6',
                                             '7',
                                             '8',
                                             '9',
                                             'A',
                                             'B',
                                             'C',
                                             'D',
                                             'E',
                                             'F' };

  /**
   * Converts byte to hex string (2 chars) (uppercase). E.g. byte
   * <code>255</code> will be appended as <code>FF</code>.
   *
   * @param aSB
   *        Target buffer
   * @param nByte
   *        byte to be appended
   */
  private static void _appendHex (final StringBuilder aSB, final byte nByte)
  {
    final int i = (nByte & 0xFF);
    aSB.append (HEX_CHARS[(i & 0xF0) >> 4]).append (HEX_CHARS[i & 0x0F]);
  }

  /**
   * Converts a byte[] to a sequence of hex chars (uppercase), limited to
   * <code>len</code> bytes and appends them to a string buffer
   *
   * @param aSB
   *        Target buffer
   * @param aSrcBuf
   *        Source buffer to be converted
   * @param nOfs
   *        Offset in the source buffer
   * @param nLen
   *        Number of bytes to use
   */
  static void appendHex (final StringBuilder aSB, final byte [] aSrcBuf, final int nOfs, final int nLen)
  {
    if (aSrcBuf == null)
      return;
    int nRealOfs = nOfs;
    int nRealLen = nLen;
    if (nRealOfs < 0)
    {
      // reduce length
      nRealLen += nRealOfs;
      nRealOfs = 0;
    }
    if (nRealLen <= 0 || nRealOfs >= aSrcBuf.length)
      return;

    if (nRealOfs + nRealLen > aSrcBuf.length)
      nRealLen = aSrcBuf.length - nRealOfs;

    for (int i = nRealOfs; i < nRealOfs + nRealLen; i++)
    {
      _appendHex (aSB, aSrcBuf[i]);
    }
  }

  /**
   * Convert plain byte[] to hex string (uppercase)
   */
  static void appendHex (final StringBuilder aSB, final byte [] aSrcBuf)
  {
    appendHex (aSB, aSrcBuf, 0, aSrcBuf.length);
  }

  /**
   * Convert bytes to hex string.
   *
   * @param buf
   * @return hex string (lowercase) or "" if buf is <code>null</code>
   */
  static String bytes2Hex (final byte [] buf)
  {
    if (buf == null)
    {
      return "";
    }
    final StringBuilder sb = new StringBuilder (buf.length * 2);
    appendHex (sb, buf);
    return sb.toString ();
  }

  /**
   * Convert hex String to byte[]
   */
  static byte [] hex2Bytes (final String s)
  {
    if ((s.length () & 1) != 0)
      throw new IllegalArgumentException ("String length must be even: " + s.length ());

    final byte [] buf = new byte [s.length () / 2];

    for (int index = 0; index < buf.length; index++)
    {
      final int stringIndex = index << 1;
      buf[index] = (byte) Integer.parseInt (s.substring (stringIndex, stringIndex + 2), 16);
    }
    return buf;
  }

  /**
   * Convert integer to hex chars (uppercase) and appends them to a string
   * builder
   */
  static void appendHex (final StringBuilder sbuf, final int i)
  {
    _appendHex (sbuf, (byte) ((i & 0xff000000) >>> 24));
    _appendHex (sbuf, (byte) ((i & 0x00ff0000) >>> 16));
    _appendHex (sbuf, (byte) ((i & 0x0000ff00) >>> 8));
    _appendHex (sbuf, (byte) ((i & 0x000000ff)));
  }

  public static byte [] stringToBytes (final String str)
  {
    if (str == null)
      return null;

    final char [] chars = str.toCharArray ();
    final int len = chars.length;
    final byte [] buf = new byte [len];

    for (int i = 0; i < len; i++)
    {
      buf[i] = (byte) chars[i];
    }
    return buf;
  }

  /**
   * Converts a null terminated byte[] string to a String object, with a
   * transparent conversion. Faster version than String.getBytes()
   */
  static String bytesToString (final byte [] buf)
  {
    if (buf == null)
    {
      return "";
    }
    return bytesToString (buf, 0, buf.length);
  }

  static String bytesToString (final byte [] buf, final int nSrc, final int nLen)
  {
    if (buf == null)
      return "";
    int src = nSrc;
    int len = nLen;
    if (src < 0)
    {
      // reduce length
      len += src;
      src = 0;
    }
    if (len <= 0)
      return "";

    if (src >= buf.length)
      return "";

    if (src + len > buf.length)
    {
      len = buf.length - src;
    }

    // string should be null terminated or whole buffer
    // first find the real length
    for (int i = src; i < src + len; i++)
    {
      if (buf[i] == 0)
      {
        len = i - src;
        break;
      }
    }

    final char [] chars = new char [len];
    for (int i = src; i < src + len; i++)
    {
      chars[i - src] = (char) buf[i];
    }
    return new String (chars);
  }

  /**
   * Converts 32 bits int to IPv4 <code>InetAddress</code>.
   *
   * @param val
   *        int representation of IPv4 address
   * @return the address object
   */
  public static final InetAddress int2InetAddress (final int val)
  {
    final byte [] value = { (byte) ((val & 0xFF000000) >>> 24),
                            (byte) ((val & 0X00FF0000) >>> 16),
                            (byte) ((val & 0x0000FF00) >>> 8),
                            (byte) ((val & 0x000000FF)) };
    try
    {
      return InetAddress.getByAddress (value);
    }
    catch (final UnknownHostException e)
    {
      return null;
    }
  }

  /**
   * Converts 32 bits int packaged into a 64bits long to IPv4
   * <code>InetAddress</code>.
   *
   * @param val
   *        int representation of IPv4 address
   * @return the address object
   */
  public static final InetAddress long2InetAddress (final long val)
  {
    if ((val < 0) || (val > 0xFFFFFFFFL))
    {
      // TODO exception ???
    }
    return int2InetAddress ((int) val);
  }

  /**
   * Converts IPv4 <code>InetAddress</code> to 32 bits int.
   *
   * @param addr
   *        IPv4 address object
   * @return 32 bits int
   * @throws NullPointerException
   *         <code>addr</code> is <code>null</code>.
   * @throws IllegalArgumentException
   *         the address is not IPv4 (Inet4Address).
   */
  public static final int inetAddress2Int (final InetAddress addr)
  {
    if (!(addr instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Only IPv4 supported");
    }

    final byte [] addrBytes = addr.getAddress ();
    return ((addrBytes[0] & 0xFF) << 24) |
           ((addrBytes[1] & 0xFF) << 16) |
           ((addrBytes[2] & 0xFF) << 8) |
           ((addrBytes[3] & 0xFF));
  }

  /**
   * Converts IPv4 <code>InetAddress</code> to 32 bits int, packages into a 64
   * bits <code>long</code>.
   *
   * @param addr
   *        IPv4 address object
   * @return 32 bits int
   * @throws NullPointerException
   *         <code>addr</code> is <code>null</code>.
   * @throws IllegalArgumentException
   *         the address is not IPv4 (Inet4Address).
   */
  public static final long inetAddress2Long (final InetAddress addr)
  {
    return inetAddress2Int (addr) & 0xFFFFFFFFL;
  }

  /**
   * Even faster version than {@link #getHostAddress} when the address is not
   * the only piece of information put in the string.
   *
   * @param sbuf
   *        the string builder
   * @param addr
   *        the Internet address
   */
  public static void appendHostAddress (final StringBuilder sbuf, final InetAddress addr)
  {
    if (addr == null)
      throw new IllegalArgumentException ("addr must not be null");
    if (!(addr instanceof Inet4Address))
      throw new IllegalArgumentException ("addr must be an instance of Inet4Address");

    final byte [] src = addr.getAddress ();
    sbuf.append (src[0] & 0xFF)
        .append ('.')
        .append (src[1] & 0xFF)
        .append ('.')
        .append (src[2] & 0xFF)
        .append ('.')
        .append (src[3] & 0xFF);
  }

  /**
   * Faster version than <code>InetAddress.getHostAddress()</code>.
   *
   * @param addr
   *        address
   * @return String representation of address.
   */
  public static String getHostAddress (final InetAddress addr)
  {
    final StringBuilder sbuf = new StringBuilder (15);
    appendHostAddress (sbuf, addr);
    return sbuf.toString ();
  }
}
