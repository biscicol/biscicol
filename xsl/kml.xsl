<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.opengis.net/kml/2.2">
	
	<xsl:template match="/biscicol">
		<kml>
			<xsl:apply-templates />
		</kml>
	</xsl:template>

	<xsl:template match="node[@type='geo:SpatialThing']"> <!-- and not(@id=following::node[@type='geo:SpatialThing']/@id)]"> skip duplicates --> 
		<xsl:variable name="coords" select="substring-before(substring-after(@id, ':'), ';')" /> 		
		<xsl:variable name="latitude" select="substring-before($coords, ',')" /> 		
		<xsl:variable name="longitude" select="substring-after($coords, ',')" /> 		
		<xsl:variable name="errorRadius" select="substring-after(@id, ';u=')" /> 
		<xsl:variable name="parents"><xsl:call-template name="parents" /></xsl:variable>
				
		<Placemark>
			<name><xsl:value-of select="$parents" /></name>
			<description>Location referenced by:&lt;br&gt;'<xsl:value-of select="$parents" /> leadsTo <xsl:value-of select="@id" />'&lt;br&gt;maxerrorinmeters = <xsl:value-of select="$errorRadius" /></description>
			<Point><coordinates><xsl:value-of select="$longitude" />,<xsl:value-of select="$latitude" />,0</coordinates></Point>
		</Placemark>
		
	</xsl:template>

	<xsl:template name="parents">
		<xsl:variable name="relation" select="ancestor::group/@relation" />
		<xsl:choose>
			<xsl:when test="$relation='ancestors'">
				<xsl:apply-templates mode="ids" />
			</xsl:when>
			<xsl:when test="$relation='descendents'">
				<xsl:value-of select="ancestor::node[1]/@id" />
			</xsl:when>
			<xsl:when test=".. is /biscicol or $relation='siblings'">
				<xsl:apply-templates select="/biscicol/node/group[@relation='ancestors']/node" mode="ids" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@id" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="node" mode="ids">
		<xsl:value-of select="concat(@id, if (position()=last()) then '' else ', ')" />
	</xsl:template>
	
</xsl:stylesheet>