package org.dspace.authority;

import org.apache.log4j.Logger;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.kernel.ServiceManager;
import org.dspace.utils.DSpace;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 12 Aug 2014
 */
public class IndexingUtils {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(IndexingUtils.class);

    private static DSpace myDSpace;
    private static AuthorityIndexingService indexingService;

    public static AuthorityIndexingService getIndexingService(ServiceManager serviceManager) {
        if (indexingService == null) {
            indexingService = serviceManager.getServiceByName(AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);
        }
        return indexingService;
    }

    public static ServiceManager getServiceManager() {
        if (myDSpace == null) {
            myDSpace = new DSpace();
        }
        return myDSpace.getServiceManager();
    }

    /**
     * sets the delete flag of the solr record to true
     * and removes the authority key from the metadata
     */
/*    public static void deleteAuthority(Context context, AuthorityValue authority) {
        authority.setDeleted(true);
        ServiceManager serviceManager = getServiceManager();
        AuthorityIndexingService indexingService = getIndexingService(serviceManager);
        indexingService.indexContent(authority, true);
        indexingService.commit();


        String mdString = authority.getField().replace('_', '.');
        try {
            ItemIterator itemIterator = ItemUtils.findByMetadataFieldAuthority(context, mdString, authority.getId());
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                ItemUtils.clearMetadataKey(item, mdString, authority.getId());
                item.update();
            }
            context.commit();

        } catch (SQLException e) {
            log.error("Error", e);
        } catch (AuthorizeException e) {
            log.error("Error", e);
        } catch (IOException e) {
            log.error("Error", e);
        }
    }*/


}
