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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import sf.dhcp4java.DHCPConstants;
import sf.dhcp4java.DHCPPacket;
import sf.dhcp4java.DHCPServer;
import sf.dhcp4java.DHCPServlet;
import sf.dhcp4java.DHCPServerInitException;


/**
 * A sample DHCP servlet (under construction).
 * 
 * @author Stephan Hadinger
 * @version 0.50
 */
public class TrivialDHCPServlet extends DHCPServlet {

    private static final Logger logger = Logger.getLogger("sf.dhcp4java.examples.trivialdhcpservlet");
    
    /* (non-Javadoc)
     * @see sf.dhcp4java.DHCPServer#service(sf.dhcp4java.DHCPPacket)
     */
    public DHCPPacket service(DHCPPacket request) {
        
        logger.info("*** Received from "+DHCPPacket.getHostAddress(request.getAddress())+
                			" ("+request.getPort()+")");
        if (logger.isLoggable(Level.FINER))		logger.finer(request.toString());
        
        if (!request.isDhcp()) return null;			// only BOOTP packet
        Byte messageTypeByte = request.getOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE);
        if (messageTypeByte == null) return null;	// no DHCP message type
        
        byte messageType = messageTypeByte.byteValue();
        if ((messageType != DHCPConstants.DHCPDISCOVER) &&
            (messageType != DHCPConstants.DHCPREQUEST))
            	return null;		// only DISCOVER and REQUEST are treated
        
        String vendor = request.getOptionAsString(DHCPConstants.DHO_VENDOR_CLASS_IDENTIFIER);
        if ((vendor == null) || (vendor.indexOf("sagem") < 0))
            return null;		// only "sagem" vendor class
        
        DHCPPacket response = new DHCPPacket(); 
        
        // copy addresses
        response.setAddress(request.getAddress());
        response.setPort(request.getPort());
        
        response.setComment(request.getComment());
        
        response.setOp(DHCPConstants.BOOTREPLY);
        response.setHtype(request.getHtype());
        response.setHlen(request.getHlen());
        response.setHops(request.getHops());
        response.setXid(request.getXid());
        response.setSecs(request.getSecs());
        response.setFlags(request.getFlags());
        response.setCiaddrRaw(request.getCiaddrRaw());
        //response.setYiaddrRaw(request.getYiaddrRaw());
        response.setSiaddrRaw(request.getSiaddrRaw());
        response.setGiaddrRaw(request.getGiaddrRaw());
        response.setChaddr(request.getChaddr());
        
        byte[] yiaddr = { (byte) 10, (byte) 1, (byte) 1, (byte) 23 };
        response.setYiaddrRaw(yiaddr);
        
        // messagetype ?
        switch (messageType) {
        	case DHCPConstants.DHCPDISCOVER:
        	    response.setOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHCPOFFER);
        		break;
        	case DHCPConstants.DHCPREQUEST:
        	    response.setOptionAsByte(DHCPConstants.DHO_DHCP_MESSAGE_TYPE, DHCPConstants.DHCPACK);
        		break;
        	default:
        	    return null;		// unsupported message type
        }
        
        // set some options
        try {
            response.setOptionAsInetAddress(DHCPConstants.DHO_DHCP_SERVER_IDENTIFIER,
                    InetAddress.getByName("192.168.1.1"));
            response.setOptionAsInt(DHCPConstants.DHO_DHCP_LEASE_TIME, 86400);
            response.setOptionAsInetAddress(DHCPConstants.DHO_SUBNET_MASK,
                    InetAddress.getByName("255.255.252.0"));
            response.setOptionAsInetAddress(DHCPConstants.DHO_ROUTERS,
                    InetAddress.getByName("10.0.0.10"));
            response.setOptionAsInetAddress(DHCPConstants.DHO_STATIC_ROUTES,
                    InetAddress.getByName("172.20.224.167")); // TODO multiple INETADDRS
            response.setOptionAsInetAddress(DHCPConstants.DHO_NTP_SERVERS,
                    InetAddress.getByName("10.0.0.10"));
            response.setOptionAsInetAddress(DHCPConstants.DHO_WWW_SERVER,
                    InetAddress.getByName("10.0.0.10"));
        } catch (UnknownHostException e) {
            // too bad
        }
        
        // copy option 82
        response.setOptionRaw(DHCPConstants.DHO_DHCP_AGENT_OPTIONS,
                	request.getOptionRaw(DHCPConstants.DHO_DHCP_AGENT_OPTIONS));
        
        logger.info("+++Sending back to "+DHCPPacket.getHostAddress(response.getAddress())
                			+" ("+response.getPort()+")");
        if (logger.isLoggable(Level.FINER))		logger.finer(response.toString());
        
        return response;
    }

    public static void main(String[] args) {
        try {
            DHCPServer server = DHCPServer.initServer(new TrivialDHCPServlet(), null);
            logger.setLevel(Level.OFF);
            new Thread(server).start();
        } catch (DHCPServerInitException e) {
            logger.log(Level.SEVERE, "Server init", e);
        }
    }
}
