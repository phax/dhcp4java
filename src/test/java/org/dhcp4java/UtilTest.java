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

import java.net.InetAddress;

import org.junit.Test;

public class UtilTest
{
  @Test
  public void testInt2InetAddress () throws Exception
  {
    InetAddress ip;

    ip = InetAddress.getByName ("0.0.0.0");
    assertEquals (ip, Util.int2InetAddress (0));
    ip = InetAddress.getByName ("255.255.255.255");
    assertEquals (ip, Util.int2InetAddress (-1));
    ip = InetAddress.getByName ("10.0.0.1");
    assertEquals (ip, Util.int2InetAddress (0x0A000001));
  }

  @Test
  public void testLong2InetAddress () throws Exception
  {
    InetAddress ip;

    ip = InetAddress.getByName ("0.0.0.0");
    assertEquals (ip, Util.long2InetAddress (0));
    ip = InetAddress.getByName ("255.255.255.255");
    assertEquals (ip, Util.long2InetAddress (-1));
    ip = InetAddress.getByName ("10.0.0.1");
    assertEquals (ip, Util.long2InetAddress (0x0A000001));
  }

  @Test
  public void testInetAddress2Int () throws Exception
  {
    InetAddress ip;

    ip = InetAddress.getByName ("0.0.0.0");
    assertEquals (Util.inetAddress2Int (ip), 0);
    ip = InetAddress.getByName ("255.255.255.255");
    assertEquals (Util.inetAddress2Int (ip), -1);
    ip = InetAddress.getByName ("10.0.0.1");
    assertEquals (Util.inetAddress2Int (ip), 0x0A000001);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInetAddress2IntIPv6 () throws Exception
  {
    Util.inetAddress2Int (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test
  public void testInetAddress2Long () throws Exception
  {
    InetAddress ip;

    ip = InetAddress.getByName ("0.0.0.0");
    assertEquals (Util.inetAddress2Long (ip), 0L);
    ip = InetAddress.getByName ("255.255.255.255");
    assertEquals (Util.inetAddress2Long (ip), 0xFFFFFFFFL);
    ip = InetAddress.getByName ("10.0.0.1");
    assertEquals (Util.inetAddress2Long (ip), 0x0A000001L);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInetAddress2LongIPv6 () throws Exception
  {
    Util.inetAddress2Long (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }

  @Test
  public void testGetHostAddress () throws Exception
  {
    InetAddress adr;
    adr = InetAddress.getByName ("0.0.0.0");
    assertEquals (adr.getHostAddress (), Util.getHostAddress (adr));
    adr = InetAddress.getByName ("255.255.255.255");
    assertEquals (adr.getHostAddress (), Util.getHostAddress (adr));
    adr = InetAddress.getByName ("10.254.11.252");
    assertEquals (adr.getHostAddress (), Util.getHostAddress (adr));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetHostAddressNull ()
  {
    Util.getHostAddress (null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetHostAddressIPv6 () throws Exception
  {
    Util.getHostAddress (InetAddress.getByName ("1080:0:0:0:8:800:200C:417A"));
  }
}
