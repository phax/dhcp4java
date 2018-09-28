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
package org.dhcp4java.examples;

import static org.dhcp4java.DHCPConstants.BOOTREQUEST;
import static org.dhcp4java.DHCPConstants.HTYPE_ETHER;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Random;

import org.dhcp4java.DHCPPacket;
import org.dhcp4java.HardwareAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example of DHCP Client (under construction).
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPClient
{

  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPClient.class);

  private DHCPClient ()
  {}

  public static void main (final String [] args) throws Exception
  {
    final InetAddress ip = InetAddress.getLocalHost ();
    s_aLogger.info ("Current IP address: " + ip.getHostAddress ());
    final NetworkInterface aNI = NetworkInterface.getByInetAddress (ip);
    final byte [] aMyMAC = aNI.getHardwareAddress ();
    s_aLogger.info ("Current MAC address: " + new HardwareAddress (aMyMAC).getAsString ());

    // first send discover
    final DHCPPacket discover = new DHCPPacket ();
    discover.setOp (BOOTREQUEST);
    discover.setHtype (HTYPE_ETHER);
    discover.setHlen ((byte) 6);
    discover.setHops ((byte) 0);
    discover.setXid ((new Random ()).nextInt ());
    discover.setSecs ((short) 0);
    discover.setFlags ((short) 0);
    discover.setChaddr (aMyMAC);
  }
}
