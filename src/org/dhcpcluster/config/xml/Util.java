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
package org.dhcpcluster.config.xml;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.dhcp4java.DHCPPacket;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public final class Util {
	
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Util.class);
    
    // Suppresses default constructor, ensuring non-instantiability.
    private Util() {
    	throw new UnsupportedOperationException();
    }	
	
	public static final InetAddress parseInetAddress(String lexicalIentAddress) {
		try {
			return InetAddress.getByName(lexicalIentAddress);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("UnknownHostException raised: "+lexicalIentAddress, e);
		}
	}
	
	public static final String printInetAddress(InetAddress address) {
		return address.getHostAddress();
	}
	
	public static final String parseStringItem(String lexicalStringItem) {
		if (DHCPPacket.stringToBytes(lexicalStringItem).length > 255) {
			throw new IllegalArgumentException("String length > 255, max is 255 for String item.");
		}
		return lexicalStringItem;
	}
	
	public static final String printStringItem(String str) {
		return str;
	}

}
