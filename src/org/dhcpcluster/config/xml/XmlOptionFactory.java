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
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.dhcp4java.DHCPConstants;
import org.dhcp4java.DHCPOption;
import org.dhcpcluster.config.xml.data.Option;
import org.dhcpcluster.config.xml.data.OptionGeneric;
import org.dhcpcluster.config.xml.data.OptionsType;
import org.dhcpcluster.config.xml.data.TypeOptionByte;
import org.dhcpcluster.config.xml.data.TypeOptionBytes;
import org.dhcpcluster.config.xml.data.TypeOptionInet;
import org.dhcpcluster.config.xml.data.TypeOptionInets;
import org.dhcpcluster.config.xml.data.TypeOptionInt;
import org.dhcpcluster.config.xml.data.TypeOptionShort;
import org.dhcpcluster.config.xml.data.TypeOptionShorts;
import org.dhcpcluster.config.xml.data.TypeOptionString;
import org.dhcpcluster.struct.DHCPRichOption;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */

public class XmlOptionFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(XmlOptionFactory.class.getName().toLowerCase());

	/**
	 * Class is not instantiable.
	 *
	 */
	private XmlOptionFactory() {
		throw new UnsupportedOperationException();
	}
	
    public static DHCPOption[] parseOptions(OptionsType options) {
    	// if no tag, we put an empty array
    	if (options == null) {
    		return DHCPOPTION_0;
    	}
		LinkedList<DHCPOption> optList = new LinkedList<DHCPOption>();
		
		for (Object optObj : options.getOptions()) {
			DHCPOption opt = parseOptionObject(optObj);
			if (opt != null) {
				optList.add(opt);
			}
		}
		return optList.toArray(DHCPOPTION_0);
    }
    
    private static DHCPOption parseOptionObject(Object obj) {
		// first check for code
		if (obj instanceof JAXBElement<?>) {
			return parseNamedOption((JAXBElement<?>)obj);
		} else if (obj instanceof Option) {
			Option opt = (Option)obj;
			DHCPOptionBuilder resOption = new DHCPOptionBuilder(opt.getCode(), opt.isMirror());
			resOption.addObject(opt.getValueByteOrValueShortOrValueInt());
			return resOption.getResultDHCPOption();
		} else {
			logger.severe("Unknown option object: "+obj);
			return null;
		}
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
			return makeOptionValue(codeByte, og);
		} else {
			throw new IllegalStateException("Unexpected type here:"+o);
		}
    }
    
	private static DHCPOption makeOptionValue(byte code, OptionGeneric o) {
		DHCPOptionBuilder resOption = new DHCPOptionBuilder(code, o.isMirror());
		if (o instanceof TypeOptionByte) {
			resOption.addObject(((TypeOptionByte)o).getValueByte());
		} else if (o instanceof TypeOptionBytes) {
			resOption.addObject(((TypeOptionBytes)o).getValueByteOrValueRawhexOrValueRaw64());
		} else if (o instanceof TypeOptionInet) {
			resOption.addObject(((TypeOptionInet)o).getValueInet());
		} else if (o instanceof TypeOptionInets) {
			resOption.addObject(((TypeOptionInets)o).getValueInet());
		} else if (o instanceof TypeOptionInt) {
			resOption.addObject(((TypeOptionInt)o).getValueInt());
		} else if (o instanceof TypeOptionShort) {
			resOption.addObject(((TypeOptionShort)o).getValueShort());
		} else if (o instanceof TypeOptionShorts) {
			resOption.addObject(((TypeOptionShorts)o).getValueShort());
		} else if (o instanceof TypeOptionString) {
			resOption.addObject(((TypeOptionString)o).getValueString());
		} else {
			logger.warning("Unsupported value type: "+o.toString());
		}
		return resOption.getResultDHCPOption();
	}
//
//	private static DHCPOption makeOptionValue(byte code, boolean mirror, List<JAXBElement<?>> optList) {
//		DHCPOption resOption = null;
//		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
//		DataOutputStream outputStream = new DataOutputStream(byteOutput);
//		try {
//			for (JAXBElement<?> optElement : optList) {
//				// switch according to base type
//				Object obj = optElement.getValue();
//				if (obj instanceof InetAddress) {
//					outputStream.write(((InetAddress)obj).getAddress());
//				} else if (obj instanceof Byte) {
//					outputStream.writeByte(((Byte)obj).intValue());
//				} else if (obj instanceof Short) {
//					outputStream.writeShort(((Short)obj).intValue());
//				} else if (obj instanceof Integer) {
//					outputStream.writeInt((Integer)obj);
//				} else if (obj instanceof String) {
//					outputStream.writeBytes((String)obj);
//				} else if (obj instanceof byte[]) {
//					outputStream.write((byte[])obj);
//				} else {
//					logger.warning("Unsupported value type: "+obj.toString());
//				}
//			}
//			resOption = new DHCPRichOption(code, byteOutput.toByteArray(), mirror);
//		} catch (IOException e) {
//			logger.log(Level.SEVERE, "Unexpected IOException", e);
//			return null;
//		}
//		
//		return resOption;
//	}

	
	private static final String OPTION_PREFIX = "option-";
    private static final DHCPOption[] DHCPOPTION_0 = new DHCPOption[0];
}

final class DHCPOptionBuilder {

	private static final Logger logger = Logger.getLogger(XmlOptionFactory.class.getName().toLowerCase());
	
	private byte code;
	private boolean mirror;
	private ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
	private DataOutputStream outputStream = new DataOutputStream(byteOutput);
	
	
	public DHCPOptionBuilder(byte code, boolean mirror) {
		this.code = code;
		this.mirror = mirror;
	}
	
	public void addObject(Object o) {
		try {
			if (o instanceof Byte) {
				outputStream.writeByte((Byte)o);
			} else if (o instanceof Short) {
				outputStream.writeShort((Short)o);
			} else if (o instanceof Integer) {
				outputStream.writeInt((Integer)o);
			} else if (o instanceof String) {
				outputStream.writeBytes((String)o);
			} else if (o instanceof InetAddress) {
				outputStream.write(((InetAddress)o).getAddress());
			} else if (o instanceof byte[]) {
				outputStream.write((byte[])o);
			} else if (o instanceof List) {
				for (Object o2 : (List)o) {
					if (o2 instanceof JAXBElement) {
						addObject(((JAXBElement)o2).getValue());
					} else {
						addObject(o2);
					}
				}
			} else {
				logger.warning("Unsupported value type: "+o.toString());
			}
		} catch (IOException e) {
			throw new IllegalStateException();		// never happens
		}
	}
			
	public DHCPOption getResultDHCPOption() {
		return new DHCPRichOption(code, byteOutput.toByteArray(), mirror);
	}
}
