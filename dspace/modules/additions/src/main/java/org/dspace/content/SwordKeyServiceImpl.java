package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.SwordKeyDAO;
import org.dspace.content.service.SwordKeyService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

public class SwordKeyServiceImpl  extends DSpaceObjectServiceImpl<SwordKey> implements SwordKeyService {

	/** log4j logger */
	private static Logger log = Logger.getLogger(SwordKeyServiceImpl.class);

	@Autowired(required = true)
	protected SwordKeyDAO swordKeyDAO;

	@Override
	public SwordKey find(Context context, UUID id) throws SQLException {
		SwordKey swordKey = swordKeyDAO.findByID(context, SwordKey.class, id);

		if (swordKey == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_swordKey",
						"not_found, id= " + id));
			}

			return null;
		}

		// not null, return swordKey
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_swordKey",
					"id= " + id));
		}

		return swordKey;
	}

	@Override
	public void updateLastModified(Context context, SwordKey swordKey) throws SQLException, AuthorizeException {
		context.addEvent(new Event(Event.MODIFY, Constants.SWORD_KEY, swordKey.getID(), null, new ArrayList<String>()));

	}

	@Override
	public void delete(Context context, SwordKey swordKey) throws SQLException, AuthorizeException, IOException {
		swordKeyDAO.delete(context, swordKey);

	}

	@Override
	public int getSupportsTypeConstant() {
		return Constants.SWORD_KEY;
	}


	@Override
	public void insertSwordKey(Context context, EPerson eperson) throws SQLException, AuthorizeException {
		createSwordKey(context, eperson);
	}

	@Override
	public String fetchSwordKey(Context context) throws SQLException {
		return fetchSwordKey(context, null);
	}

	@Override
	public String fetchSwordKey(Context context, EPerson eperson) throws SQLException {
		String jpqlQuery = "SELECT sk FROM SwordKey sk where sk.uuid = " + eperson.getID();
		SwordKey swordKey = swordKeyDAO.findUnique(context, jpqlQuery);
		if (swordKey == null)
		{
			if (log.isDebugEnabled())
			{
				log.debug(LogManager.getHeader(context, "find_swordKey",
						"not_found, eperson uuid= " + eperson.getID()));
			}

			return null;
		}

		// not null, return SwordKey
		if (log.isDebugEnabled())
		{
			log.debug(LogManager.getHeader(context, "find_SwordKey",
					"eperson uuid= " + eperson.getID()));
		}
		return swordKey.getKey();
	}

	protected  SwordKey createSwordKey(Context context, EPerson ePerson) throws SQLException, AuthorizeException {
		SwordKey swordKeyObj = new SwordKey();
		swordKeyObj.setEPerson(ePerson);
		SwordKey swordKey = swordKeyDAO.create(context, swordKeyObj);


		// Call update to give the item a last modified date. OK this isn't
		// amazingly efficient but creates don't happen that often.
		context.turnOffAuthorisationSystem();
		update(context, swordKey);
		context.restoreAuthSystemState();

		context.addEvent(new Event(Event.CREATE, Constants.ITEM, swordKey.getID(),
				null, new ArrayList<String>()));

		log.info(LogManager.getHeader(context, "create_swordKey", "swordKey id= " + swordKey.getID()));

		return swordKey;

	}

}
