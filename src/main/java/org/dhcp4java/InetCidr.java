/**
 *	This file is part of dhcp4java, a DHCP API for the Java language.
 *	(c) 2006 Stephan Hadinger
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
  private final int m_aAddr;
  private final int m_nMask;

  /**
   * Constructor for InetCidr.
   * <p>
   * Takes a network address (IPv4) and a mask length
   *
   * @param addr
   *        IPv4 address
   * @param mask
   *        mask lentgh (between 1 and 32)
   * @throws NullPointerException
   *         if addr is null
   * @throws IllegalArgumentException
   *         if addr is not IPv4
   */
  public InetCidr (final InetAddress addr, final int mask)
  {
    if (addr == null)
    {
      throw new NullPointerException ("addr is null");
    }
    if (!(addr instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Only IPv4 addresses supported");
    }
    if (mask < 1 || mask > 32)
    {
      throw new IllegalArgumentException ("Bad mask:" + mask + " must be between 1 and 32");
    }

    // apply mask to address
    this.m_aAddr = Util.inetAddress2Int (addr) & (int) gCidrMask[mask];
    this.m_nMask = mask;
  }

  /**
   * Constructs a <tt>InetCidr</tt> provided an ip address and an ip mask.
   * <p>
   * If the mask is not valid, an exception is raised.
   *
   * @param addr
   *        the ip address (IPv4)
   * @param netMask
   *        the ip mask
   * @throws IllegalArgumentException
   *         if <tt>addr</tt> or <tt>netMask</tt> is <tt>null</tt>.
   * @throws IllegalArgumentException
   *         if the <tt>netMask</tt> is not a valid one.
   */
  public InetCidr (final InetAddress addr, final InetAddress netMask)
  {
    if ((addr == null) || (netMask == null))
    {
      throw new NullPointerException ();
    }
    if (!(addr instanceof Inet4Address) || !(netMask instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Only IPv4 addresses supported");
    }
    final Integer intMask = gCidr.get (netMask);
    if (intMask == null)
    {
      throw new IllegalArgumentException ("netmask: " + netMask + " is not a valid mask");
    }
    this.m_aAddr = Util.inetAddress2Int (addr) & (int) gCidrMask[intMask.intValue ()];
    this.m_nMask = intMask.intValue ();
  }

  @Override
  public String toString ()
  {
    return Util.int2InetAddress (m_aAddr).getHostAddress () + '/' + this.m_nMask;
  }

  /**
   * @return Returns the addr.
   */
  public InetAddress getAddr ()
  {
    return Util.int2InetAddress (m_aAddr);
  }

  /**
   * @return Returns the addr as a long.
   */
  public long getAddrLong ()
  {
    return m_aAddr & 0xFFFFFFFFL;
  }

  /**
   * @return Returns the mask.
   */
  public int getMask ()
  {
    return this.m_nMask;
  }

  /**
   * Returns a <tt>long</tt> representation of Cidr.
   * <P>
   * The high 32 bits contain the mask, the low 32 bits the network address.
   *
   * @return the <tt>long</tt> representation of the Cidr
   */
  public final long toLong ()
  {
    return (m_aAddr & 0xFFFFFFFFL) + (((long) m_nMask) << 32);
  }

  /**
   * Creates a new <tt>InetCidr</tt> from its <tt>long</tt> representation.
   *
   * @param l
   *        the Cidr in its "long" format
   * @return the object
   */
  public static final InetCidr fromLong (final long l)
  {
    if (l < 0)
    {
      throw new IllegalArgumentException ("l must not be negative: " + l);
    }
    final long ip = l & 0xFFFFFFFFL;
    final long mask = l >> 32L;
    return new InetCidr (Util.long2InetAddress (ip), (int) mask);
  }

  @Override
  public int hashCode ()
  {
    return this.m_aAddr ^ this.m_nMask;
  }

  @Override
  public boolean equals (final Object obj)
  {
    if ((obj == null) || (!(obj instanceof InetCidr)))
    {
      return false;
    }
    final InetCidr cidr = (InetCidr) obj;

    return ((this.m_aAddr == cidr.m_aAddr) && (this.m_nMask == cidr.m_nMask));
  }

  /**
   * Returns an array of all cidr combinations with the provided ip address.
   * <p>
   * The array is ordered from the most specific to the most general mask.
   *
   * @param addr
   * @return array of all cidr possible with this address
   */
  public static InetCidr [] addr2Cidr (final InetAddress addr)
  {
    if (addr == null)
    {
      throw new IllegalArgumentException ("addr must not be null");
    }
    if (!(addr instanceof Inet4Address))
    {
      throw new IllegalArgumentException ("Only IPv4 addresses supported");
    }
    final int addrInt = Util.inetAddress2Int (addr);
    final InetCidr [] cidrs = new InetCidr [32];

    for (int i = cidrs.length; i >= 1; i--)
    {
      cidrs[32 - i] = new InetCidr (Util.int2InetAddress (addrInt & (int) gCidrMask[i]), i);
    }
    return cidrs;
  }

  /**
   * Compare two InetCidr by its addr as main criterion, mask as second.
   * <p>
   * Note: this class has a natural ordering that is inconsistent with equals.
   *
   * @param o
   * @return a negative integer, zero, or a positive integer as this object is
   *         less than, equal to, or greater than the specified object.
   */
  public int compareTo (final InetCidr o)
  {
    if (o == null)
    {
      throw new NullPointerException ();
    }
    if (equals (o))
      return 0;
    if (int2UnsignedLong (this.m_aAddr) < int2UnsignedLong (o.m_aAddr))
      return -1;
    if (int2UnsignedLong (this.m_aAddr) > int2UnsignedLong (o.m_aAddr))
      return 1;

    // addr are identical, now comparing mask
    if (this.m_nMask < o.m_nMask)
      return -1;
    if (this.m_nMask > o.m_nMask)
      return 1;

    // should not happen
    return 0;
  }

  private final static long int2UnsignedLong (final int i)
  {
    return (i & 0xFFFFFFFFL);
  }

  /**
   * Checks whether a list of InetCidr is strictly sorted (no 2 equal objects).
   *
   * @param list
   *        list of potentially sorted <tt>InetCidr</tt>
   * @return true if <tt>list</tt> is sorted or <tt>null</tt>
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
      {
        throw new NullPointerException ();
      }
      if (pivot == null)
      {
        pivot = cidr;
      }
      else
      {
        if (pivot.compareTo (cidr) >= 0)
        {
          return false;
        }
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
   *        sorted list of <tt>InetCidr</tt>
   * @throws NullPointerException
   *         if a list element is null
   * @throws IllegalStateException
   *         if overlapping cidr are detected
   */
  public static void checkNoOverlap (final List <InetCidr> list)
  {
    if (list == null)
      return;
    if (false)
      assert isSorted (list);
    InetCidr prev = null;
    long pivotEnd = -1;
    for (final InetCidr cidr : list)
    {
      if (cidr == null)
      {
        throw new NullPointerException ();
      }
      if ((prev != null) && (cidr.getAddrLong () <= pivotEnd))
      {
        throw new IllegalStateException ("Overlapping cidr: " + prev + ", " + cidr);
      }
      pivotEnd = cidr.getAddrLong () + (gCidrMask[cidr.getMask ()] ^ 0xFFFFFFFFL);
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

  private static final Map <InetAddress, Integer> gCidr = new HashMap <> (48);
  private static final long [] gCidrMask = new long [33];

  static
  {
    try
    {
      gCidrMask[0] = 0;
      for (int i = 0; i < CIDR_MASKS.length; i++)
      {
        final InetAddress mask = InetAddress.getByName (CIDR_MASKS[i]);

        gCidrMask[i + 1] = Util.inetAddress2Long (mask);
        gCidr.put (mask, Integer.valueOf (i + 1));
      }
    }
    catch (final UnknownHostException e)
    {
      throw new IllegalStateException ("Unable to initialize CIDR");
    }
  }
}
