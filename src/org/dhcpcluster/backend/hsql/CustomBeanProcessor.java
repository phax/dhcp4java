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
package org.dhcpcluster.backend.hsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.BeanProcessor;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public abstract class CustomBeanProcessor extends BeanProcessor {

	public CustomBeanProcessor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.BeanProcessor#toBean(java.sql.ResultSet, java.lang.Class)
	 */
	@Override
	public abstract Object toBean(ResultSet rs, Class type) throws SQLException;

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.BeanProcessor#toBeanList(java.sql.ResultSet, java.lang.Class)
	 */
	@Override
	public List toBeanList(ResultSet rs, Class type) throws SQLException {
        List<Object> results = new ArrayList<Object>();
        while (rs.next()) {
        	results.add(this.toBean(rs, type));
        }
        return results;
	}
	
}
