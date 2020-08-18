package org.dspace.content.service;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.SwordKey;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public interface SwordKeyService extends DSpaceObjectService<SwordKey> {

	/**
	 * Insert new SWORD API key.
	 * 
	 * @param context DSpace context.
	 * @param eperson DSpace user.
	 * @throws AuthorizeException 
	 * @throws SQLException 
	 */
	public void insertSwordKey(Context context, EPerson eperson) throws SQLException, AuthorizeException;
	
	/**
	 * @param context DSpace context.
	 * @return SWORD API key.
	 * @throws SQLException 
	 */
	public String fetchSwordKey(Context context) throws SQLException;
	
	/**
	 * @param context DSpace context.
	 * @param eperson DSpace user.
	 * @return SWORD API key.
	 * @throws SQLException 
	 */
	public String fetchSwordKey(Context context, EPerson eperson) throws SQLException;


}
