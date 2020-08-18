/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle.service;

import java.sql.SQLException;

import org.dspace.core.Context;
import org.dspace.handle.ShareGeoHandle;

/**
 * Interface to ShareGeo handles that are in Datashare.
 */
public interface ShareGeoHandleService {

	/*
	 * Find the database row corresponding to ShareGeo handle.
	 *
	 * @param context DSpace context
	 * 
	 * @param handle The handle to resolve
	 * 
	 * @return The database row corresponding to the ShareGeo handle
	 * 
	 * @exception SQLException If a database error occurs
	 */
	public ShareGeoHandle findByShareGeoHandle(Context context, String handle) throws SQLException;

}
