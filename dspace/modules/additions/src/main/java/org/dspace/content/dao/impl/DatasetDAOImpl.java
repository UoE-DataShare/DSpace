package org.dspace.content.dao.impl;


import org.apache.log4j.Logger;
import org.dspace.content.Dataset;
import org.dspace.content.dao.DatasetDAO;
import org.dspace.core.AbstractHibernateDSODAO;

public class DatasetDAOImpl extends AbstractHibernateDSODAO<Dataset> implements DatasetDAO {
	private static final Logger log = Logger.getLogger(DatasetDAOImpl.class);

	protected DatasetDAOImpl()
	{
		super();
	}

}
