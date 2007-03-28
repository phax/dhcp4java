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
package org.dhcpcluster.backend;

import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class LogOutputStream extends OutputStream {

	private static final Logger logger = Logger.getLogger(LogOutputStream.class);
	
	private final Level		logLevel;
	
	public LogOutputStream(Level logLevel) {
		this.logLevel = logLevel;
	}
	
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) {
		String s = bytesToString(b, off, len);
		log(s);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) {
		// TODO Auto-generated method stub
		
	}
	
	private void log(String message) {
		while ((message.endsWith("\r")) || (message.endsWith("\n"))) {
			message = message.substring(0, message.length() - 1);
		}
		logger.log(logLevel, message);
	}

    static String bytesToString(byte[] buf, int src, int len) {
        if (buf == null) { return ""; }
        if (src < 0) {
            len += src;    // reduce length
            src = 0;
        }
        if (len <= 0) { return ""; }
        if (src >= buf.length) { return ""; }
        if (src + len > buf.length) { len = buf.length - src; }
        // string should be null terminated or whole buffer
        // first find the real lentgh
        for (int i=src; i<src+len; i++) {
            if (buf[i] == 0) {
                len = i - src;
                break;
            }
        }

        char[] chars = new char[len];

        for (int i = src; i < src + len; i++) {
            chars[i - src] = (char) buf[i];
        }
        return new String(chars);
    }


}
