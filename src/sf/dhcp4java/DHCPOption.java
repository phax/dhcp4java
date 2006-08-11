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
package sf.dhcp4java;

import static sf.dhcp4java.DHCPConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for manipulating DHCP options (used internally).
 * 
 * @author Stephan Hadinger
 * @version 0.50
 * 
 * Immutable object.
 */
public class DHCPOption implements Serializable {
	private static final long   serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("sf.dhcp4java.dhcpoption");

    /**
     * The code of the option. 0 is reserved for padding, -1 for end of options.
     */
    private final byte code;
    
    /**
     * Raw bytes value of the option. Some methods are provided for higher
     * level of data structures, depending on the <tt>code</tt>.
     */
    private final byte[] value;
    
    /**
     * Constructor for <tt>DHCPOption</tt>.
     * 
     * <p>If value is <tt>null</tt> it is considered as an empty option.
     * If you add an empty option to a DHCPPacket, it removes the option from the packet.
     * @param code
     * @param value
     */
    public DHCPOption(byte code, byte[] value) {
    	if (code == DHO_PAD) {
    		throw new IllegalArgumentException("code=0 is not allowed (reserved for padding");
        }
        if (code == DHO_END) {
    		throw new IllegalArgumentException("code=-1 is not allowed (reserved for End Of Options)");
        }
        if (value != null && value.length > 255) {
    		throw new IllegalArgumentException("value lentgth is too big (" + value.length + ") max is 255");
        }

        this.code  = code;
        this.value = value;
    }

    /**
     * Return the <tt>code</tt> field (byte).
     * 
     * @return code field
     */
    public byte getCode() {
        return this.code;
    }

    /**
     * returns true if two <tt>DHCPOption</tt> objects are equal, i.e. have same <tt>code</tt>
     * and same <tt>value</tt>.
     */
    public boolean equals(Object o) {
    	if (o == this) {
            return true;
        }
        if (!(o instanceof DHCPOption)) {
            return false;
        }
        DHCPOption opt = (DHCPOption) o;
        return ((opt.code == this.code) &&
        		 Arrays.equals(opt.value, this.value));
    	
    }

    /**
     * Returns hashcode.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.code ^ Arrays.hashCode(this.value);
	}

	/**
     * 
     * @return option value, can be null.
     */
    public byte[] getValue() {
        return ((this.value == null) ? null : this.value.clone());
    }

    /**
     * 
     * @return option value, never <tt>null</tt>. Minimal value is <tt>byte[0]</tt>.
     */
    public byte[] getValueFast() {
        return this.value;
    }

    /**
     * Creates a DHCP Option as Byte format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_IP_FORWARDING(19)
	 * DHO_NON_LOCAL_SOURCE_ROUTING(20)
	 * DHO_DEFAULT_IP_TTL(23)
	 * DHO_ALL_SUBNETS_LOCAL(27)
	 * DHO_PERFORM_MASK_DISCOVERY(29)
	 * DHO_MASK_SUPPLIER(30)
	 * DHO_ROUTER_DISCOVERY(31)
	 * DHO_TRAILER_ENCAPSULATION(34)
	 * DHO_IEEE802_3_ENCAPSULATION(36)
	 * DHO_DEFAULT_TCP_TTL(37)
	 * DHO_TCP_KEEPALIVE_GARBAGE(39)
	 * DHO_NETBIOS_NODE_TYPE(46)
	 * DHO_DHCP_OPTION_OVERLOAD(52)
	 * DHO_DHCP_MESSAGE_TYPE(53)
	 * DHO_AUTO_CONFIGURE(116)
     * </pre>
     * 
     * @param code the option code.
     * @param val the value
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    public static DHCPOption newOptionAsByte(byte code, byte val) {
    	if (!OptionFormat.BYTE.equals(_DHO_FORMATS.get(code))) {
            throw new IllegalArgumentException("DHCP option type (" + code + ") is not byte");
        }
        return new DHCPOption(code, byte2Bytes(val));
    }

    /**
     * Returns a DHCP Option as Byte format.
     * 
     * This method is only allowed for the following option codes:
     * <pre>
	 * DHO_IP_FORWARDING(19)
	 * DHO_NON_LOCAL_SOURCE_ROUTING(20)
	 * DHO_DEFAULT_IP_TTL(23)
	 * DHO_ALL_SUBNETS_LOCAL(27)
	 * DHO_PERFORM_MASK_DISCOVERY(29)
	 * DHO_MASK_SUPPLIER(30)
	 * DHO_ROUTER_DISCOVERY(31)
	 * DHO_TRAILER_ENCAPSULATION(34)
	 * DHO_IEEE802_3_ENCAPSULATION(36)
	 * DHO_DEFAULT_TCP_TTL(37)
	 * DHO_TCP_KEEPALIVE_GARBAGE(39)
	 * DHO_NETBIOS_NODE_TYPE(46)
	 * DHO_DHCP_OPTION_OVERLOAD(52)
	 * DHO_DHCP_MESSAGE_TYPE(53)
	 * DHO_AUTO_CONFIGURE(116)
     * </pre>
     * 
     * @return the option value, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     * @throws DHCPBadPacketException the option value in packet is of wrong size.
     */
    public byte getValueAsByte() throws IllegalArgumentException {
        if (!OptionFormat.BYTE.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not byte");
        }
        if (this.value == null) {
        	throw new IllegalStateException("value is null");
        }
        if (this.value.length != 1) {
        	throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 1");
        }
        return this.value[0];
    }

