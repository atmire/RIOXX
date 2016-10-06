<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->

<!--
    Rendering of the authority control related pages.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->

<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                xmlns:dri="http://di.tamu.edu/DRI/1.0/"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:xlink="http://www.w3.org/TR/xlink/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">

    <xsl:output indent="yes"/>

    <xsl:template match="dri:list[@id='aspect.submission.StepTransformer.list.submit-project' or @id='aspect.administrative.item.EditItemFundingForm.list.submit-project']">
        <fieldset id="aspect_submission_StepTransformer_list_submit-project" class="col ds-form-list">
            <xsl:apply-templates select="dri:head"/>
            <xsl:apply-templates select="dri:item[dri:field/@id='aspect.submission.StepTransformer.field.rioxxterms_funder' or dri:field/@id='aspect.submission.StepTransformer.field.rioxxterms_identifier_project'
                or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_funder' or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_identifier_project']"/>

            <br/>

            <xsl:apply-templates select="dri:item[@id='aspect.submission.StepTransformer.item.project_funder_help' or @id='aspect.administrative.item.EditItemFundingForm.item.project_funder_help']"/>

            <br/>

            <xsl:apply-templates select="dri:item[dri:field/@id='aspect.submission.StepTransformer.field.submit_add' or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.submit_add']"/>
        </fieldset>
    </xsl:template>

</xsl:stylesheet>
