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
package org.dhcp4java;

/**
 * Used to indicate that this option must be mirrored from the client request (used by servers).
 * 
 * <p>This class represent a restricted version of a DHCPOption, in DHCPOption options lists, indicating
 * that the server response must mirror the client request  
 * 
 * @author Stephan Hadinger
 *
 */
public class DHCPOptionMirror extends DHCPOption {
	
	private static final long   serialVersionUID = 2L;

	public DHCPOptionMirror(byte code) {
		super(code, null);
	}
	
	public byte[] getMirrorValue(DHCPPacket request) {
		return request.getOptionRaw(this.getCode());
	}

}
