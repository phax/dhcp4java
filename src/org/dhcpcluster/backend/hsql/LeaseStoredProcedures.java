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
import java.sql.Date;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.dhcp4java.InetCidr;
import org.dhcp4java.Util;
import org.dhcpcluster.struct.DHCPLease;

import static org.dhcpcluster.backend.hsql.DataAccess.queries;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class LeaseStoredProcedures {

	private static final Logger logger = Logger.getLogger(LeaseStoredProcedures.class);

	private static final QueryRunner 			qRunner = new QueryRunner();
	private static final ResultSetHandler 	arrayHandler = new ArrayHandler();
	
	/**
	 * 
	 * @param conn
	 * @param poolId
	 * @param mac
	 * @return the IP address to be sent back to the client (always > 0 if successful)<br>
	 * 			 0: out of free addresses, the tables are full<br>
	 * 			-1: could not update the T_BUBBLE table<br>
	 * 			-2: could not delete in T_BUBBLE table<br>
	 * 			-3: could not update the T_LEASE table
	 * @throws SQLException
	 */
	public static long findDiscoverLease(Connection conn, long poolId, String mac) throws SQLException {
		assert(conn != null);
		// first find if the client already has an allocated lease on that network
		DHCPLease[] leases = (DHCPLease[]) qRunner.query(conn, SELECT_LEASE_MAC, mac, DataAccess.leaseListHandler);
		if ((leases != null) && (leases.length > 0)) {
			// check if one lease is in the correct address pool
			// TODO
		}
		
		// find first free bubble for poolId
		Object[] res = (Object[]) qRunner.query(conn, SELECT_BUBBLE_FROM_POOL_SET, (Long) poolId, arrayHandler);
		if (res == null) {
			logger.warn("No lease left for poolId="+poolId);
			return 0;
		}
		assert(res.length == 3);
		long bubbleId = (Integer) res[0];
		long startIp = (Long) res[1];
		long endIp = (Long) res[2];
		
		if (startIp < endIp) {
			// we shrink the bubble
			Object[] args = new Object[2];
			args[0] = (Long) (startIp + 1);
			args[1] = (Long) bubbleId;
			if (qRunner.update(conn, UPDATE_BUBBLE_START_IP, args) != 1) {
				logger.error("Cannot update bubble startIp="+(startIp+1)+" bubble_id="+bubbleId);
				return -1;
			}
		} else {
			// delete bubble which is now empty
			if (qRunner.update(conn, DELETE_BUBBLE_ID, (Long) bubbleId) != 1) {
				logger.error("Cannot delete bubble bubble_id="+bubbleId);
				return -2;
			}
		}
		
		// create of modify lease
		Object[] args = new Object[7];
		args[0] = (Long) startIp;		// ip
		args[1] = null;					// creation date
		args[2] = null;					// update date
		args[3] = null;					// expiration date
		args[4] = mac;					// mac address
		args[5] = null;					// uid
		args[6] = 1;					// status
		if (qRunner.update(conn, INSERT_LEASE, args) != 1) {
			logger.error("Cannot insert Lease ip="+startIp+" mac="+mac);
			return -3;
		}
		
		//logLease(startIp, )
		return startIp;		// this is the ip of the prepared lease
	}
	
	/**
	 * 
	 * <p>Note: the caller must first check that the requested address is indeed in the LAN. No check here.
	 * @param conn
	 * @param poolId
	 * @param requestedIp
	 * @param mac
	 * @return status of the request, positive is ok, 0 is must be ignored, negative must be an NAK<br>  
	 * @throws SQLException
	 */
	public static int confirmOfferLease (Connection conn, long poolId, long requestedIp, int leaseTime, int margin, String macHex, String uid) throws SQLException {
		if ((macHex == null) || (macHex.length() < 2)) {
			throw new IllegalArgumentException("macHex is null or too short:"+macHex);
		}
		DHCPLease curLease = DataAccess.getLease(conn, requestedIp);
		long now = System.currentTimeMillis();
		if (curLease == null) {
			logger.debug("No active lease for ip: "+requestedIp);
			// create new lease
			curLease = new DHCPLease();
			curLease.setIp(requestedIp);
			curLease.setCreationDate(now);
			curLease.setUpdateDate(now);
			curLease.setExpirationDate(now + 1000L*leaseTime);
			curLease.setRecycleDate(now + ((1000L * margin)/100L) * leaseTime);
			curLease.setMacHex(macHex);
			curLease.setUid(uid);
			curLease.setStatus(DHCPLease.Status.USED);
			DataAccess.insertLease(conn, curLease);
			return 2;
		}
		// we have an already existing lease for this IP address
		// is it the same client ?
		DHCPLease.Status curStatus = curLease.getStatus();
		// is the ip allowable ?
		if (curStatus == DHCPLease.Status.ABANDONED) {
			return -1;
		}
		if (macHex.equalsIgnoreCase(curLease.getMacHex())) {
			// ok this is allowed, be allow a new lease period
			curLease.setUpdateDate(now);
			curLease.setExpirationDate(now + 1000L*leaseTime);
			curLease.setRecycleDate(now + ((1000L * margin)/100L) * leaseTime);
			curLease.setUid(uid);
			DataAccess.updateLease(conn, curLease);
			if (curStatus == DHCPLease.Status.OFFERED) {
				return 1;
			} else {
				return 2;
			}
		}
		return -2;		// address is not usable for this client
	}
	
	public static String longAddressToString(long ip) {
		return Util.long2InetAddress(ip).getHostAddress();
	}
	
	public static String longCidrToString(long cidr) {
		return InetCidr.fromLong(cidr).toString();
	}
	

	public static void logLease(long ip, Date creation, Date update, Date expiration, DHCPLease.Status status, DHCPLease.Status prevStatus, String mac,
									String uid, String icc) {
		StringBuffer sb = new StringBuffer(127);
		sb.append(Util.long2InetAddress(ip).getHostAddress());
		sb.append(';');
		dateFormatter.format(creation, sb, null);
		sb.append(';');
		dateFormatter.format(update, sb, null);
		sb.append(';');
		dateFormatter.format(expiration, sb, null);
		sb.append(';');
		sb.append(status.toString()).append(';');
		sb.append(prevStatus.toString()).append(';');
		sb.append(mac).append(';');
		sb.append((uid!=null)? uid: "").append(';');
		sb.append((icc!=null)? icc: "");
		archiveLog.info(sb);
	}

	private static final ISO8601DateFormat dateFormatter = new ISO8601DateFormat();
	private static final org.apache.log4j.Logger archiveLog = org.apache.log4j.Logger.getLogger("archive.dhcp");

	private static final String	SELECT_BUBBLE_FROM_POOL_SET = queries.get("SELECT_BUBBLE_FROM_POOL_SET");
	private static final String	UPDATE_BUBBLE_START_IP = queries.get("UPDATE_BUBBLE_START_IP");
	private static final String	DELETE_BUBBLE_ID = queries.get("DELETE_BUBBLE_ID");
	private static final String	INSERT_LEASE = queries.get("INSERT_LEASE");
	private static final String	SELECT_LEASE_MAC = queries.get("SELECT_LEASE_MAC");
}
