/**
 *  This file is part of dhcp4java, a DHCP API for the Java language.
 *  (c) 2006 Stephan Hadinger
 *  (c) 2018 Philip Helger
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.dhcp4java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class HardwareAddressTest
{
  private static HardwareAddress s_aHA = new HardwareAddress (DHCPConstants.HTYPE_ETHER, "001122334455");

  @Test
  public void testConstructor ()
  {
    assertEquals (DHCPConstants.HTYPE_ETHER, s_aHA.getHardwareType ());
    assertEquals ("001122334455", s_aHA.getHardwareAddressHex ());
    assertTrue (Arrays.equals (HexUtils.hexToBytes ("001122334455"), s_aHA.getHardwareAddress ()));

    final HardwareAddress ha2 = new HardwareAddress (DHCPConstants.HTYPE_ETHER, HexUtils.hexToBytes ("001122334455"));
    assertEquals (s_aHA, ha2);
    final HardwareAddress ha3 = new HardwareAddress (HexUtils.hexToBytes ("001122334455"));
    assertEquals (s_aHA, ha3);
    final HardwareAddress ha4 = new HardwareAddress ("001122334455");
    assertEquals (s_aHA, ha4);
    final HardwareAddress ha5 = new HardwareAddress (DHCPConstants.HTYPE_FDDI, HexUtils.hexToBytes ("001122334455"));
    assertFalse (s_aHA.equals (ha5));

    assertTrue (s_aHA.hashCode () != 0);
    assertEquals (s_aHA.hashCode (), ha2.hashCode ());
    assertEquals (s_aHA.hashCode (), ha3.hashCode ());
    assertEquals (s_aHA.hashCode (), ha4.hashCode ());
    assertTrue (s_aHA.hashCode () != ha5.hashCode ());

    assertFalse (s_aHA.equals (null));
    assertFalse (s_aHA.equals (new Object ()));
  }

  @Test
  public void testToString ()
  {
    assertEquals ("00:11:22:33:44:55", s_aHA.getAsString ());
    final HardwareAddress ha2 = new HardwareAddress (DHCPConstants.HTYPE_FDDI, "0011045508");
    assertEquals ("8/00:11:04:55:08", ha2.getAsString ());
  }

  @Test
  public void testGetHardwareAddressByString ()
  {
    final HardwareAddress ha2 = new HardwareAddress ("0011045508FF");
    final HardwareAddress ha3 = HardwareAddress.getHardwareAddressByString ("0:11:4:55:8:Ff");
    assertEquals (ha2, ha3);
  }

  @Test (expected = NullPointerException.class)
  public void testGetHardwareAddressByStringNull ()
  {
    HardwareAddress.getHardwareAddressByString (null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetHardwareAddressByStringEmpty ()
  {
    HardwareAddress.getHardwareAddressByString ("");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testGetHardwareAddressByStringMax ()
  {
    HardwareAddress.getHardwareAddressByString ("0:11:4:55:8:1Ff");
  }
}
