package org.dspace.app.xmlui.objectmanager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.project.ProjectService;
import org.dspace.utils.DSpace;

import java.util.LinkedList;
import java.util.List;

/**
 * Metadata enricher that will add the Rioxx Project - Funder relation to the metadata
 */
public class RioxxProjectFunderEnricher implements MetaDatumEnricher {

    private static final String RIOXX_SHEMA = "rioxxterms";
    private static final String IDENTIFIER_ELEMENT = "identifier";
    private static final String PROJECT_QUALIFIER = "project";
    private static final String FUNDER_ELEMENT = "funder";


    private ProjectService projectService = null;

    private ProjectService getProjectService() {
        if(projectService == null) {
            projectService = new DSpace().getServiceManager().getServiceByName("ProjectService", ProjectService.class);
        }

        return projectService;
    }

    public void enrichMetadata(final Context context, final List<Metadatum> metadataList) {
        if(CollectionUtils.isNotEmpty(metadataList)) {
            List<Metadatum> newMetaData = new LinkedList<>();
            //For all RIOXX project identifiers
            for (Metadatum metadatum : metadataList) {
                if(StringUtils.equals(RIOXX_SHEMA, metadatum.schema)
                        && StringUtils.equals(IDENTIFIER_ELEMENT, metadatum.element)
                        && StringUtils.equals(PROJECT_QUALIFIER, metadatum.qualifier)) {

                    //Check if we can find the corresponding Funder Authority
                    ProjectAuthorityValue authorityValue = getProjectService().getProjectByAuthorityId(context, metadatum.authority);
                    if(authorityValue != null && authorityValue.getFunderAuthorityValue() != null) {
                        String language = metadatum.language;
                        int confidence = metadatum.confidence;
                        //If we do, create a new "fake" metadata value that contains the link between the project and the funder
                        Metadatum newMetadatum = createProjectFunderRelationMetadatumRecord(authorityValue, language, confidence);

                        newMetaData.add(newMetadatum);
                    }
                }
            }
            metadataList.addAll(newMetaData);
        }
    }

    public static Metadatum createProjectFunderRelationMetadatumRecord(final ProjectAuthorityValue authorityValue, final String language, final int confidence) {
        Metadatum newMetadatum = new Metadatum();
        newMetadatum.schema = RIOXX_SHEMA;
        newMetadatum.element = FUNDER_ELEMENT;
        newMetadatum.qualifier = PROJECT_QUALIFIER;

        //Store the funder authority in the authority field
        newMetadatum.authority = authorityValue.getFunderAuthorityValue().getId();
        newMetadatum.confidence = confidence;
        newMetadatum.language = language;

        //Store the project authority in the value field
        newMetadatum.value = authorityValue.getId();
        return newMetadatum;
    }

}