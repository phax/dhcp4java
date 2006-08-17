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
package org.dhcp4java.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

import junit.framework.JUnit4TestAdapter;

import org.dhcp4java.DHCPServer;
import org.dhcp4java.DHCPServlet;
import org.dhcp4java.DHCPServerInitException;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class DHCPServerTest {

    private static final String SERVER_ADDR = "127.0.0.1";
    private static final int    SERVER_PORT = 6767;
    
    private DHCPServer server = null;
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DHCPServerTest.class);
    }
    
    @After
    public void stopServer() {
    	if (server != null) {
    		server.stopServer();
    		server = null;
    	}
    }
    
    @Test (expected=IllegalArgumentException.class)
    public void testInitServerNull() throws Exception {
    	DHCPServer.initServer(null, null);
    }
    @Test (expected=DHCPServerInitException.class)
    public void testInitServerPortAlreadyInUse() throws Exception {
        Properties localProperties = new Properties();

        localProperties.put(DHCPServer.SERVER_ADDRESS, SERVER_ADDR + ':' + SERVER_PORT);
        localProperties.put(DHCPServer.SERVER_THREADS, "1");
        
        server = DHCPServer.initServer(new DHCPServerTestServlet(), localProperties);
        DHCPServer.initServer(new DHCPServerTestServlet(), localProperties);
        
    }
    
    @Test
    public void testInitServerNullProps() throws Exception {
    	DHCPServer server = DHCPServer.initServer(new DHCPServerTestServlet(), null);
    	assertNotNull(server);
    	server.stopServer();
    }
    
    @Test
    public void testInitServer() throws Exception {
        Properties localProperties = new Properties();

        localProperties.put(DHCPServer.SERVER_ADDRESS, SERVER_ADDR + ':' + SERVER_PORT);
        localProperties.put(DHCPServer.SERVER_THREADS, "1");

        DHCPServer server = DHCPServer.initServer(new DHCPServerTestServlet(), localProperties);
        new Thread(server).start();
        synchronized (this) {
        	wait(300);
        }
        server.stopServer();
    }
    
    // parseSocketAddress
    @Test
    public void testParseSocketAddress() throws Exception {
    	assertEquals(new InetSocketAddress(InetAddress.getByName("254.10.220.0"), 67),
    				 DHCPServer.parseSocketAddress("254.10.220.0:67"));
    }
    @Test (expected=IllegalArgumentException.class)
    public void testParseSocketAddressNull() {
    	DHCPServer.parseSocketAddress(null);
    }
    @Test (expected=IllegalArgumentException.class)
    public void testParseSocketAddressNoSemicolon() {
    	DHCPServer.parseSocketAddress("254.10.220.0/67");
    }
//    @Test (expected=IllegalArgumentException.class)
//    public void testParseSocketAddressBadAddress() {
//    	InetSocketAddress sockadr = DHCPServer.parseSocketAddress("rubish:67");
//    	System.out.println(sockadr);
//    }

}

class DHCPServerTestServlet extends DHCPServlet {
	
	
	
}