    /**
     * Returns a DHCP Option as Short format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_BOOT_SIZE(13)
	 * DHO_MAX_DGRAM_REASSEMBLY(22)
	 * DHO_INTERFACE_MTU(26)
	 * DHO_DHCP_MAX_MESSAGE_SIZE(57)
     * </pre>
     * 
     * @return the option value, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     * @throws DHCPBadPacketException the option value in packet is of wrong size.
     */
    public short getValueAsShort() throws IllegalArgumentException {
    	if (!OptionFormat.SHORT.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not short");
        }
        if (this.value == null) {
        	throw new IllegalStateException("value is null");
        }
        if (this.value.length != 2) {
        	throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 2");
        }

        return (short) ((this.value[0] & 0xff) << 8 | (this.value[1] & 0xFF));
    }

    /**
     * Returns a DHCP Option as Integer format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_TIME_OFFSET(2)
	 * DHO_PATH_MTU_AGING_TIMEOUT(24)
	 * DHO_ARP_CACHE_TIMEOUT(35)
	 * DHO_TCP_KEEPALIVE_INTERVAL(38)
	 * DHO_DHCP_LEASE_TIME(51)
	 * DHO_DHCP_RENEWAL_TIME(58)
	 * DHO_DHCP_REBINDING_TIME(59)
     * </pre>
     * 
     * @return the option value, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     * @throws DHCPBadPacketException the option value in packet is of wrong size.
     */
    public int getValueAsInt() throws IllegalArgumentException {
    	if (!OptionFormat.INT.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type (" + this.code + ") is not int");
        }
        if (this.value == null) {
        	throw new IllegalStateException("value is null");
        }
        if (this.value.length != 4) {
        	throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 4");
        }
        return ((this.value[0] & 0xFF) << 24 |
                (this.value[1] & 0xFF) << 16 |
                (this.value[2] & 0xFF) <<  8 |
                (this.value[3] & 0xFF));
    }

    /**
     * Returns a DHCP Option as InetAddress format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_SUBNET_MASK(1)
	 * DHO_SWAP_SERVER(16)
	 * DHO_BROADCAST_ADDRESS(28)
	 * DHO_ROUTER_SOLICITATION_ADDRESS(32)
	 * DHO_DHCP_REQUESTED_ADDRESS(50)
	 * DHO_DHCP_SERVER_IDENTIFIER(54)
	 * DHO_SUBNET_SELECTION(118)
     * </pre>
     * 
     * @return the option value, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     * @throws DHCPBadPacketException the option value in packet is of wrong size.
     */
    public InetAddress getValueAsInetAddr() throws IllegalArgumentException {
    	if (!OptionFormat.INET.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type ("+ this.code +") is not InetAddr");
        }
        if (this.value == null) {
            throw new IllegalStateException("value is null");
        }
        if (this.value.length != 4) {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 4");
        }
        try {
            return InetAddress.getByAddress(this.value);
        } catch (UnknownHostException e) {
			logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
            return null;	// normally impossible
        }
    }

    /**
     * Returns a DHCP Option as String format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_HOST_NAME(12)
	 * DHO_MERIT_DUMP(14)
	 * DHO_DOMAIN_NAME(15)
	 * DHO_ROOT_PATH(17)
	 * DHO_EXTENSIONS_PATH(18)
	 * DHO_NETBIOS_SCOPE(47)
	 * DHO_DHCP_MESSAGE(56)
	 * DHO_VENDOR_CLASS_IDENTIFIER(60)
	 * DHO_NWIP_DOMAIN_NAME(62)
	 * DHO_NIS_DOMAIN(64)
	 * DHO_NIS_SERVER(65)
	 * DHO_TFTP_SERVER(66)
	 * DHO_BOOTFILE(67)
	 * DHO_NDS_TREE_NAME(86)
	 * DHO_USER_AUTHENTICATION_PROTOCOL(98)
     * </pre>
     * 
     * @return the option value, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    public String getValueAsString() throws IllegalArgumentException {
    	if (!OptionFormat.STRING.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type ("+ this.code +") is not String");
        }
        if (this.value == null) {
            throw new IllegalStateException("value is null");
        }
        return DHCPPacket.bytesToString(this.value);
    }

    /**
     * Returns a DHCP Option as Short array format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_PATH_MTU_PLATEAU_TABLE(25)
	 * DHO_NAME_SERVICE_SEARCH(117)
     * </pre>
     * 
     * @return the option value array, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     * @throws DHCPBadPacketException the option value in packet is of wrong size.
     */
    public short[] getValueAsShorts() throws IllegalArgumentException {
    	if (!OptionFormat.SHORTS.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type ("+ this.code +") is not short[]");
        }
        if (this.value == null) {
            throw new IllegalStateException("value is null");
        }
        if ((this.value.length % 2) != 0)		// multiple of 2
        {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 2*X");
        }
        
        short[] shorts = new short[this.value.length / 2];
        for (int i=0, a=0; a< this.value.length; i++, a+=2) {
            shorts[i] = (short) (((this.value[a] & 0xFF)<<8) | (this.value[a+1] & 0xFF));
        }
        return shorts;
    }

