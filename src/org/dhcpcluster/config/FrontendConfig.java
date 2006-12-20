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
package org.dhcpcluster.config;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class FrontendConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")	// TODO
	private static final Logger logger = Logger.getLogger(FrontendConfig.class.getName().toLowerCase());
    
    private InetAddress	listenIp = null;
    private int			listenPort = 67;
    
    private int			threadsNb = 2;
    private int			threadsCore = 1;
    private int			threadsMax = 4;
    private int			threadsKeepalive = 10000;	// 10s default
    
	/**
	 * @return Returns the listenIp.
	 */
	public InetAddress getListenIp() {
		return listenIp;
	}
	/**
	 * @param listenIp The listenIp to set.
	 */
	public void setListenIp(InetAddress listenIp) {
		this.listenIp = listenIp;
	}
	/**
	 * @return Returns the listenPort.
	 */
	public int getListenPort() {
		return listenPort;
	}
	/**
	 * @param listenPort The listenPort to set.
	 */
	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}
	/**
	 * @return Returns the threadsMax.
	 */
	public int getThreadsMax() {
		return threadsMax;
	}
	/**
	 * @param threadsMax The threadsMax to set.
	 */
	public void setThreadsMax(int threadsMax) {
		this.threadsMax = threadsMax;
	}
	/**
	 * @return Returns the threadsCore.
	 */
	public int getThreadsCore() {
		return threadsCore;
	}
	/**
	 * @param threadsCore The threadsCore to set.
	 */
	public void setThreadsCore(int threadsCore) {
		this.threadsCore = threadsCore;
	}
	/**
	 * @return Returns the threadsKeepalive.
	 */
	public int getThreadsKeepalive() {
		return threadsKeepalive;
	}
	/**
	 * @param threadsKeepalive The threadsKeepalive to set.
	 */
	public void setThreadsKeepalive(int threadsKeepalive) {
		this.threadsKeepalive = threadsKeepalive;
	}
	/**
	 * @return Returns the threadsNb.
	 */
	public int getThreadsNb() {
		return threadsNb;
	}
	/**
	 * @param threadsNb The threadsNb to set.
	 */
	public void setThreadsNb(int threadsNb) {
		this.threadsNb = threadsNb;
	}

}
