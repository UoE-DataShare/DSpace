package org.dspace.content.service;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.UUN2Email;
import org.dspace.core.Context;

public interface UUN2EmailService extends DSpaceObjectService<UUN2Email> {
	
	/**
	 * Insert a new UUN/Email mapping entry
	 * 
	 * @param context DSpace context
	 * @param uun     University User Name
	 * @param email   Email address
	 * @throws AuthorizeException 
	 * @throws SQLException 
	 */
	public void insertUUNEntry(Context context, String uun, String email) throws SQLException, AuthorizeException;
	
	/**
	 * Fetch the university user name from the registration data table with an email
	 * address
	 * 
	 * @param context DSpace context
	 * @param email   Email address
	 * @return The university user name
	 * @throws SQLException 
	 */
	public String fetchUUN(Context context, String email) throws SQLException;

}
