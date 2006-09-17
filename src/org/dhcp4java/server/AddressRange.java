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
package org.dhcp4java.server;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.logging.Logger;

import org.dhcp4java.InetCidr;
import org.dhcp4java.Util;

/**
 * 
 * 
 * <p>This class is immutable.
 * 
 * @author Stephan Hadinger
 * @version 0.60
 */
public final class AddressRange implements Serializable, Comparable {

	/*
	 * Invariant: rangeStart <= rangeEnd
	 */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(AddressRange.class.getName().toLowerCase());
    
    private final int rangeStart;
    private final int rangeEnd;
    
    public AddressRange(InetAddress rangeStart, InetAddress rangeEnd) {
    	if ((rangeStart == null) || (rangeEnd == null)) {
    		throw new NullPointerException("rangeStart ou rangeEnd are null");
    	}
    	
    	// TODO check parameters
    	this.rangeStart = Util.inetAddress2Int(rangeStart);
    	this.rangeEnd = Util.inetAddress2Int(rangeEnd);
    	if ((this.rangeStart & 0xFFFFFFFFL) > (this.rangeEnd & 0xFFFFFFFFL)) {
    		throw new IllegalArgumentException("rangeStart is greater than rangeEnd");
    	}
    }
    
    public boolean isInRange(InetAddress adr) {
    	if (adr == null) {
    		throw new NullPointerException("adr is null");
    	}
    	if (!(adr instanceof Inet4Address)) {
    		throw new IllegalArgumentException("adr is not IPv4 address");
    	}
    	// convert to long to do some unsigned int comparisons
    	long startL = rangeStart & 0xFFFFFFFFL;
    	long endL = rangeEnd & 0xFFFFFFFFL;
    	long adrL = Util.inetAddress2Int(adr) & 0xFFFFFFFFL;
    	
    	return (adrL >= startL) && (adrL <= endL);
    }

	/**
	 * @return Returns the rangeEnd.
	 */
	public InetAddress getRangeEnd() {
		return Util.int2InetAddress(rangeEnd);
	}

	/**
	 * @return Returns the rangeStart.
	 */
	public InetAddress getRangeStart() {
		return Util.int2InetAddress(rangeStart);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
        if ((obj == null) || (!(obj instanceof InetCidr))) {
            return false;
        }
        AddressRange range = (AddressRange) obj;
        return (this.rangeStart == range.rangeStart) &&
        		(this.rangeEnd == range.rangeEnd);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return rangeStart ^ (rangeEnd >> 2);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Util.int2InetAddress(rangeStart).getHostAddress() + "-" + Util.int2InetAddress(rangeEnd).getHostAddress();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
		AddressRange range = (AddressRange) o;
		if (unsignedInt(range.rangeStart) < unsignedInt(this.rangeStart)) {
			return -1;
		} else if (unsignedInt(range.rangeStart) > unsignedInt(this.rangeStart)) {
			return 1;
		} else if (unsignedInt(range.rangeEnd) < unsignedInt(this.rangeEnd)) {
			return -1;
		} else if (unsignedInt(range.rangeEnd) > unsignedInt(this.rangeEnd)) {
			return 1;
		} else {
			return 0;
		}
	}

	private static final long unsignedInt(int i) {
		return i & 0xFFFFFFFFL;
	}
}
