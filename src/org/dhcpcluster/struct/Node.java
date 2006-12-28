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
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class Node extends NodeAbstract implements Serializable {
    private static final long serialVersionUID = 2L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Node.class.getName().toLowerCase());

    
    protected  List<Node>					nodeList = new LinkedList<Node>();

    public Node() {
    }
    
    
	/**
	 * @return Returns the nodeList.
	 */
	public List<Node> getNodeList() {
		return nodeList;
	}
	/**
	 * @param nodeList The nodeList to set.
	 */
	public void setNodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}

}
