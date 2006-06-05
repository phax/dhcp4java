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
package sf.dhcp4java.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import sf.dhcp4java.DHCPConstants;
import sf.dhcp4java.DHCPOption;
import sf.dhcp4java.DHCPPacket;

/**
 * A simple generic DHCP Relay.
 * 
 * @author Stephan Hadinger
 * @version 0.50
 */
public class DHCPRelay {

    // instances of relays
    private RelayThread serverToClientThread = null;
    private RelayThread clientToServerThread = null;
    
    private DatagramSocket serverSocket = null;
    private DatagramSocket relaySocket = null;
    static protected final int PACKET_SIZE = 1500; // default MTU for ethernet
    
    private InetAddress targetServer = null;
    private int targetPort = 0;
    
    private byte[] giaddr = null;
    private byte[] giaddrEmpy = { (byte) 0, (byte) 0, (byte) 0, (byte) 0 };
    private byte[] option82 = null;

    private static final Logger logger = Logger.getLogger("sf.dhcp4java.genericdhcprelay");

    /**
     * Constructor
     */
    public DHCPRelay() {
        super();
        serverToClientThread = new ServerToClientThread();
        clientToServerThread = new ClientToServerThread();
    }
    /**
     * 
     */
    public void init() {
        try {
            Properties props = new Properties();
            
            InputStream propFileStream = this.getClass().getResourceAsStream("/DHCPRelay.properties");
            props.load(propFileStream);
            
            InetAddress saddress = InetAddress.getByName(props.getProperty(SERVER_ADDRESS, SERVER_ADDRESS_DEFAULT));
            int sport = Integer.parseInt(props.getProperty(SERVER_PORT, SERVER_PORT_DEFAULT));
            serverSocket = new DatagramSocket(sport, saddress);
            
            InetAddress raddress = InetAddress.getByName(props.getProperty(RELAY_ADDRESS, RELAY_ADDRESS_DEFAULT));
            int rport = Integer.parseInt(props.getProperty(RELAY_PORT, RELAY_PORT_DEFAULT));
            relaySocket = new DatagramSocket(rport, raddress);
            
            clientToServerThread.setImplThreads(serverSocket, relaySocket);
            serverToClientThread.setImplThreads(relaySocket, serverSocket);
            
            targetServer = InetAddress.getByName(props.getProperty(TARGET_ADDRESS));
            targetPort = Integer.parseInt(props.getProperty(TARGET_PORT));
            
            giaddr = InetAddress.getByName(props.getProperty(GIADDR)).getAddress();
            
            String clid = props.getProperty(CLID);
            int clidSub = Integer.parseInt(props.getProperty(CLID_SUB));
            LinkedHashMap<Byte, String> map = new LinkedHashMap<Byte, String>();
            map.put((byte) clidSub, clid);
            option82 = DHCPOption.agentOptionToRaw(map);
            
        } catch (Exception e) {
            serverSocket = null;
            relaySocket = null;
            logger.log(Level.SEVERE, "Cannot open socket", e);
        }
    }
    
    /**
     * 
     * 
     */
    public void run() {
        serverToClientThread.start();
        clientToServerThread.start();
        try {
            //serverToClientThread.join();
            //clientToServerThread.join();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception during execution", e);
        }
    }
    
    private abstract class RelayThread extends Thread {
        
        protected DatagramSocket listenSocket = null;
        protected DatagramSocket sendSocket = null;
        
        public RelayThread() {
            super();
        }
        
        public void setImplThreads(DatagramSocket listen, DatagramSocket send) {
            listenSocket = listen;
            sendSocket = send;
        }
        
        /**
         * 
         * @author yshi7355
         *
         * TODO To change the template for this generated type comment go to
         * Window - Preferences - Java - Code Style - Code Templates
         */
        public void run() {
            while (true) {
                dispatch();
            }
        }
        
        public void dispatch() {
            try {
    	        DatagramPacket requestDatagram = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
    	        InetAddress address;
    	        int port;
    	        
    	        listenSocket.receive(requestDatagram);
    	        DHCPPacket request = DHCPPacket.getPacket(requestDatagram);
    	        if (logger.isLoggable(Level.FINER))
    	            logger.fine(request.toString());
    	        
    	        DHCPPacket response = service(request); // call service function
    	        if (response != null) {
    	            // we have something to send back
    	            byte[] responseBuf = response.serialize();
    	            address = response.getAddress();
    	            if (address == null)
    	                throw new IllegalArgumentException("Address needed in response");
    	            port = response.getPort();
    	            
    	            DatagramPacket responseDatagram = new DatagramPacket(responseBuf, responseBuf.length, address, port);
    	            sendSocket.send(responseDatagram);
    	        }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Skipping", e);
            }
        }
        
        public abstract DHCPPacket service(DHCPPacket request);
    }
    
    private class ClientToServerThread extends RelayThread {

        public DHCPPacket service(DHCPPacket request) {
            try {
                if (request.getOp() != DHCPConstants.BOOTREQUEST) {
                    logger.warning("Packet received from client is not BOOTREQUEST");
                    return null;
                }
                
                DHCPPacket response = (DHCPPacket) request.clone();
                
                response.setAddress(targetServer);
                response.setPort(targetPort);
                
                response.setGiaddrRaw(giaddr);
                response.setOptionRaw(DHCPConstants.DHO_DHCP_AGENT_OPTIONS, option82);
                
                return response;
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected exception", e);
            }
            
            
            return null;
        }
    }

    private class ServerToClientThread extends RelayThread {
        
        public DHCPPacket service(DHCPPacket request) {
            try {
                if (request.getOp() != DHCPConstants.BOOTREPLY) {
                    logger.warning("Packet received from client is not BOOTREQUEST");
                    return null;
                }
                
                DHCPPacket response = (DHCPPacket) request.clone();
                
                response.setAddress(DHCPConstants.INADDR_BROADCAST);
                response.setPort(68);
                
                response.setGiaddrRaw(giaddrEmpy);
                response.removeOption(DHCPConstants.DHO_DHCP_AGENT_OPTIONS);
                
                return response;
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected exception", e);
            }
            
            
            return null;
        }
    }
    
    /**
     * main
     */
    public static void main(String[] args) {
        DHCPRelay relay = new DHCPRelay();
        relay.init();
        relay.run();
    }

    
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String SERVER_ADDRESS_DEFAULT = "127.0.0.1";
    private static final String SERVER_PORT = "serverPort";
    private static final String SERVER_PORT_DEFAULT = "67";
    private static final String RELAY_ADDRESS = "relayAddress";
    private static final String RELAY_ADDRESS_DEFAULT = "127.0.0.1";
    private static final String RELAY_PORT = "relayPort";
    private static final String RELAY_PORT_DEFAULT = "67";
    
    private static final String TARGET_ADDRESS = "targetAddress";
    private static final String TARGET_PORT = "targetPort";
    
    private static final String GIADDR = "giaddr";
    private static final String CLID_SUB = "clidsub";
    private static final String CLID = "clid";

}
