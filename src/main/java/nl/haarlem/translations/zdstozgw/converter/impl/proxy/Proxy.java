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
package nl.haarlem.translations.zdstozgw.converter.impl.proxy;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.SpringContext;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.client.ZDSClient;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;

public class Proxy extends Converter {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public Proxy(RequestResponseCycle session, Translation translation, ZaakService zaakService) {
		super(session, translation, zaakService);
	}

	@Override
	public void load() throws ResponseStatusException {
		// nothing to do here, we dont set the zdsDocument
		this.zdsDocument = null;
	}

	@Override
	public ResponseEntity<?> execute() throws ConverterException {
		var url = this.getTranslation().getLegacyservice();
		var soapaction = this.getTranslation().getSoapAction();
		var request = this.getSession().getClientRequestBody();

		this.getSession().setFunctie("Proxy");
		this.getSession().setKenmerk(url);

		log.info("relaying request to url: " + url + " with soapaction: " + soapaction + " request-size:"
				+ request.length());

		ZDSClient zdsClient = SpringContext.getBean(ZDSClient.class);
		return zdsClient.post(this.getSession().getReferentienummer(), url, soapaction, request);
	}
}
