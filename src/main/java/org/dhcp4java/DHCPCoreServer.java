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

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple generic DHCP Server. The DHCP Server provided is based on a
 * multi-thread model. The main thread listens at the socket, then dispatches
 * work to a pool of threads running the servlet.
 * <p>
 * Configuration: the Server reads the following properties in
 * "/DHCPd.properties" at the root of the class path. You can however provide a
 * properties set when contructing the server. Default values are: <blockquote>
 * <code>serverAddress=127.0.0.1:67</code> <i>[address:port]</i> <br>
 * <code>serverThreads=2</code> <i>[number of concurrent threads for
 * servlets]</i> </blockquote>
 * <p>
 * Note: this class implements <code>Runnable</code> allowing it to be run in a
 * dedicated thread.
 * <p>
 * Example:
 *
 * <pre>
 * public static void main (String [] args)
 * {
 *   try
 *   {
 *     DHCPCoreServer server = DHCPCoreServer.initServer (new DHCPStaticServlet (), null);
 *     new Thread (server).start ();
 *   }
 *   catch (DHCPServerInitException e)
 *   {
 *     // die gracefully
 *   }
 * }
 * </pre>
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPCoreServer implements Runnable
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPCoreServer.class);
  private static final int BOUNDED_QUEUE_SIZE = 20;

  /** default MTU for ethernet */
  protected static final int PACKET_SIZE = 1500;

  /** the servlet it must run */
  protected DHCPServlet m_aServlet;
  /** working threads pool. */
  protected ThreadPoolExecutor m_aThreadPool;
  /** Consolidated parameters of the server. */
  protected Properties m_aProperties;
  /** Reference of user-provided parameters */
  protected Properties m_aUserProps;
  /** IP address and port for the server */
  private InetSocketAddress m_aSockAddress;
  /** The socket for receiving and sending. */
  private DatagramSocket m_aServerSocket;
  /** do we need to stop the server? */
  private boolean m_bStopped = false;

  /**
   * Constructor
   * <p>
   * Constructor shall not be called directly. New servers are created through
   * <code>initServer()</code> factory.
   */
  private DHCPCoreServer (final DHCPServlet servlet, final Properties userProps)
  {
    this.m_aServlet = servlet;
    this.m_aUserProps = userProps;
  }

  /**
   * Creates and initializes a new DHCP Server.
   * <p>
   * It instanciates the object, then calls <code>init()</code> method.
   *
   * @param servlet
   *        the <code>DHCPServlet</code> instance processing incoming requests,
   *        must not be <code>null</code>.
   * @param userProps
   *        specific properties, overriding file and default properties, may be
   *        <code>null</code>.
   * @return the new <code>DHCPCoreServer</code> instance (never null).
   * @throws DHCPServerInitException
   *         unable to start the server.
   */
  public static DHCPCoreServer initServer (final DHCPServlet servlet,
                                           final Properties userProps) throws DHCPServerInitException
  {
    if (servlet == null)
      throw new IllegalArgumentException ("servlet must not be null");
    final DHCPCoreServer server = new DHCPCoreServer (servlet, userProps);
    server.init ();
    return server;
  }

  /**
   * Initialize the server context from the Properties, and open socket.
   *
   * @throws DHCPServerInitException
   *         on error
   */
  protected void init () throws DHCPServerInitException
  {
    if (this.m_aServerSocket != null)
      throw new IllegalStateException ("Server already initialized");

    try
    {
      // default built-in minimal properties
      this.m_aProperties = new Properties (DEF_PROPS);

      // try to load default configuration file
      final InputStream propFileStream = this.getClass ().getResourceAsStream ("/DHCPd.properties");
      if (propFileStream != null)
      {
        this.m_aProperties.load (propFileStream);
      }
      else
      {
        s_aLogger.error ("Could not load /DHCPd.properties");
      }

      // now integrate provided properties
      if (this.m_aUserProps != null)
      {
        this.m_aProperties.putAll (this.m_aUserProps);
      }

      // load socket address, this method may be overriden
      m_aSockAddress = this.getInetSocketAddress (this.m_aProperties);
      if (m_aSockAddress == null)
      {
        throw new DHCPServerInitException ("Cannot find which SockAddress to open");
      }

      // open socket for listening and sending
      this.m_aServerSocket = new DatagramSocket (null);
      this.m_aServerSocket.setBroadcast (true); // allow sending broadcast
      this.m_aServerSocket.bind (m_aSockAddress);

      // initialize Thread Pool
      final int numThreads = Integer.parseInt (this.m_aProperties.getProperty (SERVER_THREADS));
      final int maxThreads = Integer.parseInt (this.m_aProperties.getProperty (SERVER_THREADS_MAX));
      final int keepaliveThreads = Integer.parseInt (this.m_aProperties.getProperty (SERVER_THREADS_KEEPALIVE));
      this.m_aThreadPool = new ThreadPoolExecutor (numThreads,
                                                   maxThreads,
                                                   keepaliveThreads,
                                                   TimeUnit.MILLISECONDS,
                                                   new ArrayBlockingQueue <Runnable> (BOUNDED_QUEUE_SIZE),
                                                   new ServerThreadFactory ());
      this.m_aThreadPool.prestartAllCoreThreads ();

      // now intialize the servlet
      this.m_aServlet.setServer (this);
      this.m_aServlet.init (this.m_aProperties);
    }
    catch (final DHCPServerInitException e)
    {
      throw e; // transparently re-throw
    }
    catch (final Exception e)
    {
      this.m_aServerSocket = null;
      s_aLogger.error ("Cannot open socket", e);
      throw new DHCPServerInitException ("Unable to init server", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  protected void dispatch ()
  {
    try
    {
      final DatagramPacket requestDatagram = new DatagramPacket (new byte [PACKET_SIZE], PACKET_SIZE);
      if (s_aLogger.isDebugEnabled ())
        s_aLogger.debug ("Waiting for packet");

      // receive datagram
      this.m_aServerSocket.receive (requestDatagram);

      if (s_aLogger.isDebugEnabled ())
      {
        final StringBuilder sbuf = new StringBuilder ("Received packet from ");

        DHCPPacket.appendHostAddress (sbuf, requestDatagram.getAddress ());
        sbuf.append ('(').append (requestDatagram.getPort ()).append (')');
        s_aLogger.debug (sbuf.toString ());
      }

      // send work to thread pool
      final DHCPServletDispatcher dispatcher = new DHCPServletDispatcher (this, m_aServlet, requestDatagram);
      m_aThreadPool.execute (dispatcher);
    }
    catch (final IOException e)
    {
      s_aLogger.info ("IOException", e);
    }
  }

  /**
   * Send back response packet to client.
   * <p>
   * This is a callback method used by servlet dispatchers to send back
   * responses.
   *
   * @param responseDatagram
   *        suff to send back
   */
  protected void sendResponse (final DatagramPacket responseDatagram)
  {
    if (responseDatagram == null)
    {
      return; // skipping
    }

    try
    {
      // sending back
      this.m_aServerSocket.send (responseDatagram);
    }
    catch (final IOException e)
    {
      s_aLogger.error ("IOException", e);
    }
  }

  /**
   * Returns the <code>InetSocketAddress</code> for the server (client-side).
   *
   * <pre>
   *
   *  serverAddress (default 127.0.0.1)
   *  serverPort (default 67)
   * </pre>
   * <p>
   * This method can be overridden to specify an non default socket behaviour
   *
   * @param props
   *        Properties loaded from /DHCPd.properties
   * @return the socket address, null if there was a problem
   */
  protected InetSocketAddress getInetSocketAddress (final Properties props)
  {
    if (props == null)
    {
      throw new IllegalArgumentException ("null props not allowed");
    }
    final String serverAddress = props.getProperty (SERVER_ADDRESS);
    if (serverAddress == null)
    {
      throw new IllegalStateException ("Cannot load SERVER_ADDRESS property");
    }
    return parseSocketAddress (serverAddress);
  }

  /**
   * Parse a string of the form 'server:port' or '192.168.1.10:67'.
   *
   * @param address
   *        string to parse
   * @return InetSocketAddress newly created
   * @throws IllegalArgumentException
   *         if unable to parse string
   */
  public static InetSocketAddress parseSocketAddress (final String address)
  {
    if (address == null)
    {
      throw new IllegalArgumentException ("Null address not allowed");
    }
    final int index = address.indexOf (':');
    if (index <= 0)
    {
      throw new IllegalArgumentException ("semicolon missing for port number");
    }

    final String serverStr = address.substring (0, index);
    final String portStr = address.substring (index + 1, address.length ());
    final int port = Integer.parseInt (portStr);

    return new InetSocketAddress (serverStr, port);
  }

  /**
   * This is the main loop for accepting new request and delegating work to
   * servlets in different threads.
   */
  public void run ()
  {
    if (this.m_aServerSocket == null)
      throw new IllegalStateException ("Listening socket is not open - terminating");

    while (!this.m_bStopped)
    {
      try
      {
        this.dispatch (); // do the stuff
      }
      catch (final Exception e)
      {
        s_aLogger.warn ("Unexpected Exception", e);
      }
    }
  }

  /**
   * This method stops the server and closes the socket.
   */
  public void stopServer ()
  {
    this.m_bStopped = true;
    this.m_aServerSocket.close (); // this generates an exception when trying to
    // receive
  }

  private static final Properties DEF_PROPS = new Properties ();

  public static final String SERVER_ADDRESS = "serverAddress";
  private static final String SERVER_ADDRESS_DEFAULT = "127.0.0.1:67";
  public static final String SERVER_THREADS = "serverThreads";
  private static final String SERVER_THREADS_DEFAULT = "2";
  public static final String SERVER_THREADS_MAX = "serverThreadsMax";
  private static final String SERVER_THREADS_MAX_DEFAULT = "4";
  public static final String SERVER_THREADS_KEEPALIVE = "serverThreadsKeepalive";
  private static final String SERVER_THREADS_KEEPALIVE_DEFAULT = "10000";

  static
  {
    // initialize defProps
    DEF_PROPS.put (SERVER_ADDRESS, SERVER_ADDRESS_DEFAULT);
    DEF_PROPS.put (SERVER_THREADS, SERVER_THREADS_DEFAULT);
    DEF_PROPS.put (SERVER_THREADS_MAX, SERVER_THREADS_MAX_DEFAULT);
    DEF_PROPS.put (SERVER_THREADS_KEEPALIVE, SERVER_THREADS_KEEPALIVE_DEFAULT);
  }

  private static class ServerThreadFactory implements ThreadFactory
  {
    private static final AtomicInteger S_aPoolNumber = new AtomicInteger (1);

    private final AtomicInteger m_aThreadNumber = new AtomicInteger (1);
    private final String m_sNamePrefix;

    ServerThreadFactory ()
    {
      this.m_sNamePrefix = "DHCPCoreServer-" + S_aPoolNumber.getAndIncrement () + "-thread-";
    }

    public Thread newThread (final Runnable runnable)
    {
      return new Thread (runnable, this.m_sNamePrefix + this.m_aThreadNumber.getAndIncrement ());
    }
  }

  /**
   * @return Returns the socket address.
   */
  public InetSocketAddress getSockAddress ()
  {
    return m_aSockAddress;
  }
}

/**
 * Servlet dispatcher
 */
class DHCPServletDispatcher implements Runnable
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (DHCPServletDispatcher.class);

  private final DHCPCoreServer m_aServer;
  private final DHCPServlet m_aDispatchServlet;
  private final DatagramPacket m_aDispatchPacket;

  public DHCPServletDispatcher (final DHCPCoreServer server, final DHCPServlet servlet, final DatagramPacket req)
  {
    this.m_aServer = server;
    this.m_aDispatchServlet = servlet;
    this.m_aDispatchPacket = req;
  }

  public void run ()
  {
    try
    {
      final DatagramPacket response = this.m_aDispatchServlet.serviceDatagram (this.m_aDispatchPacket);
      this.m_aServer.sendResponse (response); // invoke callback method
    }
    catch (final Exception e)
    {
      s_aLogger.info ("Exception in dispatcher", e);
    }
  }
}
