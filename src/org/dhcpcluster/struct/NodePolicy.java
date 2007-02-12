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

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class NodePolicy implements Serializable {

    private static final long serialVersionUID = 3L;
	
	private int							defaultLease = 86400;
    private int							maxLease = 86400;
    
	/**
	 * @return Returns the defaultLease.
	 */
	public int getDefaultLease() {
		return defaultLease;
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
		return maxLease;
	}
	/**
	 * @param maxLease The maxLease to set.
	 */
	public void setMaxLease(int maxLease) {
		this.maxLease = maxLease;
	}

    
}
