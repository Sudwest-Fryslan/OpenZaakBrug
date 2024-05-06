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

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsEdcLa01GeefZaakdocumentLezen;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsEdcLv01;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsParameters;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentAntwoord;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocumentInhoud;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZgwAuthorization;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class GeefZaakdocumentLezenTranslator extends Converter {

	public GeefZaakdocumentLezenTranslator(RequestResponseCycle session, Translation translation,
			ZaakService zaakService) {
		super(session, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsEdcLv01) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsEdcLv01.class);
	}

	@Override
	public ResponseEntity<?> execute(ZgwAuthorization authorization) throws ResponseStatusException {
		var zdsEdcLv01 = (ZdsEdcLv01) this.getZdsDocument();
		var documentIdentificatie = zdsEdcLv01.gelijk.identificatie;

		this.getSession().setFunctie("GeefZaakdocumentLezen");
		this.getSession().setKenmerk("documentidentificatie:" + documentIdentificatie);

		ZdsZaakDocumentInhoud document = this.getZaakService().getZaakDocumentLezen(authorization, documentIdentificatie);
		var edcLa01 = new ZdsEdcLa01GeefZaakdocumentLezen(zdsEdcLv01.stuurgegevens, this.getSession().getReferentienummer());
		edcLa01.antwoord = new ZdsZaakDocumentAntwoord();
		edcLa01.antwoord.document = new ArrayList<ZdsZaakDocumentInhoud>();
		edcLa01.antwoord.document.add(document);
		edcLa01.parameters = new ZdsParameters(zdsEdcLv01.parameters);
		var response = XmlUtils.getSOAPMessageFromObject(edcLa01);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}