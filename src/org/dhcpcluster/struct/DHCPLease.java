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

import org.apache.log4j.Logger;

/**
 * This class represent a DHCP Lease given to a client.
 * 
 * <p>This class is basically a databean and has very little business logic.
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class DHCPLease implements Serializable {
	
    private static final long serialVersionUID = 2L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DHCPLease.class);

    public enum Status {
    	
    	FREE(0),
    	OFFERED(1),
    	USED(2),
    	ABANDONED(-1);
    	
    	private final int code;
    	
    	Status(int code) {
    		this.code = code;
    	}
    	public int getCode() {
    		return code;
    	}
    	public static Status fromInt(int code) {
    		for (Status s : Status.values()) {
    			if (s.code == code) {
    				return s;
    			}
    		}
    		throw new IllegalArgumentException("code="+code+" is not a valid DHCPLease.Status");
    	}
    }
    
    private long creationDate;
    private long updateDate;
    private long expirationDate;
    private long recycleDate;
    
    private long ip;
    private byte[] mac;
    private String uid = null;
    private Status status;
//
//	Date creationDate = res.getDate("CREATION_DATE");
//	Date updateDate = res.getDate("UPDATE_DATE");
//	Date expirationDate = res.getDate("EXPIRATION_DATE");
    
    private long classId;
    private long nodeId;
    private long profileId;
    private long concentratorId;
    private long subnetId;
    private long subnetIp;
    private long maskIp;
    private long gatewayIp;
    
    public DHCPLease() {
    	// empty constructor
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
	 * @return Returns the concentratorId.		// TODO what is a concentratorId ?
	 */
	public long getConcentratorId() {
		return concentratorId;
	}

	/**
	 * @param concentratorId The concentratorId to set.		// TODO what is a concentratorId ?
	 */
	public void setConcentratorId(long concentratorId) {
		this.concentratorId = concentratorId;
	}

	/**
	 * @return Returns the gatewayIp.			// gateway (giaddr) associated to the Lease
	 */
	public long getGatewayIp() {
		return gatewayIp;
	}

	/**
	 * @param gatewayIp The gatewayIp to set.
	 */
	public void setGatewayIp(long gatewayIp) {
		this.gatewayIp = gatewayIp;
	}

	/**
	 * @return Returns the maskIp.
	 */
	public long getMaskIp() {
		return maskIp;
	}

	/**
	 * @param maskIp The maskIp to set.
	 */
	public void setMaskIp(long maskIp) {
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
	public long getSubnetIp() {
		return subnetIp;
	}

	/**
	 * @param subnetIp The subnetIp to set.
	 */
	public void setSubnetIp(long subnetIp) {
		this.subnetIp = subnetIp;
	}

	/**
	 * @return Returns the creationDate.
	 */
	public long getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate The creationDate to set.
	 */
	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return Returns the expirationDate.
	 */
	public long getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate The expirationDate to set.
	 */
	public void setExpirationDate(long expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return Returns the recycleDate.
	 */
	public long getRecycleDate() {
		return recycleDate;
	}

	/**
	 * @param recycleDate The recycleDate to set.
	 */
	public void setRecycleDate(long recycleDate) {
		this.recycleDate = recycleDate;
	}

	/**
	 * @return Returns the updateDate.
	 */
	public long getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate The updateDate to set.
	 */
	public void setUpdateDate(long updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * @return Returns the ip.
	 */
	public long getIp() {
		return ip;
	}

	/**
	 * @param ip The ip to set.
	 */
	public void setIp(long ip) {
		this.ip = ip;
	}

	/**
	 * @return Returns the mac.
	 */
	public byte[] getMac() {
		return mac;
	}
	public String getMacHex() {
		return bytesToHex(mac);
	}

	/**
	 * @param mac The mac to set.
	 */
	public void setMac(byte[] mac) {
		this.mac = mac;
	}
	public void setMacHex(String s) {
		this.mac = hex2Bytes(s);
	}

	/**
	 * @return Returns the status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return Returns the uid.
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid The uid to set.
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}
	

    /**
     * Converts byte to hex string (2 chars) (uppercase)
     */
    private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    private static final String bytesToHex(byte[] buf) {
    	if (buf ==  null) {
    		return "";
    	}
    	StringBuffer sbuf = new StringBuffer(buf.length*2);
    	for (int k=0; k<buf.length; k++) {
    		int i = (buf[k] & 0xFF);
    		sbuf.append(hex[(i & 0xF0) >> 4])
            	.append(hex[i & 0x0F]);
    	}
    	return sbuf.toString();
    }
    /**
     * Convert hes String to byte[]
     */
    private static final byte[] hex2Bytes(String s) {
    	if (s == null) {
    		return null;
    	}
    	int len = s.length();
        if ((len & 1) != 0) {
            len--;
        }

        byte[] buf = new byte[len / 2];

        for (int index = 0; index < len; index++) {
            final int stringIndex = index << 1;
            buf[index] = (byte) Integer.parseInt(s.substring(stringIndex, stringIndex + 2), 16);
        }
        return buf;
    }

        
}
