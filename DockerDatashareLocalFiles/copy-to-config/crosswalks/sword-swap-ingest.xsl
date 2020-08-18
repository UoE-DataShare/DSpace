<?xml version="1.0" encoding="utf-8"?>
<!-- sord-swap-ingest.xsl
 * 
 * Copyright (c) 2007, Aberystwyth University
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above
 *    copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 *  - Neither the name of the Centre for Advanced Software and
 *    Intelligent Systems (CASIS) nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 -->

<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
        xmlns:epdcx="http://purl.org/eprint/epdcx/2006-11-16/"
        version="1.0">

<!-- NOTE: This stylesheet is a work in progress, and does not
     cover all aspects of the SWAP and EPDCX specification/schema.
     It is used principally to demonstrate the SWORD ingest
     process -->

<!-- This stylesheet converts incoming DC metadata in a SWAP
     profile into the DSpace Interal Metadata format (DIM) -->

	<!-- Catch all.  This template will ensure that nothing
	     other than explicitly what we want to xwalk will be dealt
	     with -->
	<xsl:template match="text()"></xsl:template>
    
    <!-- match the top level descriptionSet element and kick off the
         template matching process -->
    <xsl:template match="/epdcx:descriptionSet">
    	<dim:dim>
    		<xsl:apply-templates/>
    	</dim:dim>
    </xsl:template>
    
    <!-- general matcher for all "statement" elements -->
    <xsl:template match="/epdcx:descriptionSet/epdcx:description/epdcx:statement">

    <!-- Depositor -->
    <!-- Automatically populated -->

    <!-- Funder -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/contributor'">
      <dim:field mdschema="dc" element="contributor" qualifier="other">
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Spatial Coverage -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/spatial'">
      <dim:field mdschema="dc" element="coverage" qualifier="spatial" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Time Period -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/temporal'">
      <dim:field mdschema="dc" element="coverage" qualifier="temporal" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Data Creator -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/creator'">
      <dim:field mdschema="dc" element="creator">
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Embargo -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/available'">
      <dim:field mdschema="dc" element="date" qualifier="embargo" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Dataset Description (abstract) -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/abstract'">
      <dim:field mdschema="dc" element="description" qualifier="abstract" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Dataset Description (TOC) -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/tableOfContents'">
      <dim:field mdschema="dc" element="description" qualifier="tableofcontents" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Language -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/language'">
      <dim:field mdschema="dc" element="language" qualifier="iso" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Data Publisher -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/publisher'">
      <dim:field mdschema="dc" element="publisher">
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Relation (Is Version Of) -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/isVersionOf'">
      <dim:field mdschema="dc" element="relation" qualifier="isversionof" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Relation (Is Referenced By) -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/isReferencedBy'">
      <dim:field mdschema="dc" element="relation" qualifier="isreferencedby" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Supercedes -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/replaces'">
      <dim:field mdschema="dc" element="relation" qualifier="replaces" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Rights -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/rights'">
      <dim:field mdschema="dc" element="rights"  >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Source -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/source'">
      <dim:field mdschema="dc" element="source"  >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Subject Keywords -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/subject'">
      <dim:field mdschema="dc" element="subject" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Subject Classification TDB ?? -->
    <!--
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/subject'">
      <dim:field mdschema="dc" element="subject" >
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>-->

    <!-- Title -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/title'">
      <dim:field mdschema="dc" element="title">
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Alternative Title -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/terms/alternative'">
      <dim:field mdschema="dc" element="title" qualifier="alternative">
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

    <!-- Type -->
    <xsl:if test="./@epdcx:propertyURI='http://purl.org/dc/elements/1.1/type'">
      <dim:field mdschema="dc" element="type">
    	<xsl:value-of select="epdcx:valueString"/>
      </dim:field>
    </xsl:if>

  </xsl:template>

</xsl:stylesheet>
