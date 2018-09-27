package org.dhcp4java.examples;

import static org.dhcp4java.DHCPConstants.BOOTREQUEST;
import static org.dhcp4java.DHCPConstants.HTYPE_ETHER;

import java.util.Random;

import org.dhcp4java.DHCPPacket;

/**
 * Example of DHCP Client (under construction).
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPClient
{
  private static byte [] macAddress = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05 };

  private DHCPClient ()
  {
    throw new UnsupportedOperationException ();
  }

  public static void main (final String [] args)
  {
    // first send discover
    final DHCPPacket discover = new DHCPPacket ();

    discover.setOp (BOOTREQUEST);
    discover.setHtype (HTYPE_ETHER);
    discover.setHlen ((byte) 6);
    discover.setHops ((byte) 0);
    discover.setXid ((new Random ()).nextInt ());
    discover.setSecs ((short) 0);
    discover.setFlags ((short) 0);
    discover.setChaddr (macAddress);
  }
}
