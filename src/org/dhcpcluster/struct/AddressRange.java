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
package org.dhcpcluster.struct;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.dhcp4java.InetCidr;
import org.dhcp4java.Util;

/**
 * This class represents a linear IPv4 address range.
 * 
 * <p>Invariants:
 * <ul>
 * 	<li>only IPv4 addresses are supported
 * 	<li>lower address <= higher address
 * </ul>
 * 
 * <p>This class is immutable.
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public final class AddressRange implements Serializable, Comparable {

	/*
	 * Invariant: rangeStart <= rangeEnd
	 */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AddressRange.class);
    
    /** first ip address in the range (inclusive) */
    private final long rangeStart;
    /** last ip address in the range (inclusive) */
    private final long rangeEnd;
    
    /**
     * Constructor for AddressRange. This class is immutable.
     * 
     * @param rangeStart first ip address in range, inclusive.
     * @param rangeEnd last ip address in range, inclusive.
     * @throws NullPointerException <tt>rangeStart</tt> or <tt>rangeEnd</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException address is not IPv4
     */
    public AddressRange(InetAddress rangeStart, InetAddress rangeEnd) {
    	if ((rangeStart == null) || (rangeEnd == null)) {
    		throw new NullPointerException("rangeStart ou rangeEnd are null");
    	}
    	if ((!(rangeStart instanceof Inet4Address)) || (!(rangeEnd instanceof Inet4Address))) {
    		throw new IllegalArgumentException("address is not IPv4");
    	}
    	
    	this.rangeStart = Util.inetAddress2Long(rangeStart);
    	this.rangeEnd = Util.inetAddress2Long(rangeEnd);
    	if (this.rangeStart > this.rangeEnd) {
    		throw new IllegalArgumentException("rangeStart is greater than rangeEnd");
    	}
    }
    
    /**
     * Checks whether the specified address is contained in the range
     * 
     * @param adr address to check against the range
     * @return true if the address is contained in the range.
     * @throws NullPointerException <tt>adr</tt> is <tt>null</t>
     * @throws IllegalArgumentException <tt>adr</tt> is not IPv4.
     */
    public boolean isInRange(InetAddress adr) {
    	if (adr == null) {
    		throw new NullPointerException("adr is null");
    	}
    	if (!(adr instanceof Inet4Address)) {
    		throw new IllegalArgumentException("adr is not IPv4 address");
    	}
    	// convert to long to do some unsigned int comparisons
    	long adrL = Util.inetAddress2Long(adr);
    	
    	return (adrL >= rangeStart) && (adrL <= rangeEnd);
    }

	/**
	 * @return Returns the rangeEnd.
	 */
	public InetAddress getRangeEnd() {
		return Util.long2InetAddress(rangeEnd);
	}

	/**
	 * @return Returns the rangeStart.
	 */
	public InetAddress getRangeStart() {
		return Util.long2InetAddress(rangeStart);
	}
	
	public long getRangeStartLong() {
		return rangeStart;
	}
	
	public long getRangeEndLong() {
		return rangeEnd;
	}

	/**
     * returns true if two <tt>DHCPOption</tt> objects are equal, i.e. have same <tt>code</tt>
     * and same <tt>value</tt>. 
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


	/**
     * Returns hashcode.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (rangeStart ^ (rangeEnd >> 2));
	}


    /**
     * Returns a detailed string representation of the DHCP datagram.
     * 
     * @return a string representation of the object.
     */
	@Override
	public String toString() {
		return Util.long2InetAddress(rangeStart).getHostAddress() + "-" + Util.long2InetAddress(rangeEnd).getHostAddress();
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, 
	 * 			or greater than the specified object.
	 */
	public int compareTo(Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
		AddressRange range = (AddressRange) o;
		if (range.rangeStart < this.rangeStart) {
			return -1;
		} else if (range.rangeStart > this.rangeStart) {
			return 1;
		} else if (range.rangeEnd < this.rangeEnd) {
			return -1;
		} else if (range.rangeEnd > this.rangeEnd) {
			return 1;
		} else {
			return 0;
		}
	}

}
