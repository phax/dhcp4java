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

import org.apache.log4j.Logger;
/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class BackendConfig implements Serializable {

    private static final long serialVersionUID = 2L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BackendConfig.class);

    private String			hsqlAddress = null;
    private String			hsqlDbName = null;
    private String			hsqlDbPath = null;
    private int			hsqlDbNumber = 0;
    
    public BackendConfig() {
    	// empty constructor
    }

	/**
	 * @return Returns the hsqlAddress.
	 */
	public String getHsqlAddress() {
		return hsqlAddress;
	}

	/**
	 * @param hsqlAddress The hsqlAddress to set.
	 */
	public void setHsqlAddress(String hsqlAddress) {
		this.hsqlAddress = hsqlAddress;
	}

	/**
	 * @return Returns the hsqlDbName.
	 */
	public String getHsqlDbName() {
		return hsqlDbName;
	}

	/**
	 * @param hsqlDbName The hsqlDbName to set.
	 */
	public void setHsqlDbName(String hsqlDbName) {
		this.hsqlDbName = hsqlDbName;
	}

	/**
	 * @return Returns the hsqlDbPath.
	 */
	public String getHsqlDbPath() {
		return hsqlDbPath;
	}

	/**
	 * @param hsqlDbPath The hsqlDbPath to set.
	 */
	public void setHsqlDbPath(String hsqlDbPath) {
		this.hsqlDbPath = hsqlDbPath;
	}

	/**
	 * @return Returns the hsqlDbNumber.
	 */
	public int getHsqlDbNumber() {
		return hsqlDbNumber;
	}

	/**
	 * @param hsqlDbNumber The hsqlDbNumber to set.
	 */
	public void setHsqlDbNumber(int hsqlDbNumber) {
		this.hsqlDbNumber = hsqlDbNumber;
	}
    
}
