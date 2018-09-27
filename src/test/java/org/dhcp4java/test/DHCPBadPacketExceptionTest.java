package org.dhcp4java.test;

import org.dhcp4java.DHCPBadPacketException;
import org.junit.Test;

/**
 * These are complementary test (not essential ones) designed to increase the
 * test coverage.
 *
 * @author Stephan Hadinger
 */
public class DHCPBadPacketExceptionTest
{
  @Test (expected = DHCPBadPacketException.class)
  public void testDHCPBadPacketExceptionVoid ()
  {
    throw new DHCPBadPacketException ();
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testDHCPBadPacketExceptionString ()
  {
    throw new DHCPBadPacketException ("foobar");
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testDHCPBadPacketExceptionThrowableString ()
  {
    throw new DHCPBadPacketException ("foobar", new RuntimeException ());
  }

  @Test (expected = DHCPBadPacketException.class)
  public void testDHCPBadPacketExceptionThrowable ()
  {
    throw new DHCPBadPacketException (new RuntimeException ());
  }

}
