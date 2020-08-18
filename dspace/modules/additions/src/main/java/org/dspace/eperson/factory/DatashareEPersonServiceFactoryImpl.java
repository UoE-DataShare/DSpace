package org.dspace.eperson.factory;

import org.dspace.eperson.service.DatashareAccountService;
import org.springframework.beans.factory.annotation.Autowired;

public class DatashareEPersonServiceFactoryImpl extends DatashareEPersonServiceFactory {
	
	@Autowired(required = true)
	private DatashareAccountService datashareAccountService;

	@Override
	public DatashareAccountService getDatashareAccountService() {
		return datashareAccountService;
	}

}
