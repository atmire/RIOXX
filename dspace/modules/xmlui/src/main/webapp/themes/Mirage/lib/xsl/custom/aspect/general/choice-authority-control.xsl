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

    <xsl:template name="addLookupButtonAuthor">
        <xsl:param name="isName" select="'missing value'"/>
        <input type="button" name="{concat('lookup_',@n)}" class="ds-button-field ds-add-button" >
            <xsl:attribute name="value">
                <xsl:choose>
                    <xsl:when test="@id='aspect.submission.StepTransformer.field.rioxxterms_funder'">
                        <i18n:text>Lookup Funder</i18n:text>
                    </xsl:when>
                    <xsl:when test="@id='aspect.submission.StepTransformer.field.rioxxterms_identifier_project'">
                        <i18n:text>Lookup Project</i18n:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>Lookup</xsl:text>
                        <xsl:if test="contains(dri:params/@operations,'add')">
                            <xsl:text> &amp; Add</xsl:text>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
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
        </input>
    </xsl:template>

</xsl:stylesheet>
