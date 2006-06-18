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

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A simple generic DHCP Server.
 * 
 * The DHCP Server provided is based on a multi-thread model. The main thread listens
 * at the socket, then dispatches work to a pool of threads running the servlet.
 * 
 * <p>Configuration: the Server reads the following properties in "/DHCPd.properties"
 * at the root of the class path. You can however provide a properties set when
 * contructing the server. Default values are:
 * 
 * <blockquote>
 * <tt>serverAddress=127.0.0.1:67</tt> <i>[address:port]</i>
 * <br>
 * <tt>serverThreads=2</tt> <i>[number of concurrent threads for servlets]</i>
 * </blockquote>
 * 
 * <p>Note: this class implements <tt>Runnable</tt> allowing it to be run
 * in a dedicated thread.
 * 
 * <p>Example:
 * 
 * <pre>
 *     public static void main(String[] args) {
 *         try {
 *             DHCPServer server = DHCPServer.initServer(new TrivialDHCPServlet(), null);
 *             new Thread(server).start();
 *         } catch (DHCPServerInitException e) {
 *             // die gracefully
 *         }
 *     }
 * </pre>
 * 
 * @author Stephan Hadinger
 * @version 0.51
 */
public class DHCPServer implements Runnable {
    private static final Logger logger = Logger.getLogger("sf.dhcp4java.dhcpserver");
    
    protected DHCPServlet servlet = null; // the servlet it must run
    
    protected WorkQueue workQueue = null; // working threads pool

    protected Properties props = null; // consolidated parameters of the server

    protected Properties userProps = null; // reference of user-provided parameters

    private DatagramSocket serverSocket = null; // the socket for receiving and sending

    static protected final int PACKET_SIZE = 1500; // default MTU for ethernet

