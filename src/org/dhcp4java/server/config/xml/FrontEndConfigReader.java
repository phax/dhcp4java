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
package org.dhcp4java.server.config.xml;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.FrontendConfig;

import static org.dhcp4java.server.config.xml.Util.get1Attribute;
import static org.dhcp4java.server.config.xml.Util.getOptAttribute;
import static org.dhcp4java.server.config.xml.Util.getOptAttributeInteger;
import static org.dhcp4java.server.config.xml.Util.getOptAttributeInetAddress;;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class FrontEndConfigReader {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FrontEndConfigReader.class.getName().toLowerCase());
	
	public static FrontendConfig xmlFrontEndConfigReader(Element frontendElt) throws ConfigException {
		try {
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
				
				Integer min = getOptAttributeInteger(threadsElt, "min");
				if (min != null) {
					frontEndConfig.setThreadsMin(min);
				}
				
				Integer max = getOptAttributeInteger(threadsElt, "max");
				if (max != null) {
					frontEndConfig.setThreadsMax(max);
				}
			}
			return frontEndConfig;
			
		} catch (Exception e) {
			throw new ConfigException("Error parsing configuration", e);
		}
	}

}
