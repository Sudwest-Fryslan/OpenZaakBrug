/*
 * Copyright 2020-2021, 2024 The Open Zaakbrug Contributors
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
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGenereerZaakIdentificatieDi02;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsGenereerZaakIdentificatieDu02;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaakIdentificatie;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;


public class GenereerZaakIdentificatieEmulator extends Converter {

	public GenereerZaakIdentificatieEmulator(RequestResponseCycle session, Translation translation,
			ZaakService zaakService) {
		super(session, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		this.zdsDocument = (ZdsGenereerZaakIdentificatieDi02) XmlUtils.getStUFObject(this.getSession().getClientRequestBody(), ZdsGenereerZaakIdentificatieDi02.class);
	}

	@Override
	public ResponseEntity<?> execute() throws ConverterException {
		/*
 			zaakidentificatie, hiervoor gelden de volgende regels (genomen uit RGBZ):
			1e 4 posities: gemeentecode van de gemeente die verantwoordelijk is voor de behandeling van de zaak;
			pos. 5 – 40: alle alfanumerieke tekens m.u.v. diacrieten  
		 */

		EmulateParameterRepository repository = SpringContext.getBean(EmulateParameterRepository.class);
		var prefixparam = repository.getById("ZaakIdentificatiePrefix");
		var identificatie = repository.getZaakId();
		var zid = prefixparam.getParameterValue() + identificatie;

		this.getSession().setFunctie("GenereerZaakIdentificatie");
		this.getSession().setKenmerk("zaakidentificatie:" + zid);

		var di02 = (ZdsGenereerZaakIdentificatieDi02) this.zdsDocument;
		var du02 = new ZdsGenereerZaakIdentificatieDu02(di02.stuurgegevens, this.getSession().getReferentienummer());
		du02.zaak = new ZdsZaakIdentificatie();
		du02.zaak.functie = "entiteit";
		du02.zaak.identificatie = zid;

		var response = XmlUtils.getSOAPMessageFromObject(du02);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
