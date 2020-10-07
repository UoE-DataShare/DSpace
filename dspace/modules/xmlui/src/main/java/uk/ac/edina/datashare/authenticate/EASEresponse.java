package uk.ac.edina.datashare.authenticate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.utils.AuthenticationUtil;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
// import org.dspace.content.service.SwordKeyService;


import edu.umich.auth.AuthFilterRequestWrapper;
import edu.umich.auth.cosign.CosignPrincipal;
import edu.umich.auth.cosign.CosignServletCallbackHandler;

import uk.ac.edina.datashare.eperson.DSpaceAccount;
import uk.ac.edina.datashare.ldap.LDAPAccess;
import uk.ac.edina.datashare.ldap.User;
import uk.ac.edina.datashare.utils.DSpaceUtils;

/**
 * EASE login call back class.
 */
public class EASEresponse extends CosignServletCallbackHandler
{
	/** log4j category */
	private static final Logger LOG = Logger.getLogger(EASEresponse.class);

	/** The ease university user name attribute name */
	public static final String EASE_UUN = "ease.uun";
	
	private static final String LOCATION = "Location";

	private EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();

	//	private SwordKeyService swordKeyService = DatashareContentServiceFactory.getInstance().getSwordKeyService();

	/**
	 * User has successfully logged into EASE, determine if the user has a
	 * dspace account. If dspace account exists, log the user on.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleSuccessfulLogin() throws ServletException
	{ 
		this.getRequest().setAttribute(EASE_UUN, null);

		// do standard successful login
		super.handleSuccessfulLogin();

		if(this.getRequest() instanceof AuthFilterRequestWrapper)
		{ 
			HttpServletResponse response = this.getResponse();
			AuthFilterRequestWrapper request = (AuthFilterRequestWrapper)this.getRequest();
			String url = request.getContextPath();
			CosignPrincipal principal = (CosignPrincipal)request.getUserPrincipal();

			// get uun from principal name
			String uun = principal.getName();
			LOG.info("uun: " + uun);

			try
			{
				// try and get eperson from uun
//				Context context = ContextUtil.obtainContext(request);
				Context context = new Context();
				EPerson eperson = epersonService.findByNetid(context, uun);

				LOG.info("eperson: " + eperson);

				if(eperson != null)
				{
					LOG.info("Found user, so log in");
					// found user log them in
					DSpaceAccount.login(context, request, eperson);
				}
				else
				{
					LOG.info("User not found try LDAP");
					// user not found try LDAP
					User user = LDAPAccess.instance().getUserDetailsForUun(uun);
					LOG.info("Has user be found using LDAP: " + (user != null));

					if(user != null)
					{
						// details found in LDAP - first check if account exists
						eperson = DSpaceUtils.findByEmail(
								context,
								user.getEmail());
						LOG.info("eperson: " + eperson);

						if(eperson != null)
						{
							LOG.info("Found eperson, so update Eperson with uun: " + uun);
							// account exists, update netid
							DSpaceAccount.updateNetId(context, eperson, uun);
						}
						else
						{
							LOG.info("Eperson not found, so create Eperson with uun: " + uun);
							// account doesn't exits create new one
							eperson = DSpaceAccount.createAccount(
									context,
									user,
									user.getEmail(),
									uun);
							//							Commented out Sword functionality
							//							try {
							//								// generate sword key
							//								swordKeyService.insertSwordKey(context, eperson);
							//							} catch (AuthorizeException ae) {
							//								LOG.warn("Adding swordKey failed: " + ae);
							//							}
						} 

						context.commit();

						LOG.info("eperson: " + eperson);
						LOG.info("uun: " + uun);
						LOG.info("Log in as Eperson");
						
						if(eperson != null) {
							LOG.info("Before login - eperson.getNetid(): " + eperson.getNetid());
						}
		
						// log user in
						DSpaceAccount.login(context, request, eperson);
					}
					else
					{
						// clear any interrupted requests
						request.getSession().setAttribute(AuthenticationUtil.REQUEST_INTERRUPTED, null);

						// unable to automatically create account go to registration
						url = response.encodeRedirectURL(request.getContextPath() +
								"/register?uun=" + uun);
					}
				}

				// resume any interrupted request
				Map om = new HashMap();
				om.put(HttpEnvironment.HTTP_REQUEST_OBJECT, request);
				String interruptUrl = AuthenticationUtil.resumeInterruptedRequest(om);

				try
				{
					LOG.info("url: "  + url);
					LOG.info("interruptUrl: " + interruptUrl);
					LOG.info("response.containsHeader(LOCATION): " + response.containsHeader(LOCATION));
					
					String dspaceUrl = ConfigurationManager.getProperty("dspace.url") ;
					if(StringUtils.isEmpty(dspaceUrl)) {
						dspaceUrl = "/";
					}
					LOG.info("dspaceUrl: "  + dspaceUrl);

					if(StringUtils.isEmpty(url)) {
						url = dspaceUrl;
					}
					if(interruptUrl == null){
						response.sendRedirect(url);
					} else{
						response.sendRedirect(interruptUrl);
					}
				}
				catch(IOException ex)
				{
					throw new RuntimeException(ex);
				}
			}
			catch(SQLException ex)
			{
				throw new RuntimeException("Failed to fetch context: " + ex.getMessage());
			} 
		}
	}

	/**
	 * User failed to logon to EASE
	 */
	public boolean handleFailedLogin(Exception ex) throws ServletException
	{ 
		LOG.warn("EASE login failed: " + ex);
		return super.handleFailedLogin(ex);
	}
}
