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

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple DHCP sniffer.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class MainDHCPSniffer
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (MainDHCPSniffer.class);

  private MainDHCPSniffer ()
  {}

  public static void main (final String [] args)
  {
    try (final DatagramSocket aSocket = new DatagramSocket (DHCPConstants.BOOTP_REQUEST_PORT))
    {
      while (true)
      {
        final DatagramPacket aDP = new DatagramPacket (new byte [1500], 1500);
        aSocket.receive (aDP);
        final DHCPPacket aPacket = DHCPPacket.getPacket (aDP);
        s_aLogger.info (aPacket.getAsString ());
      }
    }
    catch (final Exception e)
    {
      s_aLogger.error ("Ooops", e);
    }
  }
}
