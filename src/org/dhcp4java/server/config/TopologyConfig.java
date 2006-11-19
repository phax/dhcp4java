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
package org.dhcp4java.server.config;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.dhcp4java.InetCidr;
import org.dhcp4java.server.Subnet;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class TopologyConfig implements Serializable {
	
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(GlobalConfig.class.getName().toLowerCase());

    /** list of subnets hashed by their cidr */
    private final Map<InetCidr, Subnet> subnetsByCidr = new HashMap<InetCidr, Subnet>();
    
    /** lowest mask value in all subnets declared */
    private int lowestMask = 32;
    
    /** highest mask value in all subnets declared */
    private int highestMask = -1;
    
    /** provide a fast search for subnets via an associated giaddr */
    private final Map<InetAddress, Subnet> subnetsByGiaddr = new HashMap<InetAddress, Subnet>();
    
    /**
     * Constructor
     *
     */
    public TopologyConfig() {
    	
    }
    
    public Subnet findSubnetByCidr(InetCidr cidr) {
    	return subnetsByCidr.get(cidr);
    }
    
    public Subnet findSubnetByGiaddr(InetAddress giaddr) {
    	return subnetsByGiaddr.get(giaddr);
    }
    
    public void addSubnet(Subnet subnet) throws ConfigException {
    	if (subnet == null) {
    		throw new NullPointerException("subnet is null");
    	}
    	// TODO check consistence with already existing subnets
    	subnetsByCidr.put(subnet.getCidr(), subnet);
    	int mask = subnet.getCidr().getMask();
    	if (mask < lowestMask) {
    		lowestMask = mask;
    	}
    	if (mask > highestMask) {
    		highestMask = mask;
    	}
    	// check for giaddrs to hash
    	for (InetAddress giaddr : subnet.getGiaddrs()) {
    		if (subnetsByGiaddr.containsKey(giaddr)) {
    			throw new ConfigException("giaddr: "+giaddr.getHostName()+" already present in subnet "+
    					subnetsByGiaddr.get(giaddr).getCidr());
    		}
    		subnetsByGiaddr.put(giaddr, subnet);
    	}
    }
}
