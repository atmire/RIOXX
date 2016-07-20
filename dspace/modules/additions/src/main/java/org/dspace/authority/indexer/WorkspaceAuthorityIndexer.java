package org.dspace.authority.indexer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.AuthorityValueGenerator;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.*;

/**
 * Authority indexer that will (re)index all authorities used in items that are still
 * in the workspace.
 */
public class WorkspaceAuthorityIndexer implements AuthorityIndexerInterface {

    private static final Logger log = Logger.getLogger(WorkspaceAuthorityIndexer.class);

    private AuthorityValueFinder authorityValueFinder;
    private Context context;

    private Set<String> metadataFields;

    private Map<String, AuthorityValue> cache;

    private Iterator<Metadatum> metadataIterator = null;
    private Iterator<WorkspaceItem> itemIterator = null;

    private Item currentItem = null;
    private AuthorityValue nextValue = null;

    private boolean useCache = true;

    @Autowired(required = true)
    protected ConfigurationService configurationService;

    @Override
    public void init(Context context, Item item) {
        //This indexer should do nothing when called from the AuthorityConsumer
    }

    @Override
    public void init(Context context, boolean useCache) {
        loadConfiguration();
        loadWorkspaceItems(context);
        this.useCache = useCache;
        this.context = context;
    }

    @Override
    public void init(Context context) {
        loadConfiguration();
        loadWorkspaceItems(context);
        this.context = context;
    }

    @Override
    public AuthorityValue nextValue() {
        return nextValue;
    }

    @Override
    public boolean hasMore() {

        while ( (metadataIterator != null && metadataIterator.hasNext())
            || (itemIterator != null && itemIterator.hasNext()) ) {

            if(metadataIterator != null && metadataIterator.hasNext()) {
                Metadatum metadatum = metadataIterator.next();

                //If this metadatum has an authority and needs to be indexed, look it up or create it and return true;
                if(StringUtils.isNotBlank(metadatum.authority)
                        && metadataFields.contains(metadatum.getField())) {

                    prepareNextValue(metadatum.getField(), metadatum);

                    return true;
                }

            } else if(itemIterator != null && itemIterator.hasNext()) {
                WorkspaceItem workspaceItem = itemIterator.next();
                currentItem = workspaceItem.getItem();
                metadataIterator = currentItem.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY, Item.ANY).iterator();
            }
        }

        return false;
    }

    @Override
    public void close() {
        metadataIterator = null;
        itemIterator = null;
        currentItem = null;
        nextValue = null;
        if(cache != null) {
            cache.clear();
        }
    }

    @Override
    public boolean isConfiguredProperly() {
        return true;
    }

    private void loadConfiguration() {
        int counter = 1;
        String field;
        metadataFields = new HashSet<String>();
        while ((field = configurationService.getProperty("authority.author.indexer.field." + counter)) != null) {
            metadataFields.add(field);
            counter++;
        }

        authorityValueFinder = new AuthorityValueFinder();
        cache = new HashMap<String, AuthorityValue>();
    }

    private void loadWorkspaceItems(Context context) {
        try {
            List<WorkspaceItem> itemList = Arrays.asList(WorkspaceItem.findAll(context));
            itemIterator = itemList.iterator();
        } catch (SQLException e) {
            log.error("Unable to retrieve all workspace items for indexing authorities", e);
        }
    }

    /**
     * This method looks at the authority of a metadata.
     * If the authority can be found in solr, that value is reused.
     * Otherwise a new authority value will be generated that will be indexed in solr.
     * If the authority starts with AuthorityValueGenerator.GENERATE, a specific type of AuthorityValue will be generated.
     * Depending on the type this may involve querying an external REST service
     *
     * @param metadataField Is one of the fields defined in dspace.cfg to be indexed.
     * @param value         Is one of the values of the given metadataField in one of the items being indexed.
     */
    private void prepareNextValue(String metadataField, Metadatum value) {

        nextValue = null;

        String content = value.value;
        String authorityKey = value.authority;
        //We only want to update our item IF our UUID is not present or if we need to generate one.
        boolean requiresItemUpdate = StringUtils.isBlank(authorityKey)
            || StringUtils.startsWith(authorityKey, AuthorityValueGenerator.GENERATE);

        if (org.apache.commons.lang.StringUtils.isNotBlank(authorityKey)
                && !authorityKey.startsWith(AuthorityValueGenerator.GENERATE)) {
            // !uid.startsWith(AuthorityValueGenerator.GENERATE) is not strictly necessary here but it prevents exceptions in solr
            nextValue = authorityValueFinder.findByUID(context, authorityKey);
        }
        if (nextValue == null && org.apache.commons.lang.StringUtils.isBlank(authorityKey) && useCache) {
            // A metadata without authority is being indexed
            // If there is an exact match in the cache, reuse it rather than adding a new one.
            AuthorityValue cachedAuthorityValue = cache.get(content);
            if (cachedAuthorityValue != null) {
                nextValue = cachedAuthorityValue;
            }
        }
        if (nextValue == null) {
            nextValue = AuthorityValueGenerator.generate(context, authorityKey, content, metadataField.replaceAll("\\.", "_"));
        }
        if (nextValue != null && requiresItemUpdate) {
            nextValue.updateItem(currentItem, value);
            try {
                currentItem.update();
            } catch (Exception e) {
                log.error("Error creating a metadatum value's authority", e);
            }
        }
        if (useCache) {
            cache.put(content, nextValue);
        }
    }
}
