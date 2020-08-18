/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle.factory;

import org.dspace.handle.service.ShareGeoHandleService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory implementation to get services for the ShareGeoHandle package, use
 * ShareGeoHandleServiceFactory.getInstance() to retrieve an implementation
 */
public class ShareGeoHandleServiceFactoryImpl extends ShareGeoHandleServiceFactory {

	@Autowired(required = true)
	private ShareGeoHandleService ShareGeoHandleService;

	@Override
	public ShareGeoHandleService getShareGeoHandleService() {
		return ShareGeoHandleService;
	}
}
