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

import org.apache.log4j.Logger;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPPacket;

/**
 * Class for manipulating DHCP options (used internally).
 * 
 * @author Stephan Hadinger
 * @version 0.72
 * 
 * Immutable object.
 */
public class DHCPRichOption extends DHCPOption {
	private static final long   serialVersionUID = 3L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DHCPRichOption.class);

    public enum Mode { REPLACE, CONCAT, REGEX }

    private Mode mode = Mode.REPLACE;
    
    public DHCPRichOption(byte code, byte[] value, boolean mirror, Mode mode) {
    	super(code, value, mirror);
    	this.mode = mode;
    }
    public DHCPRichOption(byte code, byte[] value, boolean mirror) {
    	super(code, value, mirror);
    }
    public DHCPRichOption(byte code, byte[] value) {
    	super(code, value);
    }
	/* (non-Javadoc)
	 * @see org.dhcp4java.DHCPOption#applyOption(org.dhcp4java.DHCPPacket)
	 */
	@Override
	public DHCPOption applyOption(DHCPPacket request) {
		switch (mode) {
			case REPLACE:
				return super.applyOption(request);
			case CONCAT:
				// TODO
			case REGEX:
				// TODO
			default:
				return null;
		
		}
	}
    
    
}
