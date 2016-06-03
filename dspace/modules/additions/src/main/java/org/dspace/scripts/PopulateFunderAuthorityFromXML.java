package org.dspace.scripts;

import org.dspace.authority.FunderXmlFileParser;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.DefaultAuthorityCreator;
import org.dspace.utils.DSpace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 24 Apr 2015
 */
public class PopulateFunderAuthorityFromXML extends PopulateAuthorityFromXML<FunderAuthorityValue> {

    public static void main(String[] args) {
        PopulateFunderAuthorityFromXML script = new PopulateFunderAuthorityFromXML();
        script.mainImpl(args);
    }

    @Override
    protected FunderAuthorityValue valueToIndex(AuthorityIndexingService indexingService, FunderAuthorityValue value) {
        FunderAuthorityValue valueToIndex = value;
        AuthorityValueFinder authorityValueFinder = new AuthorityValueFinder();
        AuthorityValue cachedRecord = authorityValueFinder.findByFunderID(null, value.getFunderID());

        if (cachedRecord instanceof FunderAuthorityValue) {
            if (cachedRecord.hasTheSameInformationAs(value)) {
                unchangedAuhtorityValues.add(value);
                valueToIndex = null;
            } else {
                FunderAuthorityValue record = (FunderAuthorityValue) cachedRecord;
                record.setValues(value);
                valueToIndex = record;
                updatedAuhtorityValues.add(record);
            }
        } else {
            newAuthorityValues.add(value);
        }
        return valueToIndex;
    }

    public void parseXML(File file) {
        validAuthorityValues = new ArrayList<>();
        invalidAuthorityValues = new ArrayList<>();
        FunderXmlFileParser funderXmlFileParser = new FunderXmlFileParser();
        funderXmlFileParser.setProgressWriter(print);
        List<FunderAuthorityValue> funderAuthorities = funderXmlFileParser.getFunderAuthorities(file);
        for (FunderAuthorityValue funderAuthority : funderAuthorities) {
            if (funderAuthority.isValid()) {
                validAuthorityValues.add(funderAuthority);
            } else {
                invalidAuthorityValues.add(funderAuthority);
            }
        }

        FunderAuthorityValue defaultFunder = new DSpace().getServiceManager().getServiceByName("defaultAuthorityCreator", DefaultAuthorityCreator.class).retrieveDefaultFunder();
        if (defaultFunder != null) {
            validAuthorityValues.add(defaultFunder);
        }
    }
}
