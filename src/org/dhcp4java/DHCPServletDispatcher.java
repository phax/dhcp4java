/*
 *	This file is part of dhcp4java, a DHCP API for the Java language.
 *	(c) 2006 Stephan Hadinger
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.dhcp4java;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.DatagramPacket;

/**
 * Servlet dispatcher
 */
class DHCPServletDispatcher implements Runnable {
    private static final Logger logger = Logger.getLogger(DHCPServletDispatcher.class.getName().toLowerCase());

    private final DHCPCoreServer     server;
    private final DHCPServlet    dispatchServlet;
    private final DatagramPacket dispatchPacket;

    public DHCPServletDispatcher(DHCPCoreServer server, DHCPServlet servlet, DatagramPacket req) {
        this.server          = server;
        this.dispatchServlet = servlet;
        this.dispatchPacket  = req;
    }

    public void run() {
        try {
            DatagramPacket response = this.dispatchServlet.serviceDatagram(this.dispatchPacket);
            this.server.sendResponse(response);		// invoke callback method
        } catch (Exception e) {
            logger.log(Level.FINE, "Exception in dispatcher", e);
        }
    }
}
