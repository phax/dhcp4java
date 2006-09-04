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
package org.dhcp4java.test;

import java.util.Arrays;

import org.dhcp4java.DHCPOption;
import org.dhcp4java.DHCPMirrorOption;
import org.dhcp4java.DHCPPacket;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.*;

import static org.dhcp4java.DHCPConstants.*;

public class DHCPMirrorOptionTest {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DHCPMirrorOptionTest.class);
    }
    
    private DHCPMirrorOption opt = null;
    
    @Test (expected=IllegalArgumentException.class)
    public void testConstructorPad() {
    	new DHCPMirrorOption((byte)0);		// 0 is reserved for padding
    }
    @Test (expected=IllegalArgumentException.class)
    public void testConstructorEnd() {
    	new DHCPMirrorOption((byte)0xFF);	// 0xFF is reserved for "end of options"
    }

    @Before
    public void setupOpt() {
    	opt = new DHCPMirrorOption(DHO_DHCP_LEASE_TIME);
    }
    
    @Test
    public void testConstructor() {
    	assertTrue(opt instanceof DHCPOption);
    	assertEquals(DHO_DHCP_LEASE_TIME, opt.getCode());
    	assertNull(opt.getValue());
    }
    
    @Test
    public void testGetMirrorValue() {
    	DHCPOption mirrorOpt;
    	DHCPPacket pac = new DHCPPacket();
    	assertNull(opt.getMirrorValue(pac));
    	
    	pac.setOptionAsInt(DHO_DHCP_LEASE_TIME, 86400);
    	
    	mirrorOpt = opt.getMirrorValue(pac);
    	assertEquals(DHO_DHCP_LEASE_TIME, mirrorOpt.getCode());
    	assertTrue(Arrays.equals(DHCPOption.int2Bytes(86400), mirrorOpt.getValue()));
    	
    	pac.setOptionRaw(DHO_DHCP_LEASE_TIME, new byte[0]);
    	mirrorOpt = opt.getMirrorValue(pac);
    	assertEquals(DHO_DHCP_LEASE_TIME, mirrorOpt.getCode());
    	assertTrue(Arrays.equals(new byte[0], mirrorOpt.getValue()));
    }
    @Test (expected=NullPointerException.class)
    public void testGetMirrorValueNull() {
    	opt.getMirrorValue(null);
    }

}
