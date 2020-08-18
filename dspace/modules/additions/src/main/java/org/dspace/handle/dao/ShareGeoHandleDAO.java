/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle.dao;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.content.dao.impl.BatchImportDAOImpl;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;
import org.dspace.handle.ShareGeoHandle;

/**
 * Database Access Object interface class for the ShareGeoHandle object. The
 * implementation of this class is responsible for all database calls for the
 * ShareGeoHandle object and is autowired by spring This class should only be
 * accessed from a single service and should never be exposed outside of the API
 *
 */
public interface ShareGeoHandleDAO extends GenericDAO<ShareGeoHandle> {

	public ShareGeoHandle findByHandle(Context context, String handle) throws SQLException;

}
