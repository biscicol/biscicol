<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="ISO-8859-1" indent="yes" version="4.01" />

	<xsl:template match="/biscicol">
		<table>
			<xsl:apply-templates select="node" />
			<tr><th>distance</th><th>ID</th><th>type</th><th>date</th></tr>
			<xsl:apply-templates select="node//node[not(@expired)]">
				<xsl:sort select="count(ancestor::node)" />
				<xsl:sort select="@type" />
			</xsl:apply-templates>
		</table>
	</xsl:template>

	<xsl:template match="node">
		<tr>
			<td>
				<xsl:if test="not(ancestor::group[@relation='siblings'])">
					<xsl:value-of select="count(ancestor::node)" />
				</xsl:if>
			</td>
			<td><a href="{@uri}" target="_blank"><xsl:value-of select="@id" /></a></td>
			<td><xsl:value-of select="@type" /></td>
			<td><xsl:value-of select="@dateModified" /></td>
		</tr>
	</xsl:template>

</xsl:stylesheet>