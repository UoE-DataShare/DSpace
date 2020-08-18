/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.step;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;

/**
 * Resumable Upload step for DSpace. Processes the actual upload of files for an
 * item being submitted into DSpace.
 */
public class ResumableUploadStep extends UploadStep{
    private static final boolean fileRequired = 
            ConfigurationManager.getBooleanProperty("webui.submit.upload.required", true);
    public static final String RESUMABLE_PARAM = "resumable";
    
    protected BitstreamService bitstreamService = ContentServiceFactory.getInstance()
			.getBitstreamService();
    protected BundleService bundleService = ContentServiceFactory.getInstance()
			.getBundleService();
    protected ItemService itemService = ContentServiceFactory.getInstance()
			.getItemService();
    
    
    /*
     * (non-Javadoc)
     * @see org.dspace.submit.step.UploadStep#doProcessing(org.dspace.core.Context, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.dspace.app.util.SubmissionInfo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public int doProcessing(
            Context context,
            HttpServletRequest request,
            HttpServletResponse response,
            SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException
    {
        int status = UploadStep.STATUS_COMPLETE;
        boolean next = false;
                
        String resumable = request.getParameter(RESUMABLE_PARAM);
        if(resumable != null && Boolean.parseBoolean(resumable))
        { 
            for (Enumeration<Object> e = request.getParameterNames(); e.hasMoreElements();)
            {
                String key = e.nextElement().toString();
                if(key.startsWith("description-"))
                {
                    String val = request.getParameter(key.toString()); 
                    if(val != null && val.length() > 0)
                    {
                        UUID bistreamId = UUID.fromString(key.split("-")[1]);

                        Bitstream b = bitstreamService.find(context, bistreamId);
                        b.setDescription(context, val);
                        bitstreamService.update(context, b);
                    }
                }
                else if(key.equals("primary_bitstream_id"))
                {
                    Item item = subInfo.getSubmissionItem().getItem();
                    List<Bundle> bundles = item.getBundles("ORIGINAL");
                    if (bundles.size() > 0)
                    {
                    	UUID bistreamId = UUID.fromString(request.getParameter(key.toString()));
                        Bitstream b = bitstreamService.find(context, bistreamId);
                        
                        bundles.get(0).setPrimaryBitstreamID(b);
                        bundleService.update(context, bundles.get(0));
                    }
                }
                else if(key.equals(NEXT_BUTTON)){
                    next = true;
                }
            }

            Item item = subInfo.getSubmissionItem().getItem();
            if (fileRequired && next && !itemService.hasUploadedFiles(item)){
                // if next has been chosen check files have been uploaded
                status = UploadStep.STATUS_NO_FILES_ERROR;
            }
        }
        else{
            // client is using traditional upload form, pass request onto base class
            status = super.doProcessing(context, request, response, subInfo);
        }
        
        return status;
    }
    
}
