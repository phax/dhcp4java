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
package org.dhcpcluster.config.xml;

import java.util.logging.Logger;

import org.dhcpcluster.config.GlobalConfig;
import org.dhcpcluster.config.xml.data.DhcpServer;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public final class XmlGlobalConfigReader {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlGlobalConfigReader.class.getName().toLowerCase());
	
	public static GlobalConfig xmlGlobalConfigReader(DhcpServer.Global globalData) {
		GlobalConfig globalConfig = new GlobalConfig();
		
		// <server>
		if (globalData.getServer() != null) {
			globalConfig.setServerIdentifier(globalData.getServer().getIdentifier());
		}
		
		// <filter>
		
		
		// <options>
		if (globalData.getOptions() != null) {
			globalConfig.getRootNode().setDhcpOptions(TopologyConfigReader.parseOptions(globalData.getOptions()));
		}
		
		// <classes>
		
		return globalConfig;
	}

}
