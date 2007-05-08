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
package org.dhcpcluster.backend;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.dhcp4java.HardwareAddress;
import org.dhcpcluster.DHCPClusterNode;
import org.dhcpcluster.config.ConfigException;
import org.dhcpcluster.config.TopologyConfig;
import org.dhcpcluster.struct.DHCPLease;
import org.dhcpcluster.struct.Subnet;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public interface DHCPBackendIntf {
	
	public void init(DHCPClusterNode dhcpCoreServer, Properties configProperties) throws ConfigException;
	
	/*
	 * 
	public static long callDhcpDiscoverSP(Connection conn, long poolId, String macHex, int iccQuota, String icc,
											long offerTime) throws SQLException {
	 */
	public long discover(Subnet subnet, HardwareAddress mac, int iccQuota, String icc, long offerTime);
	
	public void prepareBackend(TopologyConfig topology);
	
	public void shutdown();
	
	
	//public DHCPLease 
}
