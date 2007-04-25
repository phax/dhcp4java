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
public class NodeRoot implements Serializable {

    private static final long serialVersionUID = 2L;
    /** freely usable comment */
    protected String comment = null;
    
    protected String nodeType = null;
    protected String nodeId = null;
    
    protected NodePolicy	policy = null;
    
    /** filter applicable to Subnet */
    protected RequestFilter				requestFilter = ALWAYS_TRUE_FILTER;
    
    /** array of dhcp options */
    protected DHCPOption[]					dhcpOptions = DHCPOPTION_0;
    
    /** parent node in node tree */
    protected NodeRoot						parentNode = null;
    
    public NodeRoot() {
    	// empty constructor
    }
    
    public NodeRoot(NodePolicy policy) {
    	this.policy = policy;
    }
    
    /**
     * Applying DHCP options recursively from top node down to subnet leaves.
     * 
     * @param request DHCP request received from client
     * @param response DHCP response being built to send back to client
     */
    public void applyOptions(DHCPPacket request, DHCPPacket response) {
    	if (parentNode != null) {
    		parentNode.applyOptions(request, response);
    	}
    	for (DHCPOption opt : dhcpOptions) {
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
    	if (parentNode != null) {
    		if (!parentNode.isRequestAccepted(request)) {
    			return false;
    		}
    	}
    	return requestFilter.isRequestAccepted(request);
    }
    
    
    protected static final DHCPOption[] DHCPOPTION_0 = new DHCPOption[0];
    protected static final RequestFilter ALWAYS_TRUE_FILTER = new AlwaysTrueFilter();

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

	/**
	 * @return Returns the policy.
	 */
	public NodePolicy getPolicy() {
		return policy;
	}

	/**
	 * @param policy The policy to set.
	 */
	public void setPolicy(NodePolicy policy) {
		this.policy = policy;
	}

	/**
	 * @return Returns the nodeId.
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId The nodeId to set.
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return Returns the nodeType.
	 */
	public String getNodeType() {
		return nodeType;
	}

	/**
	 * @param nodeType The nodeType to set.
	 */
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * @return Returns the parentNode.
	 */
	public NodeRoot getParentNode() {
		return parentNode;
	}

	/**
	 * @param parentNode The parentNode to set.
	 */
	public void setParentNode(NodeRoot parentNode) {
		this.parentNode = parentNode;
	}
	
	
}
