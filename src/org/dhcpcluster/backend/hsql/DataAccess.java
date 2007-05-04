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
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryLoader;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.Logger;

import org.dhcp4java.InetCidr;
import org.dhcp4java.Util;
import org.dhcpcluster.backend.QueryRunner2;
import org.dhcpcluster.struct.AddressRange;
import org.dhcpcluster.struct.DHCPLease;
import org.dhcpcluster.struct.Subnet;

import static org.apache.commons.dbutils.DbUtils.closeQuietly;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class DataAccess {

	private static final Logger logger = Logger.getLogger(DataAccess.class);
	
	private static QueryRunner2 qRunner = new QueryRunner2();
	
	private DataAccess() {
		throw new UnsupportedOperationException();
	}
	
	public static void shutdown(Connection conn) throws SQLException {
		assert(conn != null);
		qRunner.update(conn, SHUTDOWN);
	}
	
	public static void shutdownCompact(Connection conn) throws SQLException {
		assert(conn != null);
		qRunner.update(conn, SHUTDOWN_COMPACT);
	}
	
	public static int identity(Connection conn) throws SQLException {
		assert(conn != null);
		Integer id = (Integer) qRunner.query(conn, IDENTITY, scalarHandler);
		return id;
	}

	private static final ResultSetHandler scalarHandler = new ScalarHandler();
	
	public static void deletePools(Connection conn) throws SQLException {
		assert(conn != null);
		int res = qRunner.update(conn, DELETE_T_POOL);
		logger.debug("Delete all from T_POOL: "+res+" deleted");
	}
	
	public static void deletePoolSets(Connection conn) throws SQLException {
		assert(conn != null);
		int res = qRunner.update(conn, DELETE_T_POOL_SET);
		logger.debug("Delete all from T_POOL_SET: "+res+" deleted");
	}
	
	public static void deleteBubbles(Connection conn) throws SQLException {
		assert(conn != null);
		int res = qRunner.update(conn, DELETE_T_BUBBLE);
		logger.debug("Delete all from T_BUBBLE: "+res+" deleted");
	}
	
	private static void checkNotOverlap(Collection<Subnet> subnetColl) {
		List<InetCidr> cidrList = new ArrayList<InetCidr>(subnetColl.size());
		for (Subnet subnet : subnetColl) {
			cidrList.add(subnet.getCidr());
			List<AddressRange> adrRanges = new ArrayList<AddressRange>(subnet.getAddrRanges().size());
			for (AddressRange adrr : subnet.getAddrRanges()) {
				adrRanges.add(adrr);
			}
			Collections.sort(adrRanges);
			AddressRange.checkNoOverlap(adrRanges);
		}
		Collections.sort(cidrList);
		InetCidr.checkNoOverlap(cidrList);
	}
	
	public static void insertPoolsAndPoolSets(Connection conn, Collection<Subnet> subnetColl) throws SQLException {
		assert(conn != null);
		PreparedStatement pstPoolSet = null;
		PreparedStatement pstPool = null;
		
		checkNotOverlap(subnetColl);

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
					long rangeId = Util.inetAddress2Long(range.getRangeStart());
					pstPool.setLong(1, rangeId);
					pstPool.setLong(2, Util.inetAddress2Long(range.getRangeEnd()));
					pstPool.setNull(3, Types.BIGINT);
					pstPool.setLong(4, subnet.getCidr().toLong());
					res = pstPool.executeUpdate();
					if (res != 1) {
						throw new SQLException("Cannot update T_POOL");
					}
					// create bubbles
					insertBubbles(conn, range, rangeId);
				}
			}
		} finally {
			closeQuietly(pstPoolSet);
			closeQuietly(pstPool);
		}
	}
	
	/**
	 * 
	 * @param conn
	 * @param range
	 * @param rangeId
	 * @throws SQLException
	 */
	public static void insertBubbles(Connection conn, AddressRange range, long rangeId) throws SQLException {
		assert(conn != null);
		long rangeStart = range.getRangeStartLong();
		long rangeEnd = range.getRangeEndLong();
		List<DHCPLease> leaseList = getLeasesFromRanges(conn, rangeStart, rangeEnd);
		for (DHCPLease lease : leaseList) {
			long leaseAddr = lease.getIp();
			if (leaseAddr > rangeStart) {
				// inserting new bubble
				insertBubble(conn, rangeId, rangeStart, leaseAddr-1);
			}
			rangeStart = leaseAddr+1;
		}
		if (rangeStart <= rangeEnd) {
			insertBubble(conn, rangeId, rangeStart, rangeEnd);
		}
	}
	
	public static boolean insertLease(Connection conn, DHCPLease lease) throws SQLException {
		assert(conn != null);
		Object[] args = new Object[8];
		args[0] = (Long) lease.getIp();
		args[1] = (Timestamp) new Timestamp(lease.getCreationDate());
		args[2] = (Timestamp) new Timestamp(lease.getUpdateDate());
		args[3] = (Timestamp) new Timestamp(lease.getExpirationDate());
		args[4] = (Timestamp) new Timestamp(lease.getRecycleDate());
		args[5] = (String) lease.getMacHex();
		args[6] = (String) lease.getUid();
		args[7] = (Integer) lease.getStatus().getCode();
		if (qRunner.update(conn, INSERT_LEASE, args) != 1) {
			logger.warn("Cannot insert T_LEASE: ip="+lease.getIp());
			return false;
		}
		return true;
	}

	public static boolean updateLease(Connection conn, DHCPLease lease) throws SQLException {
		assert(conn != null);
		Object[] args = new Object[8];
		args[0] = (Timestamp) new Timestamp(lease.getCreationDate());
		args[1] = (Timestamp) new Timestamp(lease.getUpdateDate());
		args[2] = (Timestamp) new Timestamp(lease.getExpirationDate());
		args[3] = (Timestamp) new Timestamp(lease.getRecycleDate());
		args[4] = (String) lease.getMacHex();
		args[5] = (String) lease.getUid();
		args[6] = (Integer) lease.getStatus().getCode();
		args[7] = (Long) lease.getIp();
		if (qRunner.update(conn, UPDATE_LEASE, args) != 1) {
			logger.warn("Cannot update T_LEASE: ip="+lease.getIp());
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param conn
	 * @param rangeId
	 * @param start
	 * @param end
	 * @throws SQLException
	 */
	public static void insertBubble(Connection conn, long rangeId, long start, long end) throws SQLException {
		assert(conn != null);
		Object[] args = new Object[3];
		args[0] = (Long) rangeId;
		args[1] = (Long) start;
		args[2] = (Long) end;
		if (qRunner.update(conn, INSERT_T_BUBBLE, args) != 1) {
			logger.warn("Cannot insert T_BUBBLE: rangeId="+rangeId+" start="+start+" end="+end);
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
		
		try  {
			return (DHCPLease) qRunner.query(conn, SELECT_LEASE, ip, leaseHandler);
		} catch (SQLException e) {
			logger.error("Unexpected SQLException when getting Lease", e);
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
		
		Long[] ll = new Long[2];
		ll[0] = start;
		ll[1] = end;
		List<DHCPLease> lLeases;
		
		lLeases = (List<DHCPLease>) qRunner.query(conn, SELECT_LEASE_RANGE, ll, leaseListHandler);
		return lLeases;
	}
	
	public static void insertLeaseArchive(Connection conn, long ip, Date creation, Date update, Date expiration, String mac, String uid, int prevStatus) throws SQLException {
		assert(conn != null);
		Object[] args = new Object[7];
		args[0] = (Long) ip;
		args[1] = creation;
		args[2] = update;
		args[3] = expiration;
		args[4] = mac;
		args[5] = uid;
		args[6] = (Integer) prevStatus;
		if (qRunner.update(conn, INSERT_T_LEASE_ARCHIVE, args) != 1) {
			logger.warn("Cannot insert INSERT_T_LEASE_ARCHIVE: ip="+ip);
		}

	}

	@SuppressWarnings("unchecked")
	public static List<AddressRange> selectPoolsFromPoolSet(Connection conn, long poolId) throws SQLException {
		assert(conn != null);
		return (List<AddressRange>) qRunner.query(conn, SELECT_T_POOL_RANGES_FROM_SET_ID, poolId, addressRangeHandler);
	}
	
	public static Bubble selectBubbleContainingIp(Connection conn, long ip, long poolId) throws SQLException {
		assert(conn != null);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ip", (Long) ip);
		args.put("poolid", (Long) poolId);
		return (Bubble) qRunner.queryNamedParams(conn, SELECT_BUBBLE_CONTAINING_IP, args, bubbleHandler);
	}
	
	@SuppressWarnings("unchecked")
	public static List<DHCPLease> selectActiveLeasesByIcc(Connection conn, String icc) throws SQLException {
		assert(conn != null);
		if ((icc == null) || (icc.length() == 0)) {
			return null;
		}
		return (List<DHCPLease>) qRunner.query(conn, SELECT_LEASE_ACTIVE_ICC, icc, leaseListHandler);
	}
	
	public static Bubble selectBubblesByPoolId(Connection conn, long poolId) throws SQLException {
		assert(conn != null);
		return (Bubble) qRunner.query(conn, SELECT_BUBBLE_FROM_POOL_SET, (Long) poolId, bubbleHandler);
	}
	
	public static long callDhcpDiscoverSP(Connection conn, long poolId, String macHex, int iccQuota, String icc,
											long offerTime) throws SQLException {
		assert(conn != null);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("poolid", (Long) poolId);
		args.put("mac", macHex);
		args.put("iccquota", (Integer) iccQuota);
		args.put("icc", icc);
		args.put("offertime", (Long) offerTime);
		return (Long) qRunner.queryNamedParams(conn, CALL_DHCP_DISCOVER, args, scalarHandler);
	}

	public static int callDhcpRequestSP(Connection conn, long poolId, long requestedIp, int leaseTime, int margin,
											String macHex, boolean optimisticAllocation) throws SQLException {
		assert(conn != null);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("poolid", (Long) poolId);
		args.put("requestedip", (Long) requestedIp);
		args.put("leasetime", (Integer) leaseTime);
		args.put("margin", (Integer) margin);
		args.put("mac", macHex);
		args.put("optimistic", (Boolean) optimisticAllocation);
		return (Integer) qRunner.queryNamedParams(conn, CALL_DHCP_REQUEST, args, scalarHandler);
	}

	/* QueryLoader for loading sql from properties files */

	static final Map<String, String>				queries;
	static {
		try {
			queries = QueryLoader.instance().load("/org/dhcpcluster/backend/hsql/queries.properties");
		} catch (IOException e) {
			logger.fatal("Cannot load properties /org/dhcpcluster/backend/hsql/queries.properties", e);
			throw new IllegalStateException(e);
		}
	}

	static final ResultSetHandler leaseHandler = new BeanHandler(DHCPLease.class, new BasicRowProcessor(new LeaseHandler()));
	static final ResultSetHandler bubbleHandler = new BeanHandler(Bubble.class, new BasicRowProcessor(new BubbleHandler()));
	static final ResultSetHandler leaseListHandler = new BeanListHandler(DHCPLease.class, new BasicRowProcessor(new LeaseHandler()));
	static final ResultSetHandler addressRangeHandler = new BeanListHandler(AddressRange.class, new BasicRowProcessor(new AddressRangeHandler()));


	private static final String	SHUTDOWN = queries.get("SHUTDOWN");
	private static final String	SHUTDOWN_COMPACT = queries.get("SHUTDOWN_COMPACT");
	private static final String	IDENTITY = queries.get("IDENTITY");
	
	private static final String	DELETE_T_POOL = queries.get("DELETE_T_POOL");
	private static final String	DELETE_T_POOL_SET = queries.get("DELETE_T_POOL_SET");
	private static final String	SELECT_BUBBLE_FROM_POOL_SET = queries.get("SELECT_BUBBLE_FROM_POOL_SET");
	private static final String	DELETE_T_BUBBLE = queries.get("DELETE_T_BUBBLE");

	private static final String	INSERT_T_POOL_SET = queries.get("INSERT_T_POOL_SET");
	private static final String	INSERT_T_POOL = queries.get("INSERT_T_POOL");
	private static final String	INSERT_T_BUBBLE = queries.get("INSERT_T_BUBBLE");
	private static final String	INSERT_LEASE = queries.get("INSERT_LEASE");
	private static final String	UPDATE_LEASE = queries.get("UPDATE_LEASE");

	private static final String	SELECT_BUBBLE_CONTAINING_IP = queries.get("SELECT_BUBBLE_CONTAINING_IP");

	private static final String	SELECT_LEASE = queries.get("SELECT_LEASE");
	private static final String	SELECT_LEASE_RANGE = queries.get("SELECT_LEASE_RANGE");
	private static final String	SELECT_LEASE_ACTIVE_ICC = queries.get("SELECT_LEASE_ACTIVE_ICC");
	private static final String	SELECT_T_POOL_RANGES_FROM_SET_ID = queries.get("SELECT_T_POOL_RANGES_FROM_SET_ID");

	private static final String	INSERT_T_LEASE_ARCHIVE = queries.get("INSERT_T_LEASE_ARCHIVE");

	private static final String	CALL_DHCP_DISCOVER = queries.get("CALL_DHCP_DISCOVER");
	private static final String	CALL_DHCP_REQUEST = queries.get("CALL_DHCP_REQUEST");

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
        }
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
		lease.setCreationDate(dateToLong(rs.getTimestamp("CREATION_DATE")));
		lease.setUpdateDate(dateToLong(rs.getTimestamp("UPDATE_DATE")));
		lease.setExpirationDate(dateToLong(rs.getTimestamp("EXPIRATION_DATE")));
		lease.setRecycleDate(dateToLong(rs.getTimestamp("RECYCLE_DATE")));
		lease.setMacHex(rs.getString("MAC"));
		lease.setUid(rs.getString("UID"));
		lease.setStatus(DHCPLease.Status.fromInt(rs.getInt("STATUS")));
		return lease;
	}
	
	private static final long dateToLong(Timestamp date) {
		if (date == null) {
			return 0;
		} else {
			return date.getTime();
		}
	}

}

class AddressRangeHandler extends CustomBeanProcessor {
	@Override
	public AddressRange toBean(ResultSet rs, Class type) throws SQLException {
		if (rs == null) {
			throw new NullPointerException();
		}
		InetAddress start = Util.long2InetAddress(rs.getLong("START_IP"));
		InetAddress end = Util.long2InetAddress(rs.getLong("END_IP"));
		return new AddressRange(start, end);
	}
}

class BubbleHandler extends CustomBeanProcessor {
	@Override
	public Bubble toBean(ResultSet rs, Class type) throws SQLException {
		if (rs == null) {
			throw new NullPointerException();
		}
		Bubble bubble = new Bubble(rs.getInt("BUBBLE_ID"));
		bubble.setStart(rs.getLong("START_IP"));
		bubble.setEnd(rs.getLong("END_IP"));
		bubble.setPoolId(rs.getLong("POOL_ID"));
		return bubble;
	}
}

/**
 * Java mapping of the T_BUBBLE table
 *
 */
class Bubble {
	
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