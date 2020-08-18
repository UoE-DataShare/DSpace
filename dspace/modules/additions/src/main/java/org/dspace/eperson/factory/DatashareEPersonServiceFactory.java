package org.dspace.eperson.factory;

import org.dspace.eperson.service.DatashareAccountService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Abstract factory to get services for the content package, use DatashareContentServiceFactory.getInstance() to retrieve an implementation
 *
 */
public abstract class DatashareEPersonServiceFactory {
	
	public abstract DatashareAccountService getDatashareAccountService();
	
	 public static DatashareEPersonServiceFactory getInstance(){
	        return DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName("datashareEpersonServiceFactory", DatashareEPersonServiceFactory.class);
	    }

}
