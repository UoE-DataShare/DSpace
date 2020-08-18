/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle.factory;

import org.dspace.handle.service.ShareGeoHandleService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Abstract factory to get services for the handle package, use
 * ShareGeoHandleServiceFactory.getInstance() to retrieve an implementation
 *
 */
public abstract class ShareGeoHandleServiceFactory {

	public abstract ShareGeoHandleService getShareGeoHandleService();

	public static ShareGeoHandleServiceFactory getInstance() {
		return DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName("shareGeoHandleServiceFactory",
				ShareGeoHandleServiceFactory.class);
	}
}
