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
package org.dhcpcluster.backend.hsql;

import java.io.Serializable;

/**
 * Java mapping of the T_BUBBLE table
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class Bubble implements Serializable {

    private static final long serialVersionUID = 1L;
    

	private int			id = 0;
	private long			start = -1;
	private long			end = -1;
	private long			poolId = -1;
	
	public Bubble(int id) {
		this.id = id;
	}

	/**
	 * @return Returns the end.
	 */
	public long getEnd() {
		return end;
	}

	/**
	 * @param end The end to set.
	 */
	public void setEnd(long end) {
		this.end = end;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return Returns the poolId.
	 */
	public long getPoolId() {
		return poolId;
	}

	/**
	 * @param poolId The poolId to set.
	 */
	public void setPoolId(long poolId) {
		this.poolId = poolId;
	}

	/**
	 * @return Returns the start.
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @param start The start to set.
	 */
	public void setStart(long start) {
		this.start = start;
	}
	
}
