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

import java.sql.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;
import org.dhcpcluster.backend.hsql.LeaseStoredProcedures;
import org.dhcpcluster.struct.DHCPLease;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class Log4jLog {

	private static final Logger logger = Logger.getLogger(Log4jLog.class);
	

	private static void bench(int len) {
		long now = System.currentTimeMillis();
		Date date = new Date(now);
		
		for (int i=0; i<len; i++) {
			now++;
			LeaseStoredProcedures.logLease(now, date, date, date, DHCPLease.Status.fromInt(1), DHCPLease.Status.fromInt(2), "foo", "bar", null);
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure(Loader.getResource("log4j.xml"));
		int len = 5;
		
		long before = System.currentTimeMillis();
		bench(len);
		long after = System.currentTimeMillis();
		logger.info("After-Before: "+(after-before)+", rq/s:"+(len*1000/(after-before)));
	}

}
