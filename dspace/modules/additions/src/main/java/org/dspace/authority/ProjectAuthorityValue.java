package org.dspace.authority;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.core.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 02/09/15
 * Time: 11:29
 */
public class ProjectAuthorityValue extends AuthorityValue {
    private static Logger log = Logger.getLogger(ProjectAuthorityValue.class);

    private FunderAuthorityValue funderAuthorityValue;

    public FunderAuthorityValue getFunderAuthorityValue() {
        return funderAuthorityValue;
    }

    public void setFunderAuthorityValue(FunderAuthorityValue funderAuthorityValue) {
        this.funderAuthorityValue = funderAuthorityValue;
    }

    public static ProjectAuthorityValue create() {
        ProjectAuthorityValue projectAuthorityValue = new ProjectAuthorityValue();
        projectAuthorityValue.setId(UUID.randomUUID().toString());
        projectAuthorityValue.updateLastModifiedDate();
        projectAuthorityValue.setCreationDate(new Date());
        projectAuthorityValue.setField("rioxxterms_identifier_project");
        return projectAuthorityValue;
    }

    @Override
    public SolrInputDocument getSolrInputDocument() {
        SolrInputDocument doc = super.getSolrInputDocument();
        if (funderAuthorityValue!=null) {
            doc.addField("label_funder_authority_ID", funderAuthorityValue.getId());
        }

        return doc;
    }

    @Override
    public void setValues(SolrDocument document) {
        super.setValues(document);
        ArrayList list;
        list = ((ArrayList) document.getFieldValue("label_funder_authority_ID"));
        if (list != null && !list.isEmpty()) {
            Context context = null;
            try {
                context = new Context();
                String funderAuthorityID = ObjectUtils.toString(list.get(0));

                AuthorityValueFinder finder = new AuthorityValueFinder();
                AuthorityValue funder = finder.findByUID(context, funderAuthorityID);

                if(funder!=null && funder.getAuthorityType().equals("funder")){
                    funderAuthorityValue = (FunderAuthorityValue) funder;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            finally {
                if(context!=null) {
                    context.abort();
                }
            }
        }
    }

    @Override
    public Map<String, String> choiceSelectMap() {
        Map<String, String> map = super.choiceSelectMap();

        if (StringUtils.isNotBlank(getValue())) {
            map.put("Project", getValue());
        }

        if (funderAuthorityValue!=null && StringUtils.isNotBlank(funderAuthorityValue.getValue())) {
            map.put("Funder", funderAuthorityValue.getValue());
        }
        return map;
    }

    @Override
    public String getAuthorityType() {
        return "project";
    }

    @Override
    public boolean hasTheSameInformationAs(Object o) {
        return equals(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof FunderAuthorityValue))
            return false;

        ProjectAuthorityValue that = (ProjectAuthorityValue) o;

        if (getValue() != null ? !getValue().equals(that.getValue()) : that.getValue() != null) {
            return false;
        }

        return funderAuthorityValue.equals(that.funderAuthorityValue);
    }

    @Override
    public int hashCode() {
        return getValue() != null ? getValue().hashCode() : 0;
    }
}
