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

  private static final long serialVersionUID = 2L;

  private final byte hardwareType;
  private final byte [] hardwareAddress;

  private static final byte HTYPE_ETHER = 1; // default type

  /*
   * Invariants: 1- hardwareAddress is not null
   */

  public HardwareAddress (final byte [] macAddr)
  {
    this.hardwareType = HTYPE_ETHER;
    this.hardwareAddress = macAddr;
  }

  public HardwareAddress (final byte hType, final byte [] macAddr)
  {
    this.hardwareType = hType;
    this.hardwareAddress = macAddr;
  }

  public HardwareAddress (final String macHex)
  {
    this (DHCPPacket.hex2Bytes (macHex));
  }

  public HardwareAddress (final byte hType, final String macHex)
  {
    this (hType, DHCPPacket.hex2Bytes (macHex));
  }

  public byte getHardwareType ()
  {
    return hardwareType;
  }

  /**
   * <p>
   * Object is cloned to avoid any side-effect.
   */
  public byte [] getHardwareAddress ()
  {
    return hardwareAddress.clone ();
  }

  @Override
  public int hashCode ()
  {
    return this.hardwareType ^ Arrays.hashCode (hardwareAddress);
  }

  @Override
  public boolean equals (final Object obj)
  {
    if ((obj == null) || (!(obj instanceof HardwareAddress)))
    {
      return false;
    }
    final HardwareAddress hwAddr = (HardwareAddress) obj;

    return ((this.hardwareType == hwAddr.hardwareType) &&
            (Arrays.equals (this.hardwareAddress, hwAddr.hardwareAddress)));
  }

  public String getHardwareAddressHex ()
  {
    return DHCPPacket.bytes2Hex (this.hardwareAddress);
  }

  /**
   * Prints the hardware address in hex format, split by ":".
   */
  @Override
  public String toString ()
  {
    final StringBuffer sb = new StringBuffer (28);
    if (hardwareType != HTYPE_ETHER)
    {
      // append hType only if it is not standard ethernet
      sb.append (this.hardwareType).append ("/");
    }
    for (int i = 0; i < hardwareAddress.length; i++)
    {
      if ((hardwareAddress[i] & 0xff) < 0x10)
        sb.append ("0");
      sb.append (Integer.toString (hardwareAddress[i] & 0xff, 16));
      if (i < hardwareAddress.length - 1)
      {
        sb.append (":");
      }
    }
    return sb.toString ();
  }

  /**
   * Parse the MAC address in hex format, split by ':'.
   * <p>
   * E.g. <tt>0:c0:c3:49:2b:57</tt>.
   *
   * @param macStr
   * @return the newly created HardwareAddress object
   */
  public static HardwareAddress getHardwareAddressByString (final String macStr)
  {
    if (macStr == null)
    {
      throw new NullPointerException ("macStr is null");
    }
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
