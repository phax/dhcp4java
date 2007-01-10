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

import java.io.Serializable;
import java.util.Arrays;

import org.dhcpcluster.config.ConfigException;

/**
 * Class is immutable.
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class HardwareAddress implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final byte   hardwareType;
	private final byte[] hardwareAddress;
	
	private static final byte HTYPE_ETHER = 1;	// default type
	
	/*
	 * Invariants:
	 * 	1- hardwareAddress is not null
	 */
	
	public HardwareAddress(byte[] macAddr) {
		this.hardwareType = HTYPE_ETHER;
		this.hardwareAddress = macAddr;
	}

	public HardwareAddress(byte hType, byte[] macAddr) {
		this.hardwareType = hType;
		this.hardwareAddress = macAddr;
	}
	
	public byte getHardwareType() {
		return hardwareType;
	}
	
	/**
	 * 
	 * <p>Object is cloned to avoid any side-effect.
	 */
	public byte[] getHardwareAddress() {
		return hardwareAddress.clone();
	}

    public int hashCode() {
    	return this.hardwareType ^ Arrays.hashCode(hardwareAddress);
    }

    public boolean equals(Object obj) {
        if ((obj == null) || (!(obj instanceof HardwareAddress))) {
            return false;
        }
        HardwareAddress hwAddr = (HardwareAddress) obj;

        return ((this.hardwareType == hwAddr.hardwareType) &&
                 (Arrays.equals(this.hardwareAddress, hwAddr.hardwareAddress)));
    }
	/**
	 * Prints the hardware address in hex format, split by ":".
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(28);
		if (hardwareType != HTYPE_ETHER) {
			// append hType only if it is not standard ethernet
			sb.append(this.hardwareType).append("/");
		}
		for (int i=0; i<hardwareAddress.length; i++) {
			sb.append(Integer.toString(hardwareAddress[i]  & 0xff, 16));
            if (i<hardwareAddress.length-1) {
            	sb.append(":");
            }
		}
		return sb.toString();
	}
	
	/**
	 * Parse the MAC address in hex format, split by ':'.
	 * 
	 * <p>E.g. <tt>0:c0:c3:49:2b:57</tt>.
	 * 
	 * @param macStr
	 * @return the newly created HardwareAddress object
	 */
	public static HardwareAddress getHardwareAddressByString(String macStr) {
		if (macStr == null) {
			throw new NullPointerException("macStr is null");
		}
		String[] macAdrItems = macStr.split(":");
		if (macAdrItems.length != 6) {
			throw new IllegalArgumentException("macStr["+macStr+"] has not 6 items");
		}
		byte[] macBytes = new byte[6];
		for (int i=0; i<6; i++) {
			int val = Integer.parseInt(macAdrItems[i], 16);
			if ((val < -128) || (val > 255)) {
				throw new IllegalArgumentException("Value is out of range:"+macAdrItems[i]);
			}
			macBytes[i] = (byte) val;
		}
		return new HardwareAddress(macBytes);		
	}

	/**
	 * Parse the MAC address in hex format, split by ':'.
	 * 
	 * <p>E.g. <tt>0:c0:c3:49:2b:57</tt>.
	 * @param macStr
	 * @return bytes representation of the HardwareAddress
	 * @throws ConfigException
	 */
	public static final byte[] parseHardwareAddress(String macStr) throws ConfigException {
		if (macStr == null) {
			throw new NullPointerException("macStr is null");
		}
		String[] macAdrItems = macStr.split(":");
		if (macAdrItems.length != 6) {
			throw new ConfigException("macStr["+macStr+"] has not 6 items");
		}
		byte[] macBytes = new byte[6];
		for (int i=0; i<6; i++) {
			int val = Integer.parseInt(macAdrItems[i], 16);
			if ((val < -128) || (val > 255)) {
				throw new ConfigException("Value is out of range:"+macAdrItems[i]);
			}
			macBytes[i] = (byte) val;
		}
		return macBytes;
	}
}
