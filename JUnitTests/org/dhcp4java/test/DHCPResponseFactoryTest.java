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

import static org.dhcp4java.DHCPConstants.*;
import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import junit.framework.JUnit4TestAdapter;

import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPResponseFactory;
import org.junit.Test;

public class DHCPResponseFactoryTest {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DHCPResponseFactoryTest.class);
    }
	
    // test getDefaultSocketAddress
    @Test
    public void testGetDefaultSocketAddress() throws Exception {
    	InetAddress adr = InetAddress.getByName("252.10.0.200");
    	// RFC 2131 compliance
    	// sorry we ignore the broadcast bit
    	// fully broadcast by client
    	getDefaultSocketAddressTester(INADDR_ANY, INADDR_ANY, DHCPOFFER, INADDR_ANY, 68);
    	getDefaultSocketAddressTester(INADDR_ANY, INADDR_ANY, DHCPACK, INADDR_ANY, 68);
    	getDefaultSocketAddressTester(INADDR_ANY, INADDR_ANY, DHCPNAK, INADDR_ANY, 68);
    	// unicast from client
    	getDefaultSocketAddressTester(adr, INADDR_ANY, DHCPOFFER, adr, 68);
    	getDefaultSocketAddressTester(adr, INADDR_ANY, DHCPACK, adr, 68);
    	getDefaultSocketAddressTester(adr, INADDR_ANY, DHCPNAK, INADDR_ANY, 68);
    	// when though a relay
    	getDefaultSocketAddressTester(INADDR_ANY, adr, DHCPOFFER, adr, 67);
    	getDefaultSocketAddressTester(INADDR_ANY, adr, DHCPACK, adr, 67);
    	getDefaultSocketAddressTester(INADDR_ANY, adr, DHCPNAK, adr, 67);
    }


	@Test (expected=IllegalArgumentException.class)
    public void testGetDefaultSocketAddressNull() {
    	DHCPResponseFactory.getDefaultSocketAddress(null, DHCPOFFER);
    }
    @Test (expected=IllegalArgumentException.class)
    public void testGetDefaultSocketAddressBadType() throws Exception {
    	getDefaultSocketAddressTester(INADDR_ANY, INADDR_ANY, (byte)-10, INADDR_ANY, 68);
    }
    private static final void getDefaultSocketAddressTester(
    		InetAddress ciaddr, InetAddress giaddr, byte responseType,
    		InetAddress expectedAddress, int expectedPort) throws Exception {
    	DHCPPacket pac = new DHCPPacket();
    	InetSocketAddress sockAdr;
    	pac.setCiaddr(ciaddr);
    	pac.setGiaddr(giaddr);
    	sockAdr = DHCPResponseFactory.getDefaultSocketAddress(pac, responseType);
    	assertNotNull(sockAdr);
    	assertEquals(expectedAddress, sockAdr.getAddress());
    	assertEquals(expectedPort, sockAdr.getPort());
    }
}

