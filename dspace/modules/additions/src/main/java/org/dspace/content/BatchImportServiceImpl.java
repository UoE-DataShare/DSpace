package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.BatchImportDAO;
import org.dspace.content.service.BatchImportService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

public class BatchImportServiceImpl extends DSpaceObjectServiceImpl<BatchImport> implements BatchImportService {

	/** log4j logger */
	private static Logger log = Logger.getLogger(BatchImportServiceImpl.class);

	@Autowired(required = true)
	protected BatchImportDAO batchImportDAO;

	@Override
	public BatchImport findByLegacyId(Context context, int id) throws SQLException {
		return batchImportDAO.findByLegacyId(context, id, BatchImport.class);
	}


	@Override
	public UUID insertBatchImport(Context context, String mapFile) throws SQLException, AuthorizeException {
		BatchImport batchImport = createBatchImport(context, mapFile);
		return batchImport.getID();
	}

	@Override
	public void deleteBatchImport(Context context, String batchId) throws SQLException {
		BatchImport batchImport = findByIdOrLegacyId(context, batchId);
		if (batchImport == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_batchImport",
						"not_found, id= " + batchId));
			}
		}

		// not null, return BatchImport
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_batchImport",
					"id= " + batchId));
		}
		batchImportDAO.delete(context, batchImport);

	}

	@Override
	public String fetchBatchMapFile(Context context, String batchId) throws SQLException {
		BatchImport batchImport = findByIdOrLegacyId(context, batchId);
		if (batchImport == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_batchImport",
						"not_found, id= " + batchId));
			}

			return null;
		}

		// not null, return BatchImport
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_batchImport",
					"id= " + batchId));
		}
		return batchImport.getMapFile();
	}

	@Override
	public BatchImport find(Context context, UUID id) throws SQLException {
		BatchImport batchImport = batchImportDAO.findByID(context, BatchImport.class, id);

		if (batchImport == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_batchImport",
						"not_found, id= " + id));
			}

			return null;
		}

		// not null, return BatchImport
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_batchImport",
					"id= " + id));
		}

		return batchImport;
	}

	@Override
	public void updateLastModified(Context context, BatchImport batchImport) throws SQLException, AuthorizeException {
        context.addEvent(new Event(Event.MODIFY, Constants.BATCH_IMPORT, batchImport.getID(), null, new ArrayList<String>()));
	}

	@Override
	public void delete(Context context, BatchImport batchImport) throws SQLException, AuthorizeException, IOException {
		batchImportDAO.delete(context, batchImport);
	}

	@Override
	public int getSupportsTypeConstant() {
		return Constants.BATCH_IMPORT;
	}

	@Override
	public BatchImport findByIdOrLegacyId(Context context, String id) throws SQLException {
		if(StringUtils.isNumeric(id))
        {
            return findByLegacyId(context, Integer.parseInt(id));
        }
        else
        {
            return find(context, UUID.fromString(id));
        }
	}
	
	
	protected  BatchImport createBatchImport(Context context, String mapFile) throws SQLException, AuthorizeException {
		BatchImport batchImportObj = new BatchImport();
		batchImportObj.setMapFile(mapFile);
		batchImportObj.setEPerson(context.getCurrentUser());
		BatchImport batchImport = batchImportDAO.create(context, batchImportObj);
        
		
        // Call update to give the item a last modified date. OK this isn't
        // amazingly efficient but creates don't happen that often.
        context.turnOffAuthorisationSystem();
        update(context, batchImport);
        context.restoreAuthSystemState();

        // Erroring 
        context.addEvent(new Event(Event.CREATE, Constants.ITEM, batchImport.getID(),
                   null, new ArrayList<String>()));

        log.info(LogManager.getHeader(context, "create_batchImport", "batchImport id= " + batchImport.getID()));

        return batchImport;
		
	}

}
