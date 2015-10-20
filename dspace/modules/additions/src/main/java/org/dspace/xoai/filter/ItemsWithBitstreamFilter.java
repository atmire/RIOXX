package org.dspace.xoai.filter;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.dspace.xoai.data.DSpaceItem;
import org.dspace.xoai.filter.results.DatabaseFilterResult;
import org.dspace.xoai.filter.results.SolrFilterResult;

import java.sql.SQLException;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 21/04/15
 * Time: 15:18
 */
public class ItemsWithBitstreamFilter extends DSpaceFilter {

    private static Logger log = LogManager.getLogger(ItemsWithBitstreamFilter.class);

    @Override
    public DatabaseFilterResult buildDatabaseQuery(Context context) {
        return new DatabaseFilterResult();
    }

    @Override
    public SolrFilterResult buildSolrQuery() {
    return new SolrFilterResult("item.hasbitstream:true");
    }

    @Override
    public boolean isShown(DSpaceItem item) {
        try {
            String handle = DSpaceItem.parseHandle(item.getIdentifier());
            if (handle == null) return false;
            Item dspaceItem = (Item) HandleManager.resolveToObject(context, handle);
            for (Bundle b : dspaceItem.getBundles("ORIGINAL")){
                if(b.getBitstreams().length>0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
