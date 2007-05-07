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
package org.dhcpcluster.test.hsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.dhcp4java.DHCPServerInitException;
import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.SystemTime;
import org.dhcpcluster.backend.hsql.Bubble;
import org.dhcpcluster.backend.hsql.DataAccess;
import org.dhcpcluster.struct.DHCPLease;
import org.dhcpcluster.struct.DHCPLease.Status;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

import static org.junit.Assert.*;

public class StoredProcedureTest {

	public static junit.framework.Test suite() {
       return new JUnit4TestAdapter(StoredProcedureTest.class);
    }
	
	private static DHCPClusterNode node = null;
	private static Connection conn;
	private static long originOfTime;

	private static final String macAdr = "001122334455";
	private static final String macAdr2 = "001122334456";
	
	@BeforeClass
	public static void launchServer() throws DHCPServerInitException, SQLException, ParseException {
		Properties props = new Properties();
		
		props.setProperty("config.reader", "org.dhcpcluster.config.xml.XmlConfigReader");
		props.setProperty("config.xml.file", "./JUnitTests/org/dhcpcluster/test/hsql/conf/configtest.xml");
		
		props.setProperty("backend.hsql.address", "localhost");
		props.setProperty("backend.hsql.dbnumber", "0");
		props.setProperty("backend.hsql.dbname", "dhcpcluster");
		props.setProperty("backend.hsql.dbpath", "./JUnitTests/org/dhcpcluster/test/hsql/db/dhcpcluster");

    	node = new DHCPClusterNode(props);
		conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpcluster", "sa", "");
		conn.setAutoCommit(true);
		
		// change time reference
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = (Date)formatter.parse("01/01/2007");
        originOfTime = date.getTime();
		SystemTime.setForcedTime(originOfTime);
		SystemTime.setForcedMode(true);
	}
	
	@Before
	public void prepareDb() throws SQLException {
		SystemTime.setForcedTime(originOfTime);
		QueryRunner qRunner = new QueryRunner();
		qRunner.update(conn, "DELETE FROM T_LEASE");
		node.getBackend().prepareBackend(node.getTopologyConfig());
		conn.commit();
	}
	
	@Test
	public void testDhcpDiscover() throws Exception {
		long poolId = -1;
		long res;
		long now = originOfTime;
		// bad PoolId
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr, -1, null, 60);
		assertEquals(0L, res);
		
