/*
 * Copyright 2020-2021 The Open Zaakbrug Contributors
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
package nl.haarlem.translations.zdstozgw.converter.impl.translate;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsAntwoordLijstZaakdocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsHeeftRelevant;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsObjectLijstZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsParameters;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsStuurgegevens;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLa01LijstZaakdocumenten;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLv01;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZgwAuthorization;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class GeefLijstZaakdocumentenTranslator extends Converter {

	public GeefLijstZaakdocumentenTranslator(RequestResponseCycle context, Translation translation,
			ZaakService zaakService) {
		super(context, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsZakLv01) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsZakLv01.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
		String rsin = this.getZaakService().getRSIN(this.zdsDocument.stuurgegevens);
		var authorization = this.getZaakService().zgwClient.getAuthorization(rsin);

		ZdsZakLv01 zdsZakLv01 = (ZdsZakLv01) this.getZdsDocument();
		var zaakidentificatie = zdsZakLv01.gelijk.identificatie;

		this.getSession().setFunctie("GeefLijstZaakdocumenten");
		this.getSession().setKenmerk("zaakidentificatie:" + zaakidentificatie);

		List<ZdsHeeftRelevant> gerelateerdeDocumenten = this.getZaakService()
				.geefLijstZaakdocumenten(authorization, zaakidentificatie);

		ZdsZakLa01LijstZaakdocumenten zdsZakLa01LijstZaakdocumenten = new ZdsZakLa01LijstZaakdocumenten(
				zdsZakLv01.stuurgegevens, this.getSession().getReferentienummer());
		zdsZakLa01LijstZaakdocumenten.antwoord = new ZdsAntwoordLijstZaakdocument();
		zdsZakLa01LijstZaakdocumenten.stuurgegevens = new ZdsStuurgegevens(zdsZakLv01.stuurgegevens,
				this.getSession().getReferentienummer());
		zdsZakLa01LijstZaakdocumenten.stuurgegevens.berichtcode = "La01";
		zdsZakLa01LijstZaakdocumenten.stuurgegevens.entiteittype = "ZAK";
		zdsZakLa01LijstZaakdocumenten.parameters = new ZdsParameters(zdsZakLv01.parameters);
		zdsZakLa01LijstZaakdocumenten.antwoord = new ZdsAntwoordLijstZaakdocument();
		zdsZakLa01LijstZaakdocumenten.antwoord.object = new ZdsObjectLijstZaakDocument();
		zdsZakLa01LijstZaakdocumenten.antwoord.object.heeftRelevant = gerelateerdeDocumenten;

		var response = XmlUtils.getSOAPMessageFromObject(zdsZakLa01LijstZaakdocumenten);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
