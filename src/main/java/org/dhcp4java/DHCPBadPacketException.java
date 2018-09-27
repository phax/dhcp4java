package org.dhcp4java;

/**
 * Thrown to indicate that a DHCP datagram is malformed.
 * <p>
 * The DHCP datagram may be too big, too small, or contain garbage data that
 * makes it impossible to parse correctly.
 * <p>
 * It inherits from <tt>IllegalArgumentException</tt> and
 * <tt>RuntimeException</tt> so it doesn't need to be explicitly caught.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPBadPacketException extends IllegalArgumentException
{
  /**
   * Constructs an <tt>DHCPBadPacketException</tt> with no detail message.
   */
  public DHCPBadPacketException ()
  {}

  /**
   * Constructs an <tt>DHCPBadPacketException</tt> with the specified detail
   * message.
   *
   * @param message
   *        the detail message.
   */
  public DHCPBadPacketException (final String message)
  {
    super (message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   * <p>
   * Note that the detail message associated with <tt>cause</tt> is <i>not</i>
   * automatically incorporated in this exception's detail message.
   *
   * @param message
   *        the detail message (which is saved for later retrieval by the
   *        <tt>Throwable.getMessage()</tt> method).
   * @param cause
   *        the cause (which is saved for later retrieval by the
   *        <tt>Throwable.getCause()</tt> method). (A <tt>null</tt> value is
   *        permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public DHCPBadPacketException (final String message, final Throwable cause)
  {
    super (message, cause);
  }

  /**
   * Constructs a new exception with the specified cause and a detail message of
   * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains
   * the class and detail message of cause).
   *
   * @param cause
   *        the cause (which is saved for later retrieval by the
   *        <tt>Throwable.getCause()</tt> method). (A <tt>null</tt> value is
   *        permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public DHCPBadPacketException (final Throwable cause)
  {
    super (cause);
  }
}