		// PoolID = 97721516032
		poolId = 97721516032L;
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr, -1, null, 60);
		assertEquals(3232235535L, res);
		DHCPLease lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(originOfTime, lease.getCreationDate());
		assertEquals(originOfTime, lease.getUpdateDate());
		assertEquals(originOfTime + 60000L, lease.getExpirationDate());
		assertEquals(originOfTime + 60000L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.OFFERED, lease.getStatus());
		
		// small advance in time (30s)
		now += 30000;
		SystemTime.setForcedTime(now);
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr, -1, null, 60);
		assertEquals(3232235535L, res);
		lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(originOfTime, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 60000L, lease.getExpirationDate());
		assertEquals(now + 60000L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.OFFERED, lease.getStatus());
		
		// now go next day, without GC (status is always OFFERED)
		now += 1000L * 3600 * 24;
		SystemTime.setForcedTime(now);
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr, -1, null, 60);
		assertEquals(3232235535L, res);
		lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(originOfTime, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 60000L, lease.getExpirationDate());
		assertEquals(now + 60000L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.OFFERED, lease.getStatus());

		// small advance in time (30s)
		now += 30000;
		SystemTime.setForcedTime(now);
		// change status to USED
		lease = DataAccess.getLease(conn, 3232235535L);
		lease.setStatus(Status.USED);
		DataAccess.updateLease(conn, lease);
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr, -1, null, 60);
		assertEquals(3232235535L, res);
		lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(originOfTime, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 60000L, lease.getExpirationDate());
		assertEquals(now + 60000L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.USED, lease.getStatus());

		// second mac address
		long creationDate2 = now;
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr2 /* new mac */, -1, null, 60);
		assertEquals(3232235536L, res);
		lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 60000L, lease.getExpirationDate());
		assertEquals(now + 60000L, lease.getRecycleDate());
		assertEquals(macAdr2, lease.getMacHex());
		assertEquals(Status.OFFERED, lease.getStatus());
		
		// force second lease to "ABANDONED"
		lease = DataAccess.getLease(conn, 3232235536L);
		lease.setStatus(Status.ABANDONED);
		DataAccess.updateLease(conn, lease);
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr2 /* new mac */, -1, null, 60);
		assertEquals(3232235537L, res);
		lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 60000L, lease.getExpirationDate());
		assertEquals(now + 60000L, lease.getRecycleDate());
		assertEquals(macAdr2, lease.getMacHex());
		assertEquals(Status.OFFERED, lease.getStatus());

		// now go next day
		now += 1000L * 3600 * 24;
		SystemTime.setForcedTime(now);
		// force second lease to USED
		lease = DataAccess.getLease(conn, 3232235536L);
		lease.setStatus(Status.USED);
		DataAccess.updateLease(conn, lease);
		res = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr2 /* new mac */, -1, null, 60);
		assertEquals(3232235536L, res);
		lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(creationDate2, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 60000L, lease.getExpirationDate());
		assertEquals(now + 60000L, lease.getRecycleDate());
		assertEquals(macAdr2, lease.getMacHex());
		assertEquals(Status.OFFERED, lease.getStatus());
		
	}
	
	@Test
	public void testDhcpRequest() throws Exception {
		long poolId = -1;
		long ip;
		int res;
		long now = originOfTime;
		DHCPLease lease;
		Bubble bubble;

		// PoolID = 97721516032
		poolId = 97721516032L;
		ip = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr, -1, null, 60);
		assertEquals(3232235535L, ip);
		ip = DataAccess.callDhcpDiscoverSP(conn, poolId, macAdr2, -1, null, 60);
		assertEquals(3232235536L, ip);

		// ===== First try bad calls
		
		// change lease2 to ABANDONED
		lease = DataAccess.getLease(conn, 3232235536L);
		assertEquals(Status.OFFERED, lease.getStatus());
		lease.setStatus(Status.ABANDONED);
		DataAccess.updateLease(conn, lease);
		// now try to obtain this address
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235536L, 86400, 30, macAdr2, true);
		assertEquals(-5, res);
		// same try for another macAdr
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235536L, 86400, 30, macAdr, true);
		assertEquals(-5, res);
		// witch back to OFFERED
		lease = DataAccess.getLease(conn, 3232235536L);
		lease.setStatus(Status.OFFERED);
		DataAccess.updateLease(conn, lease);
		
		// try to ask with wrong macAdr2
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235535L, 86400, 30, macAdr2, true);
		assertEquals(-2, res);
		
		// now try a correct call
		now += 10000;
		SystemTime.setForcedTime(now);
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235535L, 86400, 30, macAdr, true);
		assertEquals(1, res);
		lease = DataAccess.getLease(conn, 3232235535L);
		assertEquals(3232235535L, lease.getIp());
		assertEquals(originOfTime, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 86400*1000L, lease.getExpirationDate());
		assertEquals(now + 86400*1000L*(100+30)/100L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.USED, lease.getStatus());
		
		// now try a free address
		now += 10000L;
		SystemTime.setForcedTime(now);
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235537L, 86400, 30, macAdr, true);
		assertEquals(2, res);
		lease = DataAccess.getLease(conn, 3232235537L);
		assertEquals(3232235537L, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 86400*1000L, lease.getExpirationDate());
		assertEquals(now + 86400*1000L*(100+30)/100L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.USED, lease.getStatus());
		// check bubble
		bubble = DataAccess.selectBubbleContainingIp(conn, 3232235537L + 1, poolId);
		assertEquals(3232235537L + 1, bubble.getStart());
		assertEquals(3232235545L, bubble.getEnd());
		assertEquals(3232235535L, bubble.getPoolId());
		
		
		// try to reallocate an expired lease
		now += 60000L;
		SystemTime.setForcedTime(now);
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235536L, 86400, 30, macAdr, true);
		assertEquals(5, res);
		lease = DataAccess.getLease(conn, 3232235536L);
		assertEquals(3232235536L, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 86400*1000L, lease.getExpirationDate());
		assertEquals(now + 86400*1000L*(100+30)/100L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.USED, lease.getStatus());
		
		// now try to reserve an address which is not in bubble
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235534L, 86400, 30, macAdr, true);
		assertEquals(-6, res);
		
		// allocate last address of bubble
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235545L, 86400, 30, macAdr, true);
		assertEquals(2, res);
		lease = DataAccess.getLease(conn, 3232235545L);
		assertEquals(3232235545L, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 86400*1000L, lease.getExpirationDate());
		assertEquals(now + 86400*1000L*(100+30)/100L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.USED, lease.getStatus());
		// check bubble
		bubble = DataAccess.selectBubbleContainingIp(conn, 3232235537L + 1, poolId);
		assertEquals(3232235537L + 1, bubble.getStart());
		assertEquals(3232235544L, bubble.getEnd());
		assertEquals(3232235535L, bubble.getPoolId());
		
		// allocate second address in bubble -> bubble need to be split
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235539L, 86400, 30, macAdr, true);
		assertEquals(2, res);
		lease = DataAccess.getLease(conn, 3232235539L);
		assertEquals(3232235539L, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 86400*1000L, lease.getExpirationDate());
		assertEquals(now + 86400*1000L*(100+30)/100L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.USED, lease.getStatus());
		// check bubble low
		bubble = DataAccess.selectBubbleContainingIp(conn, 3232235538L, poolId);
		assertEquals(3232235538L, bubble.getStart());
		assertEquals(3232235538L, bubble.getEnd());
		assertEquals(3232235535L, bubble.getPoolId());
		// check bubble high
		bubble = DataAccess.selectBubbleContainingIp(conn, 3232235540L, poolId);
		assertEquals(3232235540L, bubble.getStart());
		assertEquals(3232235544L, bubble.getEnd());
		assertEquals(3232235535L, bubble.getPoolId());
		
		// allocate address in remaining single-address bubble
		res = DataAccess.callDhcpRequestSP(conn, poolId, 3232235538L, 86400, 30, macAdr, true);
		assertEquals(2, res);
		lease = DataAccess.getLease(conn, 3232235538L);
		assertEquals(3232235538L, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 86400*1000L, lease.getExpirationDate());
		assertEquals(now + 86400*1000L*(100+30)/100L, lease.getRecycleDate());
		assertEquals(macAdr, lease.getMacHex());
		assertEquals(Status.USED, lease.getStatus());
		// check bubble low
		bubble = DataAccess.selectBubbleContainingIp(conn, 3232235538L, poolId);
		assertNull(bubble);
	}

	
	@Test(expected=NullPointerException.class)
	public void dhcpDiscoverConnNull() throws SQLException {
		DataAccess.callDhcpDiscoverSP(null, 0, "0011", 0, null, 0);
	}

	@Test(expected=SQLException.class)
	public void dhcpDiscoverMacNull() throws SQLException {
		DataAccess.callDhcpDiscoverSP(conn, 0, null, 0, null, 0);
	}
	
	@After
	public void cleanupDb() throws SQLException {
		conn.rollback();
	}

	@AfterClass
	public static void stopServer() {
		node.stop();
	}
	
	// TODO add ICC quotas tests on dhcpDiscover
}
