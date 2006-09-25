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
import org.dhcp4java.server.Subnet;
import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.GlobalConfig;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
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
			
			Element root = expect1Node(doc, "/dhcp-server");		// check root-node
			
			// parse subnets
			Nodes subnets = root.query("subnet");
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("xp:/dhcp-server/subnet, "+subnets.size()+" found");
			}
			
			for (int i=0; i<subnets.size(); i++) {
				Node subnetNode = subnets.get(i);
				//getElementPath(subnet);
				
				String address = get1Attribute(subnetNode, "address");
				logger.fine("address: "+address);
				String mask = get1Attribute(subnetNode, "mask");
				logger.fine("mask: "+mask);
				
				String comment = getOptAttribute(subnetNode, "comment");
				
				InetCidr cidr = new InetCidr(InetAddress.getByName(address),
											 InetAddress.getByName(mask));
				
				// instantiate the Subnet object
				Subnet subnet = new Subnet(cidr);
				subnet.setComment(comment);
				
				// TODO -> expect1Node
				Nodes addresses = subnetNode.query("@address");
				logger.fine("xp:@addresses "+addresses.size()+" found");
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
	 * @param query the xPath query
	 * @return the Element found
	 * @throws ConfigException	there is not 1 and only 1 element returned by the query
	 */
	private static Element expect1Node(Node base, String query) throws ConfigException {
		Nodes nodes = base.query(query);
		if (nodes == null) {
			throw new ConfigException("xp:"+query+" returned null.");
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("xp:"+query+" from "+getElementPath(base)+", returned "+nodes.size()+" node(s)");
		}
		if (nodes.size() != 1) {
			throw new ConfigException("xp:"+query+" returned "+nodes.size()+ "node(s), 1 expected");
		}
		Node expectedElement = nodes.get(0);
		if (!(expectedElement instanceof Element)) {
			throw new ConfigException("xp:"+query+" is not of type Element");
		}
		return (Element) expectedElement;
	}
	
	private static String get1Attribute(Node base, String attributeName) throws ConfigException {
		if ((base == null) || (attributeName == null)) {
			throw new NullPointerException();
		}
		if (!(base instanceof Element)) {
			throw new IllegalArgumentException("base must be of Element class");
		}
		Attribute attr = ((Element)base).getAttribute(attributeName);
		if (attr == null) {
			logger.fine("attr:"+attributeName+" from "+getElementPath(base)+", returned null");
			throw new ConfigException("attr: "+attributeName+" was not found");
		}
		return attr.getValue();
	}
	
	private static String getOptAttribute(Node base, String attributeName) {
		if ((base == null) || (attributeName == null)) {
			throw new NullPointerException();
		}
		if (!(base instanceof Element)) {
			throw new IllegalArgumentException("base must be of Element class");
		}
		Attribute attr = ((Element)base).getAttribute(attributeName);
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
