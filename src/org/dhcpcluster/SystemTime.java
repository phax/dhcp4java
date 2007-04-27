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
package org.dhcpcluster;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public final class SystemTime {
	
	private static boolean	forcedMode = false;
	private static long		forcedTime;
	
	public static final long currentTimeMillis() {
		return forcedMode ? forcedTime : System.currentTimeMillis();
	}

	/**
	 * @return Returns the forcedMode.
	 */
	public static boolean isForcedMode() {
		return forcedMode;
	}

	/**
	 * @param forcedMode The forcedMode to set.
	 */
	public static void setForcedMode(boolean forcedMode) {
		SystemTime.forcedMode = forcedMode;
	}

	/**
	 * @return Returns the forcedTime.
	 */
	public static long getForcedTime() {
		return forcedTime;
	}

	/**
	 * @param forcedTime The forcedTime to set.
	 */
	public static void setForcedTime(long forcedTime) {
		SystemTime.forcedTime = forcedTime;
	}
	

}
