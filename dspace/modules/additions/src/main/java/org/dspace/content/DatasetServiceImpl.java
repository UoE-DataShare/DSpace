package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.DatasetDAO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.DatasetService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

public class DatasetServiceImpl  extends DSpaceObjectServiceImpl<Dataset> implements DatasetService {

	/** log4j logger */
	private static Logger log = Logger.getLogger(DatasetServiceImpl.class);

	@Autowired(required = true)
	protected DatasetDAO datasetDAO;

	@Override
	public Dataset find(Context context, UUID id) throws SQLException {
		Dataset dataset = datasetDAO.findByID(context, Dataset.class, id);

		if (dataset == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_Dataset",
						"not_found, id= " + id));
			}

			return null;
		}

		// not null, return Dataset
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_Dataset",
					"id= " + id));
		}

		return dataset;
	}

	@Override
	public void updateLastModified(Context context, Dataset dataset) throws SQLException, AuthorizeException {
		 context.addEvent(new Event(Event.MODIFY, Constants.DATASET, dataset.getID(), null, new ArrayList<String>()));
	}

	@Override
	public void delete(Context context, Dataset dataset) throws SQLException, AuthorizeException, IOException {
		datasetDAO.delete(context, dataset);
	}

	@Override
	public int getSupportsTypeConstant() {
		return Constants.DATASET;
	}

	@Override
	public Dataset findByIdOrLegacyId(Context context, String id) throws SQLException {
		if(StringUtils.isNumeric(id))
        {
            return findByLegacyId(context, Integer.parseInt(id));
        }
        else
        {
            return find(context, UUID.fromString(id));
        }
	}

	@Override
	public Dataset findByLegacyId(Context context, int legacyId) throws SQLException {
		return datasetDAO.findByLegacyId(context, legacyId, Dataset.class);
	}

	@Override
	public UUID insertDataset(Context context, UUID itemUUID, String fileName, String checksum) throws SQLException, AuthorizeException {
		Dataset dataset = createDataset(context, itemUUID, fileName, checksum);
		return dataset.getID();
	}

	@Override
	public void deleteDataset(Context context, String fileName) throws SQLException {
		Dataset dataset = findDatsetByFileName(context, fileName);
		if (dataset == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_Dataset",
						"not_found, fileName= " + fileName));
			}
		}

		// not null, return Dataset
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_Dataset",
					"fileName= " + fileName));
		}
		datasetDAO.delete(context, dataset);
		
	}

	@Override
	public String fetchDatasetChecksum(Context context, Item item) throws SQLException {
		Dataset dataset = findDatsetByItem(context, item);
		if (dataset == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_Dataset",
						"not_found, item uuid= " + item.getID()));
			}
		}

		// not null, return Dataset
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_Dataset",
					"item uuid= " + item.getID()));
		}
		return dataset.getChecksum();
	}

	@Override
	public List<Dataset> fetchDatasets(Context context) throws SQLException {
		return datasetDAO.findAll(context, Dataset.class);
	}
		
	@Override
	public void insertUUNEntry(Context context, String optionValue, String optionValue2) {
		// TODO Auto-generated method stub
		
	}
	

	protected  Dataset createDataset(Context context, UUID itemUUID, String fileName, String checksum) throws SQLException, AuthorizeException {
		Dataset datasetObj = new Dataset();
		ItemService itemService = ContentServiceFactory.getInstance().getItemService();
		Item item = itemService.find(context, itemUUID);
		datasetObj.setItem(item);
		datasetObj.setFileName(fileName);
		datasetObj.setChecksum(checksum);
		Dataset dataset = datasetDAO.create(context, datasetObj);
        

        // Call update to give the item a last modified date. OK this isn't
        // amazingly efficient but creates don't happen that often.
        context.turnOffAuthorisationSystem();
        update(context, dataset);
        context.restoreAuthSystemState();

        context.addEvent(new Event(Event.CREATE, Constants.ITEM, dataset.getID(),
                null, new ArrayList<String>()));

        log.info(LogManager.getHeader(context, "create Dataset", "Dataset id= " + dataset.getID()));

        return dataset;
		
	}
	
	protected Dataset findDatsetByFileName(Context context, String fileName) {
		// When we move files we can have duplicates so we only want the latest
		String jpqlQuery = "SELECT dset FROM Dataset dset where dset.fileName = " + fileName + 
				" and dset.id = (SELECT MAX(dset.id) FROM Dataset dset where dset.fileName = '" + fileName +"')";
		log.debug("jpqlQuery: " + jpqlQuery);
		Dataset dataset;
		try {
			dataset = datasetDAO.findUnique(context, jpqlQuery);
		} catch(SQLException e) {
			log.debug("Error that we may ignore: " + e);
			dataset = null;
		}
		if (dataset == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_Dataset",
						"not_found, fileName = " + fileName));
			}

			return null;
		}

		// not null, return Dataset
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_Dataset",
					"fileName = " + fileName));
		}
		return dataset;
	}
	
	protected Dataset findDatsetByItem(Context context, Item item) throws SQLException {
		// When we move files we can have duplicates so we only want the latest
		String jpqlQuery = "SELECT dset FROM Dataset dset where dset.item.id = '" + item.getID() + "'"
				 + " and dset.legacyId = (SELECT MAX(dset.legacyId) FROM Dataset dset where dset.item.id = '" + item.getID() + "')";
		log.debug("jpqlQuery: " +  jpqlQuery);
				 Dataset dataset = datasetDAO.findUnique(context, jpqlQuery);
		if (dataset == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_Dataset",
						"not_found, Item uuid = " + item.getID()));
			}

			return null;
		}

		// not null, return Dataset
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_Dataset",
					"Item uuid = " + item.getID()));
		}
		return dataset;
	}

	
}
