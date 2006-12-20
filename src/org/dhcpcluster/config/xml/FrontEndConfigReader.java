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

import java.net.InetAddress;
import java.util.logging.Logger;

import nu.xom.Element;
import nu.xom.Elements;

import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.config.FrontendConfig;

import static org.dhcpcluster.config.xml.Util.getOptAttributeInetAddress;
import static org.dhcpcluster.config.xml.Util.getOptAttributeInteger;
;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class FrontEndConfigReader {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FrontEndConfigReader.class.getName().toLowerCase());
	
	public static FrontendConfig xmlFrontEndConfigReader(Element frontendElt) throws ConfigException {
		FrontendConfig frontEndConfig = new FrontendConfig();
		
		// parse "listen"
		Elements listenElts = frontendElt.getChildElements("listen");
		if (listenElts.size() > 1) {
			logger.warning("more than one 'listen' element");
		}
		if (listenElts.size() > 0) {
			Element listenElt = listenElts.get(0);
			
			InetAddress ip = getOptAttributeInetAddress(listenElt, "inet");
			if (ip != null) {
				frontEndConfig.setListenIp(ip);
			}
			
			Integer port = getOptAttributeInteger(listenElt, "port");
			if (port != null) {
				frontEndConfig.setListenPort(port);
			}

		}
		
		// parse "threads"
		Elements threadsElts = frontendElt.getChildElements("threads");
		if (threadsElts.size() > 1) {
			logger.warning("more than one 'threads' element");
		}
		if (threadsElts.size() > 0) {
			Element threadsElt = threadsElts.get(0);
			
			Integer nb = getOptAttributeInteger(threadsElt, "nb");
			if (nb != null) {
				frontEndConfig.setThreadsNb(nb);
			}
			
			Integer core = getOptAttributeInteger(threadsElt, "core");
			if (core != null) {
				frontEndConfig.setThreadsCore(core);
			}
			
			Integer max = getOptAttributeInteger(threadsElt, "max");
			if (max != null) {
				frontEndConfig.setThreadsMax(max);
			}
			
			Integer keepalive = getOptAttributeInteger(threadsElt, "keepalive");
			if (keepalive != null) {
				frontEndConfig.setThreadsKeepalive(keepalive);
			}
		}
		return frontEndConfig;
	}

}
