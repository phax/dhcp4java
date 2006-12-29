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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.config.FrontendConfig;
import org.dhcpcluster.config.GenericConfigReader;
import org.dhcpcluster.config.GlobalConfig;
import org.dhcpcluster.config.TopologyConfig;
import org.dhcpcluster.config.xml.data.DhcpServer;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class XmlConfigReader implements GenericConfigReader {
	
	private static final Logger logger = Logger.getLogger(XmlConfigReader.class.getName().toLowerCase());

	private boolean inited = false;
	
	private FrontendConfig frontendConfig = null;
	
	private GlobalConfig globalConfig = null;
	
	private TopologyConfig topologyConfig = null; 

	public void init(DHCPClusterNode dhcpClusterNode, Properties configProperties) throws ConfigException {
		InputStream xml;
		
		if (inited) {
			throw new IllegalStateException("XmlConfigReader already inited");
		}
		if ((dhcpClusterNode == null) || (configProperties == null)) {
			throw new NullPointerException();
		}
		String xmlFilename = configProperties.getProperty(CONFIG_XML_FILE);
		logger.config("xmlResourcePath ="+xmlFilename);
		try {
			xml = new FileInputStream(xmlFilename);
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "File not found!"+xmlFilename, e);
    		throw new ConfigException("File not found!"+xmlFilename, e);
		}
    	//InputStream xml = ClassLoader.getSystemResourceAsStream("org/dhcp4java/server/config/xml/configtest.xml");

    	try {
    		parseXmlFile(xml);
    		inited = true;
    	} catch (ConfigException e) {
    		logger.log(Level.SEVERE, "config exception", e);
    	}
		// TODO
	}


	/* (non-Javadoc)
	 * @see org.dhcpcluster.config.GenericConfigReader#getFrontEndConfig()
	 */
	public FrontendConfig getFrontEndConfig() {
		if (!inited) {
			throw new IllegalStateException("Config not inited");
		}
		return frontendConfig;
	}


	/* (non-Javadoc)
	 * @see org.dhcpcluster.config.GenericConfigReader#getGlobalConfig()
	 */
	public GlobalConfig getGlobalConfig() {
		if (!inited) {
			throw new IllegalStateException("Config not inited");
		}
		return globalConfig;
	}


	/* (non-Javadoc)
	 * @see org.dhcpcluster.config.GenericConfigReader#getTopologyConfig()
	 */
	public TopologyConfig getTopologyConfig() {
		if (!inited) {
			throw new IllegalStateException("Config not inited");
		}
		return topologyConfig;
	}


	/* (non-Javadoc)
	 * @see org.dhcpcluster.config.GenericConfigReader#reloadTopologyConfig()
	 */
	public TopologyConfig reloadTopologyConfig() {
		// TODO reload the file
		return null;		// null means we don't change anything
	}


	public void parseXmlFile(InputStream xml) throws ConfigException {
		DhcpServer dhcpServerData = null;
		
		try {
			JAXBContext jc = JAXBContext.newInstance("org.dhcpcluster.config.xml.data");
			Unmarshaller u = jc.createUnmarshaller();
			dhcpServerData = (DhcpServer)u.unmarshal(xml);
		} catch (JAXBException e) {
			logger.log(Level.SEVERE, "XML Parsing error", e);
			throw new ConfigException("XML Parsing error", e);
		}
		// ready to read data in memory
		
		// front-end data
		this.frontendConfig = XmlFrontEndConfigReader.parseFrontEnd(dhcpServerData.getFrontEnd());
		this.globalConfig = XmlGlobalConfigReader.xmlGlobalConfigReader(dhcpServerData.getGlobal());
		this.topologyConfig = TopologyConfigReader.xmlTopologyReader(dhcpServerData.getTopology());
		
		return;
//    	try {
//			Builder parser = new Builder();
//			Document doc = parser.build(xml);
//
//			Element root = doc.getRootElement();
//			if (!"dhcp-server".equals(root.getLocalName())) {
//				throw new ConfigException("root node is not dhcp-server but "+root.getLocalName());
//			}
//			
//			// parse "front-end" element
//			Elements frontendElts = root.getChildElements("front-end");
//			if (frontendElts.size() != 1) {
//				throw new ConfigException("One 'front-end' element expected, found "+frontendElts.size());
//			}
//			this.frontendConfig = XmlFrontEndConfigReader.xmlFrontEndConfigReader(frontendElts.get(0));
//			//FrontendConfig frontendConfig = FrontendConfigReader...
//			
//			// parse "global" element
//			Elements globalElts = root.getChildElements("global");
//			if (globalElts.size() != 1) {
//				throw new ConfigException("One 'global' element expected, found "+globalElts.size());
//			}
//			this.globalConfig = XmlGlobalConfigReader.xmlGlobalConfigReader(globalElts.get(0));
//			
//			// parse "topology" element
//			Elements topologyElts = root.getChildElements("topology");
//			if (topologyElts.size() != 1) {
//				throw new ConfigException("One 'subnets' element expected, found "+topologyElts.size());
//			}
//			this.topologyConfig = TopologyConfigReader.xmlTopologyReader(topologyElts.get(0));
//    	} catch (ConfigException e) {
//    		throw e;		// re-throw
//    	} catch (Exception e) {
//    		logger.log(Level.WARNING, "global exception", e);
//    		throw new ConfigException("global exception", e);
//    	}
    }

	
	private static final String CONFIG_XML_FILE = "config.xml.file";

}
