<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="ISO-8859-1" indent="yes" version="4.01" />

	<xsl:template match="node[not(@expired) or node or group]">
		<li class="{if (group/node) then 'jstree-open' else ''}{if (@expired) then ' expired' else ''}">
			<a target="_blank">
				<xsl:if test="not(@expired)">
					<xsl:attribute name="href"><xsl:value-of select="@uri" /></xsl:attribute>
				</xsl:if>
				<xsl:value-of select="concat(@id, ' (', @type, if (@dateModified) then '; ' else '', @dateModified, ')')" />
			</a>
			<ul>
				<xsl:apply-templates />
			</ul>
		</li>
	</xsl:template>

	<xsl:template match="group[/biscicol/@queryType='relations' and node[not(@expired) or node]]">
		<li>
			<xsl:value-of select="@relation" />
			<ul>
				<xsl:apply-templates />
			</ul>
		</li>
	</xsl:template>
	
</xsl:stylesheet>