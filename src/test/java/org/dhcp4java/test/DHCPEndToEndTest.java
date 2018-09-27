package org.dhcp4java.test;

import static org.dhcp4java.DHCPConstants.BOOTREQUEST;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Stephan Hadinger
 */
public class DHCPEndToEndTest
{

  private static final String SERVER_ADDR = "127.0.0.1";
  private static final int SERVER_PORT = 6767;
  private static final int CLIENT_PORT = 6768;

  private static DHCPCoreServer server;
  private static DatagramSocket socket;

  /**
   * Start Server.
   *
   * @throws Exception
   *         on error
   */
  @BeforeClass
  public static void startServer () throws Exception
  {
    final Properties localProperties = new Properties ();

    localProperties.put (DHCPCoreServer.SERVER_ADDRESS, SERVER_ADDR + ':' + SERVER_PORT);
    localProperties.put (DHCPCoreServer.SERVER_THREADS, "1");

    server = DHCPCoreServer.initServer (new DHCPEndToEndTestServlet (), localProperties);

    new Thread (server).start ();

    socket = new DatagramSocket (CLIENT_PORT);
  }

  @Test (timeout = 1000)
  public void testDiscover () throws Exception
  {
    byte [] buf;
    DatagramPacket udp;
    final DHCPPacket pac = new DHCPPacket ();
    pac.setOp (BOOTREQUEST);
    buf = pac.serialize ();
    udp = new DatagramPacket (buf, buf.length);
    udp.setAddress (InetAddress.getByName (SERVER_ADDR));
    udp.setPort (SERVER_PORT);
    socket.send (udp);
    udp = new DatagramPacket (new byte [1500], 1500);
    // TODO
    // socket.receive(udp);
  }

  @AfterClass
  public static void shutdownServer ()
  {
    if (socket != null)
    {
      socket.close ();
      socket = null;
    }
    if (server != null)
    { // do some cleanup
      server.stopServer ();
      server = null;
    }
  }
}

class DHCPEndToEndTestServlet extends DHCPServlet
{
  // to be completed

}
