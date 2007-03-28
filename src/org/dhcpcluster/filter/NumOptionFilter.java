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
package org.dhcpcluster.filter;

import org.dhcp4java.DHCPPacket;

/**
 * 
 * Class is immutable.
 * 
 * @author Stephan Hadinger
 * @version 0.71
 *
 */
public final class NumOptionFilter implements RequestFilter {

	public enum CompareOp { EQ, NE, GT, LT, GE, LE }
	
	private final byte code;
	private final CompareOp compareOp;
	private final int compareValue;
	
	public NumOptionFilter(byte code, int compareValue, CompareOp compareOp) {
		this.code = code;
		this.compareValue = compareValue;
		this.compareOp = compareOp;
	}
	
	/* (non-Javadoc)
	 * @see org.dhcpcluster.filter.RequestFilter#isRequestAccepted(org.dhcp4java.DHCPPacket)
	 */
	public boolean isRequestAccepted(DHCPPacket request) {
		if (request == null) {
			throw new NullPointerException("request is null");
		}
		Integer value = request.getOptionAsNum(code);
		if (value == null) {
			return false;
		}
		switch (compareOp) {
		case EQ:
			return value == compareValue;
		case NE:
			return value != compareValue;
		case GT:
			return value >  compareValue;
		case LT:
			return value <  compareValue;
		case GE:
			return value >= compareValue;
		case LE:
			return value <= compareValue;
		default:
			return false;
		}
	}

	
}
