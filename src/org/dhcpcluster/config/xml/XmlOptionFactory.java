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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPOption;
import org.dhcpcluster.config.xml.data.Option;
import org.dhcpcluster.config.xml.data.OptionGeneric;
import org.dhcpcluster.config.xml.data.Options;
import org.dhcpcluster.struct.DHCPRichOption;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */

public class XmlOptionFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlOptionFactory.class.getName().toLowerCase());

    public static DHCPOption[] parseOptions(Options options) {
    	// if no tag, we put an empty array
    	if (options == null) {
    		return DHCPOPTION_0;
    	}
		LinkedList<DHCPOption> optList = new LinkedList<DHCPOption>();
		
		for (Object optObj : options.getOptionOrOptionTimeServersOrOptionRouters()) {
			DHCPOption opt = parseOptionObject(optObj);
			if (opt != null) {
				optList.add(opt);
			}
		}
		return optList.toArray(DHCPOPTION_0);
    }
    
    private static DHCPOption parseOptionObject(Object obj) {
		byte code = 0;
		// first check for code
		if (obj instanceof JAXBElement<?>) {
			return parseNamedOption((JAXBElement<?>)obj);
		} else if (obj instanceof Option) {
			Option opt = (Option)obj;
			code = opt.getCode();
			return makeOptionValue(opt.getCode(), opt.isMirror(), opt.getValueByteOrValueShortOrValueInt());
		} else {
			logger.severe("Unknown option object: "+obj);
		}
		return null;	// TODO
    	
    }
    
    private static DHCPOption parseNamedOption(JAXBElement<?> obj) {
		// parse name for code resolution
		String optName = ((JAXBElement)obj).getName().getLocalPart();
		optName = "DHO_" + optName.substring(OPTION_PREFIX.length());
		optName = optName.replace("-", "_").toUpperCase();
		Byte codeByte = DHCPConstants.getDhoNamesReverse(optName);
		if (codeByte == null) {
			logger.warning("Unrecognized option name: "+optName);
			return null;
		}
		Object o = obj.getValue();
		if (o instanceof OptionGeneric) {
			OptionGeneric og = (OptionGeneric)o;
			return makeOptionValue(codeByte, og.isMirror(), og.getValueByteOrValueShortOrValueInt());
		} else {
			throw new IllegalStateException("Unexpected type here:"+o);
		}
    }
    
	
	private XmlOptionFactory() {
		throw new UnsupportedOperationException();
	}

	private static DHCPOption makeOptionValue(byte code, boolean mirror, List<JAXBElement<?>> optList) {
		DHCPOption resOption = null;
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(byteOutput);
		try {
			for (JAXBElement<?> optElement : optList) {
				// switch according to base type
				Object obj = optElement.getValue();
				if (obj instanceof InetAddress) {
					outputStream.write(((InetAddress)obj).getAddress());
				} else if (obj instanceof Byte) {
					outputStream.writeByte(((Byte)obj).intValue());
				} else if (obj instanceof Short) {
					outputStream.writeShort(((Short)obj).intValue());
				} else if (obj instanceof Integer) {
					outputStream.writeInt((Integer)obj);
				} else if (obj instanceof String) {
					outputStream.writeBytes((String)obj);
				} else if (obj instanceof byte[]) {
					outputStream.write((byte[])obj);
				} else {
					logger.warning("Unsupported value type: "+obj.toString());
				}
			}
			resOption = new DHCPRichOption(code, byteOutput.toByteArray(), mirror);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unexpected IOException", e);
			return null;
		}
		
		return resOption;
	}

	private static final String OPTION_PREFIX = "option-";
    private static final DHCPOption[] DHCPOPTION_0 = new DHCPOption[0];
}
