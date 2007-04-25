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
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.dhcp4java.InetCidr;
import org.dhcp4java.Util;
import org.dhcpcluster.struct.AddressRange;
import org.dhcpcluster.struct.DHCPLease;
import org.dhcpcluster.struct.DHCPLease.Status;

import static org.dhcpcluster.backend.hsql.DataAccess.queries;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class LeaseStoredProcedures {

	private static final Logger logger = Logger.getLogger(LeaseStoredProcedures.class);

	private static final QueryRunner 			qRunner = new QueryRunner();
	
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
	public static long dhcpDiscover(Connection conn, long poolId, String macHex, int iccQuota, String icc,
											long offerTime) throws SQLException {
		assert(conn != null);
		if ((macHex == null) || (macHex.length() < 2)) {
			throw new IllegalArgumentException("macHex is null or too short:"+macHex);
		}
		boolean autocommitSave = conn.getAutoCommit();
		conn.setAutoCommit(false);
		boolean commit = false;
		DHCPLease existingLease = null;
		try {
			long candidateAdr = -1;
			
			// ============================================================
			// Step 1- Check if the client already has an active lease in the specified address pool
			
			// Select T_LEASE table with client’s Mac address and Status is not FREE or ABANDONED,
			DHCPLease[] leases = (DHCPLease[]) qRunner.query(conn, SELECT_LEASE_MAC, macHex, DataAccess.leaseListHandler);
			// then filtering the result set with the specified address pool.
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
							existingLease = leases[leaseIdx];
							break;
						}
					}
				}
			}
	
			// ============================================================
			// Step 2- If no active lease is found, then look for the first free available address
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
				// TODO we could change the request with a SELECT COUNT(*)...
			}
	
			// ============================================================
			// Step 2.2- Find a the first free address in the address pool
			// Select T_BUBBLE to find the first free bubble, retrieve the first address
			// then update the bubble: increase its start address or delete it if it becomes empty
			
			// TODO check algorithm
			if (candidateAdr < 0) {
				// we must allocate a new free address
				
				// find first free bubble for poolId
				Bubble bubble = DataAccess.selectBubblesByPoolId(conn, poolId); 
				if (bubble == null) {
					logger.warn("No lease left for poolId="+poolId);
					return 0;
				}
				long bubbleId = bubble.getId();
				long startIp = bubble.getStart();
				long endIp = bubble.getEnd();
				
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
			// Step 3- Reserve a new lease

			Long now = System.currentTimeMillis();
			
			if (existingLease != null) {
				DHCPLease.Status status = existingLease.getStatus();
				
				// check whether the lease has expired but has not been garbarge collected
				if (existingLease.getRecycleDate() < now) {
					status = Status.FREE;
				}
				
				if (status == Status.OFFERED) {
					// Step 3.1- If it already exists in OFFERED state, extend the EXPIRATION_DATE and update UPDATE_DATE
					existingLease.setUpdateDate(now);
					existingLease.setExpirationDate(now + 1000L*offerTime);
					if (!DataAccess.updateLease(conn, existingLease)) {
						return -3;
					}
				} else if (status == Status.USED) {
					// Step 3.2- If it already exists in USED state, just update the UPDATE_DATE
					existingLease.setUpdateDate(now);
					if (existingLease.getExpirationDate() < now + 1000L*offerTime) {
						existingLease.setExpirationDate(now + 1000L*offerTime);
					}
					if (!DataAccess.updateLease(conn, existingLease)) {
						return -3;
					}
				} else if (status == Status.ABANDONED) {
					// Step 3.3- If it already exists in ABANDONED state, this is an internal error (pre-condition violation), return error (-5)
					return -5;
				} else if (status == Status.FREE) {
					existingLease.setCreationDate(now);
					existingLease.setUpdateDate(now);
					existingLease.setExpirationDate(now + 1000L*offerTime);
					existingLease.setRecycleDate(now + 1000L*offerTime);
					existingLease.setMacHex(macHex);
					// TODO existingLease.setIcc();
					existingLease.setStatus(Status.OFFERED);
					if (!DataAccess.updateLease(conn, existingLease)) {
						return -3;
					}
				} else {
					logger.warn("Unhandled status code for lease: "+status+" for IP: "+Util.long2InetAddress(existingLease.getIp()).getHostAddress());
					return -6;
				}
			} else {
				// ============================================================
				// Step 3.2- Register a new lease

				// insert lease into T_LEASE
				DHCPLease newLease = new DHCPLease();
				newLease.setIp(candidateAdr);
				newLease.setCreationDate(now);
				newLease.setUpdateDate(now);
				newLease.setExpirationDate(now + 1000L*offerTime);
				newLease.setRecycleDate(now + 1000L*offerTime);
				newLease.setMacHex(macHex);
				//TODO ICC newLease.setUid();
				newLease.setStatus(DHCPLease.Status.OFFERED);
				if (!DataAccess.insertLease(conn, newLease)) {
					return -3;					// cannot insert new lease
				}
			}
			conn.commit();
			commit = true;				// intend to commit now
			
			return candidateAdr;		// this is the ip of the prepared lease
		} finally {
			if (!commit) {
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
	public static int dhcpRequest(Connection conn, long poolId, long requestedIp, int leaseTime, int margin, String macHex, String uid) throws SQLException {
		assert(conn != null);
		if ((macHex == null) || (macHex.length() < 2)) {
			throw new IllegalArgumentException("macHex is null or too short:"+macHex);
		}
		long now = System.currentTimeMillis();
		
		boolean autocommitSave = conn.getAutoCommit();		// for restoring previous autocommit state on exit
		conn.setAutoCommit(false);
		boolean commit = false;							// if false, we do a rollback on exit
		try {
			// Step 1 - Retrieve the lease for the requested IP
			DHCPLease existingLease = DataAccess.getLease(conn, requestedIp);
	
			if (existingLease != null) {
				Status status = existingLease.getStatus();
				
				// check whether the lease has expired but has not been garbarge collected
				if (existingLease.getRecycleDate() < now) {
					status = Status.FREE;
				}
				
				// nominal case
				
				if ( ((status == Status.OFFERED) || (status == Status.USED))  &&
						macHex.equalsIgnoreCase(existingLease.getMacHex()) ) {
					// Step 1.1 - [Nominal] if status is OFFERED or USED, and lease is owned by same client
					existingLease.setUpdateDate(now);
					// TODO verify if the lease can only be increased, or can it be shrinked ?
					existingLease.setExpirationDate(now + 1000L*leaseTime);
					existingLease.setRecycleDate(now + ((1000L * margin)/100L) * leaseTime);
					// TODO existingLease.setIcc
					existingLease.setStatus(Status.USED);		// force tu USED if it was OFFERED
					if (!DataAccess.updateLease(conn, existingLease)) {
						return -3;
					}
					return 3;
				} else if ((status == Status.OFFERED) || (status == Status.USED)) {
					// Step 1.2 - if status if OFFERED or USED and lease is owned by another client, return an error (-2)
				} else if (status == Status.ABANDONED) {
					// Step 1.3 - if Status if ABANDONED, address is not usable, return an error (-5)
					return -5;
				} else if (status == Status.FREE) {
					// Step 1.4 - if status is FREE, we use optimistic behaviour and give the lease
					existingLease.setCreationDate(now);
					existingLease.setUpdateDate(now);
					existingLease.setExpirationDate(now + 1000L*leaseTime);
					existingLease.setRecycleDate(now + ((1000L * margin)/100L) * leaseTime);
					existingLease.setMacHex(macHex);
					existingLease.setStatus(DHCPLease.Status.USED);
					if (!DataAccess.updateLease(conn, existingLease)) {
						return -3;
					}
					return 4;
				}
			} else {
				logger.debug("No active lease for ip: "+requestedIp);
				// create new lease
				existingLease = new DHCPLease();
				existingLease.setIp(requestedIp);
				existingLease.setCreationDate(now);
				existingLease.setUpdateDate(now);
				existingLease.setExpirationDate(now + 1000L*leaseTime);
				existingLease.setRecycleDate(now + ((1000L * margin)/100L) * leaseTime);
				existingLease.setMacHex(macHex);
				// TODO existingLease.setUid(uid);
				existingLease.setStatus(DHCPLease.Status.USED);
				if (!DataAccess.insertLease(conn, existingLease)) {
					return -3;
				}
				return 2;
			}
			// TODO how to reach this code ?
			return -2;		// address is not usable for this client
		} finally {
			if (!commit) {
				conn.rollback();
			}
			conn.setAutoCommit(autocommitSave);
		}
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

	private static final String	UPDATE_BUBBLE_START_IP = queries.get("UPDATE_BUBBLE_START_IP");
	private static final String	DELETE_BUBBLE_ID = queries.get("DELETE_BUBBLE_ID");
	private static final String	SELECT_LEASE_MAC = queries.get("SELECT_LEASE_MAC");
}