    /**
     * Returns a DHCP Option as InetAddress array format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_ROUTERS(3)
	 * DHO_TIME_SERVERS(4)
	 * DHO_NAME_SERVERS(5)
	 * DHO_DOMAIN_NAME_SERVERS(6)
	 * DHO_LOG_SERVERS(7)
	 * DHO_COOKIE_SERVERS(8)
	 * DHO_LPR_SERVERS(9)
	 * DHO_IMPRESS_SERVERS(10)
	 * DHO_RESOURCE_LOCATION_SERVERS(11)
	 * DHO_POLICY_FILTER(21)
	 * DHO_STATIC_ROUTES(33)
	 * DHO_NIS_SERVERS(41)
	 * DHO_NTP_SERVERS(42)
	 * DHO_NETBIOS_NAME_SERVERS(44)
	 * DHO_NETBIOS_DD_SERVER(45)
	 * DHO_FONT_SERVERS(48)
	 * DHO_X_DISPLAY_MANAGER(49)
	 * DHO_MOBILE_IP_HOME_AGENT(68)
	 * DHO_SMTP_SERVER(69)
	 * DHO_POP3_SERVER(70)
	 * DHO_NNTP_SERVER(71)
	 * DHO_WWW_SERVER(72)
	 * DHO_FINGER_SERVER(73)
	 * DHO_IRC_SERVER(74)
	 * DHO_STREETTALK_SERVER(75)
	 * DHO_STDA_SERVER(76)
	 * DHO_NDS_SERVERS(85)
     * </pre>
     * 
     * @return the option value array, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     * @throws DHCPBadPacketException the option value in packet is of wrong size.
     */
    public InetAddress[] getValueAsInetAddrs() throws IllegalArgumentException {
    	if (!OptionFormat.INETS.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type ("+ this.code +") is not InetAddr[]");
        }
        if (this.value == null) {
            throw new IllegalStateException("value is null");
        }
        if ((this.value.length % 4) != 0)		// multiple of 4
        {
            throw new DHCPBadPacketException("option " + this.code + " is wrong size:" + this.value.length + " should be 4*X");
        }
        try {
            byte[] addr = new byte[4];
            InetAddress[] addrs = new InetAddress[this.value.length / 4];
            for (int i=0, a=0; a< this.value.length; i++, a+=4) {
                addr[0] = this.value[a];
                addr[1] = this.value[a+1];
                addr[2] = this.value[a+2];
                addr[3] = this.value[a+3];
                addrs[i] = InetAddress.getByAddress(addr); 
            }
            return addrs;
        } catch (UnknownHostException e) {
			logger.log(Level.SEVERE, "Unexpected UnknownHostException", e);
            return null;	// normally impossible
        }
    }

