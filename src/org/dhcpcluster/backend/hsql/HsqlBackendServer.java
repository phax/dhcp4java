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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hsqldb.Server;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class HsqlBackendServer {

	private static final Logger logger = Logger.getLogger(HsqlBackendServer.class.getName().toLowerCase());
	
	private Server sqlServer = null;
	private Connection conn = null;
	
	public HsqlBackendServer() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot load hsql driver", e);
			throw new RuntimeException(e);
		}
		
		sqlServer = new Server();
		sqlServer.setSilent(true);
		sqlServer.setTrace(true);
		sqlServer.setAddress("localhost");
		sqlServer.setDatabaseName(0, "dhcpcluster");
		sqlServer.setDatabasePath(0, "./backenddb/dhcpcluster");
		//sqlServer.setLogWriter(logger.)
		sqlServer.start();
		
		Runtime.getRuntime().addShutdownHook(new HsqlShutdownHook());
		
	}
	
	public void startServer() throws SQLException {
		conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpcluster", "sa", "");
	}
	
	class HsqlShutdownHook extends Thread {

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			//logger.entering("HsqlBackendServer:HsqlShutdownHook", "run");
			if (conn == null) {
				return;
			}
			try {
				logger.fine("issuing SHUTDOWN sql command");
				Statement st = conn.createStatement();
				st.executeUpdate("SHUTDOWN");
				logger.fine("SHUTDOWN sql command complete");
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Cannot SHUTDOWN db", e);
			}
			//logger.exiting("HsqlBackendServer:HsqlShutdownHook", "run");
		}
		
	}
}


