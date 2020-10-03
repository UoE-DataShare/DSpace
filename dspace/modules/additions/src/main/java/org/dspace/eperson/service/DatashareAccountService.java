package org.dspace.eperson.service;

import java.io.IOException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.RegistrationData;

public interface DatashareAccountService extends AccountService{
	
	/**
	 * Get class logger.
	 * 
	 * @return Logger
	 */
	public Logger getLogger();
	
	/**
	 * Log info message.
	 * 
	 */
	public void logInfoMessage(Object message);
	
	/**
	 * Log debug message.
	 * 
	 */
	public void logDebugMessage(Object message);
	
	/**
	 * Log error message.
	 * 
	 */
	public void logErrorMessage(Object message);
	
	
	
	/**
	 * 
	 * @param context DSpace context
	 * @param email   Email Address
	 * @param uun     University user name
	 * @throws SQLException
	 * @throws IOException
	 * @throws MessagingException
	 * @throws AuthorizeException
	 */
	public void sendInfo(Context context, String email, String uun)
			throws SQLException, IOException, MessagingException, AuthorizeException;
	
	/**
	 * Fetch the registration datya for a given token
	 * 
	 * @param context The DSpace context
	 * @param token   Registration token
	 * @return The registration data
	 * @throws SQLException 
	 */
	public RegistrationData getRegistrationData(Context context, String token) throws SQLException;
}
