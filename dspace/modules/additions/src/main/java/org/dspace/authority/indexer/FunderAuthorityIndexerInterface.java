package org.dspace.authority.indexer;

import java.util.*;
import org.dspace.authority.*;
import org.dspace.content.*;
import org.dspace.core.*;

/**
 * @author philip at atmire.com
 */
public class FunderAuthorityIndexerInterface implements AuthorityIndexerInterface {

    private Iterator<AuthorityValue> funderIterator;

    @Override
    public void init(Context context, Item item) {
        loadFunders(context);
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
