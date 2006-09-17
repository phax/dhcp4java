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
package org.dhcp4java.server;

import java.util.logging.Logger;

import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPServlet;
import org.dhcp4java.server.config.FrontendConfiguration;
import org.dhcp4java.server.config.GlobalConfiguration;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.60
 */
public class MainServlet extends DHCPServlet {

    private static final Logger logger = Logger.getLogger(MainServlet.class.getName().toLowerCase());

	private final FrontendConfiguration frontendConfiguration;
	private final GlobalConfiguration globalConfiguration;
	
	public MainServlet(FrontendConfiguration frontendConfiguration,
						GlobalConfiguration globalConfiguration) {
		super();
		this.frontendConfiguration = frontendConfiguration;
		this.globalConfiguration = globalConfiguration;
	}
	
	/**
	 * Handles DHCPDISCOVER from a client.
	 * 
	 * <p>Normal order is:<br>
	 * 1. filter client request from global parameters<br>
	 * 2. find out which Subnet(s) the client belongs<br>
	 * 3. for each eligible subnet, filter request based on subnet refquirements<br>
	 * 4. calculate the client IP address and lease time,
	 * 		reserve the address for a limited duration
	 * 5. generate the DHCPOFFER response, and send it back<br>
	 * 
	 * @see org.dhcp4java.DHCPServlet#doDiscover(org.dhcp4java.DHCPPacket)
	 */
	@Override
	protected DHCPPacket doDiscover(DHCPPacket request) {
		// TODO Auto-generated method stub
		return super.doDiscover(request);
	}

}
