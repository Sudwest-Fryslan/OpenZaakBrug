<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:cf="custom-functions">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
	<xsl:strip-space elements="*"/>
	
	<xsl:template match="/Report">
		<xsl:copy>
			<!-- Select all report attributes -->
			<!-- <xsl:apply-templates select="@*"/> -->

			<!-- Select the report name attribute -->
			<!-- <xsl:apply-templates select="@Name"/> -->

			<!-- Select all report attributes except the name attribute -->
			<!-- <xsl:apply-templates select="@*[local-name() != 'Name']"/> -->

			<!-- For comparing reports it's better not to select (most of) the report attributes (and not enable the -->
			<!-- examples above) because (most of) the report attributes change on every rerun or can be adjusted by -->
			<!-- the user in between runs (e.g. when renaming a report) -->
	
			<!-- Select the first and last checkpoint -->
			<!-- <xsl:apply-templates select="Checkpoint[1]"/> -->
			<!-- <xsl:apply-templates select="Checkpoint[last()]"/> -->
	
			<!-- Select all checkpoints -->
			<xsl:apply-templates select="node()"/>

			<!-- Select the checkpoint with name "Pipe Example" -->
			<!-- <xsl:apply-templates select="Checkpoint[@Name='Pipe Example']"/> -->
		</xsl:copy>
	</xsl:template>

	<!-- Ignore content of checkpoint referentienummer that contains a UUID like ozb-a71a7abb-8fb7-4466-9328-b7502eb90d68 -->
	<xsl:template match="*[local-name()='Checkpoint' and @Name='referentienummer' and @Type='Infopoint' and @Level='1']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:choose>
				<xsl:when test="string-length(.) > 10"><xsl:value-of select="'IGNORED'"/></xsl:when>
				<xsl:otherwise>
					<!-- Add timestamp to make test fail and inform user to adjust this xslt -->
					<xsl:value-of select="concat('[', current-dateTime(), ' WRONG referentienummer or transform-ladybug-report.xslt needs to be adjusted for value: ', ., ']')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<!-- Ignore content of element referentienummer that contains a UUID like ozb-a71a7abb-8fb7-4466-9328-b7502eb90d68-->
	<xsl:template match="*[local-name()='referentienummer']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:choose>
				<xsl:when test="string-length(.) > 10"><xsl:value-of select="'IGNORED'"/></xsl:when>
				<xsl:otherwise>
					<!-- Add timestamp to make test fail and inform user to adjust this xslt -->
					<xsl:value-of select="concat('[', current-dateTime(), ' WRONG referentienummer or transform-ladybug-report.xslt needs to be adjusted for value: ', ., ']')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<!-- Ignore content of element identificatie that contains an id like 190028 or can be empty -->
	<xsl:template match="*[local-name()='identificatie']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:choose>
				<xsl:when test="string-length(.) = 0"/>
				<xsl:when test="string-length(.) > 4"><xsl:value-of select="'IGNORED'"/></xsl:when>
				<xsl:otherwise>
					<!-- Add timestamp to make test fail and inform user to adjust this xslt -->
					<xsl:value-of select="concat('[', current-dateTime(), ' WRONG identificatie or transform-ladybug-report.xslt needs to be adjusted for value: ', ., ']')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<!-- Ignore content of element tijdstipBericht that contains a timestamp like 20201207224233 -->
	<xsl:template match="*[local-name()='tijdstipBericht']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:value-of select="substring(., 1, 2)"/>
			<xsl:value-of select="'IGNORED'"/>
			<xsl:value-of select="string-length(.) - 2"/>
			<xsl:value-of select="'CHARS'"/>
		</xsl:copy>
	</xsl:template>

	<!-- Ignore hostname in checkpoint url that will change when switching backend between fieldlab and local -->
	<xsl:template match="*[local-name()='Checkpoint' and @Name='url' and @Type='Inputpoint' and @Level='2']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:value-of select="replace(replace(replace(replace(replace(replace(replace(., 'fieldlab.westeurope.cloudapp.azure.com', 'IGNORED'), 'fieldlab.westeurope.cloudapp.azure.com', 'IGNORED'), 'openzaak.local', 'IGNORED'), 'test.openzaak.nl', 'IGNORED'), 'localhost:8000', 'IGNORED'), 'localhost', 'IGNORED'), 'https://', 'http://')"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- Ignore time taken in checkpoint "Total duration" that will change based upon the duration of handling the complete translation -->
	<xsl:template match="*[local-name()='Checkpoint' and @Name='Total duration' and @Type='Infopoint' and @Level='1']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:value-of select="'IGNORED'"/>
		</xsl:copy>
	</xsl:template>	

	<!-- Ignore time taken in checkpoint "Duration" that will change based upon the duration of a request to a client -->
	<xsl:template match="*[local-name()='Checkpoint' and @Name='Duration' and @Type='Infopoint' and @Level='1']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:value-of select="'IGNORED'"/>
		</xsl:copy>
	</xsl:template>	

	<!-- Ignore time taken in checkpoint "Duration" that will change based upon the duration of a request to a client -->
	<xsl:template match="*[local-name()='Checkpoint' and @Name='Warning' and @Type='Infopoint' and @Level='1']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:value-of select="'IGNORED'"/>
		</xsl:copy>
	</xsl:template>	

	<!-- Ignore content of checkpoint kenmerk that contains an id like zaakidentificatie:8000361, documentidentificatie:8000325 and bsn:111111110 -->
	<xsl:template match="*[local-name()='Checkpoint' and @Name='kenmerk' and @Type='Outputpoint' and @Level='1']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:if test="contains(.,':')">
				<xsl:value-of select="concat(substring-before(., ':'), ':')"/>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="substring-before(., ':') = 'zaakidentificatie'"><xsl:value-of select="'IGNORED'"/></xsl:when>
				<xsl:when test="substring-before(., ':') = 'bsn'"><xsl:value-of select="'IGNORED'"/></xsl:when>
				<xsl:when test="substring-before(., ':') = 'documentidentificatie'"><xsl:value-of select="'IGNORED'"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<!-- IGNORE datumStatusGezet-->
	<xsl:template match="*[local-name()='Checkpoint' and @Name='ZGWClient POST' and @Type='Startpoint']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:choose>
				<xsl:when test="contains(.,'datumStatusGezet') and contains(., 'statustoelichting')">
					<xsl:value-of select="concat(substring-before(., 'datumStatusGezet'), 'datumStatusGezet&quot;: &quot;IGNORED&quot;,', '&quot;statustoelichting&quot;', substring-after(., 'statustoelichting'))"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="."/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<!-- Ignore java version like 11.0.2 and 15 -->
	<xsl:template match="*[local-name()='java' and @class='java.beans.XMLDecoder']">
		<xsl:copy>
			<xsl:attribute name="version"><xsl:value-of select="'IGNORED'"/></xsl:attribute>
			<xsl:apply-templates select="node() | @*[local-name() != 'version']"/>
		</xsl:copy>
	</xsl:template>

	<!-- Ignore line numbers and Windows newlines in stacktrace -->
	<xsl:template match="*[local-name()='details']">
		<xsl:copy>
			<!-- Shorten to 900 because RequestHandler shortens to 1000 which will give different last chars on Windows as Windows uses two chars for newlines -->
			<xsl:value-of select="substring(cf:ignoreHelpfulNpeMessage(replace(replace(., '\r', ''), '.java:.*\)', '.java:IGNORED)')), 0, 900)"/>
		</xsl:copy>
	</xsl:template>

	<!--  Ignore difference in NPE message between java versions before version 14 and version 14 and onwards -->
	<xsl:template match="*[local-name()='faultstring' or local-name()='omschrijving']">
		<xsl:copy>
			<xsl:value-of select="cf:ignoreHelpfulNpeMessage(.)"/>
		</xsl:copy>
	</xsl:template>
	<xsl:function name="cf:ignoreHelpfulNpeMessage">
			<xsl:param name="message"/>
			<xsl:value-of select="replace($message, 'java.lang.NullPointerException.*', 'java.lang.NullPointerExceptionIGNORED')"/>
	</xsl:function>

	<!-- General template -->
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
