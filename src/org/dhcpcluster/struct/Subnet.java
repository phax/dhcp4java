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
package org.dhcpcluster.struct;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.HardwareAddress;
import org.dhcp4java.InetCidr;
import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.filter.RequestFilter;
/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class Subnet extends NodeRoot implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(Subnet.class.getName().toLowerCase());
    
    /** network range of the subnet = CIDR */
    protected final InetCidr cidr;
    
    /** giaddr pointing to this Subnet */
    protected final Collection<InetAddress> giaddrs = new LinkedList<InetAddress>();
    
    /** list of address ranges, sorted */
    protected final SortedSet<AddressRange> addrRanges = new TreeSet<AddressRange>();

    /** list of static addresses already assigned */
    protected final Map<HardwareAddress, InetAddress> staticAddressesByMac = new HashMap<HardwareAddress, InetAddress>();
    protected final Map<InetAddress, HardwareAddress> staticAddressesByIp = new HashMap<InetAddress, HardwareAddress>();
    
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

	public void addAddrRange(AddressRange range) {
		addrRanges.add(range);
	}
	/**
	 * @return Returns the addrRanges.
	 */
	public SortedSet<AddressRange> getAddrRanges() {
		return addrRanges;
	}

	/**
	 * @return Returns the dhcpOptions.
	 */
	public DHCPOption[] getDhcpOptions() {
		return dhcpOptions;
	}

	/**
	 * @param dhcpOptions The dhcpOptions to set.
	 */
	public void setDhcpOptions(DHCPOption[] dhcpOptions) {
		this.dhcpOptions = dhcpOptions;
	}

	public void addStaticAddress(HardwareAddress macAddr, InetAddress ipAddr) throws ConfigException {
		if (macAddr == null) {
			throw new NullPointerException("hardwareAddr is null");
		}
		if (ipAddr == null) {
			throw new NullPointerException("ipAddr is null");
		}
		if (!(ipAddr instanceof Inet4Address)) {
			throw new IllegalArgumentException("ipAddr is not IPv4 address");
		}
		
		// check if address is already assigned
		if (staticAddressesByIp.containsKey(ipAddr)) {
			throw new ConfigException("static ip ["+ipAddr.getHostAddress()+"] is already used");
		}
		
		// check if mac address is already assigned
		if (staticAddressesByMac.containsKey(macAddr)) {
			logger.warning("Hardware address ["+macAddr+"]already has an IP address statically assigned");
		}
		
		// assign address
		staticAddressesByIp.put(ipAddr, macAddr);
		staticAddressesByMac.put(macAddr, ipAddr);
	}
	
	public InetAddress getStaticAddress(HardwareAddress mac) {
		return staticAddressesByMac.get(mac);
	}

	/**
	 * @return Returns the requestFilter.
	 */
	public RequestFilter getRequestFilter() {
		return requestFilter;
	}

	/**
	 * @param requestFilter The requestFilter to set.
	 */
	public void setRequestFilter(RequestFilter requestFilter) {
		this.requestFilter = requestFilter;
	}
	
	public void addGiaddr(InetAddress giaddr) {
		if (giaddr == null) {
			throw new NullPointerException();
		}
		if (!(giaddr instanceof Inet4Address)) {
			throw new IllegalArgumentException("Only IPv4 address allowed");
		}
		this.giaddrs.add(giaddr);
	}
	
}
