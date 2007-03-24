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
import org.dhcpcluster.struct.AddressRange;
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
	 * 			-4: ICC quota reached, cannot allocate a new lease for this ICC
	 * @throws SQLException
	 */
	public static long findDiscoverLease(Connection conn, long poolId, String mac, int iccQuota, String icc) throws SQLException {
		assert(conn != null);
		boolean autocommitSave = conn.getAutoCommit();
		conn.setAutoCommit(false);
		boolean commit = false;
		try {
			long candidateAdr = -1;
			
			// ============================================================
			// Step 1- Check if there is already an active lease for this client in these address pools
			
			// first find if the client already has an allocated lease on that network
			DHCPLease[] leases = (DHCPLease[]) qRunner.query(conn, SELECT_LEASE_MAC, mac, DataAccess.leaseListHandler);
			if ((leases != null) && (leases.length > 0)) {
				// check if one lease is in the correct address pool
				AddressRange[] pools = DataAccess.selectPoolsFromPoolSet(conn, poolId);
				if ((pools != null) && (pools.length > 0)) {
					// Check whether a lease address is already in a pool
					// so that we can reuse it exonomically
					// PRE-REQUISITE:
					// 1- leases[] is a sorted array, non-empty
					// 2- pools[] is a sorted array, non-empty
					int leaseIdx = 0;
					int poolIdx = 0;
					while ((leaseIdx < leases.length) && (poolIdx < pools.length)) {
						long leaseAdrL = leases[leaseIdx].getIp();
						if (leaseAdrL < pools[poolIdx].getRangeStartLong()) {
							// go to next lease
							leaseIdx++;
						} else if (leaseAdrL > pools[poolIdx].getRangeEndLong()) {
							// go to next pool
							poolIdx++;
						} else {
							// found an in-range lease
							candidateAdr = leaseAdrL;
							break;
						}
					}
				}
			}
	
			// ============================================================
			// Step 2- If no address found, find out a free address
			// ============================================================
			// Step 2.1- Check if we have not reach the ICC quota
			
			// first, do we need a quota check, i.e. quota is > 0 and an ICC has been passed as parameter
			if ((iccQuota > 0) && (icc != null) && (icc.length() > 0)) {
				DHCPLease[] leaseSameIcc = DataAccess.selectActiveLeasesByIcc(conn, icc);
				if ((leaseSameIcc != null) && (leaseSameIcc.length > 0)) {
					if (leaseSameIcc.length > iccQuota) {
						// icc quota reached, cannot allocate a new address
						return -4;
					}
				}
			}
	
			// ============================================================
			// Step 2.2- Try to find a free address in the bubbles
			
			// TODO check algorithm
			if (candidateAdr < 0) {
				// we must allocate a new free address
				
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
				// free address is now ready to be used
				candidateAdr = startIp;
			}

			// ============================================================
			// Step 3- Reserve the new lease

			// ============================================================
			// Step 3.1- Retrieve the lease if it already exists
			
			DHCPLease curLease = DataAccess.getLease(conn, candidateAdr);
			if (curLease == null) {
				// ============================================================
				// Step 3.2- Register a new lease

				// insert lease into T_LEASE
				Long now = System.currentTimeMillis();
				DHCPLease newLease = new DHCPLease();
				newLease.setIp(candidateAdr);
				newLease.setCreationDate(now);
				newLease.setUpdateDate(now);
				newLease.setExpirationDate(now + 10000);
				newLease.setRecycleDate(now + 10000 + 10000);
				newLease.setMac(mac.getBytes());
				//newLease.setUid();
				newLease.setStatus(DHCPLease.Status.OFFERED);
				if (!DataAccess.insertLease(conn, newLease)) {
					return -3;					// cannot insert new lease
				}
				commit = true;				// intend to commit now
					
			}
			
			return candidateAdr;		// this is the ip of the prepared lease
		} finally {
			if (commit) {
				conn.commit();
			} else {
				conn.rollback();
			}
			conn.setAutoCommit(autocommitSave);
		}
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
