<?xml version="1.0" encoding="UTF-8"?>
<!--


    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

	Developed by DSpace @ Lyncode <dspace@lyncode.com>
	Following Driver Guidelines 2.0:
		- http://www.driver-support.eu/managers.html#guidelines

 -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:doc="http://www.lyncode.com/xoai">
	<xsl:output indent="yes" method="xml" omit-xml-declaration="yes" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

    <!-- Formatting dc.date.issued -->
    <xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='issued']/doc:element/doc:field/text()">
        <xsl:call-template name="formatdate">
            <xsl:with-param name="datestr" select="." />
        </xsl:call-template>
    </xsl:template>

    <!-- Formatting dc.date.available -->
    <xsl:template match="/doc:metadata/doc:element[@name='dc']/doc:element[@name='date']/doc:element[@name='available']/doc:element/doc:field/text()">
        <xsl:call-template name="formatdate">
            <xsl:with-param name="datestr" select="." />
        </xsl:call-template>
    </xsl:template>

    <!-- Formatting dx.doi.org -->
    <xsl:template match="/doc:metadata/doc:element[@name='rioxxterms']/doc:element[@name='versionofrecord']/doc:element/doc:field/text()">
        <xsl:choose>
            <xsl:when test="starts-with(.,'http')">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat('http://dx.doi.org/',.)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="formatdate">
        <xsl:param name="datestr" />
        <xsl:variable name="sub">
            <xsl:value-of select="substring($datestr,1,10)" />
        </xsl:variable>
        <xsl:value-of select="$sub" />
    </xsl:template>

    <xsl:template match="doc:metadata/doc:element[@name='dc']/doc:element[@name='type']/doc:element/doc:field[@name='value']/text()">
        <xsl:variable name="value">
            <xsl:value-of select="."/>
        </xsl:variable>

        <!--DSpace types-->
        <xsl:choose>
            <xsl:when test="$value = 'Book'">
                <xsl:text>Book</xsl:text>
            </xsl:when>
            <xsl:when test="$value = 'Book chapter'">
                <xsl:text>Book chapter</xsl:text>
            </xsl:when>
            <xsl:when test="$value = 'Article'">
                <xsl:text>Journal Article/Review</xsl:text>
            </xsl:when>
            <xsl:when test="$value = 'Technical Report'">
                <xsl:text>Technical Report</xsl:text>
            </xsl:when>
            <xsl:when test="$value = 'Thesis'">
                <xsl:text>Thesis</xsl:text>
            </xsl:when>
            <xsl:when test="$value = 'Working Paper'">
                <xsl:text>Working paper</xsl:text>
            </xsl:when>


            <xsl:when test="contains($nonSuppressedTypes,$value)">
               <xsl:value-of select="$value"/>
            </xsl:when>

            <xsl:otherwise>
                <xsl:text>Other</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']"/>

    <xsl:template match="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle'][descendant-or-self::doc:element[@name='bitstream']/doc:field[@name='primary']='true' or child::doc:field[@name='name']/text()='LICENSE']">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']/doc:element[@name='bitstreams']/doc:element[@name='bitstream']"/>

    <xsl:template match="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle']/doc:element[@name='bitstreams']/doc:element[@name='bitstream'][descendant::doc:field[@name='primary']='true']">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="doc:metadata/doc:element[@name='bundles']/doc:element[@name='bundle'][child::doc:field[@name='name']/text()='LICENSE']/doc:element[@name='bitstreams']/doc:element[@name='bitstream']">
        <xsl:copy-of select="."/>
    </xsl:template>

    <!--RIOXX types without DSpace equivalent-->
    <xsl:variable name="nonSuppressedTypes">
        Book edited|Conference Paper/Proceeding/Abstract|Review|Manual/Guide|Monograph|Policy briefing report|Technical Standard|Consultancy Report|Working paper
    </xsl:variable>
</xsl:stylesheet>

