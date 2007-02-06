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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryLoader;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import org.dhcp4java.Util;
import org.dhcpcluster.struct.AddressRange;
import org.dhcpcluster.struct.DHCPLease;
import org.dhcpcluster.struct.Subnet;

import static org.apache.commons.dbutils.DbUtils.closeQuietly;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class DataAccess {

	private static final Logger logger = Logger.getLogger(DataAccess.class.getName().toLowerCase());
	
	private static QueryRunner qRunner = new QueryRunner();
	
	private DataAccess() {
		throw new UnsupportedOperationException();
	}
	
	public static void shutdown(Connection conn) throws SQLException {
		assert(conn != null);
		qRunner.update(conn, SHUTDOWN);
	}
	
	public static void deletePools(Connection conn) throws SQLException {
		assert(conn != null);
		int res = qRunner.update(conn, DELETE_T_POOL);
		logger.fine("Delete all from T_POOL: "+res+" deleted");
	}
	
	public static void deletePoolSets(Connection conn) throws SQLException {
		assert(conn != null);
		int res = qRunner.update(conn, DELETE_T_POOL_SET);
		logger.fine("Delete all from T_POOL_SET: "+res+" deleted");
	}
	
	public static void insertPoolsAndPoolSets(Connection conn, Collection<Subnet> subnetColl) throws SQLException {
		assert(conn != null);
		PreparedStatement pstPoolSet = null;
		PreparedStatement pstPool = null;

		try {
			pstPoolSet = conn.prepareStatement(INSERT_T_POOL_SET);
			pstPool = conn.prepareStatement(INSERT_T_POOL);
			
			for (Subnet subnet : subnetColl) {
				int res;
				pstPoolSet.setLong(1, subnet.getCidr().toLong());
				res = pstPoolSet.executeUpdate();
				if (res != 1) {
					throw new SQLException("Cannot update T_POOL_SET");
				}
				for (AddressRange range : subnet.getAddrRanges()) {
					pstPool.setLong(1, Util.inetAddress2Long(range.getRangeStart()));
					pstPool.setLong(2, Util.inetAddress2Long(range.getRangeEnd()));
					pstPool.setNull(3, Types.BIGINT);
					pstPool.setLong(4, subnet.getCidr().toLong());
					pstPool.setNull(5, Types.BIGINT);
					res = pstPool.executeUpdate();
					if (res != 1) {
						throw new SQLException("Cannot update T_POOL");
					}
				}
			}
		} finally {
			closeQuietly(pstPoolSet);
			closeQuietly(pstPool);
		}
	}
	
	/**
	 * 
	 * @param ip primary key, IP address as <tt>long</tt>
	 * @return the lease found in DB or <tt>null</tt> if none found
	 */
	public static DHCPLease getLease(Connection conn, long ip) throws SQLException {
		if (conn == null) {
			throw new NullPointerException();
		}
		
		QueryRunner qRunner = new QueryRunner();
		try  {
			return (DHCPLease) qRunner.query(conn, SELECT_LEASE, ip, leaseHandler);
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Unexpected SQLException when getting Lease", e);
			throw e;
		} finally {
			//
		}		
	}
	
	@SuppressWarnings("unchecked")
	public static List<DHCPLease> getLeasesFromRanges(Connection conn, long start, long end) throws SQLException {
		if (conn == null) {
			throw new NullPointerException();
		}
		
		QueryRunner qRunner = new QueryRunner();
		Long[] ll = new Long[2];
		ll[0] = start;
		ll[1] = end;
		List<DHCPLease> lLeases;
		
		lLeases = (List<DHCPLease>) qRunner.query(conn, SELECT_LEASE_RANGE, ll, leaseListHandler);
		return lLeases;
	}
	
	public static DHCPLease makeDHCPLease(ResultSet res) throws SQLException {
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

	/* QueryLoader for loading sql from properties files */
	static final Map<String, String>				queries;

	static {
		try {
			queries = QueryLoader.instance().load("/org/dhcpcluster/backend/hsql/queries.properties");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Cannot load properties /org/dhcpcluster/backend/hsql/queries.properties", e);
			throw new IllegalStateException(e);
		}
	}

	private static final ResultSetHandler leaseHandler = new BeanHandler(DHCPLease.class, new BasicRowProcessor(new LeaseHandler()));
	private static final ResultSetHandler leaseListHandler = new BeanListHandler(DHCPLease.class, new BasicRowProcessor(new LeaseHandler()));

	private static final String	SHUTDOWN = queries.get("SHUTDOWN");
	
	private static final String	DELETE_T_POOL = queries.get("DELETE_T_POOL");
	private static final String	DELETE_T_POOL_SET = queries.get("DELETE_T_POOL_SET");

	private static final String	INSERT_T_POOL_SET = queries.get("INSERT_T_POOL_SET");
	private static final String	INSERT_T_POOL = queries.get("INSERT_T_POOL");

	private static final String	SELECT_LEASE = queries.get("SELECT_LEASE");
	private static final String	SELECT_LEASE_RANGE = queries.get("SELECT_LEASE_RANGE");
	private static final String	INSERT_LEASE = queries.get("INSERT_LEASE");

}

class ListResultSetHandler implements ResultSetHandler {
	
	private final ResultSetHandler rsh;
	
	public ListResultSetHandler(ResultSetHandler rsh) {
		if (rsh == null) {
			throw new NullPointerException();
		}
		this.rsh = rsh;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	public Object handle(ResultSet rs) throws SQLException {
        List<Object> results = new ArrayList<Object>();
        while (rs.next()) {
        	results.add(rsh.handle(rs));
        };
        return results;
	}
	
}


class LeaseHandler extends CustomBeanProcessor {

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.BeanProcessor#toBean(java.sql.ResultSet, java.lang.Class)
	 */
	@Override
	public DHCPLease toBean(ResultSet rs, Class type) throws SQLException {
		if (rs == null) {
			throw new NullPointerException();
		}
		DHCPLease lease = new DHCPLease();
		lease.setIp(rs.getLong("IP"));
		lease.setCreationDate(rs.getDate("CREATION_DATE"));
		lease.setUpdateDate(rs.getDate("UPDATE_DATE"));
		lease.setExpirationDate(rs.getDate("EXPIRATION_DATE"));
		//lease.setMac(res.getString("MAC"));
		//lease.setUid(res.getString("UID"));
		lease.setStatus(DHCPLease.Status.fromInt(rs.getInt("STATUS")));
		return lease;
	}

}
