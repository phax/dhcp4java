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
package org.dhcp4java.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.dhcp4java.DHCPCoreServer;
import org.dhcp4java.server.config.GenericConfigReader;
import org.dhcp4java.server.config.FrontendConfig;
import org.dhcp4java.server.config.GlobalConfig;
import org.dhcp4java.server.config.TopologyConfig;
import org.dhcp4java.server.config.xml.XmlConfigReader;

/**
 * This is the main class for DHCP Cluster Server (FrontEnd).
 * 
 * <p>This class is responsible for loading all configuration parameters,
 * and launching the server engine.
 * 
 * <p>Bootstrap is done through a properties file.
 * 
 * @author Stephan Hadinger
 * @version 0.70
 */
public class DHCPClusterNode implements Serializable, Runnable {

    private static final long serialVersionUID = 1L;
    
	private static final Logger logger = Logger.getLogger(DHCPClusterNode.class.getName().toLowerCase());

	private final Properties					bootstrapProps;
	
	private GenericConfigReader				configReader = null;
	
	/* 3 Levels of configuration */
	private AtomicReference<FrontendConfig>	frontendConfig = new AtomicReference<FrontendConfig>();
	private AtomicReference<GlobalConfig>		globalConfig = new AtomicReference<GlobalConfig>();
	private AtomicReference<TopologyConfig>	topologyConfig = new AtomicReference<TopologyConfig>();
	
	/* The DHCP Server Core used */
	private DHCPCoreServer internalServer;
	
	/* Thread called when Finalizing */
	private Thread					finalizerThread;
	
	/* Batch update of leases */
	private Executor				leaseBgExecutor;
	
	/* Back-End */
	// TODO
	
	public DHCPClusterNode(Properties bootstrapProps) {
		this.bootstrapProps = bootstrapProps;
		
		// TODO specialization
		configReader = new XmlConfigReader();
		configReader.init(this, bootstrapProps);
		frontendConfig.set(configReader.getFrontEndConfig());
		globalConfig.set(configReader.getGlobalConfig());
		topologyConfig.set(configReader.getTopologyConfig());
		// ready to run
	}
	

	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// TODO ready to run
	}



	/**
	 * Causes a topology reload.
	 * 
	 * <p>This method enforces no reentratn reloads
	 */
	public void triggerReloadTopologyConfig() {
		// TODO
		TopologyConfig newTopologyConfig = configReader.reloadTopologyConfig();
		if (newTopologyConfig != null) {
			topologyConfig.set(newTopologyConfig);
		}
	}
	
	
	/**
	 * @return Returns the frontendConfig.
	 */
	public FrontendConfig getFrontendConfig() {
		return frontendConfig.get();
	}
	/**
	 * @param frontendConfig The frontendConfig to set.
	 */
	protected void setFrontendConfig(FrontendConfig frontendConfig) {
		this.frontendConfig.set(frontendConfig);
	}
	/**
	 * @return Returns the globalConfig.
	 */
	public GlobalConfig getGlobalConfig() {
		return globalConfig.get();
	}
	/**
	 * @param globalConfig The globalConfig to set.
	 */
	protected void setGlobalConfig(GlobalConfig globalConfig) {
		this.globalConfig.set(globalConfig);
	}
	/**
	 * @return Returns the topologyConfig.
	 */
	public TopologyConfig getTopologyConfig() {
		return topologyConfig.get();
	}
	/**
	 * @param topologyConfig The topologyConfig to set.
	 */
	protected void setTopologyConfig(TopologyConfig topologyConfig) {
		this.topologyConfig.set(topologyConfig);
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		// set all logging levels
    	LogManager.getLogManager().readConfiguration(ClassLoader.getSystemResourceAsStream("logging.properties"));
    	// read bootstrap properties
    	FileInputStream bootstrapFile = new FileInputStream("./config/DHCPd.properties");
    	
    	Properties props = new Properties();
    	props.load(bootstrapFile);
    	//props.put("config.xml.resourcepath", "org/dhcp4java/server/config/xml/configtest.xml");

    	DHCPClusterNode cluster = new DHCPClusterNode(props);
    	cluster.run();
//    	try {
//    		//ClusterMainConfigReader.parseXmlFile(xml, cluster);
//    	} catch (ConfigException e) {
//    		logger.log(Level.SEVERE, "config exception", e);
//    	}
    }

}
