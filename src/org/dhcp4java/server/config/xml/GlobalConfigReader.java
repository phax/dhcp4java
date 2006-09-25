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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.GlobalConfig;

import nu.xom.Builder;
import nu.xom.Document;
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
			
			Nodes root = doc.query("/dhcp-server");
			if (root.size() != 1) {
				throw new ConfigException("no root element: dhcp-server");
			}
			
			// parse subnets
			Nodes subnets = doc.query("/dhcp-server/subnet");
			logger.fine("xp:/dhcp-server/subnet, "+subnets.size()+" found");
			
			for (int i=0; i<subnets.size(); i++) {
				Node subnet = subnets.get(i);
				
				// TODO -> expect1Node
				Nodes addresses = subnet.query("@address");
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
