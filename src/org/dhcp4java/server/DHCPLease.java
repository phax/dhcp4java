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
import java.util.logging.Logger;
/**
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class DHCPLease implements Serializable {
	
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DHCPLease.class.getName().toLowerCase());

    private long classId;
    private long nodeId;
    private long profileId;
    private long concentratorId;
    private long subnetId;
    private InetAddress subnetIp;
    private InetAddress maskIp;
    private InetAddress gatewayIp;
    
    public DHCPLease() {
    	
    }

	/**
	 * @return Returns the classId.
	 */
	public long getClassId() {
		return classId;
	}

	/**
	 * @param classId The classId to set.
	 */
	public void setClassId(long classId) {
		this.classId = classId;
	}

	/**
	 * @return Returns the concentratorId.
	 */
	public long getConcentratorId() {
		return concentratorId;
	}

	/**
	 * @param concentratorId The concentratorId to set.
	 */
	public void setConcentratorId(long concentratorId) {
		this.concentratorId = concentratorId;
	}

	/**
	 * @return Returns the gatewayIp.
	 */
	public InetAddress getGatewayIp() {
		return gatewayIp;
	}

	/**
	 * @param gatewayIp The gatewayIp to set.
	 */
	public void setGatewayIp(InetAddress gatewayIp) {
		this.gatewayIp = gatewayIp;
	}

	/**
	 * @return Returns the maskIp.
	 */
	public InetAddress getMaskIp() {
		return maskIp;
	}

	/**
	 * @param maskIp The maskIp to set.
	 */
	public void setMaskIp(InetAddress maskIp) {
		this.maskIp = maskIp;
	}

	/**
	 * @return Returns the nodeId.
	 */
	public long getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId The nodeId to set.
	 */
	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return Returns the profileId.
	 */
	public long getProfileId() {
		return profileId;
	}

	/**
	 * @param profileId The profileId to set.
	 */
	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}

	/**
	 * @return Returns the subnetId.
	 */
	public long getSubnetId() {
		return subnetId;
	}

	/**
	 * @param subnetId The subnetId to set.
	 */
	public void setSubnetId(long subnetId) {
		this.subnetId = subnetId;
	}

	/**
	 * @return Returns the subnetIp.
	 */
	public InetAddress getSubnetIp() {
		return subnetIp;
	}

	/**
	 * @param subnetIp The subnetIp to set.
	 */
	public void setSubnetIp(InetAddress subnetIp) {
		this.subnetIp = subnetIp;
	}
        
}
