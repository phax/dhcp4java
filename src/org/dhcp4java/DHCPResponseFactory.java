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
package org.dhcp4java;

import static org.dhcp4java.DHCPConstants.DHCPACK;
import static org.dhcp4java.DHCPConstants.DHCPNAK;
import static org.dhcp4java.DHCPConstants.DHCPOFFER;
import static org.dhcp4java.DHCPConstants.INADDR_ANY;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * This class provides some standard factories for DHCP responses.
 * 
 * <p>This simplifies DHCP Server development as basic behaviour is already usable
 * as-is.
 * 
 * @author Stephan Hadinger
 *
 */
public final class DHCPResponseFactory {

    /**
     * Calculates the addres/port to which the response must be sent, according to
     * rfc 2131, section 4.1.
     * 
     * <p>This is a method ready to use for *standard* behaviour for any RFC
     * compliant DHCP Server.
     * 
     * <p>If <tt>giaddr</tt> is null, it is the client's addres/68, otherwise
     * giaddr/67.
     * 
     * <p>Standard behaviour is to set the response packet as follows:
     * <pre>
     * 		response.setAddrPort(getDefaultSocketAddress(request), response.getOp());
     * </pre>
     *  
     * @param request the client DHCP request
     * @param responseType the DHCP Message Type the servers wants to send (DHCPOFFER,
     * 							DHCPACK, DHCPNAK)
     * @return the ip/port to send back the response
     * @throws IllegalArgumentException if request is <tt>null</tt>.
     * @throws IllegalArgumentException if responseType is not valid.
     */
    public static InetSocketAddress getDefaultSocketAddress(DHCPPacket request, byte responseType) {
    	if (request == null) {
    		throw new IllegalArgumentException("request is null");
    	}
    	InetSocketAddress sockAdr;
    	InetAddress giaddr = request.getGiaddr();
    	InetAddress ciaddr = request.getCiaddr();
    	// check whether there is a giaddr
    	
    	switch (responseType) {
    	case DHCPOFFER:
    	case DHCPACK:
        	if (INADDR_ANY.equals(giaddr)) {
        		if (INADDR_ANY.equals(ciaddr)) {	// broadcast to LAN
            		sockAdr = new InetSocketAddress(request.getGiaddr(), 68);
        		} else {
        			sockAdr = new InetSocketAddress(ciaddr, 68);
        		}
        	} else {							// unicast to relay
        		sockAdr = new InetSocketAddress(giaddr, 67);
        	}
    		break;
    	case DHCPNAK:
        	if (INADDR_ANY.equals(giaddr)) {	// always broadcast
        		sockAdr = new InetSocketAddress(INADDR_ANY, 68);
        	} else {							// unicast to relay
        		sockAdr = new InetSocketAddress(giaddr, 67);
        	}
    		break;
    	default:
    		throw new IllegalArgumentException("responseType not valid");
    	}
    	return sockAdr;
    }

	
}
