/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.handle.dao.ShareGeoHandleDAO;
import org.dspace.handle.service.ShareGeoHandleService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Interface to the <a href="http://www.handle.net" target=_new>CNRI Handle
 * System </a>.
 *
 * <p>
 * Currently, this class simply maps handles to local facilities; handles which
 * are owned by other sites (including other DSpaces) are treated as
 * non-existent.
 * </p>
 *
 * @author Peter Breton
 * @version $Revision$
 */
public class ShareGeoHandleServiceImpl implements ShareGeoHandleService {
	/** log4j category */
	private static Logger log = Logger.getLogger(ShareGeoHandleServiceImpl.class);

	@Autowired(required = true)
	protected ShareGeoHandleDAO shareGeoHandleDAO;

	@Autowired(required = true)
	protected ConfigurationService configurationService;

	/** Public Constructor */
	public ShareGeoHandleServiceImpl() {
		super();
	}

	/**
	 * Find the database row corresponding to ShareGeo handle.
	 *
	 * @param context DSpace context
	 * @param handle  The ShareGeo handle to resolve
	 * @return The database row corresponding to the ShareGeo handle
	 * @exception SQLException If a database error occurs
	 */
	public ShareGeoHandle findByShareGeoHandle(Context context, String handle) throws SQLException {
		if (handle == null) {
			throw new IllegalArgumentException("ShareGeoHandle is null");
		}

		return shareGeoHandleDAO.findByHandle(context, handle);
	}

}
