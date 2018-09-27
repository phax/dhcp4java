package org.dhcp4java.examples;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPPacket;

/**
 * A simple DHCP sniffer.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPSniffer
{
  private DHCPSniffer ()
  {
    throw new UnsupportedOperationException ();
  }

  public static void main (final String [] args)
  {
    try (final DatagramSocket socket = new DatagramSocket (DHCPConstants.BOOTP_REQUEST_PORT))
    {
      while (true)
      {
        final DatagramPacket pac = new DatagramPacket (new byte [1500], 1500);
        DHCPPacket dhcp;

        socket.receive (pac);
        dhcp = DHCPPacket.getPacket (pac);
        System.out.println (dhcp.toString ());
      }
    }
    catch (final Exception e)
    {
      e.printStackTrace ();
    }
  }
}
