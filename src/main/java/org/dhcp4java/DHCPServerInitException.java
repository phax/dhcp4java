package org.dhcp4java;

/**
 * Thrown to indicate there was a problem starting the DHCP Server.
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPServerInitException extends Exception
{
  private static final long serialVersionUID = 1L;

  /**
   *
   *
   */
  public DHCPServerInitException ()
  {
    super ();
  }

  /**
   * @param message
   */
  public DHCPServerInitException (final String message)
  {
    super (message);
  }

  /**
   * @param cause
   */
  public DHCPServerInitException (final Throwable cause)
  {
    super (cause);
  }

  /**
   * @param message
   * @param cause
   */
  public DHCPServerInitException (final String message, final Throwable cause)
  {
    super (message, cause);
  }
}
