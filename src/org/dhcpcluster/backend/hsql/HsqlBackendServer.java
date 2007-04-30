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
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dhcp4java.HardwareAddress;
import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.backend.DHCPBackendIntf;
import org.dhcpcluster.backend.LogOutputStream;
import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.config.TopologyConfig;
import org.dhcpcluster.struct.DHCPLease;
import org.hsqldb.Server;

import static org.apache.commons.dbutils.DbUtils.*;
import static org.dhcpcluster.backend.hsql.DataAccess.*;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class HsqlBackendServer implements DHCPBackendIntf {

	private static final Logger logger = Logger.getLogger(HsqlBackendServer.class);
		
	private Server sqlServer = null;
	private Connection conn0 = null;
	
	private final PrintWriter		debugWriter = new PrintWriter(new LogOutputStream(Level.DEBUG));
	private final PrintWriter		logWriter = new PrintWriter(new LogOutputStream(Level.INFO));
	private final PrintWriter		errWriter = new PrintWriter(new LogOutputStream(Level.ERROR));

    private String			hsqlAddress = null;
    private String			hsqlDbName = null;
    private String			hsqlDbPath = null;
    private int			hsqlDbNumber = 0;
	
	public HsqlBackendServer(Properties props) {
		parseHsqlProperties(props);
		
		if (!loadDriver(HSQL_DRIVER)) {
			logger.fatal("Cannot load hsql driver "+HSQL_DRIVER);
		}
		
		sqlServer = new Server();
		sqlServer.setErrWriter(errWriter);
		sqlServer.setLogWriter(debugWriter);
		
		sqlServer.setSilent(true);
		sqlServer.setLogWriter(logWriter);
		sqlServer.setTrace(true);
		sqlServer.setAddress(hsqlAddress);
		sqlServer.setDatabaseName(hsqlDbNumber, hsqlDbName);
		sqlServer.setDatabasePath(hsqlDbNumber, hsqlDbPath);
		//sqlServer.setLogWriter(logger.)
		sqlServer.start();
	}
	
	public void startServer() throws SQLException {
		DriverManager.setLogWriter(debugWriter);
		conn0 = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpcluster", "sa", "");
		
		// testing
//		DataAccess.getLeasesFromRanges(conn0, 10, 20);
//		DataAccess.getLease(conn0, 10);
//		DataAccess.getLease(conn0, 11);
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
		try {
			conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpcluster", "sa", "");
			conn.setAutoCommit(false);
			
			// delete all from T_POOL & T_POOL_SET
			deleteBubbles(conn);
			deletePools(conn);
			deletePoolSets(conn);
			conn.commit();
			
			insertPoolsAndPoolSets(conn, topology.getSubnetCollection());
			conn.commit();
			
		} catch (SQLException e) {
			logger.fatal("Unexpected SQLException when preparing backend", e);
		} finally {
			closeQuietly(conn);
		}
	}

	public void shutdown() {
		try {
			logger.debug("issuing SHUTDOWN sql command");
			DataAccess.shutdown(conn0);
			logger.debug("SHUTDOWN sql command complete");
		} catch (SQLException e) {
			logger.error("Cannot SHUTDOWN db", e);
		} finally {
			closeQuietly(conn0);
		}
	}
	
	private void parseHsqlProperties(Properties props) throws ConfigException {
		hsqlAddress = props.getProperty(HSQL_ADDRESS, "localhost");
		String propHsqlDbNumber = props.getProperty(HSQL_DBNUMBER);
		if (propHsqlDbNumber != null) {
			hsqlDbNumber = Integer.parseInt(propHsqlDbNumber);
		}
		hsqlDbName = props.getProperty(HSQL_DBNAME);
		if (hsqlDbName == null) {
			throw new ConfigException("Property "+HSQL_DBNAME+" is null");
		}
		hsqlDbPath = props.getProperty(HSQL_DBPATH);
		if (hsqlDbPath == null) {
			throw new ConfigException("Property "+HSQL_DBPATH+" is null");
		}
	}
	
	private static final String		HSQL_ADDRESS="backend.hsql.address";
	private static final String		HSQL_DBNUMBER="backend.hsql.dbnumber";
	private static final String		HSQL_DBNAME="backend.hsql.dbname";
	private static final String		HSQL_DBPATH="backend.hsql.dbpath";

	private static final String		HSQL_DRIVER = "org.hsqldb.jdbcDriver";
}


