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
package nl.haarlem.translations.zdstozgw.converter;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZknDocument;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;

@Data
public abstract class Converter {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Translation translation;
	protected ZaakService zaakService;
	private RequestResponseCycle session;
	protected ZdsZknDocument zdsDocument;

	public Converter(RequestResponseCycle session, Translation translation, ZaakService zaakService) {
		this.session = session;
		this.translation = translation;
		this.zaakService = zaakService;
	}

	public abstract void load() throws ResponseStatusException;

	public abstract ResponseEntity<?> execute() throws ConverterException;
}