    /**
     * Returns a DHCP Option as Byte array format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
     * DHO_DHCP_PARAMETER_REQUEST_LIST(55)
     * </pre>
     * 
     * <p>Note: this mehtod is similar to getOptionRaw, only with option type checking.
     * 
     * @return the option value array, <tt>null</tt> if option is not present.
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    public byte[] getValueAsBytes() throws IllegalArgumentException {
    	if (!OptionFormat.BYTES.equals(_DHO_FORMATS.get(this.code))) {
            throw new IllegalArgumentException("DHCP option type ("+ this.code +") is not bytes");
        }
        if (this.value == null) {
            throw new IllegalStateException("value is null");
        }
        return this.getValue();
    }

    /**
     * Creates a DHCP Option as Short format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_BOOT_SIZE(13)
	 * DHO_MAX_DGRAM_REASSEMBLY(22)
	 * DHO_INTERFACE_MTU(26)
	 * DHO_DHCP_MAX_MESSAGE_SIZE(57)
     * </pre>
     * 
     * @param code the option code.
     * @param val the value
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    // TODO newOptionAsShorts
    public static DHCPOption newOptionAsShort(byte code, short val) {
    	if (!OptionFormat.SHORT.equals(_DHO_FORMATS.get(code))) {
            throw new IllegalArgumentException("DHCP option type ("+code+") is not short");
        }
        return new DHCPOption(code, short2Bytes(val));
    }

    /**
     * Creates a DHCP Option as Integer format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_TIME_OFFSET(2)
	 * DHO_PATH_MTU_AGING_TIMEOUT(24)
	 * DHO_ARP_CACHE_TIMEOUT(35)
	 * DHO_TCP_KEEPALIVE_INTERVAL(38)
	 * DHO_DHCP_LEASE_TIME(51)
	 * DHO_DHCP_RENEWAL_TIME(58)
	 * DHO_DHCP_REBINDING_TIME(59)
     * </pre>
     * 
     * @param code the option code.
     * @param val the value
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    public static DHCPOption newOptionAsInt(byte code, int val) {
    	if (!OptionFormat.INT.equals(_DHO_FORMATS.get(code))) {
            throw new IllegalArgumentException("DHCP option type ("+code+") is not int");
        }
        return new DHCPOption(code, int2Bytes(val));
    }

    /**
     * Sets a DHCP Option as InetAddress format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_SUBNET_MASK(1)
	 * DHO_SWAP_SERVER(16)
	 * DHO_BROADCAST_ADDRESS(28)
	 * DHO_ROUTER_SOLICITATION_ADDRESS(32)
	 * DHO_DHCP_REQUESTED_ADDRESS(50)
	 * DHO_DHCP_SERVER_IDENTIFIER(54)
	 * DHO_SUBNET_SELECTION(118)
     * </pre>
     * and also as a simplified version for setOptionAsInetAddresses
     * <pre>
	 * DHO_ROUTERS(3)
	 * DHO_TIME_SERVERS(4)
	 * DHO_NAME_SERVERS(5)
	 * DHO_DOMAIN_NAME_SERVERS(6)
	 * DHO_LOG_SERVERS(7)
	 * DHO_COOKIE_SERVERS(8)
	 * DHO_LPR_SERVERS(9)
	 * DHO_IMPRESS_SERVERS(10)
	 * DHO_RESOURCE_LOCATION_SERVERS(11)
	 * DHO_POLICY_FILTER(21)
	 * DHO_STATIC_ROUTES(33)
	 * DHO_NIS_SERVERS(41)
	 * DHO_NTP_SERVERS(42)
	 * DHO_NETBIOS_NAME_SERVERS(44)
	 * DHO_NETBIOS_DD_SERVER(45)
	 * DHO_FONT_SERVERS(48)
	 * DHO_X_DISPLAY_MANAGER(49)
	 * DHO_MOBILE_IP_HOME_AGENT(68)
	 * DHO_SMTP_SERVER(69)
	 * DHO_POP3_SERVER(70)
	 * DHO_NNTP_SERVER(71)
	 * DHO_WWW_SERVER(72)
	 * DHO_FINGER_SERVER(73)
	 * DHO_IRC_SERVER(74)
	 * DHO_STREETTALK_SERVER(75)
	 * DHO_STDA_SERVER(76)
	 * DHO_NDS_SERVERS(85)
     * </pre>
     * 
     * @param code the option code.
     * @param val the value
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    public static DHCPOption newOptionAsInetAddress(byte code, InetAddress val) {
    	if ((!OptionFormat.INET.equals(_DHO_FORMATS.get(code))) &&
    		(!OptionFormat.INETS.equals(_DHO_FORMATS.get(code)))) {
            throw new IllegalArgumentException("DHCP option type ("+code+") is not InetAddress");
        }
        return new DHCPOption(code, inetAddress2Bytes(val));
    }

    /**
     * Creates a DHCP Option as InetAddress array format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_ROUTERS(3)
	 * DHO_TIME_SERVERS(4)
	 * DHO_NAME_SERVERS(5)
	 * DHO_DOMAIN_NAME_SERVERS(6)
	 * DHO_LOG_SERVERS(7)
	 * DHO_COOKIE_SERVERS(8)
	 * DHO_LPR_SERVERS(9)
	 * DHO_IMPRESS_SERVERS(10)
	 * DHO_RESOURCE_LOCATION_SERVERS(11)
	 * DHO_POLICY_FILTER(21)
	 * DHO_STATIC_ROUTES(33)
	 * DHO_NIS_SERVERS(41)
	 * DHO_NTP_SERVERS(42)
	 * DHO_NETBIOS_NAME_SERVERS(44)
	 * DHO_NETBIOS_DD_SERVER(45)
	 * DHO_FONT_SERVERS(48)
	 * DHO_X_DISPLAY_MANAGER(49)
	 * DHO_MOBILE_IP_HOME_AGENT(68)
	 * DHO_SMTP_SERVER(69)
	 * DHO_POP3_SERVER(70)
	 * DHO_NNTP_SERVER(71)
	 * DHO_WWW_SERVER(72)
	 * DHO_FINGER_SERVER(73)
	 * DHO_IRC_SERVER(74)
	 * DHO_STREETTALK_SERVER(75)
	 * DHO_STDA_SERVER(76)
	 * DHO_NDS_SERVERS(85)
     * </pre>
     * 
     * @param code the option code.
     * @param val the value array
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    public static DHCPOption newOptionAsInetAddresses(byte code, InetAddress[] val) {
    	if (!OptionFormat.INETS.equals(_DHO_FORMATS.get(code))) {
            throw new IllegalArgumentException("DHCP option type ("+code+") is not InetAddresses");
        }
        return new DHCPOption(code, inetAddresses2Bytes(val));
    }

    /**
     * Creates a DHCP Option as String format.
     * 
     * <p>This method is only allowed for the following option codes:
     * <pre>
	 * DHO_HOST_NAME(12)
	 * DHO_MERIT_DUMP(14)
	 * DHO_DOMAIN_NAME(15)
	 * DHO_ROOT_PATH(17)
	 * DHO_EXTENSIONS_PATH(18)
	 * DHO_NETBIOS_SCOPE(47)
	 * DHO_DHCP_MESSAGE(56)
	 * DHO_VENDOR_CLASS_IDENTIFIER(60)
	 * DHO_NWIP_DOMAIN_NAME(62)
	 * DHO_NIS_DOMAIN(64)
	 * DHO_NIS_SERVER(65)
	 * DHO_TFTP_SERVER(66)
	 * DHO_BOOTFILE(67)
	 * DHO_NDS_TREE_NAME(86)
	 * DHO_USER_AUTHENTICATION_PROTOCOL(98)
     * </pre>
     * 
     * @param code the option code.
     * @param val the value
     * @throws IllegalArgumentException the option code is not in the list above.
     */
    public static DHCPOption newOptionAsString(byte code, String val) {
    	if (!OptionFormat.STRING.equals(_DHO_FORMATS.get(code))) {
            throw new IllegalArgumentException("DHCP option type ("+code+") is not string");
        }
    	return new DHCPOption(code, DHCPPacket.stringToBytes(val));
    }

