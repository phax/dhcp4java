package org.dhcp4java.test;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.dhcp4java.Util;
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
}
