/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.factory;

import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.content.service.BatchImportService;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.DSpaceObjectLegacySupportService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.DatasetService;
import org.dspace.content.service.InstallItemService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.content.service.MetadataSchemaService;
import org.dspace.content.service.MetadataValueService;
import org.dspace.content.service.SiteService;
import org.dspace.content.service.SupervisedItemService;
import org.dspace.content.service.SwordKeyService;
import org.dspace.content.service.UUN2EmailService;
import org.dspace.content.service.WorkspaceItemService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory implementation to get services for the content package, use ContentServiceFactory.getInstance() to retrieve an implementation
 *
 * @author kevinvandevelde at atmire.com
 */
public class DatashareContentServiceFactoryImpl extends DatashareContentServiceFactory {

    // Datashare -start
    @Autowired(required = true)
    private BatchImportService batchImportService;
    @Autowired(required = true)
    private DatasetService datasetService;
    @Autowired(required = true)
    private SwordKeyService swordKeyService;
    @Autowired(required = true)
    private UUN2EmailService uUN2EmailService;
    // Datashare - end

    // Datashare - start
	@Override
	public BatchImportService getBatchImportService() {
		// TODO Auto-generated method stub
		return batchImportService;
	}

	@Override
	public DatasetService getDatasetService() {
		// TODO Auto-generated method stub
		return datasetService;
	}

	@Override
	public SwordKeyService getSwordKeyService() {
		// TODO Auto-generated method stub
		return swordKeyService;
	}

	@Override
	public UUN2EmailService getUUN2EmailService() {
		// TODO Auto-generated method stub
		return uUN2EmailService;
	}
	// Datashare - end
}
