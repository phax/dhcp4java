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
package org.dhcp4java.server.struct;

import java.io.Serializable;
import java.util.logging.Logger;

import org.dhcp4java.DHCPPacket;
/**
 * This class represents a composite response: DHCPPacket + DHCPLease.
 * 
 * <p>Both object often go together. This class is a simple structure.
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class DHCPLeasedResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DHCPLeasedResponse.class.getName().toLowerCase());

    private DHCPPacket	response;
    private DHCPLease  lease;
    
    public DHCPLeasedResponse(DHCPPacket response, DHCPLease lease) {
    	this.response = response;
    	this.lease = lease;
    }

	/**
	 * @return Returns the lease.
	 */
	public DHCPLease getLease() {
		return lease;
	}

	/**
	 * @param lease The lease to set.
	 */
	public void setLease(DHCPLease lease) {
		this.lease = lease;
	}

	/**
	 * @return Returns the response.
	 */
	public DHCPPacket getResponse() {
		return response;
	}

	/**
	 * @param response The response to set.
	 */
	public void setResponse(DHCPPacket response) {
		this.response = response;
	}
    
}
