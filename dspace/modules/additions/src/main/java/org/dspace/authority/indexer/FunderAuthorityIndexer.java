package org.dspace.authority.indexer;

import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.content.Item;
import org.dspace.core.Context;

import java.util.Iterator;
import java.util.List;

/**
 * @author philip at atmire.com
 */
public class FunderAuthorityIndexer implements AuthorityIndexerInterface {

    private Iterator<AuthorityValue> funderIterator;

    @Override
    public void init(Context context, Item item) {
        //This indexer should do nothing when called from the AuthorityConsumer
    }

    @Override
    public void init(Context context, boolean useCache) {
        loadFunders(context);
    }

    @Override
    public void init(Context context) {
        loadFunders(context);
    }

    @Override
    public AuthorityValue nextValue() {
        return funderIterator.next();
    }

    @Override
    public boolean hasMore() {
        return funderIterator != null && funderIterator.hasNext();
    }

    @Override
    public void close() {
        funderIterator = null;
    }

    @Override
    public boolean isConfiguredProperly() {
        return true;
    }

    private void loadFunders(Context context){
        AuthorityValueFinder authorityValueFinder = new AuthorityValueFinder();
        List<AuthorityValue> funders = authorityValueFinder.findByAuthorityType(context, new FunderAuthorityValue().getAuthorityType());
        funderIterator = funders.iterator();
    }
}
