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
import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;

import static org.dhcpcluster.backend.hsql.DataAccess.queries;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class LeaseStoredProcedures {

	private static final Logger logger = Logger.getLogger(LeaseStoredProcedures.class.getName().toLowerCase());

	private static final QueryRunner 			qRunner = new QueryRunner();
	private static final ResultSetHandler 	arrayHandler = new ArrayHandler();
	
	public static long findDiscoverLease(Connection conn, long poolId, String mac) throws SQLException {
		// find first free bubble for poolId
		Object[] res = (Object[]) qRunner.query(conn, SELECT_BUBBLE_FROM_POOL_SET, (Long) poolId, arrayHandler);
		if (res == null) {
			logger.warning("No lease left for poolId="+poolId);
			return 0;
		}
		assert(res.length == 3);
		long bubbleId = (Long) res[0];
		long startIp = (Long) res[1];
		long endIp = (Long) res[2];
		
		if (startIp < endIp) {
			// we shrink the bubble
			Object[] args = new Object[2];
			args[0] = (Long) (startIp + 1);
			args[1] = (Long) bubbleId;
			if (qRunner.update(conn, UPDATE_BUBBLE_START_IP, args) != 1) {
				logger.severe("Cannot update bubble startIp="+(startIp+1)+" bubble_id="+bubbleId);
				return 0;
			}
		} else {
			// delete bubble which is now empty
			if (qRunner.update(conn, DELETE_BUBBLE_ID, (Long) bubbleId) != 1) {
				logger.severe("Cannot delete bubble bubble_id="+bubbleId);
				return 0;
			}
		}
		// create of modify lease
		
		
		return startIp;		// this is the ip of the prepared lease
	}

	private static final String	SELECT_BUBBLE_FROM_POOL_SET = queries.get("SELECT_BUBBLE_FROM_POOL_SET");
	private static final String	UPDATE_BUBBLE_START_IP = queries.get("UPDATE_BUBBLE_START_IP");
	private static final String	DELETE_BUBBLE_ID = queries.get("DELETE_BUBBLE_ID");
}
