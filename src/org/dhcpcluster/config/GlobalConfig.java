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
package org.dhcpcluster.config;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.logging.Logger;

import org.dhcpcluster.struct.NodeRoot;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class GlobalConfig implements Serializable {
	
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GlobalConfig.class.getName().toLowerCase());

    /*
     * Private data
     */
    
    /** Comment to be used freely */
    private String comment = null;
    
    /** default lease time */
    private int defLeaseTime = 86400;
    
    /** maximum lease time */
    private int maxLeaseTime = -1;
    
    /** server identifier, i.e. the IP address of the server cluster as seen by the client
     *  This id can be overriden by the front-end identifier.
     *  */
    private InetAddress serverIdentifier = null;
    
    private final NodeRoot rootNode = new NodeRoot();
    
    /** handles options to apply post subnet */
    private final NodeRoot postNode = new NodeRoot();
    
    /**
     * Constructor of server's global configuration object.
     * 
     */
    public GlobalConfig() {
    	
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("comment=").append(comment);
		
		return buf.toString();
	}

    
	// ================================================================================
	
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
	 * @return Returns the defLeaseTime.
	 */
	public int getDefLeaseTime() {
		return defLeaseTime;
	}

	/**
	 * @param defLeaseTime The defLeaseTime to set.
	 */
	public void setDefLeaseTime(int defLeaseTime) {
		this.defLeaseTime = defLeaseTime;
	}

	/**
	 * @return Returns the maxLeaseTime.
	 */
	public int getMaxLeaseTime() {
		return maxLeaseTime;
	}

	/**
	 * @param maxLeaseTime The maxLeaseTime to set.
	 */
	public void setMaxLeaseTime(int maxLeaseTime) {
		this.maxLeaseTime = maxLeaseTime;
	}

	/**
	 * @return Returns the serverIdentifier.
	 */
	public InetAddress getServerIdentifier() {
		return serverIdentifier;
	}

	/**
	 * @param serverIdentifier The serverIdentifier to set.
	 */
	public void setServerIdentifier(InetAddress serverIdentifier) {
		this.serverIdentifier = serverIdentifier;
	}

	/**
	 * @return Returns the rootNode.
	 */
	public NodeRoot getRootNode() {
		return rootNode;
	}

	/**
	 * @return Returns the postNode.
	 */
	public NodeRoot getPostNode() {
		return postNode;
	}

    
}
