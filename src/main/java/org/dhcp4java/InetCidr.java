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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Stephan Hadinger
 * @version 1.00
 */
public class InetCidr implements Serializable, Comparable <InetCidr>
{
  private final int m_nAddr;
  private final int m_nMask;

  /**
   * Constructor for InetCidr.
   * <p>
   * Takes a network address (IPv4) and a mask length
   *
   * @param addr
   *        IPv4 address
   * @param mask
   *        mask length (between 1 and 32)
   * @throws NullPointerException
   *         if addr is null
   * @throws IllegalArgumentException
   *         if addr is not IPv4
   */
  public InetCidr (final InetAddress addr, final int mask)
  {
    if (addr == null)
      throw new NullPointerException ("addr is null");
    if (!(addr instanceof Inet4Address))
      throw new IllegalArgumentException ("Only IPv4 addresses supported");
    if (mask < 1 || mask > 32)
      throw new IllegalArgumentException ("Bad mask:" + mask + " must be between 1 and 32");

    // apply mask to address
    m_nAddr = Util.inetAddress2Int (addr) & (int) CIDR_MASK_LONG[mask];
    m_nMask = mask;
  }

  /**
   * Constructs a <code>InetCidr</code> provided an ip address and an ip mask.
   * <p>
   * If the mask is not valid, an exception is raised.
   *
   * @param addr
   *        the ip address (IPv4)
   * @param netMask
   *        the ip mask
   * @throws IllegalArgumentException
   *         if <code>addr</code> or <code>netMask</code> is <code>null</code>.
   * @throws IllegalArgumentException
   *         if the <code>netMask</code> is not a valid one.
   */
  public InetCidr (final InetAddress addr, final InetAddress netMask)
  {
    if (addr == null || netMask == null)
      throw new NullPointerException ();
    if (!(addr instanceof Inet4Address) || !(netMask instanceof Inet4Address))
      throw new IllegalArgumentException ("Only IPv4 addresses supported");

    final Integer intMask = CIDR.get (netMask);
    if (intMask == null)
      throw new IllegalArgumentException ("netmask: " + netMask + " is not a valid mask");

    m_nAddr = Util.inetAddress2Int (addr) & (int) CIDR_MASK_LONG[intMask.intValue ()];
    m_nMask = intMask.intValue ();
  }

  @Override
  public String toString ()
  {
    return Util.int2InetAddress (m_nAddr).getHostAddress () + '/' + m_nMask;
  }

  /**
   * @return Returns the addr.
   */
  public InetAddress getAddr ()
  {
    return Util.int2InetAddress (m_nAddr);
  }

  /**
   * @return Returns the addr as a long.
   */
  public long getAddrLong ()
  {
    return m_nAddr & 0xFFFFFFFFL;
  }

  /**
   * @return Returns the mask.
   */
  public int getMask ()
  {
    return m_nMask;
  }

  /**
   * Returns a <code>long</code> representation of Cidr.
   * <P>
   * The high 32 bits contain the mask, the low 32 bits the network address.
   *
   * @return the <code>long</code> representation of the Cidr
   */
  public final long toLong ()
  {
    return (m_nAddr & 0xFFFFFFFFL) + (((long) m_nMask) << 32);
  }

  /**
   * Creates a new <code>InetCidr</code> from its <code>long</code>
   * representation.
   *
   * @param l
   *        the Cidr in its "long" format
   * @return the object
   */
  public static final InetCidr fromLong (final long l)
  {
    if (l < 0)
      throw new IllegalArgumentException ("l must not be negative: " + l);
    final long ip = l & 0xFFFFFFFFL;
    final long mask = l >> 32L;
    return new InetCidr (Util.long2InetAddress (ip), (int) mask);
  }

