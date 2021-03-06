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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings ("unused")
public class InetCidrTest
{
  @Test
  public void testConstructor () throws Exception
  {
    final InetCidr cidr = new InetCidr (InetAddress.getByName ("224.17.252.127"), 24);
    assertEquals (InetAddress.getByName ("224.17.252.0"), cidr.getAddr ());
    assertEquals (24, cidr.getMask ());

  }

  @Test (expected = NullPointerException.class)
  public void testConstructorBadArgNull ()
  {
    new InetCidr (null, 20);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorBadArgIPv6 () throws Exception
  {
    new InetCidr (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"), 20);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorBadArgMaskTooSmall () throws Exception
  {
    new InetCidr (InetAddress.getByName ("16.17.18.19"), 0);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorBadArgMaskTooBig () throws Exception
  {
    new InetCidr (InetAddress.getByName ("16.17.18.19"), 33);
  }

  @Test
  public void testConstructor2 () throws Exception
  {
    final InetCidr cidr0 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    final InetCidr cidr1 = new InetCidr (InetAddress.getByName ("10.11.12.0"), InetAddress.getByName ("255.255.255.0"));
    assertEquals (cidr0, cidr1);
  }

  @Test (expected = NullPointerException.class)
  public void testConstructorBadArgNull2 ()
  {
    new InetCidr (null, null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorBadArgIPv62 () throws Exception
  {
    new InetCidr (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"), InetAddress.getByName ("255.255.255.0"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorBadArgMask () throws Exception
  {
    new InetCidr (InetAddress.getByName ("10.11.12.0"), InetAddress.getByName ("255.255.255.12"));
  }

  @Test
  public void testAddrmask2CidrGood ()
  {
    final InetAddress ip = Util.int2InetAddress (0x12345678);
    final InetCidr cidr1 = new InetCidr (ip, 30);
    final InetCidr cidr2 = new InetCidr (ip, Util.int2InetAddress (0xFFFFFFFC));
    assertEquals (cidr1, cidr2);
  }

  @Test
  public void testToString () throws Exception
  {
    final InetCidr cidr = new InetCidr (InetAddress.getByName ("16.17.18.19"), 20);
    assertEquals ("16.17.16.0/20", cidr.toString ());
  }

  @Test
  public void testHashCode () throws Exception
  {
    final int hash1 = (new InetCidr (InetAddress.getByName ("224.17.252.127"), 24)).hashCode ();
    final int hash2 = (new InetCidr (InetAddress.getByName ("224.17.252.127"), 20)).hashCode ();
    assertTrue (hash1 != 0);
    assertTrue (hash2 != 0);
    assertTrue (hash1 != hash2);
  }

  @SuppressWarnings ("unlikely-arg-type")
  @Test
  public void testEquals () throws Exception
  {
    final InetCidr cidr1 = new InetCidr (InetAddress.getByName ("224.17.252.127"), 24);
    final InetCidr cidr2 = new InetCidr (InetAddress.getByName ("224.17.252.0"), 24);

    assertTrue (cidr1.equals (cidr1));
    assertTrue (cidr1.equals (cidr2));
    assertTrue (cidr2.equals (cidr1));
    assertFalse (cidr1.equals (null));
    assertFalse (cidr1.equals ("bla"));
    assertFalse (cidr1.equals (new InetCidr (InetAddress.getByName ("224.17.252.0"), 25)));
    assertFalse (cidr1.equals (new InetCidr (InetAddress.getByName ("225.17.252.0"), 24)));
  }

  @Test (expected = NullPointerException.class)
  public void testAddrmask2CidrAddrNull ()
  {
    new InetCidr (null, Util.int2InetAddress (0x12345678));
  }

  @Test (expected = NullPointerException.class)
  public void testAddrmask2CidrAddrMask ()
  {
    new InetCidr (Util.int2InetAddress (0x12345678), null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testAddrmask2CidrBadMask ()
  {
    final InetAddress ip = Util.int2InetAddress (0x12345678);
    new InetCidr (ip, ip); // exception should be raised here
  }

  @Test
  public void testAddr2Cidr ()
  {
    int ip = 0xFFFFFFFF;
    int mask = 32;
    final InetCidr [] addrs = InetCidr.addr2Cidr (Util.int2InetAddress (ip));

    assertEquals (32, addrs.length);
    for (int i = 0; i < 32; i++)
    {
      final InetCidr refValue = new InetCidr (Util.int2InetAddress (ip), mask);
      assertEquals (addrs[i], refValue);
      assertEquals (addrs[i].getAddr (), Util.int2InetAddress (ip));
      assertEquals (addrs[i].getMask (), mask);
      ip = ip << 1;
      mask--;
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testAddr2CidrNull ()
  {
    InetCidr.addr2Cidr (null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testAddr2CidrIPv6 () throws Exception
  {
    InetCidr.addr2Cidr (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test
  public void testToLong () throws UnknownHostException
  {
    final InetCidr cidr = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    assertEquals (0x180A0B0C00L, cidr.toLong ());
  }

  @Test
  public void testFromLong () throws UnknownHostException
  {
    final InetCidr cidr = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    assertEquals (cidr, InetCidr.fromLong (0x180A0B0C00L));
    assertEquals (cidr, InetCidr.fromLong (0x180A0B0CFFL));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testFromLongNegative ()
  {
    InetCidr.fromLong (-1);
  }

  @Test
  public void testCompareTo () throws UnknownHostException
  {
    final InetCidr cidr0 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    final InetCidr cidr1 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    final InetCidr cidr2 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 23);
    final InetCidr cidr3 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 25);
    final InetCidr cidr4 = new InetCidr (InetAddress.getByName ("10.11.11.0"), 24);
    final InetCidr cidr5 = new InetCidr (InetAddress.getByName ("10.11.13.0"), 24);
    final InetCidr cidr6 = new InetCidr (InetAddress.getByName ("11.11.12.0"), 24);
    final InetCidr cidr7 = new InetCidr (InetAddress.getByName ("129.11.12.0"), 24);

    assertEquals (0, cidr0.compareTo (cidr0));
    assertEquals (0, cidr0.compareTo (cidr1));
    assertEquals (1, cidr1.compareTo (cidr2));
    assertEquals (-1, cidr1.compareTo (cidr3));
    assertEquals (1, cidr1.compareTo (cidr4));
    assertEquals (-1, cidr1.compareTo (cidr5));
    assertEquals (-1, cidr1.compareTo (cidr6));
    assertEquals (-1, cidr1.compareTo (cidr7));
  }

  @Test
  public void testIsInetCidrListSorted () throws Exception
  {
    final InetCidr cidr1 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    // InetCidr cidr2 = new InetCidr(InetAddress.getByName("10.11.12.0"), 23);
    final InetCidr cidr3 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 25);
    // InetCidr cidr4 = new InetCidr(InetAddress.getByName("10.11.11.0"), 24);
    final InetCidr cidr5 = new InetCidr (InetAddress.getByName ("10.11.13.0"), 24);
    // InetCidr cidr6 = new InetCidr(InetAddress.getByName("11.11.12.0"), 24);
    final InetCidr cidr7 = new InetCidr (InetAddress.getByName ("129.11.12.0"), 24);

    assertTrue (InetCidr.isSorted (null));
    List <InetCidr> list1 = new ArrayList <> ();
    list1.add (cidr1);
    list1.add (cidr3);
    list1.add (cidr5);
    list1.add (cidr7);
    assertTrue (InetCidr.isSorted (list1));
    list1 = new ArrayList <> ();
    list1.add (cidr1);
    list1.add (cidr5);
    list1.add (cidr3);
    list1.add (cidr7);
    assertFalse (InetCidr.isSorted (list1));
    list1 = new ArrayList <> ();
    list1.add (cidr1);
    list1.add (cidr3);
    list1.add (cidr3);
    list1.add (cidr5);
    list1.add (cidr7);
    assertFalse (InetCidr.isSorted (list1));
  }

  @Test (expected = NullPointerException.class)
  public void testIsInetCidrListSortedNullElement () throws Exception
  {
    final List <InetCidr> list1 = new ArrayList <> ();
    list1.add (null);
    InetCidr.isSorted (list1);
  }

  @Test (expected = NullPointerException.class)
  public void testCompareToNull () throws UnknownHostException
  {
    final InetCidr cidr1 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    cidr1.compareTo (null);
  }

  @Test
  public void testHasNoOverlap () throws UnknownHostException
  {
    final InetCidr cidr1 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 24);
    final InetCidr cidr2 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 23);
    final InetCidr cidr3 = new InetCidr (InetAddress.getByName ("10.11.12.0"), 25);
    // InetCidr cidr4 = new InetCidr(InetAddress.getByName("10.11.11.0"), 24);
    final InetCidr cidr5 = new InetCidr (InetAddress.getByName ("10.11.13.0"), 24);
    final InetCidr cidr6 = new InetCidr (InetAddress.getByName ("11.11.12.0"), 24);
    final InetCidr cidr7 = new InetCidr (InetAddress.getByName ("129.11.12.0"), 24);
    final List <InetCidr> list = new ArrayList <> ();
    list.add (cidr1);
    list.add (cidr5);
    list.add (cidr6);
    list.add (cidr7);
    InetCidr.checkNoOverlap (list);
    list.clear ();
    list.add (cidr1);
    list.add (cidr2);
    list.add (cidr3);
    try
    {
      InetCidr.checkNoOverlap (list);
      Assert.fail ();
    }
    catch (final IllegalStateException e)
    {
      // good
    }
    list.clear ();
    list.add (new InetCidr (InetAddress.getByName ("10.11.0.0"), 16));
    list.add (cidr1);
    try
    {
      InetCidr.checkNoOverlap (list);
      Assert.fail ();
    }
    catch (final IllegalStateException e)
    {
      // good
    }
    InetCidr.checkNoOverlap (null);
  }

  @Test (expected = NullPointerException.class)
  public void testHasNoOverlapNullElement ()
  {
    final List <InetCidr> list = new ArrayList <> ();
    list.add (null);
    InetCidr.checkNoOverlap (list);
  }

}
