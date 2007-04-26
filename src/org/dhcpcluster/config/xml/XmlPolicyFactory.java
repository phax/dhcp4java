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

import org.apache.log4j.Logger;
import org.dhcpcluster.config.xml.data.PolicyType;
import org.dhcpcluster.struct.NodePolicy;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public final class XmlPolicyFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlPolicyFactory.class);
	
	/**
	 * Class is not instantiable.
	 *
	 */
	private XmlPolicyFactory() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @param xmlPolicy
	 * @return
	 */
	public static void parsePolicy(PolicyType xmlPolicy, NodePolicy curPolicy) {
		if (curPolicy == null) {
			throw new NullPointerException();
		}
		if (xmlPolicy == null) {
			return;
		}
		
		if (xmlPolicy.getFilter() != null) {
			curPolicy.setRequestFilter(XmlFilterFactory.makeFilterRoot(xmlPolicy.getFilter()));
		}
		if (xmlPolicy.getOptions() != null) {
			curPolicy.setDhcpOptions(XmlOptionFactory.parseOptions(xmlPolicy.getOptions()));
		}
		if (xmlPolicy.getPostOptions() != null) {
			curPolicy.setDhcpPostOptions(XmlOptionFactory.parseOptions(xmlPolicy.getPostOptions()));
		}
		
		PolicyType.LeaseTime xmlLeaseTime = xmlPolicy.getLeaseTime();
		if (xmlLeaseTime.getLeaseDefault() != null) {
			curPolicy.setDefaultLease(xmlLeaseTime.getLeaseDefault());
		}
		if (xmlLeaseTime.getLeaseMax() != null) {
			curPolicy.setMaxLease(xmlLeaseTime.getLeaseMax());
		}
	}

}
