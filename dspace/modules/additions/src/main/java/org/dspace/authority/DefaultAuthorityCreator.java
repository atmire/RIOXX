package org.dspace.authority;

import java.sql.*;
import org.apache.commons.lang.*;
import org.apache.log4j.*;
import org.dspace.authority.indexer.*;
import org.dspace.core.*;
import org.dspace.utils.*;

/**
 * Created by jonas - jonas@atmire.com on 09/03/16.
 */
public class DefaultAuthorityCreator {

    /* Log4j logger*/
    private static final Logger log =  Logger.getLogger(DefaultAuthorityCreator.class);

    public FunderAuthorityValue retrieveDefaultFunder(Context context) {
        String defaultFunder = ConfigurationManager.getProperty("authority.default.funder");
        if (StringUtils.isBlank(defaultFunder)) {
            defaultFunder = "Default funder";
        }

        String defaultFunderID = ConfigurationManager.getProperty("authority.default.funderID");
        if (StringUtils.isBlank(defaultFunderID)) {
            defaultFunderID = "10.99999/999999999";
        }
        AuthorityValue defaultFunderValue = new AuthorityValueFinder().findByFunderID(context, defaultFunderID);
        if (defaultFunderValue ==null) {
            FunderAuthorityValue funderAuthorityValue = FunderAuthorityValue.create();
            funderAuthorityValue.setValue(defaultFunder);
            funderAuthorityValue.setFunderID(defaultFunderID);
            AuthoritySolrServiceImpl solrService = (AuthoritySolrServiceImpl) new DSpace().getServiceManager().getServiceByName(AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);
            solrService.indexContent(funderAuthorityValue, true);
            solrService.commit();
            return funderAuthorityValue;
        } else {
            return (FunderAuthorityValue) defaultFunderValue;
        }


    }

    public FunderAuthorityValue retrieveDefaultFunder() {
        Context context = null;
        try {
            context= new Context();
            return retrieveDefaultFunder(context);
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (context!=null){
                context.abort();
            }
        }

        return  null;

    }

    public ProjectAuthorityValue retrieveDefaultProject(Context context) {
        String defaultProject = ConfigurationManager.getProperty("authority.default.project");
        if(StringUtils.isBlank(defaultProject)){
            defaultProject="Default project";
        }

        FunderAuthorityValue funderAuthorityValue = retrieveDefaultFunder(context);;

        ProjectAuthorityValue defaultProjectValue = (ProjectAuthorityValue) new AuthorityValueFinder().findByProjectIDAndFunderId(context,defaultProject, funderAuthorityValue.getId());
        if (defaultProjectValue==null) {
            ProjectAuthorityValue projectAuthorityValue = ProjectAuthorityValue.create();
            projectAuthorityValue.setValue(defaultProject);
            projectAuthorityValue.setFunderAuthorityValue(funderAuthorityValue);
            AuthoritySolrServiceImpl solrService = (AuthoritySolrServiceImpl) new DSpace().getServiceManager().getServiceByName(AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);
            solrService.indexContent(projectAuthorityValue, true);
            solrService.commit();
            return projectAuthorityValue;
        }else{
            return defaultProjectValue;
        }
    }
}
