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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.dhcp4java.InetCidr;
import org.dhcp4java.server.AddressRange;
import org.dhcp4java.server.Subnet;
import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.GlobalConfig;
import org.dhcp4java.server.config.TopologyConfiguration;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.60
 */
public final class GlobalConfigReader {

    private static final Logger logger = Logger.getLogger(GlobalConfigReader.class.getName().toLowerCase());

	
	public GlobalConfigReader() {
		
	}
	
	public static GlobalConfig XmlConfigReader(InputStream xml) throws ConfigException {
		try {
			Builder parser = new Builder();
			Document doc = parser.build(xml);
			
			//GlobalConfig globalConfig = new GlobalConfig();
			TopologyConfiguration topologyConfiguration = new TopologyConfiguration();
			
			Element root = doc.getRootElement();
			if (!"dhcp-server".equals(root.getLocalName())) {
				throw new ConfigException("root node is not dhcp-server but "+root.getLocalName());
			}
			
			// parse subnets
			Elements subnets = root.getChildElements("subnet");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("subnet: "+subnets.size()+" found");
			}
			
			for (int i=0; i<subnets.size(); i++) {
				Subnet subnet = null;
				try {
					Element subnetElt = subnets.get(i);
					//getElementPath(subnet);
					
					String address = get1Attribute(subnetElt, "address");
					logger.finest("address: "+address);
					String mask = get1Attribute(subnetElt, "mask");
					logger.finest("mask: "+mask);
					
					String comment = getOptAttribute(subnetElt, "comment");
					
					InetCidr cidr = new InetCidr(InetAddress.getByName(address),
												 InetAddress.getByName(mask));
					
					// instantiate the Subnet object
					subnet = new Subnet(cidr);
					subnet.setComment(comment);
					
					// check for giaddrs
					Elements giaddrs = subnetElt.getChildElements("giaddr");
					
					for (int j=0; j<giaddrs.size(); j++) {
						Element giaddr = giaddrs.get(j);
						subnet.getGiaddrs().add(InetAddress.getByName(giaddr.getValue()));
					}
					
					// look for ranges
					Elements ranges = subnetElt.getChildElements("range");
					for (int j=0; j<ranges.size(); j++) {
						AddressRange range = null;
						try {
							Element rangeElt = ranges.get(j);
							String rangeStart = rangeElt.getAttributeValue("start");
							String rangeEnd = rangeElt.getAttributeValue("end");
							if (rangeStart == null) {
								throw new ConfigException("range @start missing in "+getElementPath(rangeElt));
							}
							if (rangeEnd == null) {
								throw new ConfigException("range @end missing in "+getElementPath(rangeElt));
							}
							if (logger.isLoggable(Level.FINEST)) {
								logger.finest("range start: "+rangeStart+", range end: "+rangeEnd+", from "+getElementPath(rangeElt));
							}
							
							range = new AddressRange(InetAddress.getByName(rangeStart),
													 InetAddress.getByName(rangeEnd));
						} catch (ConfigException e) {
							logger.log(Level.WARNING, "address range is invalide", e);
						} finally {
							if (range != null) {
								subnet.getAddrRanges().add(range);
							}
						}
					}

				} catch (ConfigException e) {
					logger.log(Level.WARNING, "error reading subnet configuration", e);
				} finally {
					// do we do something with the subnet ?
					if (subnet != null) {
						topologyConfiguration.addSubnet(subnet);
					}
				}
			}
			
			return null;
		} catch (ParsingException e) {
			logger.log(Level.FINE, "parsing exception", e);
			throw new ConfigException("Parsing exception in XOM", e);
		} catch (IOException e) {
			logger.log(Level.FINE, "ioerror", e);
			throw new ConfigException("IO exception", e);
		}
	}
	
	public static String getElementPath(Node element) {
		String path = "/";
		Node child = element;
		Node parent = element.getParent();
		while (parent != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				if (parent.getChildCount() == 0) {
					return "[ERROR]";
				}
				int i;
				int count = -1;
				for (i=0; i<parent.getChildCount(); i++) {
					Node iterChild = parent.getChild(i);
					if (iterChild instanceof Element) {
						count++;
						if (parent.getChild(i) == child) {
							path = "/"+childElement.getLocalName()+"["+count+"]"+path;	// TODO
							break;
						}	
					}
				}
				if (i >= parent.getChildCount()) {
					path = "/[ERROR]"+path;
				}
			} else {
				path = "/{"+parent.getClass().getCanonicalName()+"}"+path;
			}
			child = parent;
			parent = child.getParent();
		}
		return path;
	}
	
	/**
	 * Execute the xPath query and return if there is one and only one Element
	 * as a result.
	 * 
	 * @param base base Node from which to execute the query
	 * @param name the name of the element
	 * @return the Element found
	 * @throws ConfigException	there is not 1 and only 1 element returned by the query
	 */
	private static Element expect1Node(Element base, String name) throws ConfigException {
		Elements elts = base.getChildElements(name);
		if (elts == null) {
			throw new NullPointerException("getChildElements returned null");
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.finest("element: "+name+" from "+getElementPath(base)+", returned "+elts.size()+" node(s)");
		}
		if (elts.size() != 1) {
			throw new ConfigException("xp: "+name+" returned "+elts.size()+ "node(s), 1 expected");
		}
		return elts.get(0);
	}
	
	private static String get1Attribute(Element base, String attributeName) throws ConfigException {
		if ((base == null) || (attributeName == null)) {
			throw new NullPointerException();
		}
		Attribute attr = base.getAttribute(attributeName);
		if (attr == null) {
			logger.fine("attr:"+attributeName+" from "+getElementPath(base)+", returned null");
			throw new ConfigException("attr: "+attributeName+" was not found");
		}
		return attr.getValue();
	}
	
	private static String getOptAttribute(Element base, String attributeName) {
		if ((base == null) || (attributeName == null)) {
			throw new NullPointerException();
		}
		Attribute attr = base.getAttribute(attributeName);
		if (attr == null) {
			return null;
		} else {
			return attr.getValue();			
		}
	}

    public static void main(String[] args) throws IOException {
    	LogManager.getLogManager().readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));
//    	logger.setLevel(Level.ALL);
//    	for (Handler handler : logger.getHandlers()) {
//    		handler.setLevel(Level.ALL);
//    	}
    	InputStream xml = ClassLoader.getSystemResourceAsStream("org/dhcp4java/server/config/configtest.xml");
    	try {
    		XmlConfigReader(xml);
    	} catch (ConfigException e) {
    		logger.log(Level.SEVERE, "config exception", e);
    	}
    }
}
