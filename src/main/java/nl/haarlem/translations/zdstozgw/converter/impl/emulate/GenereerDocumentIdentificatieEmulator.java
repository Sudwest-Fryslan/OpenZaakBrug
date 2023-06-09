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
package nl.haarlem.translations.zdstozgw.converter.impl.emulate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.SpringContext;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.jpa.EmulateParameterRepository;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGenereerDocumentIdentificatieDi02;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGenereerDocumentIdentificatieDu02;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

public class GenereerDocumentIdentificatieEmulator extends Converter {

	public GenereerDocumentIdentificatieEmulator(RequestResponseCycle session, Translation translation,
			ZaakService zaakService) {
		super(session, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsGenereerDocumentIdentificatieDi02) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(),
				ZdsGenereerDocumentIdentificatieDi02.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ConverterException {
		EmulateParameterRepository repository = SpringContext.getBean(EmulateParameterRepository.class);
		var prefixparam = repository.getOne("DocumentIdentificatiePrefix");
		var idparam = repository.getOne("DocumentIdentificatieHuidige");
		var identificatie = Long.parseLong(idparam.getParameterValue()) + 1;
		idparam.setParameterValue(Long.toString(identificatie));
		repository.save(idparam);
		var did = prefixparam.getParameterValue() + identificatie;
		this.getSession().setFunctie("GenereerDocumentIdentificatie");
		this.getSession().setKenmerk("documentidentificatie:" + did);

		var di02 = (ZdsGenereerDocumentIdentificatieDi02) this.zdsDocument;
		var du02 = new ZdsGenereerDocumentIdentificatieDu02(di02.stuurgegevens, this.getSession().getReferentienummer());
		du02.document = new ZdsZaakDocument();
		du02.document.functie = "entiteit";
		du02.document.identificatie = did;

		var response = XmlUtils.getSOAPMessageFromObject(du02);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}