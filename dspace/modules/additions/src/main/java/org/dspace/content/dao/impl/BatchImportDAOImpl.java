/**
 * 
 */
package org.dspace.content.dao.impl;

import org.apache.log4j.Logger;
import org.dspace.content.BatchImport;
import org.dspace.content.dao.BatchImportDAO;
import org.dspace.core.AbstractHibernateDSODAO;

/**
 * @author jopi
 *
 */
public class BatchImportDAOImpl extends AbstractHibernateDSODAO<BatchImport> implements BatchImportDAO {

	private static final Logger log = Logger.getLogger(BatchImportDAOImpl.class);

	protected BatchImportDAOImpl()
	{
		super();
	}


}
