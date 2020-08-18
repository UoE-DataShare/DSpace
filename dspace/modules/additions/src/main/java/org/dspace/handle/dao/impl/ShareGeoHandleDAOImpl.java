/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle.dao.impl;

import java.sql.SQLException;

import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.handle.ShareGeoHandle;
import org.dspace.handle.dao.ShareGeoHandleDAO;
import org.hibernate.Query;

/**
 * Hibernate implementation of the Database Access Object interface class for
 * the ShareGeoHandle object. This class is responsible for all database calls
 * for the ShareGeoHandle object and is autowired by spring This class should
 * never be accessed directly.
 *
 */
public class ShareGeoHandleDAOImpl extends AbstractHibernateDAO<ShareGeoHandle> implements ShareGeoHandleDAO {

	protected ShareGeoHandleDAOImpl() {
		super();
	}

	@Override
	public ShareGeoHandle findByHandle(Context context, String handle) throws SQLException {
		Query query = createQuery(context, "SELECT h " + "FROM sharegeo_handle sgh " + "WHERE h.handle_id = :handle");

		query.setParameter("handle", handle);

		query.setCacheable(true);
		return uniqueResult(query);
	}

}
