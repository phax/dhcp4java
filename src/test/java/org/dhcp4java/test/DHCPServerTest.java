package org.dhcp4java.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;
import org.junit.After;
import org.junit.Test;

public class DHCPServerTest
{
  private static final String SERVER_ADDR = "127.0.0.1";
  private static final int SERVER_PORT = 6767;

  private DHCPCoreServer m_aServer0;

  @After
  public void stopServer ()
  {
    if (m_aServer0 != null)
    {
      m_aServer0.stopServer ();
      m_aServer0 = null;
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInitServerNull () throws Exception
  {
    DHCPCoreServer.initServer (null, null);
  }

  @Test (expected = DHCPServerInitException.class)
  public void testInitServerPortAlreadyInUse () throws Exception
  {
    final Properties localProperties = new Properties ();

    localProperties.put (DHCPCoreServer.SERVER_ADDRESS, SERVER_ADDR + ':' + SERVER_PORT);
    localProperties.put (DHCPCoreServer.SERVER_THREADS, "1");

    m_aServer0 = DHCPCoreServer.initServer (new DHCPServerTestServlet (), localProperties);
    DHCPCoreServer.initServer (new DHCPServerTestServlet (), localProperties);

  }

  @Test
  public void testInitServerNullProps () throws Exception
  {
    final DHCPCoreServer server = DHCPCoreServer.initServer (new DHCPServerTestServlet (), null);
    assertNotNull (server);
    server.stopServer ();
  }

  @Test
  public void testInitServer () throws Exception
  {
    final Properties localProperties = new Properties ();

    localProperties.put (DHCPCoreServer.SERVER_ADDRESS, SERVER_ADDR + ':' + SERVER_PORT);
    localProperties.put (DHCPCoreServer.SERVER_THREADS, "1");

    final DHCPCoreServer server = DHCPCoreServer.initServer (new DHCPServerTestServlet (), localProperties);
    new Thread (server).start ();
    synchronized (this)
    {
      wait (300);
    }
    server.stopServer ();
  }

  // parseSocketAddress
  @Test
  public void testParseSocketAddress () throws Exception
  {
    assertEquals (new InetSocketAddress (InetAddress.getByName ("254.10.220.0"), 67),
                  DHCPCoreServer.parseSocketAddress ("254.10.220.0:67"));
  }

  @Test (expected = IllegalArgumentException.class)
  public void testParseSocketAddressNull ()
  {
    DHCPCoreServer.parseSocketAddress (null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testParseSocketAddressNoSemicolon ()
  {
    DHCPCoreServer.parseSocketAddress ("254.10.220.0/67");
  }
  // @Test (expected=IllegalArgumentException.class)
  // public void testParseSocketAddressBadAddress() {
  // InetSocketAddress sockadr = DHCPCoreServer.parseSocketAddress("rubish:67");
  // System.out.println(sockadr);
  // }

}

class DHCPServerTestServlet extends DHCPServlet
{
  //

}
