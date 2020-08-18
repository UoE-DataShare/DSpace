package org.dspace.eperson;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.core.Email;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.service.AccountService;
import org.dspace.eperson.service.DatashareAccountService;
import org.dspace.eperson.service.RegistrationDataService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.springframework.beans.factory.annotation.Autowired;


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

		// See if a registration token already exists for this user
		RegistrationData rd = registrationDataService.findByEmail(context, email);

		// If it already exists, just re-issue it
		if (rd == null) {
			rd = registrationDataService.create(context);

			rd.setEmail(email);

			rd.setToken(uun);

			registrationDataService.update(context, rd);

			// This is a potential problem -- if we create the callback
			// and then crash, registration will get SNAFU-ed.
			// So FIRST leave some breadcrumbs
			if (log.isDebugEnabled()) {
				log.debug("Created callback " + rd.getID() + " with token " + rd.getToken() + " with email \"" + email
						+ "\"");
			}
		}

		sendEmail(context, email, true, rd);

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
        String base = DSpaceServicesFactory.getInstance().getConfigurationService().getProperty("dspace.url");

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
