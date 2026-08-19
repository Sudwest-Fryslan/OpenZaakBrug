/*
 * Copyright 2020-2021, 2026 The Open Zaakbrug Contributors
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package nl.haarlem.translations.zdstozgw;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZGWClient;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZgwAuthorization;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwCatalogus;

// Tijdelijke, handmatige end-to-end-test: bevestigt dat OpenZaakBrug daadwerkelijk werkt tegen een
// Open Zaak zonder expand-ondersteuning (versie <1.11.0, zoals SWF's eigen productie op 1.9.1).
// Draait tegen een lokale Docker-instantie (open-zaak/open-zaak:1.9.1) die niet standaard beschikbaar is,
// vandaar @Ignore - handmatig te draaien met de juiste instantie op localhost:18010.
// Zie C:\Users\e.witteveen\.claude\projects\...\memory\feature-expand-branch-status.md voor de
// volledige achtergrond (gemma-zaken#2491, de HTTP 400 "Onbekende query parameters: expand"-vondst).
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
		"zgw.registry.zaken.url=http://localhost:18010/zaken",
		"zgw.registry.documenten.url=http://localhost:18010/documenten",
		"zgw.registry.catalogi.url=http://localhost:18010/catalogi"
})
public class ExpandFallbackLiveTest {

	@Autowired
	ZaakService zaakService;

	@Autowired
	ZGWClient zgwClient;

	@Ignore("Handmatig te draaien tegen een lokale Open Zaak 1.9.1 op localhost:18010 - zie klasse-Javadoc")
	@Test
	public void getZaakDetailsByIdentificatie_tegenOpenZaakZonderExpand_valtTerugOpLosseAanroepen() {
		String baseUrl = "http://localhost:18010";
		String jwtIssuer = "ozb-test";
		String jwtSecret = "ozbtestsecret1234567890";

		ZgwAuthorization authorization = new ZgwAuthorization();
		authorization.AddZgwAuthorization(baseUrl + "/zaken", null, jwtIssuer, jwtSecret, jwtIssuer);
		authorization.AddZgwAuthorization(baseUrl + "/documenten", null, jwtIssuer, jwtSecret, jwtIssuer);
		authorization.AddZgwAuthorization(baseUrl + "/catalogi", null, jwtIssuer, jwtSecret, jwtIssuer);

		ZgwCatalogus catalogus = zgwClient.getCatalogusByRsin(authorization, "001005650");
		Assert.assertNotNull("Catalogus met rsin 001005650 niet gevonden - is de seed-data aangemaakt?", catalogus);
		authorization.setCatalogus(catalogus);

		Assert.assertFalse(
				"Open Zaak 1.9.1 heeft Catalogi API 1.1.1 (< 1.3.0) - supportsExpand() had false moeten zijn",
				authorization.supportsExpand());

		ZdsZaak zaak = zaakService.getZaakDetailsByIdentificatie(authorization, "TESTZAAK01");

		Assert.assertNotNull("getZaakDetailsByIdentificatie gaf null terug (had de fallback moeten gebruiken)", zaak);
		Assert.assertEquals("TESTZAAK01", zaak.identificatie);
		Assert.assertNotNull("isVan (zaaktype) ontbreekt - de zaaktype-fallback werkte niet", zaak.isVan);
		Assert.assertEquals("TESTZT01", zaak.isVan.gerelateerde.code);
	}
}
