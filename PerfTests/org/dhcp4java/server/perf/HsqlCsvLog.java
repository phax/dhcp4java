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
package org.dhcp4java.server.perf;

import static org.apache.commons.dbutils.DbUtils.loadDriver;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dhcpcluster.backend.LogOutputStream;
import org.dhcpcluster.backend.hsql.DataAccess;
import org.hsqldb.Server;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class HsqlCsvLog {

	private static final Logger logger = Logger.getLogger(HsqlCsvLog.class);

	private static final PrintWriter		logWriter = new PrintWriter(new LogOutputStream(Level.INFO));
	private static final PrintWriter		errWriter = new PrintWriter(new LogOutputStream(Level.ERROR));
	
	private static Server sqlServer;
	
	public static void initServer() {
		if (!loadDriver("org.hsqldb.jdbcDriver")) {
			logger.fatal("Cannot load hsql driver org.hsqldb.jdbcDriver");
		}
		
		sqlServer = new Server();
		sqlServer.setErrWriter(errWriter);
		sqlServer.setLogWriter(logWriter);
		
		sqlServer.setSilent(true);
		sqlServer.setTrace(true);
		sqlServer.setAddress("localhost");
		sqlServer.setDatabaseName(0, "dhcpclusterPerf");
		sqlServer.setDatabasePath(0, "./PerfTests/db/dhcpcluster");
		//sqlServer.setLogWriter(logger.)
		sqlServer.start();
	}
	
	public static void purge(Connection conn) throws SQLException {
		assert(conn != null);
		QueryRunner qRunner = new QueryRunner();
		int res = qRunner.update(conn, "DELETE FROM T_LEASE_ARCHIVE");
		logger.debug("Delete all from T_LEASE_ARCHIVE: "+res+" deleted");
	}
	
	private static void insert1Log(Connection conn) throws SQLException {
		assert(conn != null);
		DataAccess.insertLeaseArchive(conn, 123L, null, null, null, "foo", "bar", 2);
	}
	
	private static void bench(Connection conn, int len) throws SQLException {
		long now = System.currentTimeMillis();
		Date date = new Date(now);
		
		for (int i=0; i<len; i++) {
			now++;
			DataAccess.insertLeaseArchive(conn, now, date, date, date, "foo", "bar", 2);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws SQLException {
		Connection conn;
		int len = 100000;
		
		initServer();
		conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/dhcpclusterPerf", "sa", "");
		purge(conn);
		long before = System.currentTimeMillis();
		bench(conn, len);
		long after = System.currentTimeMillis();
		//purge(conn);
		//insert1Log(conn);
		logger.info("After-Before: "+(after-before)+", rq/s:"+(len*1000/(after-before)));
		try {
			DataAccess.shutdownCompact(conn);
		} catch (SQLException e) {
			long afterCompact = System.currentTimeMillis();
			logger.info("AfterCompact - After: "+(afterCompact-after)+", rq/s:"+(len*1000/(afterCompact-after)));
		}
		System.exit(0);
	}

}
