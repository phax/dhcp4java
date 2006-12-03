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

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.dhcp4java.server.DHCPClusterNode;
import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.FrontendConfig;
import org.dhcp4java.server.config.GlobalConfig;
import org.dhcp4java.server.config.TopologyConfig;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class ClusterMainConfigReader {

    private static final long serialVersionUID = 1L;
    
	private static final Logger logger = Logger.getLogger(ClusterMainConfigReader.class.getName().toLowerCase());
	
	public static void parseXmlFile(InputStream xml, DHCPClusterNode cluster) throws ConfigException {
    	try {
			Builder parser = new Builder();
			Document doc = parser.build(xml);

			Element root = doc.getRootElement();
			if (!"dhcp-server".equals(root.getLocalName())) {
				throw new ConfigException("root node is not dhcp-server but "+root.getLocalName());
			}
			
			// parse "front-end" element
			Elements frontendElts = root.getChildElements("front-end");
			if (frontendElts.size() != 1) {
				throw new ConfigException("1 'front-end' element expected, found "+frontendElts.size());
			}
			FrontendConfig frontendConfig = FrontEndConfigReader.xmlFrontEndConfigReader(frontendElts.get(0));
			//FrontendConfig frontendConfig = FrontendConfigReader...
			
			// parse "global" element
			Elements globalElts = root.getChildElements("global");
			if (globalElts.size() != 1) {
				throw new ConfigException("1 'global' element expected, found "+globalElts.size());
			}
			GlobalConfig globalConfig = GlobalConfigReader.xmlGlobalConfigReader(globalElts.get(0));
			
			// parse "topology" element
			Elements topologyElts = root.getChildElements("topology");
			if (topologyElts.size() != 1) {
				throw new ConfigException("1 'subnets' element expected, found "+topologyElts.size());
			}
			TopologyConfig topologyConfig = TopologyConfigReader.xmlTopologyReader(topologyElts.get(0));
			
			cluster.setFrontendConfig(frontendConfig);
			cluster.setGlobalConfig(globalConfig);
			cluster.setTopologyConfig(topologyConfig);
    	} catch (ConfigException e) {
    		throw e;		// re-throw
    	} catch (Exception e) {
    		logger.log(Level.WARNING, "global exception", e);
    		throw new ConfigException("global exception", e);
    	}
    }

}
