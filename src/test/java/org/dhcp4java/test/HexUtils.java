package org.dhcp4java.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.BitSet;

/**
 * Number in hexadecimal format are used throughout Freenet.
 * <p>
 * Unless otherwise stated, the conventions follow the rules outlined in the
 * Java Language Specification.
 * </p>
 *
 * @author syoung
 */
final class HexUtils
{
  private HexUtils ()
  {}

  /**
   * Converts a byte array into a string of upper case hex chars.
   *
   * @param bs
   *        A byte array
   * @param off
   *        The index of the first byte to read
   * @param length
   *        The number of bytes to read.
   * @return the string of hex chars.
   */
  public static String bytesToHex (final byte [] bs, final int off, final int length)
  {
    final StringBuffer sb = new StringBuffer (length * 2);
    bytesToHexAppend (bs, off, length, sb);
    return sb.toString ();
  }

  public static void bytesToHexAppend (final byte [] bs, final int off, final int length, final StringBuffer sb)
  {
    sb.ensureCapacity (sb.length () + length * 2);
    for (int i = off; i < (off + length) && i < bs.length; i++)
    {
      sb.append (Character.forDigit ((bs[i] >>> 4) & 0xf, 16)).append (Character.forDigit (bs[i] & 0xf, 16));
    }
  }

  public static String bytesToHex (final byte [] bs)
  {
    return bytesToHex (bs, 0, bs.length);
  }

  public static byte [] hexToBytes (final String s)
  {
    return hexToBytes (s, 0);
  }

  public static byte [] hexToBytes (final String s, final int off)
  {
    final byte [] bs = new byte [off + (1 + s.length ()) / 2];
    hexToBytes (s, bs, off);
    return bs;
  }

  /**
   * Converts a String of hex characters into an array of bytes.
   *
   * @param s
   *        A string of hex characters (upper case or lower) of even length.
   * @param out
   *        A byte array of length at least s.length()/2 + off
   * @param off
   *        The first byte to write of the array
   * @throws NumberFormatException
   *         On error
   * @throws IndexOutOfBoundsException
   *         on error
   */
  public static void hexToBytes (final String sStr, final byte [] out, final int off)
  {
    String s = sStr;
    final int slen = s.length ();
    if ((slen % 2) != 0)
    {
      s = '0' + s;
    }

    if (out.length < off + slen / 2)
    {
      throw new IndexOutOfBoundsException ("Output buffer too small for input (" +
                                           out.length +
                                           '<' +
                                           off +
                                           slen / 2 +
                                           ')');
    }

    // Safe to assume the string is even length
    for (int i = 0; i < slen; i += 2)
    {
      final byte b1 = (byte) Character.digit (s.charAt (i), 16);
      final byte b2 = (byte) Character.digit (s.charAt (i + 1), 16);

      if (b1 < 0 || b2 < 0)
      {
        throw new NumberFormatException ();
      }
      out[off + i / 2] = (byte) (b1 << 4 | b2);
    }
  }

  /**
   * Pack the bits in ba into a byte[].
   */
  public static byte [] bitsToBytes (final BitSet ba, final int size)
  {
    final int bytesAlloc = countBytesForBits (size);
    final byte [] b = new byte [bytesAlloc];

    for (int i = 0; i < b.length; i++)
    {
      short s = 0;

      for (int j = 0; j < 8; j++)
      {
        final int idx = i * 8 + j;
        final boolean val = (idx <= size && ba.get (idx));

        s |= (val ? (1 << j) : 0);
      }

      if (s > 255)
      {
        throw new IllegalStateException ("WTF? s = " + s);
      }

      b[i] = (byte) s;
    }
    return b;
  }

  /**
   * Pack the bits in ba into a byte[] then convert that to a hex string and
   * return it.
   */
  public static String bitsToHexString (final BitSet ba, final int size)
  {
    return bytesToHex (bitsToBytes (ba, size));
  }

  /**
   * @return the number of bytes required to represent the bitset
   */
  public static int countBytesForBits (final int size)
  {
    // Brackets matter here! == takes precedence over the rest
    return (size / 8) + ((size % 8) == 0 ? 0 : 1);
  }

  /**
   * Read bits from a byte array into a bitset
   *
   * @param b
   *        the byte[] to read from
   * @param ba
   *        the bitset to write to
   */
  public static void bytesToBits (final byte [] b, final BitSet ba, final int maxSize)
  {
    int x = 0;

    for (final byte aB : b)
    {
      int j = 0;

      while (j < 8 && x <= maxSize)
      {
        final int mask = 1 << j;
        final boolean value = (mask & aB) != 0;

        ba.set (x, value);
        x++;
        j++;
      }
    }
  }

  /**
   * Read a hex string of bits and write it into a bitset
   *
   * @param s
   *        hex string of the stored bits
   * @param ba
   *        the bitset to store the bits in
   * @param length
   *        the maximum number of bits to store
   */
  public static void hexToBits (final String s, final BitSet ba, final int length)
  {
    final byte [] b = hexToBytes (s);
    bytesToBits (b, ba, length);
  }

  /**
   * Write a (reasonably short) BigInteger to a stream.
   *
   * @param integer
   *        the BigInteger to write
   * @param out
   *        the stream to write it to
   * @throws IOException
   *         On IO error
   */
  public static void writeBigInteger (final BigInteger integer, final DataOutputStream out) throws IOException
  {
    if (integer.signum () == -1)
    {
      // dump("Negative BigInteger", Logger.ERROR, true);
      throw new IllegalStateException ("Negative BigInteger!");
    }
    final byte [] buf = integer.toByteArray ();
    if (buf.length > Short.MAX_VALUE)
    {
      throw new IllegalStateException ("Too long: " + buf.length);
    }
    out.writeShort ((short) buf.length);
    out.write (buf);
  }

  /**
   * Read a (reasonably short) BigInteger from a DataInputStream
   *
   * @param dis
   *        the stream to read from
   * @return a BigInteger
   * @throws IOException
   *         On IO error
   */
  public static BigInteger readBigInteger (final DataInputStream dis) throws IOException
  {
    final short i = dis.readShort ();
    if (i < 0)
    {
      throw new IOException ("Invalid BigInteger length: " + i);
    }
    final byte [] buf = new byte [i];
    dis.readFully (buf);
    return new BigInteger (1, buf);
  }

  /**
   * Turn a BigInteger into a hex string. BigInteger.toString(16) NPEs on Sun
   * JDK 1.4.2_05. :< The bugs in their Big* are getting seriously irritating...
   */
  public static String biToHex (final BigInteger bi)
  {
    if (false)
      return bytesToHex (bi.toByteArray ());
    return bi.toString (16);
  }
}