  @Override
  public int hashCode ()
  {
    return m_nAddr ^ m_nMask;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !(o instanceof InetCidr))
      return false;
    final InetCidr rhs = (InetCidr) o;
    return m_nAddr == rhs.m_nAddr && m_nMask == rhs.m_nMask;
  }

  /**
   * Returns an array of all cidr combinations with the provided ip address.
   * <p>
   * The array is ordered from the most specific to the most general mask.
   *
   * @param addr
   *        address
   * @return array of all cidr possible with this address
   */
  public static InetCidr [] addr2Cidr (final InetAddress addr)
  {
    if (addr == null)
      throw new IllegalArgumentException ("addr must not be null");
    if (!(addr instanceof Inet4Address))
      throw new IllegalArgumentException ("Only IPv4 addresses supported");

    final int addrInt = Util.inetAddress2Int (addr);
    final InetCidr [] cidrs = new InetCidr [32];
    for (int i = cidrs.length; i >= 1; i--)
    {
      cidrs[32 - i] = new InetCidr (Util.int2InetAddress (addrInt & (int) CIDR_MASK_LONG[i]), i);
    }
    return cidrs;
  }

  /**
   * Compare two InetCidr by its addr as main criterion, mask as second.
   * <p>
   * Note: this class has a natural ordering that is inconsistent with equals.
   *
   * @param rhs
   *        object to compare to
   * @return a negative integer, zero, or a positive integer as this object is
   *         less than, equal to, or greater than the specified object.
   */
  public int compareTo (final InetCidr rhs)
  {
    if (rhs == null)
      throw new NullPointerException ();

    if (equals (rhs))
      return 0;
    if (_int2UnsignedLong (m_nAddr) < _int2UnsignedLong (rhs.m_nAddr))
      return -1;
    if (_int2UnsignedLong (m_nAddr) > _int2UnsignedLong (rhs.m_nAddr))
      return 1;

    // addr are identical, now comparing mask
    if (m_nMask < rhs.m_nMask)
      return -1;
    if (m_nMask > rhs.m_nMask)
      return 1;

    // should not happen
    return 0;
  }

  private final static long _int2UnsignedLong (final int i)
  {
    return (i & 0xFFFFFFFFL);
  }

  /**
   * Checks whether a list of InetCidr is strictly sorted (no 2 equal objects).
   *
   * @param list
   *        list of potentially sorted <code>InetCidr</code>
   * @return true if <code>list</code> is sorted or <code>null</code>
   * @throws NullPointerException
   *         if one or more elements of the list are null
   */
  public static boolean isSorted (final List <InetCidr> list)
  {
    if (list == null)
      return true;

    InetCidr pivot = null;
    for (final InetCidr cidr : list)
    {
      if (cidr == null)
        throw new NullPointerException ();

      if (pivot == null)
      {
        pivot = cidr;
      }
      else
      {
        if (pivot.compareTo (cidr) >= 0)
          return false;
        pivot = cidr;
      }
    }
    return true;
  }

  /**
   * Checks whether the list does not contain any overlapping cidr(s).
   * <p>
   * Pre-requisite: list must be already sorted.
   *
   * @param list
   *        sorted list of <code>InetCidr</code>
   * @throws NullPointerException
   *         if a list element is null
   * @throws IllegalStateException
   *         if overlapping cidr are detected
   */
  public static void checkNoOverlap (final List <InetCidr> list)
  {
    if (list == null)
      return;

    // Fails test if enabled
    if (false)
      assert isSorted (list);

    InetCidr prev = null;
    long pivotEnd = -1;
    for (final InetCidr cidr : list)
    {
      if (cidr == null)
        throw new NullPointerException ();

      if (prev != null && cidr.getAddrLong () <= pivotEnd)
        throw new IllegalStateException ("Overlapping cidr: " + prev + ", " + cidr);

      pivotEnd = cidr.getAddrLong () + (CIDR_MASK_LONG[cidr.getMask ()] ^ 0xFFFFFFFFL);
      prev = cidr;
    }
  }

  private static final String [] CIDR_MASKS = { "128.0.0.0",
                                                "192.0.0.0",
                                                "224.0.0.0",
                                                "240.0.0.0",
                                                "248.0.0.0",
                                                "252.0.0.0",
                                                "254.0.0.0",
                                                "255.0.0.0",
                                                "255.128.0.0",
                                                "255.192.0.0",
                                                "255.224.0.0",
                                                "255.240.0.0",
                                                "255.248.0.0",
                                                "255.252.0.0",
                                                "255.254.0.0",
                                                "255.255.0.0",
                                                "255.255.128.0",
                                                "255.255.192.0",
                                                "255.255.224.0",
                                                "255.255.240.0",
                                                "255.255.248.0",
                                                "255.255.252.0",
                                                "255.255.254.0",
                                                "255.255.255.0",
                                                "255.255.255.128",
                                                "255.255.255.192",
                                                "255.255.255.224",
                                                "255.255.255.240",
                                                "255.255.255.248",
                                                "255.255.255.252",
                                                "255.255.255.254",
                                                "255.255.255.255" };

  private static final Map <InetAddress, Integer> CIDR = new HashMap <> (48);
  private static final long [] CIDR_MASK_LONG = new long [1 + CIDR_MASKS.length];

  static
  {
    try
    {
      CIDR_MASK_LONG[0] = 0;
      for (int i = 0; i < CIDR_MASKS.length; i++)
      {
        final InetAddress mask = InetAddress.getByName (CIDR_MASKS[i]);

        CIDR_MASK_LONG[i + 1] = Util.inetAddress2Long (mask);
        CIDR.put (mask, Integer.valueOf (i + 1));
      }
    }
    catch (final UnknownHostException e)
    {
      throw new IllegalStateException ("Unable to initialize CIDR");
    }
  }
}
