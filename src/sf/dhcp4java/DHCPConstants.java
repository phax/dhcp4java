/*
 *	This file is part of dhcp4java.
 *
 *	dhcp4java is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	dhcp4java is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Foobar; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * (c) 2006 Stephan Hadinger
 */
package sf.dhcp4java;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class holding all DHCP constants.
 * 
 * @author Stephan Hadinger
 * @version 0.50
 */
public class DHCPConstants {

    // ========================================================================
    // DHCP Constants

    /** DHCP BOOTP CODES **/
    public final static byte BOOTREQUEST		= 1;
    public final static byte BOOTREPLY		= 2;
    
    /** DHCP HTYPE CODES **/
    public final static byte HTYPE_ETHER		= 1;
    public final static byte HTYPE_IEEE802	= 6;
    public final static byte HTYPE_FDDI		= 8;
    
    /** DHCP MESSAGE CODES **/
    public final static byte DHCPDISCOVER	= 1;
    public final static byte DHCPOFFER		= 2;
    public final static byte DHCPREQUEST		= 3;
    public final static byte DHCPDECLINE		= 4;
    public final static byte DHCPACK			= 5;
    public final static byte DHCPNAK			= 6;
    public final static byte DHCPRELEASE		= 7;
    public final static byte DHCPINFORM		= 8;
    public final static byte DHCPFORCERENEW	= 9;
    public final static byte DHCPLEASEQUERY	= 13;	// Cisco extension, draft-ietf-dhc-leasequery-08.txt
    
    /** DHCP OPTIONS CODE **/
    public final static byte DHO_PAD							= 0;
    public final static byte DHO_SUBNET_MASK					= 1;
    public final static byte DHO_TIME_OFFSET					= 2;
    public final static byte DHO_ROUTERS						= 3;
    public final static byte DHO_TIME_SERVERS				= 4;
    public final static byte DHO_NAME_SERVERS				= 5;
    public final static byte DHO_DOMAIN_NAME_SERVERS			= 6;
    public final static byte DHO_LOG_SERVERS					= 7;
    public final static byte DHO_COOKIE_SERVERS				= 8;
    public final static byte DHO_LPR_SERVERS					= 9;
    public final static byte DHO_IMPRESS_SERVERS				= 10;
    public final static byte DHO_RESOURCE_LOCATION_SERVERS	= 11;
    public final static byte DHO_HOST_NAME					= 12;
    public final static byte DHO_BOOT_SIZE					= 13;
    public final static byte DHO_MERIT_DUMP					= 14;
    public final static byte DHO_DOMAIN_NAME					= 15;
    public final static byte DHO_SWAP_SERVER					= 16;
    public final static byte DHO_ROOT_PATH					= 17;
    public final static byte DHO_EXTENSIONS_PATH				= 18;
    public final static byte DHO_IP_FORWARDING				= 19;
    public final static byte DHO_NON_LOCAL_SOURCE_ROUTING	= 20;
    public final static byte DHO_POLICY_FILTER				= 21;
    public final static byte DHO_MAX_DGRAM_REASSEMBLY		= 22;
    public final static byte DHO_DEFAULT_IP_TTL				= 23;
    public final static byte DHO_PATH_MTU_AGING_TIMEOUT		= 24;
    public final static byte DHO_PATH_MTU_PLATEAU_TABLE		= 25;
    public final static byte DHO_INTERFACE_MTU				= 26;
    public final static byte DHO_ALL_SUBNETS_LOCAL			= 27;
    public final static byte DHO_BROADCAST_ADDRESS			= 28;
    public final static byte DHO_PERFORM_MASK_DISCOVERY		= 29;
    public final static byte DHO_MASK_SUPPLIER				= 30;
    public final static byte DHO_ROUTER_DISCOVERY			= 31;
    public final static byte DHO_ROUTER_SOLICITATION_ADDRESS	= 32;
    public final static byte DHO_STATIC_ROUTES				= 33;
    public final static byte DHO_TRAILER_ENCAPSULATION		= 34;
    public final static byte DHO_ARP_CACHE_TIMEOUT			= 35;
    public final static byte DHO_IEEE802_3_ENCAPSULATION		= 36;
    public final static byte DHO_DEFAULT_TCP_TTL				= 37;
    public final static byte DHO_TCP_KEEPALIVE_INTERVAL		= 38;
    public final static byte DHO_TCP_KEEPALIVE_GARBAGE		= 39;
    public final static byte DHO_NIS_SERVERS					= 41;
    public final static byte DHO_NTP_SERVERS					= 42;
    public final static byte DHO_VENDOR_ENCAPSULATED_OPTIONS	= 43;
    public final static byte DHO_NETBIOS_NAME_SERVERS		= 44;
    public final static byte DHO_NETBIOS_DD_SERVER			= 45;
    public final static byte DHO_NETBIOS_NODE_TYPE			= 46;
    public final static byte DHO_NETBIOS_SCOPE				= 47;
    public final static byte DHO_FONT_SERVERS				= 48;
    public final static byte DHO_X_DISPLAY_MANAGER			= 49;
    public final static byte DHO_DHCP_REQUESTED_ADDRESS		= 50;
    public final static byte DHO_DHCP_LEASE_TIME				= 51;
    public final static byte DHO_DHCP_OPTION_OVERLOAD		= 52;
    public final static byte DHO_DHCP_MESSAGE_TYPE			= 53;
    public final static byte DHO_DHCP_SERVER_IDENTIFIER		= 54;
    public final static byte DHO_DHCP_PARAMETER_REQUEST_LIST	= 55;
    public final static byte DHO_DHCP_MESSAGE				= 56;
    public final static byte DHO_DHCP_MAX_MESSAGE_SIZE		= 57;
    public final static byte DHO_DHCP_RENEWAL_TIME			= 58;
    public final static byte DHO_DHCP_REBINDING_TIME			= 59;
    public final static byte DHO_VENDOR_CLASS_IDENTIFIER		= 60;
    public final static byte DHO_DHCP_CLIENT_IDENTIFIER		= 61;
    public final static byte DHO_NWIP_DOMAIN_NAME			= 62;
    public final static byte DHO_NWIP_SUBOPTIONS				= 63;
    public final static byte DHO_NIS_DOMAIN					= 64;
    public final static byte DHO_NIS_SERVER					= 65;
    public final static byte DHO_TFTP_SERVER					= 66;
    public final static byte DHO_BOOTFILE					= 67;
    public final static byte DHO_MOBILE_IP_HOME_AGENT		= 68;
    public final static byte DHO_SMTP_SERVER					= 69;
    public final static byte DHO_POP3_SERVER					= 70;
    public final static byte DHO_NNTP_SERVER					= 71;
    public final static byte DHO_WWW_SERVER					= 72;
    public final static byte DHO_FINGER_SERVER				= 73;
    public final static byte DHO_IRC_SERVER					= 74;
    public final static byte DHO_STREETTALK_SERVER			= 75;
    public final static byte DHO_STDA_SERVER					= 76;
    public final static byte DHO_USER_CLASS					= 77;	// rfc 3004
    public final static byte DHO_FQDN						= 81;
    public final static byte DHO_DHCP_AGENT_OPTIONS			= 82;	// rfc 3046
    public final static byte DHO_NDS_SERVERS					= 85;
    public final static byte DHO_NDS_TREE_NAME				= 86;
    public final static byte DHO_USER_AUTHENTICATION_PROTOCOL= 98;
    public final static byte DHO_AUTO_CONFIGURE				= 116;
    public final static byte DHO_NAME_SERVICE_SEARCH			= 117;
    public final static byte DHO_SUBNET_SELECTION			= 118;
    public final static byte DHO_END							= -1;
    
