package uk.ac.edina.datashare.app.xmlui.aspect.datashare;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.itemimport.factory.ItemImportServiceFactory;
import org.dspace.app.itemimport.service.ItemImportService;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.LicenseUtils;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.factory.DatashareContentServiceFactory;
import org.dspace.content.service.BatchImportService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;


/**
 * Attempt to agree to the terms of the Depositor Agreement.
 */
public class DepositAgreeAction  extends AbstractAction{
    private static Logger LOG = Logger.getLogger(DepositAgreeAction.class);
    
    private transient BatchImportService batchImportService = DatashareContentServiceFactory.getInstance().getBatchImportService();
    private transient ItemImportService itemImportService = ItemImportServiceFactory.getInstance().getItemImportService();
    private transient ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    /*
     * (non-Javadoc)
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    @SuppressWarnings({ "rawtypes" })
    public Map act(
            Redirector redirector,
            SourceResolver resolver,
            Map objectModel,
            String source,
            Parameters parameters) throws Exception{
        
        HashMap<String, String> map = new HashMap<String, String>(1);
        Request request = ObjectModelHelper.getRequest(objectModel);
        Context context = ContextUtil.obtainContext(objectModel);
        
        String iId = request.getParameter("item");
        List<Item> items = new ArrayList<Item>(100);
        EPerson user = context.getCurrentUser();
        String batchId = null;
        
        if(user != null){
            if(iId != null){
                Item item = agreeToItem(context, user, iId, map);
                
                if(item != null){
                    items.add(item);
                }
            }
            else{
                batchId = request.getParameter("batch");
                if(batchId != null){
                    agreeToBatch(context, user, batchId, map, items);
                }
                else{
                    map.put("result", "No item specified");
                }
            }            
        }
        else{
            map.put("result", "User needs to be logged in");
        }
        
        if(items.size() > 0){
            for (Item item : items) {
            	List<Bundle> licences = item.getBundles("LICENSE"); 
                if(licences.size() == 0){
                    try{
                        context.turnOffAuthorisationSystem();
                        
                        // create depositors agreement
                        String license = LicenseUtils.getLicenseText(
                                context.getCurrentLocale(),
                                item.getOwningCollection(),
                                item,
                                user);
                        LicenseUtils.grantLicense(context, item, license);
                    }
                    catch(Exception ex){
                        LOG.warn(ex);
                        map.put("result", "Error creating license");
                    }
                    finally{
                        context.restoreAuthSystemState();
                    }
                }
                else{
                    map.put("result", "Terms have already been agreed to");
                }
            }
        }
        else{
            LOG.warn("Deposit Agree failed: " + map.get("result"));
        }
        
        if(batchId != null && map.size() == 0){
        	batchImportService.deleteBatchImport(context, batchId);
        }
        
        return map;
    }
    
    /**
     * Agree to depositor agreement for a single item.
     * @param context
     * @param user
     * @param id The item id.
     * @param map
     * @return DSpace item agreed to.
     * @throws SQLException
     */
    private Item agreeToItem(Context context, EPerson user, String id, Map<String, String> map) throws SQLException{
        Item item = null;
        
        try{
            item = itemService.findByIdOrLegacyId(context, id); 
            if(item != null){
                // validate user is owner
                if(!item.getSubmitter().equals(user)){
                    user = null;
                    map.put("result", "You are not authorised");
                }               
            }
            else{
                map.put("result", "Item not found for " + id);
            }
        }
        catch(NumberFormatException ex){
            map.put("result", "Invalid item id");
        }

        return item;
    }
    
    /**
     * Agree to depositor agreement for all items in a batch import.
     * @param context
     * @param user
     * @param id Batch import id.
     * @param map
     * @param items All items in the batch.
     */
    private void agreeToBatch(Context context, EPerson user, String id, Map<String, String> map, List<Item> items){ 
        try{
            String mapFile = batchImportService.fetchBatchMapFile(context, id);
            LOG.info("using mapfile " + mapFile);
            try{
                Map<String, String> files = readMapFile(mapFile);
                for(String itemId : files.values()) {
                    Item item = agreeToItem(context, user, itemId, map);
                    
                    if(item != null){
                        LOG.info("agree to " + item.getID());
                        items.add(item);
                    }
                    else{
                        LOG.warn("couldnt find item to agree to " + itemId);
                        // if one fails stop processing
                        items.clear();
                        break;
                    }
                }
            }
            catch(Exception ex){
                LOG.error(ex);
                map.put("result", "Problem finding batch: " + ex.getMessage());
            }
            
        }
        catch(NumberFormatException ex){
            map.put("result", "Invalid batch id: " + id);
        }
        catch(SQLException ex){
            LOG.error(ex);
            map.put("result", "Database problem finding batch: " + ex.getMessage());
        }
    }
    
    // Note: Copy of protected method in ItemImportServiceImpl.
    private Map<String, String> readMapFile(String filename) throws Exception
    {
        Map<String, String> myHash = new HashMap<>();

        BufferedReader is = null;
        try
        {
            is = new BufferedReader(new FileReader(filename));

            String line;

            while ((line = is.readLine()) != null)
            {
                String myFile;
                String myHandle;

                // a line should be archive filename<whitespace>handle
                StringTokenizer st = new StringTokenizer(line);

                if (st.hasMoreTokens())
                {
                    myFile = st.nextToken();
                }
                else
                {
                    throw new Exception("Bad mapfile line:\n" + line);
                }

                if (st.hasMoreTokens())
                {
                    myHandle = st.nextToken();
                }
                else
                {
                    throw new Exception("Bad mapfile line:\n" + line);
                }

                myHash.put(myFile, myHandle);
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }

        return myHash;
    }

}
