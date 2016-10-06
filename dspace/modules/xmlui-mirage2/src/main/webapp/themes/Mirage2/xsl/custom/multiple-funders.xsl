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
                xmlns="http://www.w3.org/1999/xhtml" xmlns:xls="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">

    <xsl:output indent="yes"/>

    <xsl:template match="dri:list[@id='aspect.submission.StepTransformer.list.submit-project' or @id='aspect.administrative.item.EditItemFundingForm.list.submit-project']">
        <fieldset id="aspect_submission_StepTransformer_list_submit-project" class="col ds-form-list">
            <xsl:apply-templates select="dri:head"/>
            <div class="row">
                <xsl:apply-templates select="dri:item[dri:field/@id='aspect.submission.StepTransformer.field.rioxxterms_funder' or dri:field/@id='aspect.submission.StepTransformer.field.rioxxterms_identifier_project'
                or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_funder' or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_identifier_project']"/>
            </div>

            <xsl:apply-templates select="dri:item[@id='aspect.submission.StepTransformer.item.project_funder_help' or @id='aspect.administrative.item.EditItemFundingForm.item.project_funder_help']"/>

            <xsl:apply-templates select="dri:item[dri:field/@id='aspect.submission.StepTransformer.field.submit_add' or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.submit_add']"/>
        </fieldset>
    </xsl:template>

    <xsl:template match="dri:list[@type='form']//dri:item[dri:field/@id='aspect.submission.StepTransformer.field.rioxxterms_funder' or dri:field/@id='aspect.submission.StepTransformer.field.rioxxterms_identifier_project'
    or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_funder' or dri:field/@id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_identifier_project']" priority="3">
        <div>
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class">
                    <xsl:text>ds-form-item col-xs-6</xsl:text>
                    <xsl:if test="contains(@id, 'aspect.submission.StepTransformer') or contains(@id,'aspect.administrative.item.EditItemFundingForm')">
                        <xsl:text>table </xsl:text>
                    </xsl:if>
                </xsl:with-param>
            </xsl:call-template>
            <div>
                <xsl:attribute name="class">
                    <xsl:text>control-group</xsl:text>
                    <xsl:if test="dri:field/dri:error">
                        <xsl:text> has-error</xsl:text>
                    </xsl:if>
                </xsl:attribute>
                <xsl:call-template name="pick-label"/>
                <xsl:apply-templates />
                <!-- special name used in submission UI review page -->
                <xsl:if test="@n = 'submit-review-field-with-authority'">
                    <xsl:call-template name="authorityConfidenceIcon">
                        <xsl:with-param name="confidence" select="substring-after(./@rend, 'cf-')"/>
                    </xsl:call-template>
                </xsl:if>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="addLookupButtonAuthor">
        <xsl:param name="isName" select="'missing value'"/>
        <button type="button" name="{concat('lookup_',@n)}" class="ds-button-field ds-add-button btn btn-default ">
            <xsl:attribute name="onClick">
                <xsl:text>javascript:AuthorLookup('</xsl:text>
                <!-- URL -->
                <xsl:value-of select="concat($context-path, '/choices/')"/>
                <xsl:choose>
                    <xsl:when test="starts-with(@n, 'value_')">
                        <xsl:value-of select="dri:params/@choices"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@n"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>', '</xsl:text>
                <xsl:value-of select="@n"/>
                <xsl:text>', </xsl:text>
                <!-- Collection ID for context -->
                <xsl:choose>
                    <xsl:when test="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='choice'][@qualifier='collection']">
                        <xsl:value-of select="/dri:document/dri:meta/dri:pageMeta/dri:metadata[@element='choice'][@qualifier='collection']"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>-1</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:text>);</xsl:text>
            </xsl:attribute>

            <xsl:choose>
                <xsl:when test="@id='aspect.submission.StepTransformer.field.rioxxterms_funder' or @id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_funder'">
                    <i18n:text>xmlui.ChoiceLookupTransformer.lookup_funder</i18n:text>
                </xsl:when>
                <xsl:when test="@id='aspect.submission.StepTransformer.field.rioxxterms_identifier_project' or @id='aspect.administrative.item.EditItemFundingForm.field.rioxxterms_identifier_project'">
                    <i18n:text>xmlui.ChoiceLookupTransformer.lookup_project</i18n:text>
                </xsl:when>
                <xsl:otherwise>
                    <i18n:text>xmlui.ChoiceLookupTransformer.lookup</i18n:text>
                </xsl:otherwise>
            </xsl:choose>
        </button>
    </xsl:template>



</xsl:stylesheet>
