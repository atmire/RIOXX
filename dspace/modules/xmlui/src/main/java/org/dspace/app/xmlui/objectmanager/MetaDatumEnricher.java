package org.dspace.app.xmlui.objectmanager;

import org.dspace.content.Metadatum;
import org.dspace.core.Context;

import java.util.List;

/**
 * Class that will enrich the metadata of an item
 */
public interface MetaDatumEnricher {

    void enrichMetadata(Context context, List<Metadatum> metadataList);

}