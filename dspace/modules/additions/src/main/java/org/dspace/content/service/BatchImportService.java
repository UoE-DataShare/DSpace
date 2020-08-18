package org.dspace.content.service;

import java.sql.SQLException;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.BatchImport;
import org.dspace.core.Context;

public interface BatchImportService extends DSpaceObjectService<BatchImport>, DSpaceObjectLegacySupportService<BatchImport> {

	public UUID insertBatchImport(Context context, String mapFile) throws SQLException, AuthorizeException;
	
	/**
	 * Delete a batch import entry from database.
	 * 
	 * @param context DSpace context.
	 * @param batchId batch id.
	 * @throws SQLException 
	 */
	public void deleteBatchImport(Context context, String batchId) throws SQLException;

	/**
	 * Get the batch file for a given batch id and logged in user.
	 * 
	 * @param context DSpace context.
	 * @param id      The batch import id.
	 * @return The map file associated with a batch import.
	 * @throws SQLException 
	 */
	public String fetchBatchMapFile(Context context, String id) throws SQLException;

}
