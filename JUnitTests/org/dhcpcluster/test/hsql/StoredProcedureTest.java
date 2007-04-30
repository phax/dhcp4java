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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.dhcp4java.DHCPServerInitException;
import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.SystemTime;
import org.dhcpcluster.backend.hsql.DataAccess;
import org.dhcpcluster.backend.hsql.HsqlBackendServer;
import org.dhcpcluster.backend.hsql.LeaseStoredProcedures;
import org.dhcpcluster.struct.DHCPLease;
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
	
	@BeforeClass
	public static void launchServer() throws DHCPServerInitException, SQLException {
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
	}
	
	@Before
	public void prepareDb() throws SQLException {
		QueryRunner qRunner = new QueryRunner();
		qRunner.update(conn, "DELETE FROM T_LEASE");
		node.getBackend().prepareBackend(node.getTopologyConfig());
		conn.commit();
	}
	
	@Test
	public void testSimpleCycle() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = (Date)formatter.parse("01/01/2007");
        long now = date.getTime();
		SystemTime.setForcedTime(date.getTime());
		SystemTime.setForcedMode(true);
		
		long poolId = -1;
		long res;
		// bad PoolId
		res = LeaseStoredProcedures.dhcpDiscover(conn, poolId, "001122334455", -1, null, 60);
		assertEquals(0L, res);
		
		// PoolID = 97721516032
		poolId = 97721516032L;
		res = LeaseStoredProcedures.dhcpDiscover(conn, poolId, "001122334455", -1, null, 60);
		assertEquals(3232235535L, res);
		DHCPLease lease = DataAccess.getLease(conn, res);
		assertEquals(res, lease.getIp());
		assertEquals(now, lease.getCreationDate());
		assertEquals(now, lease.getUpdateDate());
		assertEquals(now + 60000L, lease.getExpirationDate());
		assertEquals(now + 60000L, lease.getRecycleDate());
	}
	
	@After
	public void cleanupDb() throws SQLException {
		conn.rollback();
	}

	@AfterClass
	public static void stopServer() {
		try {
			DataAccess.shutdown(conn);
		} catch (SQLException e) {
			// exception is normal here, broken connection 
		}
	}
}
