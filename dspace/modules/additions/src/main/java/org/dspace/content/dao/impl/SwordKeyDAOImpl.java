/**
 * 
 */
package org.dspace.content.dao.impl;

import org.apache.log4j.Logger;
import org.dspace.content.SwordKey;
import org.dspace.content.dao.SwordKeyDAO;
import org.dspace.core.AbstractHibernateDSODAO;

/**
 * @author jopi
 *
 */
public class SwordKeyDAOImpl extends AbstractHibernateDSODAO<SwordKey> implements SwordKeyDAO {
	private static final Logger log = Logger.getLogger(SwordKeyDAOImpl.class);

	protected SwordKeyDAOImpl()
	{
		super();
	}

}
