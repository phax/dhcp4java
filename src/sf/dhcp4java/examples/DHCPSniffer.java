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

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import sf.dhcp4java.DHCPPacket;
import sf.dhcp4java.DHCPConstants;


/**
 * A simple DHCP sniffer.
 *
 * @author Stephan Hadinger
 * @version 0.50
 */
public class DHCPSniffer {
    private DHCPSniffer() {
    }

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(DHCPConstants.BOOTP_REQUEST_PORT);

            while (true) {
                DatagramPacket pac = new DatagramPacket(new byte[1500], 1500);
                DHCPPacket     dhcp;

                socket.receive(pac);
                dhcp = DHCPPacket.getPacket(pac);
                System.out.println(dhcp.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
