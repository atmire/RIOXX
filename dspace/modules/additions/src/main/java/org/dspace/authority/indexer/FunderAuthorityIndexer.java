package org.dspace.authority.indexer;

import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.content.Item;
import org.dspace.core.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author philip at atmire.com
 */
public class FunderAuthorityIndexer implements AuthorityIndexerInterface {

    private Iterator<AuthorityValue> funderIterator;

    private static final int PAGE_SIZE = 1000;

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

        List<AuthorityValue> funders = new ArrayList<>();
        int page = 0;

        do {
            List<AuthorityValue> authorityValues = authorityValueFinder.findByAuthorityType(context, new FunderAuthorityValue().getAuthorityType(), page, PAGE_SIZE);
            funders.addAll(authorityValues);
            page++;
        }
        while (funders.size() == (page*PAGE_SIZE));

        funderIterator = funders.iterator();
    }
}
