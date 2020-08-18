package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.UUN2EmailDAO;
import org.dspace.content.service.UUN2EmailService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

public class UUN2EmailServiceImpl  extends DSpaceObjectServiceImpl<UUN2Email> implements UUN2EmailService {
	
	/** log4j logger */
	private static Logger log = Logger.getLogger(UUN2EmailServiceImpl.class);

	@Autowired(required = true)
	protected UUN2EmailDAO uun2EmailDAO;


	@Override
	public UUN2Email find(Context context, UUID id) throws SQLException {
		UUN2Email uun2Email = uun2EmailDAO.findByID(context, UUN2Email.class, id);

		if (uun2Email == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_uun2Email",
						"not_found, id= " + id));
			}

			return null;
		}

		// not null, return uun2Email
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_uun2Email",
					"id= " + id));
		}

		return uun2Email;
	}

	@Override
	public void updateLastModified(Context context, UUN2Email uun2Email) throws SQLException, AuthorizeException {
		context.addEvent(new Event(Event.MODIFY, Constants.UUN_2_EMAIL, uun2Email.getID(), null, new ArrayList<String>()));
		
	}

	@Override
	public void delete(Context context, UUN2Email dso) throws SQLException, AuthorizeException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSupportsTypeConstant() {
		return Constants.UUN_2_EMAIL;
	}

	@Override
	public void insertUUNEntry(Context context, String uun, String email) throws SQLException, AuthorizeException {
		log.info("Called insertUUNEntry with uun : " + uun + ", email: " + email);
		createUUN2Email(context, uun, email);
	}

	@Override
	public String fetchUUN(Context context, String email) throws SQLException {
		String jpqlQuery = "SELECT u FROM UUN2Email u where u.email = " + email;
		UUN2Email uun2Email = uun2EmailDAO.findUnique(context, jpqlQuery);
		if (uun2Email == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_uun2Email",
						"not_found, email = " + email));
			}

			return null;
		}

		// not null, return uun2Email
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_uun2Email",
					"email = " + email));
		}
		return uun2Email.getUUN();
	}

	protected  UUN2Email createUUN2Email(Context context, String uun, String email) throws SQLException, AuthorizeException {
		UUN2Email uun2EmailObj = new UUN2Email();
		uun2EmailObj.setUUN(uun);
		uun2EmailObj.setEmail(email);
		
		log.info("Creating uun2Email for db storage.");
		UUN2Email uun2Email = uun2EmailDAO.create(context, uun2EmailObj);

        // Call update to give the item a last modified date. OK this isn't
        // amazingly efficient but creates don't happen that often.
        context.turnOffAuthorisationSystem();
        update(context, uun2Email);
        context.restoreAuthSystemState();

        context.addEvent(new Event(Event.CREATE, Constants.UUN_2_EMAIL, uun2Email.getID(),
                null, new ArrayList<String>()));

        log.info(LogManager.getHeader(context, "create_UUN2Email", "UUN2Email id= " + uun2Email.getID()));

        return uun2Email;
		
	}

}
