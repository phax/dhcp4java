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
package org.dhcp4java.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.dhcp4java.server.config.ConfigException;
import org.dhcp4java.server.config.xml.ServerConfigSetReader;

/**
 * This is the main class for DHCP Cluster Server (FrontEnd).
 * 
 * <p>This class is responsible for loading all configuration parameters,
 * and launching the server engine.
 * 
 * <p>Bootstrap is done through a properties file.
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class DHCPClusterMain {
	
	private static final Logger logger = Logger.getLogger(DHCPClusterMain.class.getName().toLowerCase());

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
    	LogManager.getLogManager().readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));
    	InputStream xml = ClassLoader.getSystemResourceAsStream("org/dhcp4java/server/config/xml/configtest.xml");
    	ServerConfigSetReader config = new ServerConfigSetReader();
    	try {
    		config.parseXmlFile(xml);
    	} catch (ConfigException e) {
    		logger.log(Level.SEVERE, "config exception", e);
    	}
    }

}
