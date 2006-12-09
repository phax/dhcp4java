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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.dhcp4java.server.DHCPClusterNode;
import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.FrontendConfig;
import org.dhcp4java.server.config.GenericConfigReader;
import org.dhcp4java.server.config.GlobalConfig;
import org.dhcp4java.server.config.TopologyConfig;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class XmlConfigReader implements GenericConfigReader {
	
	private static final Logger logger = Logger.getLogger(XmlConfigReader.class.getName().toLowerCase());

	private boolean inited = false;
	
	private FrontendConfig frontendConfig = null;
	
	private GlobalConfig globalConfig = null;
	
	private TopologyConfig topologyConfig = null; 
	

	public void init(DHCPClusterNode dhcpClusterNode, Properties configProperties) {
		if (dhcpClusterNode == null) {
			throw new NullPointerException("dhcpClusterNode must not be null");
		}
		if (configProperties == null) {
			throw new NullPointerException("configProperties must not be null");
		}
		String xmlResourcePath = configProperties.getProperty("config.xml.resourcepath");
		logger.config("xmlResourcePath ="+xmlResourcePath);
    	InputStream xml = ClassLoader.getSystemResourceAsStream("org/dhcp4java/server/config/xml/configtest.xml");

    	try {
    		parseXmlFile(xml);
    		inited = true;
    	} catch (ConfigException e) {
    		logger.log(Level.SEVERE, "config exception", e);
    	}
		// TODO
	}


	/* (non-Javadoc)
	 * @see org.dhcp4java.server.config.GenericConfigReader#getFrontEndConfig()
	 */
	public FrontendConfig getFrontEndConfig() {
		if (!inited) {
			throw new IllegalStateException("Config not inited");
		}
		return frontendConfig;
	}


	/* (non-Javadoc)
	 * @see org.dhcp4java.server.config.GenericConfigReader#getGlobalConfig()
	 */
	public GlobalConfig getGlobalConfig() {
		if (!inited) {
			throw new IllegalStateException("Config not inited");
		}
		return globalConfig;
	}


	/* (non-Javadoc)
	 * @see org.dhcp4java.server.config.GenericConfigReader#getTopologyConfig()
	 */
	public TopologyConfig getTopologyConfig() {
		if (!inited) {
			throw new IllegalStateException("Config not inited");
		}
		return topologyConfig;
	}


	/* (non-Javadoc)
	 * @see org.dhcp4java.server.config.GenericConfigReader#reloadTopologyConfig()
	 */
	public TopologyConfig reloadTopologyConfig() {
		// TODO Auto-generated method stub
		return null;
	}


	public void parseXmlFile(InputStream xml) throws ConfigException {
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
			this.frontendConfig = FrontEndConfigReader.xmlFrontEndConfigReader(frontendElts.get(0));
			//FrontendConfig frontendConfig = FrontendConfigReader...
			
			// parse "global" element
			Elements globalElts = root.getChildElements("global");
			if (globalElts.size() != 1) {
				throw new ConfigException("1 'global' element expected, found "+globalElts.size());
			}
			this.globalConfig = GlobalConfigReader.xmlGlobalConfigReader(globalElts.get(0));
			
			// parse "topology" element
			Elements topologyElts = root.getChildElements("topology");
			if (topologyElts.size() != 1) {
				throw new ConfigException("1 'subnets' element expected, found "+topologyElts.size());
			}
			this.topologyConfig = TopologyConfigReader.xmlTopologyReader(topologyElts.get(0));
    	} catch (ConfigException e) {
    		throw e;		// re-throw
    	} catch (Exception e) {
    		logger.log(Level.WARNING, "global exception", e);
    		throw new ConfigException("global exception", e);
    	}
    }

}
