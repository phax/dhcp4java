/*
 *	This file is part of dhcp4java.
 *
 *	dhcp4java is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	dhcp4java is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Foobar; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * (c) 2006 Stephan Hadinger
 */
package sf.dhcp4java.test;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sf.dhcp4java.DHCPServer;
import sf.dhcp4java.DHCPServerInitException;
import sf.dhcp4java.examples.DHCPStaticServer;

import junit.framework.JUnit4TestAdapter;

/**
 * @author yshi7355
 *
 */
public class DHCPEndToEndTest {

    private static final String SERVER_ADDR = "127.0.0.1";
    private static final int SERVER_PORT = 6767;
    private static final String CLIENT_ADDR = "127.0.0.1";
    private static final int CLIENT_PORT = 6868;
    
	public static junit.framework.Test suite() {
		  return new JUnit4TestAdapter(DHCPEndToEndTest.class);    
		}
	/**
	 * Start Server.
	 *
	 */
	@BeforeClass
	public void startServer() throws DHCPServerInitException {
	    Properties localProperties = new Properties();
	    localProperties.put(DHCPServer.SERVER_ADDRESS, SERVER_ADDR+":"+SERVER_PORT);
	    localProperties.put(DHCPServer.SERVER_THREADS, "1");
        DHCPServer server = DHCPServer.initServer(new DHCPStaticServer(), localProperties);
        new Thread(server).start();
	}
	
	@Test
	public void testDiscover() {
		
	}
	
	@AfterClass
	public void shutdownServer() {
		
	}

}
