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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

import org.dhcp4java.server.config.ConfigException;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public final class Util {
	
    private static final Logger logger = Logger.getLogger(Util.class.getName().toLowerCase());
    
    // Suppresses default constructor, ensuring non-instantiability.
    private Util() {
    }
    
	/**
	 * Print the element's path in the document, for debugging and logging purpose
	 * 
	 * @param element base element
	 * @return the pseudo xpath of the element
	 */
	public static String getElementPath(Element element) {
		String path = "";
		Element child = element;
		Node parent = element.getParent();
		while (parent != null) {
			if (parent instanceof Element) {
				Elements children = ((Element)parent).getChildElements();
				int i;
				int count = 0;
				for (i=0; i<children.size(); i++) {
					if (children.get(i) == child) {
						path = "/"+child.getLocalName()+"["+count+"]"+path;
						break;
					}
					if (child.getLocalName().equals(children.get(i).getLocalName())) {
						count++;
					}
				}
				if (i >= children.size()) {
					path = "/[ERROR]"+path;
				}
				child = (Element)parent;
				parent = child.getParent();
			} else if (parent instanceof Document) {
				path = "/"+child.getLocalName()+path;
				break;		// we stop here !
			}
		}
		return path;
	}

    
	static String get1Attribute(Element base, String attributeName) throws ConfigException {
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
	
	static String getOptAttribute(Element base, String attributeName) {
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
	
	static Integer getOptAttributeInteger(Element base, String attributeName) {
		String value = getOptAttribute(base, attributeName);
		int res;
		
		if (value == null) {
			return null;
		}
		try {
			res = Integer.parseInt(value);
			return res;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error parsing "+attributeName+" as int", e);
			return null;
		}
	}
	
	static InetAddress getOptAttributeInetAddress(Element base, String attributeName) {
		String value = getOptAttribute(base, attributeName);
		InetAddress adr;
		
		if (value == null) {
			return null;
		}
		try {
			adr = InetAddress.getByName(value);
			if (!(adr instanceof Inet4Address)) {
				logger.log(Level.WARNING, "Only IPv4 addresses allowed "+attributeName);
				return null;
			}
			return adr;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error parsing "+attributeName+" as InetAddress", e);
			return null;
		}
	}

}
