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
package org.dhcpcluster.config.xml;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.dhcp4java.HardwareAddress;
import org.dhcp4java.InetCidr;
import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.config.TopologyConfig;
import org.dhcpcluster.config.xml.data.DhcpServer;
import org.dhcpcluster.config.xml.data.Pools;
import org.dhcpcluster.config.xml.data.TypeNodeSubnet;
import org.dhcpcluster.struct.AddressRange;
import org.dhcpcluster.struct.Node;
import org.dhcpcluster.struct.NodeRoot;
import org.dhcpcluster.struct.Subnet;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public final class XmlTopologyConfigReader {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlTopologyConfigReader.class);
    
    /**
     * Class is not instantiable.
     */
    private XmlTopologyConfigReader() {
    	throw new UnsupportedOperationException();
    }

    public static TopologyConfig xmlTopologyReader(DhcpServer.Topology topologyData) throws ConfigException {
    	TopologyConfig topologyConfig = new TopologyConfig();
    	
    	// <node> or <subnet>
    	Node rootNode = new Node();
    	for (TypeNodeSubnet subNode : topologyData.getNodeOrSubnet()) {
    		NodeRoot node = parseSubNode(subNode);
    		if (node != null) {
    			rootNode.getNodeList().add(node);
    		}
    	}
    	topologyConfig.setRootNode(rootNode);
    	
    	// register all subnets
    	registerNode(topologyConfig, rootNode);
    		
    	return topologyConfig;
    }
    
    private static final void registerNode(TopologyConfig topologyConfig, NodeRoot node) throws ConfigException {
    	if (node instanceof Node) {
    		for (NodeRoot subnode : ((Node)node).getNodeList()) {
    			registerNode(topologyConfig, subnode);
    		}
    	} else if (node instanceof Subnet) {
    		topologyConfig.registerSubnet((Subnet)node);
    	} else {
    		throw new IllegalStateException("Unexpected NodeRoot type: "+node);
    	}
    }
    
    public static final NodeRoot parseSubNode(TypeNodeSubnet subNode) {
    	if (subNode instanceof org.dhcpcluster.config.xml.data.Node) {
    		return parseNode((org.dhcpcluster.config.xml.data.Node)subNode);
    	} else if (subNode instanceof org.dhcpcluster.config.xml.data.Subnet) {
    		return parseSubnet((org.dhcpcluster.config.xml.data.Subnet)subNode);
    	} else {
    		throw new IllegalStateException("Unexpected subNode type: "+subNode);
    	}
    }
    
    public static final Node parseNode(org.dhcpcluster.config.xml.data.Node xNode) {
    	Node node = new Node();
    	node.setComment(xNode.getComment());
    	node.setNodeType(xNode.getNodeType());
    	node.setNodeId(xNode.getNodeId());
    	// TODO missing extension mechanism (parent node or explicit extension)
    	XmlPolicyFactory.parsePolicy(xNode.getPolicy(), node.getPolicy());

    	if (xNode.getSubNodes() != null) {
        	for (TypeNodeSubnet xSubnode : xNode.getSubNodes().getNodeOrSubnet()) {
        		node.getNodeList().add(parseSubNode(xSubnode));
        	}
    	}

    	return node;
    }
    
    public static final Subnet parseSubnet(org.dhcpcluster.config.xml.data.Subnet xSubnet) {
		Subnet subnet = new Subnet(new InetCidr(xSubnet.getAddress(), xSubnet.getMask()));
		subnet.setComment(xSubnet.getComment());
		subnet.setNodeType(xSubnet.getNodeType());
		subnet.setNodeId(xSubnet.getNodeId());
		XmlPolicyFactory.parsePolicy(xSubnet.getPolicy(), subnet.getPolicy());

    	// now parsing <ranges> and <static>
    	if (xSubnet.getPools() != null) {
    		for (Object o : xSubnet.getPools().getRangeOrStatic()) {
    			if (o instanceof Pools.Range) {
    				addRange(subnet, (Pools.Range)o);
    			} else if (o instanceof Pools.Static) {
    				addStatic(subnet, (Pools.Static)o);
    			} else {
    				throw new IllegalStateException("Unexpected Pools.* object: "+o);
    			}
    		}
    	}
    	// now parsing <giaddr>
    	if (xSubnet.getGiaddrs() != null) {
    		for (InetAddress giaddr : xSubnet.getGiaddrs().getGiaddr()) {
    			subnet.addGiaddr(giaddr);
    		}
    	}
		return subnet;
    }
    
    public static final void addRange(Subnet subnet, Pools.Range xRange) {
    	AddressRange range = new AddressRange(xRange.getStart(), xRange.getEnd());
    	subnet.addAddrRange(range);
    }
    
    public static final void addStatic(Subnet subnet, Pools.Static xStatic) {
    	HardwareAddress mac = HardwareAddress.getHardwareAddressByString(xStatic.getEthernet());
    	subnet.addStaticAddress(mac, xStatic.getAddress());
    }

}
