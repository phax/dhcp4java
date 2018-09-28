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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Class is immutable.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class HardwareAddress implements Serializable
{
  private final byte m_nHardwareType;
  private final byte [] m_aHardwareAddress;

  private static final byte HTYPE_ETHER = 1; // default type

  /*
   * Invariants: 1- hardwareAddress is not null
   */

  public HardwareAddress (final byte [] macAddr)
  {
    this (HTYPE_ETHER, macAddr);
  }

  public HardwareAddress (final byte hType, final byte [] macAddr)
  {
    m_nHardwareType = hType;
    m_aHardwareAddress = macAddr;
  }

  public HardwareAddress (final String macHex)
  {
    this (Util.hex2Bytes (macHex));
  }

  public HardwareAddress (final byte hType, final String macHex)
  {
    this (hType, Util.hex2Bytes (macHex));
  }

  public byte getHardwareType ()
  {
    return m_nHardwareType;
  }

  /**
   * @return Object is cloned to avoid any side-effect.
   */
  public byte [] getHardwareAddress ()
  {
    return m_aHardwareAddress.clone ();
  }

  @Override
  public int hashCode ()
  {
    return m_nHardwareType ^ Arrays.hashCode (m_aHardwareAddress);
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;

    if (o == null || !(o instanceof HardwareAddress))
      return false;

    final HardwareAddress rhs = (HardwareAddress) o;
    return m_nHardwareType == rhs.m_nHardwareType && Arrays.equals (m_aHardwareAddress, rhs.m_aHardwareAddress);
  }

  public String getHardwareAddressHex ()
  {
    return Util.bytes2Hex (m_aHardwareAddress);
  }

  /**
   * Prints the hardware address in hex format, split by ":".
   */
  @Override
  public String toString ()
  {
    final StringBuilder sb = new StringBuilder (28);
    if (m_nHardwareType != HTYPE_ETHER)
    {
      // append hType only if it is not standard ethernet
      sb.append (m_nHardwareType).append ("/");
    }
    for (int i = 0; i < m_aHardwareAddress.length; i++)
    {
      if ((m_aHardwareAddress[i] & 0xff) < 0x10)
        sb.append ("0");
      sb.append (Integer.toString (m_aHardwareAddress[i] & 0xff, 16));
      if (i < m_aHardwareAddress.length - 1)
      {
        sb.append (":");
      }
    }
    return sb.toString ();
  }

  /**
   * Parse the MAC address in hex format, split by ':'.
   * <p>
   * E.g. <code>0:c0:c3:49:2b:57</code>.
   *
   * @param macStr
   *        MAC string
   * @return the newly created HardwareAddress object
   */
  public static HardwareAddress getHardwareAddressByString (final String macStr)
  {
    if (macStr == null)
      throw new NullPointerException ("macStr is null");

    final String [] macAdrItems = macStr.split (":");
    if (macAdrItems.length != 6)
    {
      throw new IllegalArgumentException ("macStr[" + macStr + "] has not 6 items");
    }
    final byte [] macBytes = new byte [6];
    for (int i = 0; i < 6; i++)
    {
      final int val = Integer.parseInt (macAdrItems[i], 16);
      if ((val < -128) || (val > 255))
      {
        throw new IllegalArgumentException ("Value is out of range:" + macAdrItems[i]);
      }
      macBytes[i] = (byte) val;
    }
    return new HardwareAddress (macBytes);
  }

}
