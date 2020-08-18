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
import org.dspace.content.InProgressSubmission;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.BatchImportService;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.DSpaceObjectLegacySupportService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.DatasetService;
import org.dspace.content.service.InProgressSubmissionService;
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
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.workflow.factory.WorkflowServiceFactory;

/**
 * Abstract factory to get services for the content package, use DatashareContentServiceFactory.getInstance() to retrieve an implementation
 *
 */
public abstract class DatashareContentServiceFactory {

    public abstract BatchImportService getBatchImportService();
    
    public abstract DatasetService getDatasetService();
    
    public abstract SwordKeyService getSwordKeyService();
    
    public abstract UUN2EmailService getUUN2EmailService();

    public static DatashareContentServiceFactory getInstance(){
        return DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName("datashareContentServiceFactory", DatashareContentServiceFactory.class);
    }

}
