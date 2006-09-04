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

	/**
	 * Constructor. The only parameter is the option code. There is no value since the request's value
	 * is returned (context based).
	 * 
	 * <p>Internally, a <tt>null</tt> value is set in the option. This means that you cannot request
	 * values as byte, int... without getting an exception. If you set this option to a packet, this
	 * removes an already set option (since value is null).
	 * 
	 * @param code the option code to mirror
	 */
	public DHCPOptionMirror(byte code) {
		super(code, null);
	}
	
	/**
	 * Get the option value based on the context, i.e. the client's request.
	 * 
	 * <p>This should be the only method used with this class to get relevant values.
	 * 
	 * @param request the client's DHCP requets
	 * @return the value of the specific option in the client request
	 */
	public byte[] getMirrorValue(DHCPPacket request) {
		if (request == null) {
			throw new NullPointerException("request is null");
		}
		return request.getOptionRaw(this.getCode());
	}

}