    /**
     * Appends to this string builder a detailed string representation of the DHCP datagram.
     *
     * <p>This multi-line string details: the static, options and padding parts
     * of the object. This is useful for debugging, but not efficient.
     *
     * @param buffer the string builder the string representation of this object should be appended.
     */
    public void append(StringBuilder buffer) {
        // check for readable option name
        if (_DHO_NAMES.containsKey(this.code)) {
        	buffer.append(_DHO_NAMES.get(this.code));
        }
        buffer.append('(')
              .append(unsignedByte(this.code))
              .append(")=");

        // check for value printing
        if (this.code == DHO_DHCP_MESSAGE_TYPE) {
        	Byte cmd = this.getValueAsByte();
        	if (_DHCP_CODES.containsKey(cmd)) {
        		buffer.append(_DHCP_CODES.get(cmd));
        	} else {
        		buffer.append(cmd);
        	}
        } else if (this.code == DHO_USER_CLASS) {
        	buffer.append(userClassToString(this.value));
        } else if (this.code == DHO_DHCP_AGENT_OPTIONS) {
        	buffer.append(agentOptionsToString(this.value));
        } else if (_DHO_FORMATS.containsKey(this.code)) {
        	// formatted output
        	try {	// catch malformed values
        		switch (_DHO_FORMATS.get(this.code)) {
                    case INET:
                        DHCPPacket.appendHostAddress(buffer, this.getValueAsInetAddr());
                        break;
                    case INETS:
                        for (InetAddress addr : this.getValueAsInetAddrs()) {
                            DHCPPacket.appendHostAddress(buffer, addr);
                            buffer.append(' ');
                        }
                        break;
                    case INT:
                        buffer.append(this.getValueAsInt());
                        break;
                    case SHORT:
                        buffer.append(this.getValueAsShort());
                        break;
                    case SHORTS:
                        for (short aShort : this.getValueAsShorts()) {
                            buffer.append(aShort)
                                  .append(' ');
                        }
                        break;
                    case BYTE:
                        buffer.append(this.getValueAsByte());
                        break;
                    case STRING:
                        buffer.append('"')
                              .append(this.getValueAsString())
                              .append('"');
                        break;
                    case BYTES:
                        if (this.value != null) {
                            for (byte aValue : this.value) {
                                buffer.append(unsignedByte(aValue))
                                      .append(' ');
                            }
                        }
                        break;
        		default:
        			buffer.append("0x");
                    DHCPPacket.appendHex(buffer, this.value);
            		break;
        		}
        	} catch (IllegalArgumentException e) {
        		// fallback to bytes
                buffer.append("0x");
                DHCPPacket.appendHex(buffer, this.value);
        	}
        } else {
        	// unformatted raw output
        	buffer.append("0x");
            DHCPPacket.appendHex(buffer, this.value);
        }
    }

    /**
     * Returns a detailed string representation of the DHCP datagram.
     * 
     * <p>This multi-line string details: the static, options and padding parts
     * of the object. This is useful for debugging, but not efficient.
     * 
     * @return a string representation of the object.
     */
    public String toString() {
        StringBuilder s = new StringBuilder();

        this.append(s);
        return s.toString();
    }

    /**
     * Convert unsigned byte to int
     */
    private static int unsignedByte(byte b) {
        return (b & 0xFF);
    }
    
    /**************************************************************************
     * 
     * Type converters.
     * 
     **************************************************************************/

