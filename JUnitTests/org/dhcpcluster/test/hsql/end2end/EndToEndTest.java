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
package org.dhcpcluster.test.hsql.end2end;

import static org.dhcp4java.DHCPConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPPacket;
import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.SystemTime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

import static org.junit.Assert.*;

public class EndToEndTest {

	public static junit.framework.Test suite() {
       return new JUnit4TestAdapter(EndToEndTest.class);
    }
	
	private static DatagramSocket socketDirect = null;
	private static DatagramSocket socketRelay = null;
	private static InetAddress serverAddr;
	private static int serverPort;

	private static long originOfTime;

	private static DHCPClusterNode node = null;
	
	@BeforeClass
	public static void prepareSocket() throws Exception {

		// change time reference
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = (Date)formatter.parse("01/01/2007");
        originOfTime = date.getTime();
		SystemTime.setForcedTime(originOfTime);
		SystemTime.setForcedMode(true);
		
		Properties props = new Properties();
		
		props.setProperty("config.reader", "org.dhcpcluster.config.xml.XmlConfigReader");
		props.setProperty("config.xml.file", "./JUnitTests/org/dhcpcluster/test/hsql/conf/configtest.xml");
		
		props.setProperty("backend.hsql.address", "localhost");
		props.setProperty("backend.hsql.dbnumber", "0");
		props.setProperty("backend.hsql.dbname", "dhcpcluster");
		props.setProperty("backend.hsql.dbpath", "./JUnitTests/org/dhcpcluster/test/hsql/db/dhcpcluster");

    	node = new DHCPClusterNode(props);
    	Thread dispatchThread = new Thread(node);
    	dispatchThread.start();
		
		serverAddr = InetAddress.getByName("127.0.0.1");
		serverPort = 6767;
        socketDirect = new DatagramSocket(DHCPConstants.BOOTP_REPLY_PORT);
        socketDirect.setSoTimeout(1000);		// 1 sec
        socketRelay = new DatagramSocket(DHCPConstants.BOOTP_REQUEST_PORT);
        socketRelay.setSoTimeout(1000);		// 1 sec
	}
	
	@AfterClass
	public static void closeSocket() {
		if (socketDirect != null) {
			socketDirect.close();
			socketDirect = null;
		}
		if (socketRelay != null) {
			socketRelay.close();
			socketRelay = null;
		}
		node.stop();
	}
	
	@Before
	public void flushSocket() throws IOException {
        DatagramPacket pac = new DatagramPacket(new byte[1500], 1500);
        try {
        	while (true) {
                socketDirect.receive(pac);
        	}
        } catch (SocketTimeoutException e) {
        	// normal exit
        }
        try {
        	while (true) {
                socketRelay.receive(pac);
        	}
        } catch (SocketTimeoutException e) {
        	// normal exit
        }
	}
	
	@Test
	public void testDhcpDiscover() throws IOException {
		DHCPPacket discover = new DHCPPacket();

        discover.setOp(BOOTREQUEST);
        discover.setHtype(HTYPE_ETHER);
        discover.setHlen((byte) 6);
        discover.setHops((byte) 0);
        discover.setXid( (new Random()).nextInt() );
        discover.setSecs((short) 0);
        discover.setFlags((short) 0);
        discover.setChaddrHex("001122334455");
        discover.setGiaddr("127.0.0.1");
        discover.setDHCPMessageType(DHCPConstants.DHCPDISCOVER);
        discover.setOptionAsString(DHO_VENDOR_CLASS_IDENTIFIER, "MSFT5.0");
        byte[] discoverBytes = discover.serialize();
        DatagramPacket discoverPacket = new DatagramPacket(discoverBytes, discoverBytes.length, serverAddr, serverPort);
        DatagramPacket responsePacket = new DatagramPacket(new byte[1500], 1500);
        
        socketRelay.send(discoverPacket);
        socketRelay.receive(responsePacket);
        DHCPPacket response = DHCPPacket.getPacket(responsePacket);
        assertEquals(BOOTREPLY, response.getOp());
	}

}
