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

import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dhcp4java.HardwareAddress;
import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.backend.DHCPBackendIntf;
import org.dhcpcluster.backend.LogOutputStream;
import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.config.TopologyConfig;
import org.dhcpcluster.struct.DHCPLease;
import org.dhcpcluster.struct.Subnet;
import org.hsqldb.Server;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class HsqlBackendServer implements DHCPBackendIntf {

	private static final Logger logger = Logger.getLogger(HsqlBackendServer.class.getName().toLowerCase());
	
	private Server sqlServer = null;
	private Connection conn = null;
	
	private final PrintWriter		logWriter = new PrintWriter(new LogOutputStream(Level.INFO));
	private final PrintWriter		errWriter = new PrintWriter(new LogOutputStream(Level.SEVERE));
	
	public HsqlBackendServer() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot load hsql driver", e);
			throw new RuntimeException(e);
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
		conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpcluster", "sa", "");
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
		if (conn == null) {
			throw new RuntimeException("conn is null, cannot prepare backend");
		}
		Statement st = null;
		PreparedStatement pst = null;
		try {
			st = conn.createStatement();
			int res;
			
			// delete all from T_POOL & T_POOL_SET
			res = st.executeUpdate(DELETE_T_POOL);
			logger.log(Level.INFO, "Delete all from T_POOL: "+res+" deleted");
			res = st.executeUpdate(DELETE_T_POOL_SET);
			logger.log(Level.INFO, "Delete all from T_POOL_SET: "+res+" deleted");
			
			st.close();
			st = null;
			pst = conn.prepareStatement(INSERT_T_POOL_SET);
			
			for (Subnet subnet : topology.getSubnetCollection()) {
				pst.setLong(1, subnet.getCidr().toLong());
				res = pst.executeUpdate();
				if (res != 1) {
					throw new SQLException("Cannot update T_POOL_SET");
				}
			}
			conn.commit();
			
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Unexpected SQLException when preparing backend");
		} finally {
			closeStatement(st);
			st = null;
			closeStatement(pst);
			pst = null;
		}
	}

	public void shutdown() {
		if (conn == null) {
			return;
		}
		Statement st = null;
		try {
			logger.fine("issuing SHUTDOWN sql command");
			st = conn.createStatement();
			st.executeUpdate(SHUTDOWN);
			logger.fine("SHUTDOWN sql command complete");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Cannot SHUTDOWN db", e);
		} finally {
			closeAll(conn, st);
			st = null;
			conn = null;
		}
	}
	
	private static final void closeAll(Connection conn, Statement st) {
		try {
			if (conn != null) {
				if (st != null) {
					st.close();
				}
				conn.close();
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "SQLException when closing Connection and Statement", e);
		}
	}
	
	private static final void closeStatement(Statement st) {
		try {
			if (st != null) {
					st.close();
				}
		} catch (SQLException e) {
				logger.log(Level.WARNING, "SQLException when closing Statement", e);
		}
	}

	private static final String	SHUTDOWN = "SHUTDOWN";
	
	private static final String	DELETE_T_POOL = "DELETE FROM T_POOL";
	private static final String	DELETE_T_POOL_SET = "DELETE FROM T_POOL_SET";
	
	private static final String	INSERT_T_POOL_SET = "INSERT INTO T_POOL_SET (SET_ID) VALUES (?)";

}


