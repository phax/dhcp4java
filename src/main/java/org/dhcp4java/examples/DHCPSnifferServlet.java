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
