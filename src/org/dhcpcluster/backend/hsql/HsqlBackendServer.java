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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.dbutils.QueryLoader;
import org.dhcp4java.HardwareAddress;
import org.dhcp4java.Util;
import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.backend.DHCPBackendIntf;
import org.dhcpcluster.backend.LogOutputStream;
import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.config.TopologyConfig;
import org.dhcpcluster.struct.AddressRange;
import org.dhcpcluster.struct.DHCPLease;
import org.dhcpcluster.struct.Subnet;
import org.hsqldb.Server;

import static org.apache.commons.dbutils.DbUtils.*;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class HsqlBackendServer implements DHCPBackendIntf {

	private static final Logger logger = Logger.getLogger(HsqlBackendServer.class.getName().toLowerCase());
	

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
	
	private Server sqlServer = null;
	private Connection conn1 = null;
	
	private final PrintWriter		logWriter = new PrintWriter(new LogOutputStream(Level.INFO));
	private final PrintWriter		errWriter = new PrintWriter(new LogOutputStream(Level.SEVERE));
	
	public HsqlBackendServer() {
		if (!loadDriver("org.hsqldb.jdbcDriver")) {
			logger.severe("Cannot load hsql driver org.hsqldb.jdbcDriver");
		}
		
		sqlServer = new Server();
		sqlServer.setErrWriter(errWriter);
		sqlServer.setLogWriter(logWriter);
		
		sqlServer.setSilent(true);
		sqlServer.setTrace(true);
		sqlServer.setAddress("localhost");
		sqlServer.setDatabaseName(0, "dhcpcluster");
		sqlServer.setDatabasePath(0, "./backenddb/dhcpcluster");
		//sqlServer.setLogWriter(logger.)
		sqlServer.start();
	}
	
	public void startServer() throws SQLException {
		conn1 = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpcluster", "sa", "");
	}
	
	/* (non-Javadoc)
	 * @see org.dhcpcluster.backend.DHCPBackendIntf#discover(long, org.dhcp4java.HardwareAddress, java.net.InetAddress, int)
	 */
	public DHCPLease discover(long networkId, HardwareAddress mac, InetAddress giaddr, int clientClass) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dhcpcluster.backend.DHCPBackendIntf#init(org.dhcpcluster.DHCPClusterNode, java.util.Properties)
	 */
	public void init(DHCPClusterNode dhcpCoreServer, Properties configProperties) throws ConfigException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Called when topology is reloaded.
	 *
	 */
	public void prepareBackend(TopologyConfig topology) {
		Connection conn = null;
		Statement st = null;
		PreparedStatement pstPoolSet = null;
		PreparedStatement pstPool = null;
		try {
			conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpcluster", "sa", "");
			conn.setAutoCommit(false);
			st = conn.createStatement();
			int res;
			
			// delete all from T_POOL & T_POOL_SET
			res = st.executeUpdate(DELETE_T_POOL);
			logger.log(Level.INFO, "Delete all from T_POOL: "+res+" deleted");
			res = st.executeUpdate(DELETE_T_POOL_SET);
			logger.log(Level.INFO, "Delete all from T_POOL_SET: "+res+" deleted");
			
			st.close();
			st = null;
			pstPoolSet = conn.prepareStatement(INSERT_T_POOL_SET);
			pstPool = conn.prepareStatement(INSERT_T_POOL);
			
			for (Subnet subnet : topology.getSubnetCollection()) {
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
			conn.commit();
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Unexpected SQLException when preparing backend", e);
		} finally {
			closeQuietly(st);
			closeQuietly(pstPoolSet);
			closeQuietly(pstPool);
			closeQuietly(conn);
		}
	}

	public void shutdown() {
		if (conn1 == null) {
			return;
		}
		Statement st = null;
		try {
			logger.fine("issuing SHUTDOWN sql command");
			st = conn1.createStatement();
			st.executeUpdate(SHUTDOWN);
			logger.fine("SHUTDOWN sql command complete");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cannot SHUTDOWN db", e);
		} finally {
			closeQuietly(st);
			closeQuietly(conn1);
		}
	}

	private static final String	SHUTDOWN = queries.get("SHUTDOWN");
	
	private static final String	DELETE_T_POOL = queries.get("DELETE_T_POOL");
	private static final String	DELETE_T_POOL_SET = queries.get("DELETE_T_POOL_SET");

	private static final String	INSERT_T_POOL_SET = queries.get("INSERT_T_POOL_SET");
	private static final String	INSERT_T_POOL = queries.get("INSERT_T_POOL");

}


