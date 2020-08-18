/**
 * 
 */
package org.dspace.content.dao.impl;

import org.apache.log4j.Logger;
import org.dspace.content.UUN2Email;
import org.dspace.content.dao.UUN2EmailDAO;
import org.dspace.core.AbstractHibernateDSODAO;

/**
 * @author jopi
 *
 */
public class UUN2EmailDAOImpl extends AbstractHibernateDSODAO<UUN2Email> implements UUN2EmailDAO {
	private static final Logger log = Logger.getLogger(UUN2EmailDAOImpl.class);

	protected UUN2EmailDAOImpl()
	{
		super();
	}
}
