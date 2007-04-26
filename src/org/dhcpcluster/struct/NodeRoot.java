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
import org.dhcp4java.DHCPPacket;

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
    
    protected final NodePolicy	policy = new NodePolicy();
    
    
    /** parent node in node tree */
    protected NodeRoot						parentNode = null;
    
    public NodeRoot() {
    	// empty constructor
    }
    
    /**
     * Applying DHCP options recursively from top node down to subnet leaves.
     * 
     * @param request DHCP request received from client
     * @param response DHCP response being built to send back to client
     */
    public void applyOptions(DHCPPacket request, DHCPPacket response) {
    	policy.applyOptions(request, response);
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
    	return policy.isRequestAccepted(request);
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
	 * @return Returns the policy.
	 */
	public NodePolicy getPolicy() {
		return policy;
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
		policy.setParentPolicy(parentNode.getPolicy());
	}
	
}