    public static byte[] byte2Bytes(byte val) {
        byte[] raw = { val };
        return raw;
    }
    public static byte[] short2Bytes(short val) {
        byte[] raw = { (byte) ((val & 0xFF00) >>> 8), (byte) (val & 0XFF) };
        return raw;
    }
    public static byte[] int2Bytes(int val) {
        byte[] raw = {  (byte) ((val & 0xFF000000) >>> 24),
                		(byte) ((val & 0X00FF0000) >>> 16),
                		(byte) ((val & 0x0000FF00) >>>  8),
                		(byte) ((val & 0x000000FF)       ) };
        return raw;
    }
    public static byte[] inetAddress2Bytes(InetAddress val) {
        if (val == null){
        	return null;
        }
        if (!(val instanceof Inet4Address)) {
            throw new IllegalArgumentException("Adress must be of subclass Inet4Address");
        }
        return val.getAddress();
    }
    public static byte[] inetAddresses2Bytes(InetAddress[] val) {
        if (val == null) {
            return null;
        }
        
        byte[] buf = new byte[val.length * 4];
        for (int i=0; i<val.length; i++) {
            InetAddress addr = val[i];
            if (!(addr instanceof Inet4Address)) {
                throw new IllegalArgumentException("Adress must be of subclass Inet4Address");
            }
            System.arraycopy(addr.getAddress(), 0, buf, i*4, 4);
        }
        return buf;
    }

    /**
     * Convert DHO_USER_CLASS (77) option to a List.
     * 
     * @param buf option value of type User Class.
     * @return List of String values.
     */
    public static List<String> userClassToList(byte[] buf) {
        if (buf == null) {
            return null;
        }
        
        LinkedList<String> list = new LinkedList<String>();
        int i=0;
        while (i<buf.length) {
            int size = unsignedByte(buf[i++]);
            int instock = buf.length - i;
            if (size > instock) {
                size = instock;
            }
            list.add(DHCPPacket.bytesToString(buf, i, size));
            i += size;
        }
        return list;
    }