    /**
     * Constructor
     * 
     * <p>Constructor shall not be called directly. New servers are created through
     * <tt>initServer()</tt> factory.
     */
    private DHCPServer(DHCPServlet servlet, Properties userProps) throws DHCPServerInitException {
        this.servlet = servlet;
        this.userProps = userProps;
    }
    /**
     * Creates and initializes a new DHCP Server.
     * 
     * <p>It instanciates the object, then calls <tt>init()</tt> method.
     * 
     * @param servlet the <tt>DHCPServlet</tt> instance processing incoming requests,
     * 			must not be <tt>null</tt>.
     * @param userProps specific properties, overriding file and default properties,
     * 			may be <tt>null</tt>.
     * @return the new <tt>DHCPServer</tt> instance (never null).
     * @throws DHCPServerInitException unable to start the server.
     */
    public static DHCPServer initServer(DHCPServlet servlet, Properties userProps) throws DHCPServerInitException {
    	if (servlet == null)
    		throw new IllegalArgumentException("servlet must not be null");
    	DHCPServer server = new DHCPServer(servlet, userProps);
    	server.init();
    	return server;
    }
    /**
     * Initialize the server context from the Properties, and open socket.
     *  
     */
    protected void init() throws DHCPServerInitException {
        if (serverSocket != null)
            throw new IllegalStateException("Server already initialized");

        try {
            // default built-in minimal properties
            Properties defProps = new Properties(DEF_PROPS);

            // try to load default configuration file
            InputStream propFileStream = this.getClass().getResourceAsStream(
                    "/DHCPd.properties");
            if (propFileStream != null) {
                defProps.load(propFileStream);
            } else {
                logger.severe("Could not load /DHCPd.properties");
            }

            // now integrate provided properties
            if (userProps != null) {
                props = new Properties();
                props.putAll(defProps);
                props.putAll(userProps);
            } else {
                props = defProps;
            }

            // load socket address, this method may be overriden
            InetSocketAddress sockAddress = getInetSocketAddress(props);
            if (sockAddress == null)
            	throw new DHCPServerInitException("Cannot find which SockAddress to open");

            // open socket for listening and sending
            serverSocket = new DatagramSocket(sockAddress);
            if (serverSocket == null)
            	throw new DHCPServerInitException("Cannot open client-side socket");

            // initialize work queue
            int nbTthreads = Integer.valueOf(props.getProperty(SERVER_THREADS)).intValue();
            workQueue = new WorkQueue(nbTthreads);
            
            // now intialize the servlet
            servlet.init(props);
        } catch (DHCPServerInitException e) {
        	throw e;		// transparently re-throw
        } catch (Exception e) {
            serverSocket = null;
            logger.log(Level.SEVERE, "Cannot open socket", e);
            throw new DHCPServerInitException("Unable to init server", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    protected void dispatch() {
        try {
            DatagramPacket requestDatagram = new DatagramPacket(
                    new byte[PACKET_SIZE], PACKET_SIZE);
            logger.finer("Waiting for packet");

            // receive datagram
            serverSocket.receive(requestDatagram);

            if (logger.isLoggable(Level.FINER))
                logger.finer("Received packet from "
                        + DHCPPacket.getHostAddress(requestDatagram.getAddress()) + "("
                        + requestDatagram.getPort() + ")");

            // send work to thread pool
            ServletDispatcher dispatcher = new ServletDispatcher(this, servlet, requestDatagram);
            workQueue.execute(dispatcher);
        } catch (IOException e) {
	        logger.log(Level.SEVERE, "IOException", e);
        }
    }
    /**
     * Send back response packet to client.
     * 
     * <p>This is a callback method used by servlet dispatchers to send back responses.
     */
    protected void sendResponse(DatagramPacket responseDatagram) {
        if (responseDatagram == null)
            return; // skipping

        try {
	        // sending back
	        serverSocket.send(responseDatagram);
	    } catch (IOException e) {
	        logger.log(Level.SEVERE, "IOException", e);
	    }
    }
    /**
     * Returns the <tt>InetSocketAddress</tt> for the server (client-side).
     * 
     * <pre>
     * 
     *  serverAddress (default 127.0.0.1)
     *  serverPort (default 67)
     *  
     * </pre>
     * 
     * <p>
     * This method can be overriden to specify an non default socket behaviour
     * 
     * @param props Properties loaded from /DHCPd.properties
     * @return the socket address, null if there was a problem
     */
    protected InetSocketAddress getInetSocketAddress(Properties props) {
        if (props == null)
            throw new IllegalArgumentException("null props not allowed");
        String serverAddress = props.getProperty(SERVER_ADDRESS);
        if (serverAddress == null)
        	throw new IllegalStateException("Cannot load SERVER_ADDRESS property");
        return parseSocketAddress(serverAddress);
    }

    /**
     * Parse a string of the form 'server:port' or '192.168.1.10:67'.
     * 
     * @param s string to parse
     * @return InetSocketAddress newly created
     * @throws IllegalArgumentException if unable to parse string
     */
    public static final InetSocketAddress parseSocketAddress(String s) {
        if (s == null)
            throw new IllegalArgumentException("Null address not allowed");
        int i = s.indexOf(':');
        if (i <= 0)
            throw new IllegalArgumentException(
                    "semicolon missing for port number");

        String serverStr = s.substring(0, i);
        String portStr = s.substring(i + 1, s.length());
        int port = Integer.parseInt(portStr);
        return new InetSocketAddress(serverStr, port);
    }

    /**
     * This is the main loop for accepting new request and delegating work to
     * servlets in different threads.
     * 
     */
    public void run() {
        if (serverSocket == null)
            throw new IllegalStateException("Listening socket is not open - terminating");
        while (true) {
            try {
                dispatch();		// do the stuff
            } catch (Exception e) {
                logger.log(Level.WARNING, "Unexpected Exception", e);
            }
        }
    }

    private static final Properties DEF_PROPS = new Properties();

    public static final String SERVER_ADDRESS = "serverAddress";

    private static final String SERVER_ADDRESS_DEFAULT = "127.0.0.1:67";

    public static final String SERVER_THREADS = "serverThreads";

    private static final String SERVER_THREADS_DEFAULT = "2";

    static {
        // initialize defProps
        DEF_PROPS.put(SERVER_ADDRESS, SERVER_ADDRESS_DEFAULT);
        DEF_PROPS.put(SERVER_THREADS, SERVER_THREADS_DEFAULT);
    }

}

// Threads pool working queue
// http://www-128.ibm.com/developerworks/library/j-jtp0730.html
//

class ServletDispatcher implements Runnable {
    private static final Logger logger = Logger.getLogger("sf.dhcp4java.dhcpserver.servletdispatcher");
    
	private final DHCPServer server;
    private final DHCPServlet dispatchServlet;
    private final DatagramPacket dispatchPacket;
    
    public ServletDispatcher(DHCPServer server, DHCPServlet servlet, DatagramPacket req) {
    	this.server = server;
        this.dispatchServlet = servlet;
        this.dispatchPacket = req;
    }
    
    public void run() {
        try {
            DatagramPacket response = dispatchServlet.serviceDatagram(dispatchPacket);
            server.sendResponse(response);		// invoke callback method
        } catch (Exception e) {
            logger.log(Level.FINE, "Exception in dispatcher", e);
        }
    }
}

class WorkQueue {
    private final PoolWorker[] threads;

    private final LinkedList<Runnable> queue;

    public WorkQueue(int nThreads) {
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].setName("DHCPServlet-"+i);
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    r = (Runnable) queue.removeFirst();
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    r.run();
                } catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
        }
    }
}
