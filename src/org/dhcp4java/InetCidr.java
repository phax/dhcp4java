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
package org.dhcp4java;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.dhcp4java.Util;

/**
 * @author yshi7355
 *
 */
public class InetCidr implements Serializable {
	private static final long serialVersionUID = 1L;

    private final int addr;
    private final int mask;

    /**
     * Constructor for InetCidr.
     * 
     * <p>Takes a network address (IPv4) and a mask length
     * 
     * @param addr IPv4 address
     * @param mask mask lentgh (between 1 and 32)
     * @throws NullPointerException if addr is null
     * @throws IllegalArgumentException if addr is not IPv4
     */
    public InetCidr(InetAddress addr, int mask) {
        if (addr == null) {
            throw new NullPointerException("addr is null");
        }
        if (!(addr instanceof Inet4Address)) {
            throw new IllegalArgumentException("Only IPv4 addresses supported");
        }
        if (mask < 1 || mask > 32) {
            throw new IllegalArgumentException("Bad mask:" + mask + " must be between 1 and 32");
        }

        // apply mask to address
        this.addr = Util.inetAddress2Int(addr) & gCidrMask[mask];
        this.mask = mask;
    }
    
    public InetCidr(InetAddress addr, InetAddress netMask) {
    	if ((addr == null) || (netMask == null)) {
    		throw new NullPointerException();
    	}
    	if (!(addr instanceof Inet4Address) ||
    		!(netMask instanceof Inet4Address)) {
            throw new IllegalArgumentException("Only IPv4 addresses supported");
    	}
    	Integer mask = gCidr.get(netMask);
    	if (mask == null) {
    		throw new IllegalArgumentException("netmask: "+netMask+" is not a valid mask");
    	}
        this.addr = Util.inetAddress2Int(addr) & gCidrMask[mask];
        this.mask = mask;
        // TODO add to unit test
    }

    public String toString() {
        return Util.int2InetAddress(addr).getHostAddress()+ '/' + this.mask;
    }


    /**
     * @return Returns the addr.
     */
    public InetAddress getAddr() {
        return Util.int2InetAddress(addr);
    }
    /**
     * @return Returns the mask.
     */
    public int getMask() {
        return this.mask;
    }

    public int hashCode() {
        return this.addr ^ this.mask;
    }

    public boolean equals(Object obj) {
        if ((obj == null) || (!(obj instanceof InetCidr))) {
            return false;
        }
        InetCidr cidr = (InetCidr) obj;

        return ((this.addr == cidr.addr) &&
                 (this.mask == cidr.mask));
    }

    /**
     * Constructs a <tt>InetCidr</tt> provided an ip address and an ip mask.
     * 
     * <p>If the mask is not valid, an exception is raised.
     * 
     * @param addr the ip address (IPv4)
     * @param mask the ip mask
     * @return the cidr
     * @throws IllegalArgumentException if <tt>addr</tt> or <tt>mask</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException if the mask is not a valid one.
     */
    public static final InetCidr addrmask2Cidr(InetAddress addr, InetAddress mask) {
    	if (addr == null) {
            throw new IllegalArgumentException("addr must not be null");
    	}
        if (mask == null) {
            throw new IllegalArgumentException("mask must not be null");
        }

        final Integer maskIndex = gCidr.get(mask);

        if (maskIndex == null) {
        	throw new IllegalArgumentException("invalid mask: " + mask.getHostAddress());
        }

        return new InetCidr(addr, maskIndex);
    }

    /**
     * Returns an array of all cidr combinations with the provided ip address.
     * 
     * <p>The array is ordered from the most specific to the most general mask.
     * 
     * @param addr
     * @return array of all cidr possible with this address
     */
    public static InetCidr[] addr2Cidr(InetAddress addr) {
    	if (addr == null) {
            throw new IllegalArgumentException("addr must not be null");
    	}
        if (!(addr instanceof Inet4Address)) {
            throw new IllegalArgumentException("Only IPv4 addresses supported");
        }
        int        addrInt = Util.inetAddress2Int(addr);
        InetCidr[] cidrs   = new InetCidr[32];

        for (int i = cidrs.length; i >= 1; i--) {
            cidrs[32 - i] = new InetCidr(Util.int2InetAddress(addrInt & gCidrMask[i]), i);
        }
        return cidrs;
    }

    private static final String[] CIDR_MASKS = {
            "128.0.0.0",
            "192.0.0.0",
            "224.0.0.0",
            "240.0.0.0",
            "248.0.0.0",
            "252.0.0.0",
            "254.0.0.0",
            "255.0.0.0",
            "255.128.0.0",
            "255.192.0.0",
            "255.224.0.0",
            "255.240.0.0",
            "255.248.0.0",
            "255.252.0.0",
            "255.254.0.0",
            "255.255.0.0",
            "255.255.128.0",
            "255.255.192.0",
            "255.255.224.0",
            "255.255.240.0",
            "255.255.248.0",
            "255.255.252.0",
            "255.255.254.0",
            "255.255.255.0",
            "255.255.255.128",
            "255.255.255.192",
            "255.255.255.224",
            "255.255.255.240",
            "255.255.255.248",
            "255.255.255.252",
            "255.255.255.254",
            "255.255.255.255"
    };

    private static final Map<InetAddress, Integer> gCidr     = new HashMap<InetAddress, Integer>(48);
    private static final int[]                     gCidrMask = new int[33];

    static {
        try {
            gCidrMask[0] = 0;
            for (int i = 0; i < CIDR_MASKS.length; i++) {
                InetAddress mask = InetAddress.getByName(CIDR_MASKS[i]);

                gCidrMask[i + 1] = Util.inetAddress2Int(mask);
                gCidr.put(mask, i + 1);
            }
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to initialize CIDR");
        }
    }
}
