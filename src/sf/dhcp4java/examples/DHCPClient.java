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
package sf.dhcp4java.examples;

import java.util.Random;

import sf.dhcp4java.DHCPPacket;

import static sf.dhcp4java.DHCPConstants.*;


/**
 * Example of DHCP Client (under construction).
 * 
 * @author Stephan Hadinger
 * @version 0.50
 */
public class DHCPClient {
    static byte[] macAddress = {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05
    };

    public static void main(String[] args) {
        // first send discover
        DHCPPacket discover = new DHCPPacket();
        
        discover.setOp(BOOTREQUEST);
        discover.setHtype(HTYPE_ETHER);
        discover.setHlen((byte) 6);
        discover.setHops((byte) 0);
        discover.setXid( (new Random()).nextInt() );
        discover.setSecs((short) 0);
        discover.setFlags((short) 0);
        discover.setChaddr(macAddress);
    }
}