    /**
     * Converts DHO_USER_CLASS (77) option to a printable string
     * 
     * @param buf option value of type User Class.
     * @return printable string.
     */
    public static String userClassToString(byte[] buf) {
        if (buf == null) {
            return null;
        }
        
        List list = userClassToList(buf);
        Iterator it = list.iterator();
        StringBuffer s = new StringBuffer();
        
        while (it.hasNext()) {
            s.append('"').append((String) it.next()).append('"');
            if (it.hasNext()) {
                s.append(',');
            }
        }
        return s.toString();
    }
    /**
     * Converts DHO_DHCP_AGENT_OPTIONS (82) option type to a printable string
     * 
     * @param buf option value of type Agent Option.
     * @return printable string.
     */
    public static String agentOptionsToString(byte[] buf) {
    	if (buf == null) {
    		return null;
    	}
    	
        Map map = agentOptionsToMap(buf);
        Iterator it = map.keySet().iterator();
        StringBuffer s = new StringBuffer();
        
        while (it.hasNext()) {
            Byte key = (Byte) it.next();
            s.append('{').append(unsignedByte(key.byteValue())).append("}\"");
            s.append((String) map.get(key)).append('\"');
            if (it.hasNext()) {
                s.append(',');
            }
        }
        return s.toString();
    }
    /**
     * Converts Map<Byte,String> to DHO_DHCP_AGENT_OPTIONS (82) option.
     * 
     * <p>LinkedHashMap are preferred as they preserve insertion order. Regular
     * HashMap order is randon.
     * 
     * @param map Map<Byte,String> couples
     * @return byte[] buffer to use with <tt>setOptionRaw</tt>
     * @throws IllegalArgumentException if List contains anything else than String
     */
    public static byte[] agentOptionToRaw(Map<Byte, String> map) {
    	if (map == null) {
            return null;
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(64);
        DataOutputStream out = new DataOutputStream(buf);
        try {
        	for (Byte key : map.keySet()) {
	            String s = map.get(key);
	            byte[] bufTemp = DHCPPacket.stringToBytes(s);
	            int size = bufTemp.length;
	            assert (size >= 0);
	            if (size > 255) {
	            	throw new IllegalArgumentException("Value size is greater then 255 bytes");
                }
	            out.writeByte(key.byteValue());
	            out.writeByte(size);
	            out.write(bufTemp, 0, size);
	        }
	        return buf.toByteArray();
	    } catch (IOException e) {
			logger.log(Level.SEVERE, "Unexpected IOException", e);
	        return buf.toByteArray();
	    }
    }
    /**
     * Converts DHO_DHCP_AGENT_OPTIONS (82) option type to a LinkedMap.
     * 
     * <p>Order of parameters is preserved (use avc <tt>LinkedHashmap</tt<).
     * Keys are of type <tt>Byte</tt>, values are of type <tt>String</tt>.
     * 
     * @param buf byte[] buffer returned by </tt>getOptionRaw</tt>
     * @return the LinkedHashmap of values, <tt>null</tt> if buf is <tt>null</tt>
     */
    public static Map<Byte, String> agentOptionsToMap(byte[] buf) {
    	if (buf == null) {
            return null;
        }

        Map<Byte, String> map = new LinkedHashMap<Byte, String>();
        int               i   = 0;

        while (i < buf.length) {
            if (buf.length - i < 2) {
                break;    // not enough data left
            }
            Byte key     = buf[i++];
            int  size    = unsignedByte(buf[i++]);
            int  instock = buf.length - i;

            if (size > instock) {
                size = instock;
            }
            map.put(key, DHCPPacket.bytesToString(buf, i, size));
            i += size;
        }
        return map;
    }
    // ----------------------------------------------------------------------
    // Internal constants for high-level option type conversions.
    //
    // formats of options
    //
    enum OptionFormat {
        VOID,	// empty,					size = 0
        INET,	// 4 bytes IP,				size = 4
        INETS,	// list of 4 bytes IP,		size = 4*n
        INT,	// 4 bytes integer,			size = 4
        SHORT,	// 2 bytes short,			size = 2
        SHORTS,	// list of 2 bytes shorts,	size = 2*n
        BYTE,	// 1 byte,					size = 1
        BYTES,	// list of bytes,			size = n
        STRING,	// string,					size = n
        //RELAYS	= 9;	// DHCP sub-options (rfc 3046)
        //ID		= 10;	// client identifier : byte (htype) + string (chaddr)
    	
    }
    
    //
    // list of formats by options
    //
    private static final Object[] _OPTION_FORMATS = {
            DHO_PAD,							OptionFormat.VOID,
            DHO_SUBNET_MASK,					OptionFormat.INET,
            DHO_TIME_OFFSET,					OptionFormat.INT,
            DHO_ROUTERS,						OptionFormat.INETS,
            DHO_TIME_SERVERS,					OptionFormat.INETS,
            DHO_NAME_SERVERS,					OptionFormat.INETS,
            DHO_DOMAIN_NAME_SERVERS,			OptionFormat.INETS,
            DHO_LOG_SERVERS,					OptionFormat.INETS,
            DHO_COOKIE_SERVERS,					OptionFormat.INETS,
            DHO_LPR_SERVERS,					OptionFormat.INETS,
            DHO_IMPRESS_SERVERS,				OptionFormat.INETS,
            DHO_RESOURCE_LOCATION_SERVERS,		OptionFormat.INETS,
            DHO_HOST_NAME,						OptionFormat.STRING,
            DHO_BOOT_SIZE,						OptionFormat.SHORT,
            DHO_MERIT_DUMP,						OptionFormat.STRING,
            DHO_DOMAIN_NAME,					OptionFormat.STRING,
            DHO_SWAP_SERVER,					OptionFormat.INET,
            DHO_ROOT_PATH,						OptionFormat.STRING,
            DHO_EXTENSIONS_PATH,				OptionFormat.STRING,
            DHO_IP_FORWARDING,					OptionFormat.BYTE,
            DHO_NON_LOCAL_SOURCE_ROUTING,		OptionFormat.BYTE,
            DHO_POLICY_FILTER,					OptionFormat.INETS,
            DHO_MAX_DGRAM_REASSEMBLY,			OptionFormat.SHORT,
            DHO_DEFAULT_IP_TTL,					OptionFormat.BYTE,
            DHO_PATH_MTU_AGING_TIMEOUT,			OptionFormat.INT,
            DHO_PATH_MTU_PLATEAU_TABLE,			OptionFormat.SHORTS,
            DHO_INTERFACE_MTU,					OptionFormat.SHORT,
            DHO_ALL_SUBNETS_LOCAL,				OptionFormat.BYTE,
            DHO_BROADCAST_ADDRESS,				OptionFormat.INET,
            DHO_PERFORM_MASK_DISCOVERY,			OptionFormat.BYTE,
            DHO_MASK_SUPPLIER,					OptionFormat.BYTE,
            DHO_ROUTER_DISCOVERY,				OptionFormat.BYTE,
            DHO_ROUTER_SOLICITATION_ADDRESS,	OptionFormat.INET,
            DHO_STATIC_ROUTES,					OptionFormat.INETS,
            DHO_TRAILER_ENCAPSULATION,			OptionFormat.BYTE,
            DHO_ARP_CACHE_TIMEOUT,				OptionFormat.INT,
            DHO_IEEE802_3_ENCAPSULATION,		OptionFormat.BYTE,
            DHO_DEFAULT_TCP_TTL,				OptionFormat.BYTE,
            DHO_TCP_KEEPALIVE_INTERVAL,			OptionFormat.INT,
            DHO_TCP_KEEPALIVE_GARBAGE,			OptionFormat.BYTE,
            DHO_NIS_SERVERS,					OptionFormat.INETS,
            DHO_NTP_SERVERS,					OptionFormat.INETS,
            DHO_NETBIOS_NAME_SERVERS,			OptionFormat.INETS,
            DHO_NETBIOS_DD_SERVER,				OptionFormat.INETS,
            DHO_NETBIOS_NODE_TYPE,				OptionFormat.BYTE,
            DHO_NETBIOS_SCOPE,					OptionFormat.STRING,
            DHO_FONT_SERVERS,					OptionFormat.INETS,
            DHO_X_DISPLAY_MANAGER,				OptionFormat.INETS,
            DHO_DHCP_REQUESTED_ADDRESS,			OptionFormat.INET,
            DHO_DHCP_LEASE_TIME,				OptionFormat.INT,
            DHO_DHCP_OPTION_OVERLOAD,			OptionFormat.BYTE,
            DHO_DHCP_MESSAGE_TYPE,				OptionFormat.BYTE,
            DHO_DHCP_SERVER_IDENTIFIER,			OptionFormat.INET,
            DHO_DHCP_PARAMETER_REQUEST_LIST,	OptionFormat.BYTES,
            DHO_DHCP_MESSAGE,					OptionFormat.STRING,
            DHO_DHCP_MAX_MESSAGE_SIZE,			OptionFormat.SHORT,
            DHO_DHCP_RENEWAL_TIME,				OptionFormat.INT,
            DHO_DHCP_REBINDING_TIME,			OptionFormat.INT,
            DHO_VENDOR_CLASS_IDENTIFIER,		OptionFormat.STRING,
            DHO_NWIP_DOMAIN_NAME,				OptionFormat.STRING,
            DHO_NIS_DOMAIN,						OptionFormat.STRING,
            DHO_NIS_SERVER,						OptionFormat.STRING,
            DHO_TFTP_SERVER,					OptionFormat.STRING,
            DHO_BOOTFILE,						OptionFormat.STRING,
            DHO_MOBILE_IP_HOME_AGENT,			OptionFormat.INETS,
            DHO_SMTP_SERVER,					OptionFormat.INETS,
            DHO_POP3_SERVER,					OptionFormat.INETS,
            DHO_NNTP_SERVER,					OptionFormat.INETS,
            DHO_WWW_SERVER,						OptionFormat.INETS,
            DHO_FINGER_SERVER,					OptionFormat.INETS,
            DHO_IRC_SERVER,						OptionFormat.INETS,
            DHO_STREETTALK_SERVER,				OptionFormat.INETS,
            DHO_STDA_SERVER,					OptionFormat.INETS,
            DHO_NDS_SERVERS,					OptionFormat.INETS,
            DHO_NDS_TREE_NAME,					OptionFormat.STRING,
            DHO_NDS_CONTEXT,					OptionFormat.STRING,
            DHO_CLIENT_LAST_TRANSACTION_TIME,	OptionFormat.INT,
            DHO_ASSOCIATED_IP,					OptionFormat.INETS,
            DHO_USER_AUTHENTICATION_PROTOCOL,	OptionFormat.STRING,
            DHO_AUTO_CONFIGURE,					OptionFormat.BYTE,
            DHO_NAME_SERVICE_SEARCH,			OptionFormat.SHORTS,
            DHO_SUBNET_SELECTION,				OptionFormat.INET,
            DHO_DOMAIN_SEARCH,					OptionFormat.STRING,
            
    };    
    static Map<Byte, OptionFormat> _DHO_FORMATS = new LinkedHashMap<Byte, OptionFormat>();

    /*
     * preload at startup Maps with constants
     * allowing reverse lookup
     */
    static {
        // construct map of formats
        for (int i=0; i<_OPTION_FORMATS.length / 2; i++) {
            _DHO_FORMATS.put((Byte) _OPTION_FORMATS[i*2],(OptionFormat) _OPTION_FORMATS[i*2+1]);
        }
    }

    // ========================================================================
    // main: print DHCP options for Javadoc
    public static void main(String[] args) throws IllegalAccessException {
        String all     = "";
        String inet1   = "";
        String inets   = "";
        String int1    = "";
        String short1  = "";
        String shorts  = "";
        String byte1   = "";
        String bytes   = "";
        String string1 = "";

        for (Byte codeByte : _DHO_NAMES.keySet()) {
            byte code = codeByte.byteValue();
            String s = "";
            if (code != DHO_PAD && code != DHO_END) {
                s = " * " + _DHO_NAMES.get(codeByte) + '(' + (code & 0xFF) + ")\n";
            }
            
            all += s;
            if (_DHO_FORMATS.containsKey(codeByte)) {
	            switch (_DHO_FORMATS.get(codeByte)) {
	            	case VOID:                 break;
	            	case INET:   inet1   += s; break;
	            	case INETS:  inets   += s; break;
	            	case INT:    int1    += s; break;
	            	case SHORT:  short1  += s; break;
	            	case SHORTS: shorts  += s; break;
	            	case BYTE:   byte1   += s; break;
	            	case BYTES:  bytes   += s; break;
	            	case STRING: string1 += s; break;
	            	default:
	            }
            }
        }

        System.out.println("---All codes---");
        System.out.println(all);
        System.out.println("---INET---");
        System.out.println(inet1);
        System.out.println("---INETS---");
        System.out.println(inets);
        System.out.println("---INT---");
        System.out.println(int1);
        System.out.println("---SHORT---");
        System.out.println(short1);
        System.out.println("---SHORTS---");
        System.out.println(shorts);
        System.out.println("---BYTE---");
        System.out.println(byte1);
        System.out.println("---BYTES---");
        System.out.println(bytes);
        System.out.println("---STRING---");
        System.out.println(string1);
    }
}
