<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" encoding="ISO-8859-1" indent="yes" version="4.01" />

	<xsl:template match="/biscicol">
		<p>	ID: <strong><a href="{node/@uri}" target="_blank"><xsl:value-of select="node/@id" /></a></strong>, 
			type: <strong><xsl:value-of select="node/@type" /></strong>
			<xsl:if test="node/@dateModified">
				, date: <strong><xsl:value-of select="node/@dateModified" /></strong>
			</xsl:if>
		</p>
		<xsl:for-each-group select="node//node[not(@expired)]" group-by="@type">
			<xsl:sort select="current-grouping-key()" />
			<h1><xsl:value-of select="current-grouping-key()" /> (<xsl:value-of select="count(current-group())" />)</h1>
			<div><table>
				<tr><th>distance</th><th>ID</th><th>date</th></tr>
				<xsl:for-each select="current-group()">				
					<xsl:sort select="@id" />
					<tr>
						<td>
							<xsl:if test="not(ancestor::group[@relation='siblings'])">
								<xsl:value-of select="count(ancestor::node)" />
							</xsl:if>
						</td>
						<td><a href="{@uri}" target="_blank"><xsl:value-of select="@id" /></a></td>
						<td><xsl:value-of select="@dateModified" /></td>
					</tr>
				</xsl:for-each>
			</table></div>
		</xsl:for-each-group>
	</xsl:template>
	
</xsl:stylesheet>