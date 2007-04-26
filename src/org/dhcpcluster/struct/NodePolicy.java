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

import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.dhcpcluster.filter.AlwaysTrueFilter;
import org.dhcpcluster.filter.RequestFilter;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class NodePolicy implements Serializable {

    private static final long serialVersionUID = 3L;
    
    private NodePolicy						parentPolicy = null;
	
	private Integer						defaultLease = 86400;
    private Integer						maxLease = 86400;

    /** filter applicable to Subnet */
    protected RequestFilter				requestFilter = ALWAYS_TRUE_FILTER;
    
    /** array of dhcp options */
    protected DHCPOption[]					dhcpOptions = DHCPOPTION_0;
    protected DHCPOption[]					dhcpPostOptions = DHCPOPTION_0;
    
    public NodePolicy() {
    	// empty constructor
    }
    
    /**
     * Applying DHCP options.
     * 
     * @param request DHCP request received from client
     * @param response DHCP response being built to send back to client
     */
    public void applyOptions(DHCPPacket request, DHCPPacket response) {
    	if (parentPolicy != null) {
    		parentPolicy.applyOptions(request, response);
    	}
    	for (DHCPOption opt : getDhcpOptions()) {
    		response.setOption(opt.applyOption(request));
    	}
    }
    
    /**
     * Applying DHCP options.
     * 
     * @param request DHCP request received from client
     * @param response DHCP response being built to send back to client
     */
    public void applyPostOptions(DHCPPacket request, DHCPPacket response) {
    	if (parentPolicy != null) {
    		parentPolicy.applyPostOptions(request, response);
    	}
    	for (DHCPOption opt : getDhcpPostOptions()) {
    		response.setOption(opt.applyOption(request));
    	}
    }
    
    /**
     * Recursive check of requestFilter at each node level.
     * 
     * <P>Check is done top-down, i.e. root node first, then down to subnet leaves.
     * 
     * @param request DHCP request received from client
     * @return is the request to be handleds
     */
    public boolean isRequestAccepted(DHCPPacket request) {
    	if (parentPolicy != null) {
    		if (!parentPolicy.isRequestAccepted(request)) {
    			return false;
    		}
    	}
    	return getRequestFilter().isRequestAccepted(request);
    }
    
	/**
	 * @return Returns the parentPolicy.
	 */
	public NodePolicy getParentPolicy() {
		return parentPolicy;
	}

	/**
	 * @param parentPolicy The parentPolicy to set.
	 */
	public void setParentPolicy(NodePolicy parentPolicy) {
		this.parentPolicy = parentPolicy;
	}

	/**
	 * @return Returns the defaultLease.
	 */
	public int getDefaultLease() {
		return genericGetInt(defaultLease, (parentPolicy != null) ? parentPolicy.getDefaultLease() : null, DEFAULT_LEASE);
	}
	/**
	 * @param defaultLease The defaultLease to set.
	 */
	public void setDefaultLease(int defaultLease) {
		this.defaultLease = defaultLease;
	}
	/**
	 * @return Returns the maxLease.
	 */
	public int getMaxLease() {
		return genericGetInt(maxLease, (parentPolicy != null) ? parentPolicy.getMaxLease() : null, MAX_LEASE);
	}
	/**
	 * @param maxLease The maxLease to set.
	 */
	public void setMaxLease(int maxLease) {
		this.maxLease = maxLease;
	}

	protected int genericGetInt(Integer curValue, Integer parentValue, int defaultValue) {
		if (curValue != null) {
			return curValue;
		}
		if (parentValue != null) {
			return parentValue;
		}
		return defaultValue;
	}
	
	

    protected static final DHCPOption[] DHCPOPTION_0 = new DHCPOption[0];
    protected static final RequestFilter ALWAYS_TRUE_FILTER = new AlwaysTrueFilter();

    private static final int				DEFAULT_LEASE = 86400;
    private static final int				MAX_LEASE = 86400;

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

	/**
	 * @return Returns the dhcpPostOptions.
	 */
	public DHCPOption[] getDhcpPostOptions() {
		return dhcpPostOptions;
	}

	/**
	 * @param dhcpPostOptions The dhcpPostOptions to set.
	 */
	public void setDhcpPostOptions(DHCPOption[] dhcpPostOptions) {
		this.dhcpPostOptions = dhcpPostOptions;
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
}
