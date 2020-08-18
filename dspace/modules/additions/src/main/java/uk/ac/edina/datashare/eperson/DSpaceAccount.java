package uk.ac.edina.datashare.eperson;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dspace.authenticate.factory.AuthenticateServiceFactory;
import org.dspace.authenticate.service.AuthenticationService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;

/**
 * DSpaceAccount provides helper methods related to a DSpace account.
 */
public class DSpaceAccount
{
    /** log4j category */
    private static Logger LOG = Logger.getLogger(DSpaceAccount.class);
    
    /**
     * Create a new DSpace account.
     * @param context DSpace context.
     * @param email New user's email address.
     * @param firstName New user's first name.
     * @param surname New user's surname.
     * @return The new EPerson object is account creation is successful.
     */
    public static EPerson createAccount(
            Context context,
            String email,
            String firstName,
            String surname)
    {
        return createAccount(context, email, firstName, surname, null);
    }
    
    /**
     * Create a new DSpace account.
     * @param context DSpace context.
     * @param user User details.
     * @param email New user's email address.
     * @param netid The id an external system.
     * @return The new EPerson object is account creation is successful.
     */
    public static EPerson createAccount(
            Context context,
            IUser user,
            String email,
            String netid)
    {
        return createAccount(
                context,
                email,
                user.getFirstName(),
                user.getSurname(),
                netid);
    }
    
    /**
     * Create a new DSpace account.
     * @param context DSpace context.
     * @param email New user's email address.
     * @param firstName New user's first name.
     * @param surname New user's surname.
     * @param netid The id an external system.
     * @return The new EPerson object is account creation is successful.
     */
    public static EPerson createAccount(
            Context context,
            String email,
            String firstName,
            String surname,
            String netid)
    {
        EPerson eperson = null;

        context.turnOffAuthorisationSystem();

        try
        {   EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();
            // create account
            eperson = ePersonService.create(context);
            eperson.setEmail(email);
            eperson.setFirstName(context, firstName);
            eperson.setLastName(context, surname);
            eperson.setNetid(netid);
            eperson.setCanLogIn(true);
            eperson.setSelfRegistered(true);
            ePersonService.update(context, eperson);
            
            LOG.info("New account created for " + email);
        }
        catch(AuthorizeException ex)
        {
            LOG.error("Failed to create account for " + email);
            throw new RuntimeException(ex);
        }
        catch(SQLException ex)
        {
            LOG.error("Failed to create account for " + email);
            throw new RuntimeException(ex);
        }
        finally
        {
            context.restoreAuthSystemState();
        }
        
        return eperson;
    }
    
    /**
     * Log user in.
     * @param context
     * @param request
     * @param ePerson
     */
    public static void login(
            Context context,
            HttpServletRequest request,
            EPerson ePerson)
    {
        try
        {
            LOG.info(LogManager.getHeader(context, "login", "type=explicit"));
            
            // this method doesn't exist in DSpace, add it!
            HttpSession session = request.getSession();

            context.setCurrentUser(ePerson);

            // Check to see if systemwide alerts is restricting sessions
/*            if (!AuthorizeManager.isAdmin(context) && !SystemwideAlerts.canUserStartSession())
            {
            	// Do not allow this user to login because sessions are being restricted by a systemwide alert.
            	context.setCurrentUser(null);
            	return;
            }
*/           
            AuthenticationService authenticationService = AuthenticateServiceFactory.getInstance().getAuthenticationService();
            // Set any special groups - invoke the authentication manager.
            List<Group> groups = authenticationService.getSpecialGroups(context,
                    request);
            for (Group group : groups)
            {
                context.setSpecialGroup(group.getID());
            }

            // and the remote IP address to compare against later requests
            // so we can detect session hijacking.
            session.setAttribute("dspace.user.ip", request.getRemoteAddr());
            
            // Set both the effective and authenticated user to the same.
            session.setAttribute("dspace.user.effective", ePerson.getID());
            session.setAttribute("dspace.user.authenticated", ePerson.getID());
        }
        catch(Exception ex)
        {
            LOG.error("Failed to login user " + ePerson.getEmail() + ": " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Update the netid for a DSpace user. 
     * @param context DSpace context.
     * @param eperson DSpace user.
     * @param netid New netid.
     */
    public static void updateNetId(Context context, EPerson eperson, String netid)
    { 
        context.turnOffAuthorisationSystem();
        
        try
        {
            eperson.setNetid(netid);
            EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();
            ePersonService.update(context, eperson);
        }
        catch(AuthorizeException ex)
        {
            throw new RuntimeException(ex);
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            context.restoreAuthSystemState();
        }
    }       
}
