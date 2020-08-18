package org.dspace.content.service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Dataset;
import org.dspace.content.Item;
import org.dspace.core.Context;

public interface DatasetService
		extends DSpaceObjectService<Dataset>, DSpaceObjectLegacySupportService<Dataset> {
	
	/**
	 * Insert dataset metadata.
	 * 
	 * @param context  DSpace context.
	 * @param itemId   Dspace id.
	 * @param fileName dataset zip file name.
	 * @param checksum zipfile checksum.
	 * @return uuid
	 * @throws AuthorizeException 
	 * @throws SQLException 
	 */
	public UUID insertDataset(Context context, UUID itemUUID, String fileName, String checksum) throws SQLException, AuthorizeException;
	
	/**
	 * Delete dataset by dataset filename.
	 * 
	 * @param context
	 * @param fileName
	 * @throws SQLException 
	 */
	public void deleteDataset(Context context, String fileName) throws SQLException;
	
	
	/**
	 * Get checksum for dataset zipfile.
	 * 
	 * @param context DSpace context.
	 * @param item DSpace item.
	 * @return zipfile checksum
	 * @throws SQLException 
	 */
	public String fetchDatasetChecksum(Context context, Item item) throws SQLException;
	
    /**
     * Fetch the dataset id.
     * 
     * @param context DSpace context.
     * @return single id in a List.
     * @throws SQLException 
     */
	// public List<Integer> fetchDatasetIds(Context context);

	public List<Dataset> fetchDatasets(Context context) throws SQLException;

	public void insertUUNEntry(Context context, String optionValue, String optionValue2);
	

}
