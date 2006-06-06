package sf.dhcp4java.test;

public class ByteArrayComparator {

	public static boolean equalsByteArray(byte[] a, byte[] b) {
		if ((a == null) || (b == null))	return false;
		if (a.length != b.length)			return false;
		for (int i=0; i<a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}
}
