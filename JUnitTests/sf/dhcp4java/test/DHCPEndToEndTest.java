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
package sf.dhcp4java.test;

import java.util.Properties;

import org.dhcp4java.DHCPServer;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.DHCPStaticServlet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import junit.framework.JUnit4TestAdapter;

/**
 * @author Stephan Hadinger
 */
public class DHCPEndToEndTest {

    private static final String SERVER_ADDR = "127.0.0.1";
    private static final int    SERVER_PORT = 6767;
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DHCPEndToEndTest.class);
    }

    /**
     * Start Server.
     *
     */
    @BeforeClass
    public static void startServer() throws DHCPServerInitException {
        Properties localProperties = new Properties();

        localProperties.put(DHCPServer.SERVER_ADDRESS, SERVER_ADDR + ':' + SERVER_PORT);
        localProperties.put(DHCPServer.SERVER_THREADS, "1");

        DHCPServer server = DHCPServer.initServer(new DHCPStaticServlet(), localProperties);

        new Thread(server).start();
    }

    @Test
    public void testDiscover() {

    }

    @AfterClass
    public static void shutdownServer() {

    }
}
