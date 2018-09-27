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

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple DHCP sniffer based on DHCP servlets.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPSnifferServlet extends DHCPServlet
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPSnifferServlet.class);

  /**
   * Print received packet as INFO log, and do not respnd.
   */
  @Override
  public DHCPPacket service (final DHCPPacket request)
  {
    s_aLogger.info (request.toString ());
    return null;
  }

  /**
   * Launcher for the server.
   * <p>
   * No args.
   *
   * @param args
   */
  public static void main (final String [] args)
  {
    try
    {
      final DHCPCoreServer server = DHCPCoreServer.initServer (new DHCPSnifferServlet (), null);
      new Thread (server).start ();
    }
    catch (final DHCPServerInitException e)
    {
      s_aLogger.error ("Server init", e);
    }
  }
}
