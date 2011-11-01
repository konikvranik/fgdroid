<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


	<xsl:template match="/">
		<menu>
			<xsl:attribute name="time">
		<xsl:value-of
				select="/html/body//div[@class='vnitrek-tisk' and position() = 1]/p[1]/b" />
		</xsl:attribute>
			<xsl:apply-templates
				select="//table[@class='tb_jidelak' and position() = 1]/tbody/tr[1]/td"
				mode="days" />
		</menu>
	</xsl:template>

	<xsl:template match="td" mode="days">
		<xsl:variable name="pos" select="position()" />
		<day position="{$pos}" name="{normalize-space(.)}">
			<xsl:apply-templates
				select="../../tr[2]/td[$pos]/p[normalize-space(translate(.,'&#160;', ' ')) != '']"
				mode="soup" />
			<xsl:apply-templates
				select="../../tr[position() &gt; 2]/td[$pos = position() and normalize-space(translate(.,'&#160;', ' ')) != '']"
				mode="normal" />
			<xsl:apply-templates
				select="../../../following-sibling::table[@class='tb_jidelak' and ( position() = 1)]/tbody/tr[2]/td[$pos = position() and normalize-space(translate(.,'&#160;', ' ')) != '']"
				mode="superior" />
			<xsl:apply-templates
				select="../../../following-sibling::table[@class='tb_jidelak' and (position() = 2 )]/tbody/tr[2]/td[$pos = position() and normalize-space(translate(.,'&#160;', ' ')) != '']"
				mode="live" />
			<xsl:apply-templates
				select="../../../following-sibling::table[@class='tb_jidelak' and position() = 3]/tbody/tr[2]/td[normalize-space(translate(.,'&#160;', ' ')) != '']"
				mode="pasta" />

		</day>
	</xsl:template>

	<xsl:template match="td" mode="normal">
		<jidlo position="{position()}" type="normal">
			<xsl:value-of select="." />
		</jidlo>
	</xsl:template>

	<xsl:template match="td/p" mode="soup">
		<jidlo position="{position()}" type="soup">
			<xsl:value-of select="." />
		</jidlo>
	</xsl:template>

	<xsl:template match="td" mode="superior">
		<jidlo position="{position()}" type="superior">
			<xsl:value-of select="." />
		</jidlo>
	</xsl:template>

	<xsl:template match="td" mode="live">
		<jidlo position="{position()}" type="live">
			<xsl:value-of select="." />
		</jidlo>
	</xsl:template>

	<xsl:template match="td" mode="pasta">
		<jidlo position="{position()}" type="pasta">
			<xsl:value-of select="." />
		</jidlo>
	</xsl:template>
</xsl:stylesheet>