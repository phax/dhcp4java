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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.GlobalConfig;
import org.dhcp4java.server.config.TopologyConfig;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.60
 */
public final class GlobalConfigReader {

    private static final Logger logger = Logger.getLogger(GlobalConfigReader.class.getName().toLowerCase());

    
	public static GlobalConfig xmlGlobalConfigReader(Element globalElt) throws ConfigException {
//		try {
			
			GlobalConfig globalConfig = new GlobalConfig();

			return globalConfig;
//		} catch (ParsingException e) {
//			logger.log(Level.FINE, "parsing exception", e);
//			throw new ConfigException("Parsing exception in XOM", e);
//		} catch (IOException e) {
//			logger.log(Level.FINE, "ioerror", e);
//			throw new ConfigException("IO exception", e);
//		}
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
	
}
