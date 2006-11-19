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
package org.dhcp4java.server.filter;

import java.util.regex.Pattern;

import org.dhcp4java.DHCPPacket;

/**
 * 
 * Class is immutable.
 * 
 * @author Stephan Hadinger
 * @version 0.70
 *
 */
public final class StringOptionFilter implements RequestFilter {
	
	public enum CompareMode { EXACT, CASE_INSENSITIVE, REGEX };
	
	private final byte code;
	private final String compareString;
	private final CompareMode compareMode;
	private final Pattern comparePattern;
	
	public StringOptionFilter(byte code, String compareString, CompareMode compareMode) {
		if (compareString == null) {
			throw new NullPointerException("compareString is null");
		}
		this.code = code;
		this.compareString = compareString;
		this.compareMode = compareMode;
		if (compareMode == CompareMode.REGEX) {		// create the regex comparator
			comparePattern = Pattern.compile(compareString);
		} else {
			comparePattern = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.dhcp4java.server.filter.RequestFilter#isRequestAccepted(org.dhcp4java.DHCPPacket)
	 */
	public boolean isRequestAccepted(DHCPPacket request) {
		if (request == null) {
			throw new NullPointerException("request is null");
		}
		String value = request.getOptionAsString(code);
		switch (compareMode) {
		case EXACT:
			return compareString.equals(value);
		case CASE_INSENSITIVE:
			return compareString.equalsIgnoreCase(value);
		case REGEX:
			return comparePattern.matcher(value).matches();
		default:
			return false;
		}
	}
	
	

}
