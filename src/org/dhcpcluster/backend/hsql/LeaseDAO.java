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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dhcpcluster.struct.DHCPLease;

import static org.apache.commons.dbutils.DbUtils.closeQuietly;
import static org.dhcpcluster.backend.hsql.HsqlBackendServer.queries;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class LeaseDAO {

	private static final Logger logger = Logger.getLogger(LeaseDAO.class.getName().toLowerCase());
	
	private Connection conn;

	public LeaseDAO(Connection conn) {
		if (conn == null) {
			throw new NullPointerException();
		}
		this.conn = conn;
	}
	
	public void close() {
		closeQuietly(conn);
		conn = null;
	}
	
	/**
	 * 
	 * @param ip primary key, IP address as <tt>long</tt>
	 * @return the lease found in DB or <tt>null</tt> if none found
	 */
	public DHCPLease getLease(long ip) {
		assert(conn != null);
		PreparedStatement pst = null;
		ResultSet res = null;
		try {
			pst = conn.prepareStatement(SELECT_LEASE);
			pst.setLong(1, ip);
			res = pst.executeQuery();
			if (res.next()) {
				return makeDHCPLease(res);
			} else {
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("Getting lease for ip="+ip+", no result found");
				}
				return null;
			}
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Unexpected SQLException when getting Lease", e);
			return null;
		} finally {
			closeQuietly(res);
			closeQuietly(pst);
		}
	}
	
	public List<DHCPLease> getLeasesFromRanges(long start, long end) {
		assert(conn != null);
		PreparedStatement pst = null;
		ResultSet res = null;
		try {
			pst = conn.prepareStatement(SELECT_LEASE);
			pst.setLong(1, start);
			pst.setLong(2, end);
			res = pst.executeQuery();
			List<DHCPLease> leases = new ArrayList<DHCPLease>();
			while (res.next()) {
				leases.add(makeDHCPLease(res));
			}
			return leases;
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Unexpected SQLException when getting Lease", e);
			return null;
		} finally {
			closeQuietly(res);
			closeQuietly(pst);
		}
	}
	
	public DHCPLease makeDHCPLease(ResultSet res) throws SQLException {
		if (res == null) {
			throw new NullPointerException();
		}
		DHCPLease lease = new DHCPLease();
		lease.setIp(res.getLong("IP"));
		lease.setCreationDate(res.getDate("CREATION_DATE"));
		lease.setUpdateDate(res.getDate("UPDATE_DATE"));
		lease.setExpirationDate(res.getDate("EXPIRATION_DATE"));
		//lease.setMac(res.getString("MAC"));
		//lease.setUid(res.getString("UID"));
		lease.setStatus(DHCPLease.Status.fromInt(res.getInt("STATUS")));
		
		return null;
	}

	private static final String	SELECT_LEASE = queries.get("SELECT_LEASE");
	private static final String	SELECT_LEASE_RANGE = queries.get("SELECT_LEASE_RANGE");
	private static final String	INSERT_LEASE = queries.get("INSERT_LEASE");

}
