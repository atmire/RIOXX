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


    <xsl:template name="formatdate">
        <xsl:param name="datestr" />
        <xsl:variable name="sub">
            <xsl:value-of select="substring($datestr,1,10)" />
        </xsl:variable>
        <xsl:value-of select="$sub" />
    </xsl:template>


</xsl:stylesheet>
