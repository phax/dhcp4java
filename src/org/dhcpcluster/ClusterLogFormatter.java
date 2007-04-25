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
package org.dhcpcluster;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class ClusterLogFormatter extends Formatter {

    Date dat = new Date();
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    
    private static final String DATE_FORMAT = "[yyyy/MM/dd:HH:mm:ss]";
	// Line separator string. This is the value of the line.separator
	// property at the moment that the SimpleFormatter was created.
	private String lineSeparator = System.getProperty("line.separator");

	/**
	 * Format the given LogRecord.
	 * 
	 * @param record
	 *            the log record to be formatted.
	 * @return a formatted log record
	 */
	@Override
	public synchronized String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();
		
		// Minimize memory allocations here.
		dat.setTime(record.getMillis());
		sb.append(dateFormat.format(dat));
		sb.append(" ");
		String level = record.getLevel().getName();
		sb.append(level);
		for (int i=level.length(); i<"WARNING".length(); i++) {
			sb.append(' ');
		}
		sb.append(": ");
		sb.append(formatMessage(record));

		sb.append(" (");
		if (record.getSourceClassName() != null) {
			sb.append(record.getSourceClassName());
		} else {
			sb.append(record.getLoggerName());
		}
		if (record.getSourceMethodName() != null) {
			sb.append(" ");
			sb.append(record.getSourceMethodName());
		}
		sb.append(")");
		sb.append(lineSeparator);
		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
				// do nothing
			}
		}
		return sb.toString();
	}

}
