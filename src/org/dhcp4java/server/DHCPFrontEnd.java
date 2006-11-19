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

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPServlet;
import org.dhcp4java.server.config.FrontendConfig;
import org.dhcp4java.server.config.GlobalConfig;
import org.dhcp4java.server.config.TopologyConfig;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class DHCPFrontEnd extends DHCPServlet {
	private static final Logger logger = Logger.getLogger(DHCPFrontEnd.class.getName().toLowerCase());

	private FrontendConfig frontendConf;
	private GlobalConfig globalConfig;
	private TopologyConfig topologyConfig;
	private DHCPCoreServer server;
	private Thread					finalizerThread;
	private Executor				leaseBgExecutor;
		
}
