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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Organisatie;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsBv03;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsEdcLk01;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZgwAuthorization;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class VoegZaakdocumentToeTranslator extends Converter {

	public VoegZaakdocumentToeTranslator(RequestResponseCycle context, Translation translation,
			ZaakService zaakService) {
		super(context, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsEdcLk01) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsEdcLk01.class);
	}

	@Override
	public ResponseEntity<?> execute(ZgwAuthorization authorization) throws ResponseStatusException {	
		var zdsEdcLk01 = (ZdsEdcLk01) this.getZdsDocument();
		var zdsInformatieObject = zdsEdcLk01.objects.get(0);
		var zdsZaakObject = zdsInformatieObject.isRelevantVoor.gerelateerde;

		this.getSession().setFunctie("VoegZaakdocumentToe");
		this.getSession().setKenmerk("zaakidentificatie:" + zdsZaakObject.identificatie + " documentidentificatie:" + zdsInformatieObject.identificatie);

		this.getZaakService().voegZaakDocumentToe(authorization, zdsInformatieObject);
		var bv03 = new ZdsBv03(zdsEdcLk01.stuurgegevens, this.getSession().getReferentienummer());
		var response = XmlUtils.getSOAPMessageFromObject(bv03);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
