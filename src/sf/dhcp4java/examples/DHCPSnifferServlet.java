/*
 *	This file is part of dhcp4java.
 *
 *	dhcp4java is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	dhcp4java is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Foobar; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * (c) 2006 Stephan Hadinger
 */
package sf.dhcp4java.examples;
import java.util.logging.Level;
import java.util.logging.Logger;

import sf.dhcp4java.DHCPPacket;
import sf.dhcp4java.DHCPServer;
import sf.dhcp4java.DHCPServlet;
import sf.dhcp4java.DHCPServerInitException;

/**
 * A simple DHCP sniffer based on DHCP servlets.
 * 
 * @author Stephan Hadinger
 * @version 0.50
 */
public class DHCPSnifferServlet extends DHCPServlet {
    private static final Logger logger = Logger.getLogger("sf.dhcp4java.examples.dhcpsnifferservlet");
    
    /**
     * Print received packet as INFO log, and do not respnd.
     * 
     * @see sf.dhcp4java.DHCPServlet#service(sf.dhcp4java.DHCPPacket)
     */
    public DHCPPacket service(DHCPPacket request) {
        logger.info(request.toString());
        return null;
    }

    /**
     * Launcher for the server.
     * 
     * <p>No args.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            DHCPServer server = DHCPServer.initServer(new DHCPSnifferServlet(), null);
            new Thread(server).start();
        } catch (DHCPServerInitException e) {
            logger.log(Level.SEVERE, "Server init", e);
        }
    }
}
