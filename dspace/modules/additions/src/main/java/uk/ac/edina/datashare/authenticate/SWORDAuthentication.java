package uk.ac.edina.datashare.authenticate;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.SwordKeyService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;

import uk.ac.edina.datashare.utils.DSpaceUtils;

/**
 * Datashare SWORD user authentication.
 */
public class SWORDAuthentication implements AuthenticationMethod {
	private static Logger LOG = Logger.getLogger(SWORDAuthentication.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dspace.authenticate.AuthenticationMethod#canSelfRegister(org.dspace.core.
	 * Context, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public boolean canSelfRegister(Context context, HttpServletRequest request, String username) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dspace.authenticate.AuthenticationMethod#initEPerson(org.dspace.core.
	 * Context, javax.servlet.http.HttpServletRequest, org.dspace.eperson.EPerson)
	 */
	@Override
	public void initEPerson(Context context, HttpServletRequest request, EPerson eperson) throws SQLException {
		// empty
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dspace.authenticate.AuthenticationMethod#allowSetPassword(org.dspace.core
	 * .Context, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public boolean allowSetPassword(Context context, HttpServletRequest request, String username) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dspace.authenticate.AuthenticationMethod#isImplicit()
	 */
	@Override
	public boolean isImplicit() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dspace.authenticate.AuthenticationMethod#getSpecialGroups(org.dspace.core
	 * .Context, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public List<Group> getSpecialGroups(Context context, HttpServletRequest request) throws SQLException {
		return DSpaceUtils.getSpecialGroups(context, request);
	}

	/**
	 * Authenticate using SWORD API key.
	 */
	@Override
	public int authenticate(Context context, String username, String password, String realm, HttpServletRequest request)
			throws SQLException {
		int status = NO_SUCH_USER;

		if (username == null || password == null) {
			status = BAD_ARGS;
		} else {

			EPerson eperson = null;
			EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

			// is username has an @ then its probably and email address
			if (username.contains("@")) {
				eperson = ePersonService.findByEmail(context, username);
			}

			if (eperson == null) {
				// try UUN
				eperson = ePersonService.findByNetid(context, username);
			}

			if (eperson != null) {
				SwordKeyService swordKeyService = DatashareContentServiceFactory.getInstance().getSwordKeyService();
				String key = swordKeyService.fetchSwordKey(context, eperson);
				if (key != null && key.equals(password)) {
					status = SUCCESS;

					// good credentials set current user
					context.setCurrentUser(eperson);

					LOG.info("SWORD authentication success for: " + username);
				} else {
					status = BAD_CREDENTIALS;
					LOG.info("SWORD authentication failed for: " + username);
				}
			} else {
				LOG.info("SWORD authentication failed, no such user: " + username);
			}
		}

		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dspace.authenticate.AuthenticationMethod#loginPageURL(org.dspace.core.
	 * Context, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public String loginPageURL(Context context, HttpServletRequest request, HttpServletResponse response) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.dspace.authenticate.AuthenticationMethod#loginPageTitle(org.dspace.core.
	 * Context)
	 */
	@Override
	public String loginPageTitle(Context context) {
		return null;
	}
}
