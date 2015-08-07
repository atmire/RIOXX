package org.dspace.authority;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.util.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 23 Apr 2015
 */
public class FunderAuthorityValue extends AuthorityValue {

    private String funderID;
    private String fundingBodyType;
    private String fundingBodySubType;
    private List<String> nameVariants = new ArrayList<String>();

    public FunderAuthorityValue() {
    }

    public FunderAuthorityValue(SolrDocument document) {
        super(document);
    }

    public String getFunderID() {
        return funderID;
    }

    public void setFunderID(String funderID) {
        this.funderID = funderID;
    }

    public List<String> getNameVariants() {
        return nameVariants;
    }

    public void setNameVariants(List<String> nameVariants) {
        this.nameVariants = nameVariants;
    }

    public void addNameVariant(String name) {
        if (StringUtils.isNotBlank(name)) {
            nameVariants.add(name);
        }
    }

    public String getFundingBodyType() {
        return fundingBodyType;
    }

    public void setFundingBodyType(String fundingBodyType) {
        this.fundingBodyType = fundingBodyType;
    }

    public String getFundingBodySubType() {
        return fundingBodySubType;
    }

    public void setFundingBodySubType(String fundingBodySubType) {
        this.fundingBodySubType = fundingBodySubType;
    }

    @Override
    public SolrInputDocument getSolrInputDocument() {
        SolrInputDocument doc = super.getSolrInputDocument();
        if (StringUtils.isNotBlank(funderID)) {
            doc.addField("label_funderID", funderID);
        }
        if (StringUtils.isNotBlank(fundingBodyType)) {
            doc.addField("label_fundingBodyType", fundingBodyType);
        }
        if (StringUtils.isNotBlank(fundingBodySubType)) {
            doc.addField("label_fundingBodySubType", fundingBodySubType);
        }
        for (String nameVariant : getNameVariants()) {
            doc.addField("name_variant", nameVariant);
        }
        return doc;
    }

    @Override
    public void setValues(SolrDocument document) {
        super.setValues(document);
        ArrayList list;
        list = ((ArrayList) document.getFieldValue("label_funderID"));
        if (list != null && !list.isEmpty()) {
            this.funderID = ObjectUtils.toString(list.get(0));
        }
        list = ((ArrayList) document.getFieldValue("label_fundingBodyType"));
        if (list != null && !list.isEmpty()) {
            this.fundingBodyType = ObjectUtils.toString(list.get(0));
        }
        list = ((ArrayList) document.getFieldValue("label_fundingBodySubType"));
        if (list != null && !list.isEmpty()) {
            this.fundingBodySubType = ObjectUtils.toString(list.get(0));
        }

        this.nameVariants = new ArrayList<String>();
        Collection<Object> document_name_variant = document.getFieldValues("name_variant");
        if (document_name_variant != null) {
            for (Object name_variants : document_name_variant) {
                addNameVariant(String.valueOf(name_variants));
            }
        }
    }

    @Override
    public Map<String, String> choiceSelectMap() {
        Map<String, String> map = super.choiceSelectMap();

        if (StringUtils.isNotBlank(getValue())) {
            map.put("Name", getValue());
        }

        if (StringUtils.isNotBlank(getFunderID())) {
            map.put("ID", getFunderID());
        }
        return map;
    }


    @Override
    public String getAuthorityType() {
        return "funder";
    }

    public String generateString() {
        return AuthorityValueGenerator.GENERATE + getAuthorityType() + AuthorityValueGenerator.SPLIT + getValue();
        // the part after "AuthorityValueGenerator.GENERATE + getAuthorityType() + AuthorityValueGenerator.SPLIT" is the value of the "info" parameter in public AuthorityValue newInstance(String info)
    }

    @Override
    public AuthorityValue newInstance(String info) {
        FunderAuthorityValue authorityValue = FunderAuthorityValue.create();
        authorityValue.setValue(info);
        // external retrieval of information not implemented
        return authorityValue;
    }

    public static FunderAuthorityValue create() {
        FunderAuthorityValue authorityValue = new FunderAuthorityValue();
        authorityValue.setId(UUID.randomUUID().toString());
        authorityValue.updateLastModifiedDate();
        authorityValue.setCreationDate(new Date());
        authorityValue.setField("rioxxterms_funder");
        return authorityValue;
    }

    @Override
    public String toString() {
        return "FunderAuthorityValue{" +
                "funderID='" + funderID + '\'' +
                ", fundingBodyType='" + fundingBodyType + '\'' +
                ", fundingBodySubType='" + fundingBodySubType + '\'' +
                ", nameVariants=" + nameVariants +
                "} " + super.toString();
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

        FunderAuthorityValue that = (FunderAuthorityValue) o;

        return new EqualsBuilder()
                .append(getValue(), that.getValue())
                .append(getFunderID(), that.getFunderID())
                .append(getNameVariants(), that.getNameVariants())
                .append(getFundingBodyType(), that.getFundingBodyType())
                .append(getFundingBodySubType(), that.getFundingBodySubType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getValue())
                .append(getFunderID())
                .append(getNameVariants())
                .append(getFundingBodyType())
                .append(getFundingBodyType())
                .toHashCode();
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(getValue()) && StringUtils.isNotBlank(getFunderID());
    }

    public void setValues(FunderAuthorityValue value) {
        setFunderID(value.getFunderID());
        setFundingBodyType(value.getFundingBodyType());
        setFundingBodySubType(value.getFundingBodySubType());
        setNameVariants(value.getNameVariants());
        setValue(value.getValue());
    }
}
