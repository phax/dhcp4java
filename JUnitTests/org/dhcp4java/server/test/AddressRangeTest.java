package org.dhcp4java.server.test;

import junit.framework.JUnit4TestAdapter;

import org.dhcpcluster.struct.AddressRange;
import org.junit.Test;

import static org.junit.Assert.*;

public class AddressRangeTest {

	public static junit.framework.Test suite() {
       return new JUnit4TestAdapter(AddressRangeTest.class);
    }
	
	@Test (expected=NullPointerException.class)
	public void testConstructorNull() {
		new AddressRange(null, null);
	}
	
}
