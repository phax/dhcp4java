/*
 *	This file is part of dhcp4java.
 *
 *	dhcp4java is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	dhcp4java is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Foobar; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * (c) 2006 Stephan Hadinger
 */
package sf.dhcp4java;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
public class Util {

    /**
     * Converts 32 bits int to IPv4 <tt>InetAddress</tt>.
     * 
     * @param val int representation of IPv4 address
     * @return the address object
     */
    public static final InetAddress int2InetAddress(int val) {
        byte[] value = { (byte) ((val & 0xFF000000) >>> 24),
                         (byte) ((val & 0X00FF0000) >>> 16),
                         (byte) ((val & 0x0000FF00) >>>  8),
                         (byte) ((val & 0x000000FF)) };
        try {
            return InetAddress.getByAddress(value);
        } catch (UnknownHostException e) {
            return null;
        }
    }
    /**
     * Converts IPv4 <tt>InetAddress</tt> to 32 bits int.
     * 
     * @param addr IPv4 address object
     * @return 32 bits int
     * @throws NullPointerException <tt>addr</tt> is <tt>null</tt>.
     * @throws IllegalArgumentException the address is not IPv4 (Inet4Address).
     */
    public static final int inetAddress2Int(InetAddress addr) {
        if (!(addr instanceof Inet4Address)) {
            throw new IllegalArgumentException("Only IPv4 supported");
        }

        byte[] addrBytes = addr.getAddress();
        return  ((addrBytes[0] & 0xFF) << 24) |
        		((addrBytes[1] & 0xFF) << 16) |
        		((addrBytes[2] & 0xFF) <<  8) |
        		((addrBytes[3] & 0xFF));
    }
}
