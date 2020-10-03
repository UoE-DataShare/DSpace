/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.eperson;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.*;
import org.dspace.eperson.service.AccountService;
import org.dspace.eperson.service.DatashareAccountService;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.RegistrationDataService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/**
 *
 * Methods for handling registration by email and forgotten passwords. When
 * someone registers as a user, or forgets their password, the
 * sendRegistrationInfo or sendForgotPasswordInfo methods can be used to send an
 * email to the user. The email contains a special token, a long string which is
 * randomly generated and thus hard to guess. When the user presents the token
 * back to the system, the AccountManager can use the token to determine the
 * identity of the eperson.
 *
 * *NEW* now ignores expiration dates so that tokens never expire
 *
 * @author Peter Breton, John Pinto
 * @version $Revision$
 */
public class DatashareAccountServiceImpl implements DatashareAccountService {

	/** log4j log */
	public static Logger log = Logger.getLogger(DatashareAccountServiceImpl.class);
 
	@Autowired(required = true)
	private AccountService accountService;
	
	@Autowired(required = true)
	private RegistrationDataService registrationDataService;
	
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
	@Override
	public void sendInfo(Context context, String email, String uun)
			throws SQLException, IOException, MessagingException, AuthorizeException {
		try {
		log.info("email: " + email);
		log.info("uun: " + uun);
		// See if a registration token already exists for this user
		RegistrationData rd = registrationDataService.findByEmail(context, email);
		log.info("Registration token already exists for this user: " + (rd != null));


		// If it already exists, just re-issue it
		if (rd == null) {
			rd = registrationDataService.create(context);
			log.info("Create new RegistrationData for new user: " + (rd != null));
			rd.setToken(Utils.generateHexKey());
			rd.setEmail(email);

			rd.setUun(uun);

			registrationDataService.update(context, rd);
			log.info("Update new RegistrationData for new user");

			// This is a potential problem -- if we create the callback
			// and then crash, registration will get SNAFU-ed.
			// So FIRST leave some breadcrumbs
			if (log.isDebugEnabled()) {
				log.debug("Created callback " + rd.getID() + " with token " + rd.getToken() + " with email \"" + email
						+ "\"");
			}
		}

		log.info("Send email to user");
		sendEmail(context, email, true, rd);
		} catch(Exception e) {
			log.info(e.getMessage());
			throw e;
		}

	}

	
	/**
	 * Fetch the registration datya for a given token
	 * 
	 * @param context The DSpace context
	 * @param token   Registration token
	 * @return The registration data
	 * @throws SQLException 
	 */
	@Override
	public RegistrationData getRegistrationData(Context context, String token) throws SQLException {
		return registrationDataService.findByToken(context, token);
	}


	@Override
	public Logger getLogger() {
		return log;
		
	}
	
	/**
	 * Log info message.
	 * 
	 */
	public void logInfoMessage(Object message) {
		log.info(message);
	}
	
	/**
	 * Log debug message.
	 * 
	 */
	public void logDebugMessage(Object message) {
		log.debug(message);
	}
	/**
	 * Log error message.
	 * 
	 */
	public void logErrorMessage(Object message) {
		log.error(message);
	}
	


	@Override
	public void sendRegistrationInfo(Context context, String email)
			throws SQLException, IOException, MessagingException, AuthorizeException {
		accountService.sendRegistrationInfo(context, email);
	}


	@Override
	public void sendForgotPasswordInfo(Context context, String email)
			throws SQLException, IOException, MessagingException, AuthorizeException {
		accountService.sendForgotPasswordInfo(context, email);
	}


	@Override
	public EPerson getEPerson(Context context, String token) throws SQLException, AuthorizeException {
		return accountService.getEPerson(context, token);
	}


	@Override
	public String getEmail(Context context, String token) throws SQLException {
		return accountService.getEmail(context, token);
	}


	@Override
	public void deleteToken(Context context, String token) throws SQLException {
		accountService.deleteToken(context, token);
	}
	
	/**
     * Send a DSpace message to the given email address.
     *
     * If isRegister is <code>true</code>, this is registration email;
     * otherwise, it is a forgot-password email.
     *
     * @param email
     *            The email address to mail to
     * @param isRegister
     *            If true, this is registration email; otherwise it is
     *            forgot-password email.
     * @param rd
     *            The RDBMS row representing the registration data.
     * @exception MessagingException
     *                If an error occurs while sending email
     * @exception IOException
     *                If an error occurs while reading the email template.
     */
    protected void sendEmail(Context context, String email, boolean isRegister, RegistrationData rd)
            throws MessagingException, IOException, SQLException
    {
        String base = ConfigurationManager.getProperty("dspace.url");

        //  Note change from "key=" to "token="
        String specialLink = new StringBuffer().append(base).append(
                base.endsWith("/") ? "" : "/").append(
                isRegister ? "register" : "forgot").append("?")
                .append("token=").append(rd.getToken())
                .toString();
        Locale locale = context.getCurrentLocale();
        Email bean = Email.getEmail(I18nUtil.getEmailFilename(locale, isRegister ? "register"
                : "change_password"));
        bean.addRecipient(email);
        bean.addArgument(specialLink);
        bean.send();

        // Breadcrumbs
        if (log.isInfoEnabled())
        {
            log.info("Sent " + (isRegister ? "registration" : "account")
                    + " information to " + email);
        }
    }
}
