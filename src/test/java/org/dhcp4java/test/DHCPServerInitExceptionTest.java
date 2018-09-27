package org.dhcp4java.test;

import org.dhcp4java.DHCPServerInitException;
import org.junit.Test;

public class DHCPServerInitExceptionTest
{
  @Test (expected = DHCPServerInitException.class)
  public void testDHCPServerInitExceptionVoid () throws DHCPServerInitException
  {
    throw new DHCPServerInitException ();
  }

  @Test (expected = DHCPServerInitException.class)
  public void testDHCPServerInitExceptionString () throws DHCPServerInitException
  {
    throw new DHCPServerInitException ("foobar");
  }

  @Test (expected = DHCPServerInitException.class)
  public void testDHCPServerInitExceptionThrowableString () throws DHCPServerInitException
  {
    throw new DHCPServerInitException ("foobar", new RuntimeException ());
  }

  @Test (expected = DHCPServerInitException.class)
  public void testDHCPServerInitExceptionThrowable () throws DHCPServerInitException
  {
    throw new DHCPServerInitException (new RuntimeException ());
  }

}
