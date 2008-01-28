package edu.northwestern.bioinformatics.studycalendar.grid.service;

import org.apache.axis.MessageContext;
import org.globus.wsrf.Constants;

import javax.naming.InitialContext;


/** 
 * DO NOT EDIT:  This class is autogenerated!
 * 
 * This class holds all service properties which were defined for the service to have
 * access to.
 * 
 * @created by Introduce Toolkit version 1.0
 * 
 */
public class ServiceConfiguration {

	public static ServiceConfiguration  configuration = null;

	public static ServiceConfiguration getConfiguration() throws Exception {
		if (ServiceConfiguration.configuration != null) {
			return ServiceConfiguration.configuration;
		}
		MessageContext ctx = MessageContext.getCurrentContext();

		String servicePath = ctx.getTargetService();

		String jndiName = Constants.JNDI_SERVICES_BASE_NAME + servicePath + "/serviceconfiguration";
		try {
			javax.naming.Context initialContext = new InitialContext();
			ServiceConfiguration.configuration = (ServiceConfiguration) initialContext.lookup(jndiName);
		} catch (Exception e) {
			throw new Exception("Unable to instantiate service configuration.", e);
		}

		return ServiceConfiguration.configuration;
	}
	
	

	
}
