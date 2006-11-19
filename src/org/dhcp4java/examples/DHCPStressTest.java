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
package org.dhcp4java.examples;

import static org.dhcp4java.DHCPConstants.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.DHCPServerInitException;
import org.dhcp4java.server.DHCPStaticServlet;



/**
 * A simple client used for testing DHCPStaticServlet under stress conditions.
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class DHCPStressTest {
	private static final Logger logger = Logger.getLogger("org.dhcp4java.examples.dhcpstresstest");
	
	private DHCPPacket discover;
	private DHCPPacket request;
	private DatagramPacket discoverPacket;
	private DatagramPacket offerPacket = new DatagramPacket(new byte[1500], 1500);
	private DatagramPacket requestPacket;
	private DatagramPacket ackPacket = new DatagramPacket(new byte[1500], 1500);
    private DatagramSocket socket;
    
    private static final int NB_ITERATIONS = 100000;
    private static final String SERVER_ADDR = "127.0.0.1";
    private static final int SERVER_PORT = 6767;
    private static final String CLIENT_ADDR = "127.0.0.1";
    private static final int CLIENT_PORT = 6868;
	
	private void init() {
		try {
			InetAddress serverAddr = InetAddress.getByName(CLIENT_ADDR);

            this.discover = new DHCPPacket();

            this.discover.setOp(BOOTREQUEST);
            this.discover.setHtype(HTYPE_ETHER);
            this.discover.setHlen((byte) 6);
            this.discover.setHops((byte) 0);
            this.discover.setXid( (new Random()).nextInt() );
            this.discover.setSecs((short) 0);
            this.discover.setFlags((short) 0);
            this.discover.setChaddrHex("000802E7BFA5");

            this.discover.setDHCPMessageType(DHCPDISCOVER);
            this.discover.setOptionAsString(DHO_VENDOR_CLASS_IDENTIFIER, "MSFT5.0");
	        
	        byte[] discoverBytes = this.discover.serialize();
            this.discoverPacket = new DatagramPacket(discoverBytes, discoverBytes.length, serverAddr, SERVER_PORT);

            this.request = this.discover.clone();
            this.request.setDHCPMessageType(DHCPREQUEST);
            this.request.setCiaddr("10.0.0.1");
	        
	        byte[] requestBytes = this.request.serialize();
            this.requestPacket = new DatagramPacket(requestBytes, requestBytes.length, serverAddr, SERVER_PORT);

            this.socket = new DatagramSocket(CLIENT_PORT);
	        
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	
	private void run() {
		try {
			// warm-up
			
			for (int i=0; i<1000; i++) {
                this.socket.send(this.discoverPacket);
                this.socket.receive(this.offerPacket);

                this.socket.send(this.requestPacket);
                this.socket.receive(this.ackPacket);
			}
			
			Date timeBegin = new Date();
			for (int i=0; i<NB_ITERATIONS; i++) {
                this.socket.send(this.discoverPacket);
                this.socket.receive(this.offerPacket);

                this.socket.send(this.requestPacket);
                this.socket.receive(this.ackPacket);
			}
			
			Date timeEnd = new Date();
			long millis = timeEnd.getTime() - timeBegin.getTime();
			logger.info("Elapsed time "+millis/1000.0+" seconds");
			logger.info("Cycles/second "+(int) (NB_ITERATIONS*1000.0/millis));
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected exception", e);
		}
	}
	/**
	 * 
	 */
	private void startServer() throws DHCPServerInitException {
	    Properties stressProperties = new Properties();
	    stressProperties.put(DHCPCoreServer.SERVER_ADDRESS, SERVER_ADDR+ ':' +SERVER_PORT);
	    stressProperties.put(DHCPCoreServer.SERVER_THREADS, "1");
        DHCPCoreServer server = DHCPCoreServer.initServer(new DHCPStaticServlet(), stressProperties);
        new Thread(server).start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DHCPStressTest client = new DHCPStressTest();
		try {
			client.init();
			client.startServer();
			client.run();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected exception", e);
		}
		System.exit(0);
	}

}
