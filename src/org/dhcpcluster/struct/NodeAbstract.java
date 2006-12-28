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
package org.dhcpcluster.struct;

import java.io.Serializable;
import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;
import org.dhcpcluster.filter.AlwaysTrueFilter;
import org.dhcpcluster.filter.RequestFilter;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public abstract class NodeAbstract implements Serializable {

    private static final long serialVersionUID = 2L;
    /** freely usable comment */
    protected String comment = null;
    
    /** filter applicable to Subnet */
    protected RequestFilter				requestFilter = ALWAYS_TRUE_FILTER;
    
    /** array of dhcp options */
    protected DHCPOption[]					dhcpOptions = DHCPOPTION_0;

    public void applyOptions(DHCPPacket request, DHCPPacket response) {
    	for (DHCPOption opt : dhcpOptions) {
    		response.setOption(opt.applyOption(request));
    	}
    }
    
    protected static final DHCPOption[] DHCPOPTION_0 = new DHCPOption[0];
    protected static final RequestFilter ALWAYS_TRUE_FILTER = new AlwaysTrueFilter();    
}
