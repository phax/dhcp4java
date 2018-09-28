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

/**
 * Number in hexadecimal format are used throughout Freenet.
 * <p>
 * Unless otherwise stated, the conventions follow the rules outlined in the
 * Java Language Specification.
 * </p>
 *
 * @author syoung
 */
final class HexUtils
{
  private HexUtils ()
  {}

  public static byte [] hexToBytes (final String s)
  {
    return hexToBytes (s, 0);
  }

  public static byte [] hexToBytes (final String s, final int off)
  {
    final byte [] bs = new byte [off + (1 + s.length ()) / 2];
    hexToBytes (s, bs, off);
    return bs;
  }

  /**
   * Converts a String of hex characters into an array of bytes.
   *
   * @param sStr
   *        A string of hex characters (upper case or lower) of even length.
   * @param out
   *        A byte array of length at least s.length()/2 + off
   * @param off
   *        The first byte to write of the array
   * @throws NumberFormatException
   *         On error
   * @throws IndexOutOfBoundsException
   *         on error
   */
  public static void hexToBytes (final String sStr, final byte [] out, final int off)
  {
    String s = sStr;
    final int slen = s.length ();
    if ((slen % 2) != 0)
    {
      s = '0' + s;
    }

    if (out.length < off + slen / 2)
    {
      throw new IndexOutOfBoundsException ("Output buffer too small for input (" +
                                           out.length +
                                           '<' +
                                           off +
                                           slen / 2 +
                                           ')');
    }

    // Safe to assume the string is even length
    for (int i = 0; i < slen; i += 2)
    {
      final byte b1 = (byte) Character.digit (s.charAt (i), 16);
      final byte b2 = (byte) Character.digit (s.charAt (i + 1), 16);

      if (b1 < 0 || b2 < 0)
      {
        throw new NumberFormatException ();
      }
      out[off + i / 2] = (byte) (b1 << 4 | b2);
    }
  }
}