    /**
     * Broadcast Address
     */
    public static final InetAddress INADDR_BROADCAST = getInaddrBroadcast();

    private static final InetAddress getInaddrBroadcast() {
    	byte[] rawAddr = { (byte) -1, (byte) -1, (byte) -1, (byte) -1 };
    	try {
    		return InetAddress.getByAddress(rawAddr);
    	} catch (UnknownHostException e) {
    		// bad luck
    		throw new IllegalStateException("Unable to generate INADDR_BROADCAST");
    	}
    }
    
    // sanity check values
    final static int _DHCP_MIN_LEN = 548;
    final static int _BOOTP_MIN_LEN = 300;
    final static int _BOOTP_ABSOLUTE_MIN_LEN = 236;
    final static int _DHCP_MAX_MTU = 1500;
    final static int _DHCP_UDP_OVERHEAD = 14 + 20 + 8;
    
    // Magic cookie
    final static int _MAGIC_COOKIE = 0x63825363;
    
    // Maps for "code" to "string" conversion
    static Map<Byte, String> _BOOT_NAMES = new LinkedHashMap<Byte, String>();
    static Map<Byte, String> _HTYPE_NAMES = new LinkedHashMap<Byte, String>();
    static Map<Byte, String> _DHCP_CODES = new LinkedHashMap<Byte, String>();
    static Map<Byte, String> _DHO_NAMES = new LinkedHashMap<Byte, String>();

    /*
     * preload at startup Maps with constants
     * allowing reverse lookup
     */
    static {
        // do some introspection to list constants
        Field[] fields = DHCPConstants.class.getDeclaredFields();
        
        // parse internal fields
        try {
        	for (Field field : fields) {
                int mod = field.getModifiers();
                String name = field.getName();
                byte code;

                // parse only "public final static byte"
                if (!Modifier.isFinal(mod) || !Modifier.isPublic(mod)
                        || !Modifier.isStatic(mod) ||
                        !field.getType().equals(byte.class)) {
                    continue;
                }
                
                code = field.getByte(null);
                if (name.startsWith("BOOT")) {
                    _BOOT_NAMES.put(code, name);
                } else if (name.startsWith("HTYPE_")) {
                    _HTYPE_NAMES.put(code, name);
                } else if (name.startsWith("DHCP")) {
                    _DHCP_CODES.put(code, name);
                } else if (name.startsWith("DHO_")) {
                    _DHO_NAMES.put(code, name);
                }
            }
            
        } catch (IllegalAccessException e) {
            // we have a problem
            throw new IllegalStateException("Fatal error while parsing internal fields");
        }
    }
}
