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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.InetCidr;
import org.dhcp4java.server.config.GlobalConfig;
/**
 * 
 * @author Stephan Hadinger
 * @version 0.60
 */
public class Subnet implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(Subnet.class.getName().toLowerCase());

    /** freely usable comment */
    private String comment = null;
    
    /** network range of the subnet = CIDR */
    private final InetCidr cidr;
    
    /** giaddr pointing to this Subnet */
    private final Collection<InetAddress> giaddrs = new LinkedList<InetAddress>();
    
    /** list of address ranges, sorted */
    private SortedSet<AddressRange> adrRanges = new TreeSet<AddressRange>();
    
    /** list of dhcp options */
    private List<DHCPOption> dhcpOptions = new LinkedList<DHCPOption>();

    public Subnet(InetCidr cidr) {
    	if (cidr == null) {
    		throw new NullPointerException();
    	}
    	this.cidr = cidr;
    }
    
    /**
     * 
     */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("comment=").append(comment);
		
		return buf.toString();
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return Returns the giaddrs.
	 */
	public Collection<InetAddress> getGiaddrs() {
		return giaddrs;
	}

	/**
	 * @return Returns the cidr.
	 */
	public InetCidr getCidr() {
		return cidr;
	}
    
    
}
