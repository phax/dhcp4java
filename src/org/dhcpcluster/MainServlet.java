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
package org.dhcpcluster;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.dhcp4java.DHCPResponseFactory;
import org.dhcp4java.DHCPServlet;
import org.dhcp4java.HardwareAddress;
import org.dhcpcluster.config.GlobalConfig;
import org.dhcpcluster.config.TopologyConfig;
import org.dhcpcluster.filter.RequestFilter;
import org.dhcpcluster.struct.Subnet;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class MainServlet extends DHCPServlet {

    private static final Logger logger = Logger.getLogger(MainServlet.class.getName().toLowerCase());

    /* link to the cluster node object */
    private final DHCPClusterNode clusterNode;
	
	public MainServlet(DHCPClusterNode clusterNode) {
		super();
		this.clusterNode = clusterNode;
	}
	
	/**
	 * Handles DHCPDISCOVER from a client.
	 * 
	 * <p>Normal order is:<br>
	 * 1. filter client request from global parameters<br>
	 * 2. find out which Subnet(s) the client belongs<br>
	 * 3. for each eligible subnet, filter request based on subnet refquirements<br>
	 * 4. calculate the client IP address and lease time,
	 * 		reserve the address for a limited duration<br>
	 * 5. generate standard response
	 * 6. calculate client options<br>
	 * 6a. global pre-options<br>
	 * 6b. recursive options from nodes top-down<br>
	 * 6c. global post-options (e.g. option 82 needs to be last one)<br>
	 * 
	 * @see org.dhcp4java.DHCPServlet#doDiscover(org.dhcp4java.DHCPPacket)
	 */
	@Override
	protected DHCPPacket doDiscover(DHCPPacket request) {
		TopologyConfig topologyConfig = clusterNode.getTopologyConfig();
		GlobalConfig globalConfig = clusterNode.getGlobalConfig();
		
		/* 1. Filter client from global parameters */
		RequestFilter globalFilter = topologyConfig.getGlobalFilter();
		if (!globalFilter.isRequestAccepted(request)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Request rejected on global filter "+request);
			}
			return null;		// escape
		}
		
		/* 2. find out which subnet the client belongs */
		Subnet subnet = topologyConfig.findSubnetFromRequestGiaddr(request.getGiaddr());
				
		// what have we got for a subnet ?
		if (subnet == null) {
			logger.warn("Packet is not in any subnet: "+request);
			return null;		// ignore request
		}
		
		/* 3. filter by specific subnet parameters */
		if (!subnet.isRequestAccepted(request)) {
			if (!globalFilter.isRequestAccepted(request)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Request rejected on node filter "+request);
				}
				return null;		// escape
			}
		}
		
		/* 4. calculate the client lease (ip+duration) */
		// first check if there is a static address
		HardwareAddress mac = request.getHardwareAddress();
		InetAddress clientAddr = subnet.getStaticAddress(mac);
		if (clientAddr == null) {
			// now check if we can affect a dynamic address
			// TODO
		}
		
		int clientLease = 0;

		/* 5. generate DHCPOFFER */
		InetAddress serverId = globalConfig.getServerIdentifier();
		String message = null;
		DHCPOption[] options = null;
		DHCPPacket response;
		response = DHCPResponseFactory.makeDHCPOffer(request, clientAddr, clientLease, serverId, message, options);
				
		/* 6. calculate client options */
		/* 6a. global pre-options */
		globalConfig.getRootNode().applyOptions(request, response);
		
		/* 6b. recursive options from nodes top-down */
		subnet.applyOptions(request, response);
		
		/* 6c. global post-options (e.g. option 82 needs to be last one) */
		globalConfig.getPostNode().applyOptions(request, response);

		return response;
	}


	/**
	 * Handles DHCPREQUEST from a client.
	 * 
	 * <p>Normal order is:<br>
	 * 1. filter client request from global parameters<br>
	 * 2. find out which Subnet(s) the client belongs<br>
	 * 3. for each eligible subnet, filter request based on subnet refquirements<br>
	 * 4. verify the client IP address and lease time,
	 * 		update the reserved lease<br>
	 * 5. generate standard response
	 * 6. calculate client options<br>
	 * 6a. global pre-options<br>
	 * 6b. recursive options from nodes top-down<br>
	 * 6c. global post-options (e.g. option 82 needs to be last one)<br>
	 * 
	 * @see org.dhcp4java.DHCPServlet#doDiscover(org.dhcp4java.DHCPPacket)
	 */
	@Override
	protected DHCPPacket doRequest(DHCPPacket request) {
		TopologyConfig topologyConfig = clusterNode.getTopologyConfig();
		GlobalConfig globalConfig = clusterNode.getGlobalConfig();
		
		/* 1. Filter client from global parameters */
		RequestFilter globalFilter = topologyConfig.getGlobalFilter();
		if (!globalFilter.isRequestAccepted(request)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Request rejected on global filter "+request);
			}
			return null;		// escape
		}
		
		/* 2. find out which subnet the client belongs */
		Subnet subnet = topologyConfig.findSubnetFromRequestGiaddr(request.getGiaddr());
				
		// what have we got for a subnet ?
		if (subnet == null) {
			logger.warn("Packet is not in any subnet: "+request);
			return null;		// ignore request
		}
		
		/* 3. filter by specific subnet parameters */
		// TODO
		
		/* 4. calculate the client lease (ip+duration) */
		// TODO
		InetAddress clientAddr = null;
		int clientLease = 0;
		boolean confirmRequest = true;
		
		/* 5. generate DHCPACK/NAK */
		InetAddress serverId = globalConfig.getServerIdentifier();
		String message = null;
		DHCPOption[] options = null;
		if (!confirmRequest) {
			// send a NAK
			DHCPPacket response = DHCPResponseFactory.makeDHCPNak(request, serverId, message);
			return response;
		}
		DHCPPacket response;
		response = DHCPResponseFactory.makeDHCPAck(request, clientAddr, clientLease, serverId, message, options);

		/* 6. calculate client options */
		/* 6a. global pre-options */
		globalConfig.getRootNode().applyOptions(request, response);
		
		/* 6b. recursive options from nodes top-down */
		subnet.applyOptions(request, response);
		
		/* 6c. global post-options (e.g. option 82 needs to be last one) */
		globalConfig.getPostNode().applyOptions(request, response);
		
		return response;
	}
	
}
