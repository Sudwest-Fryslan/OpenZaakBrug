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
package nl.haarlem.translations.zdstozgw.converter.impl.replicate;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.impl.translate.GeefZaakDetailsTranslator;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZakLv01;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;

public class GeefZaakDetailsReplicator extends GeefZaakDetailsTranslator {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public GeefZaakDetailsReplicator(RequestResponseCycle session, Translation translation, ZaakService zaakService) {
		super(session, translation, zaakService);
	}

    /**
     * Replicates the zaak before returning zaakdetails
     *
     * @return ZDS Zaakdetails
     * @throws ResponseStatusException
     */
    @Override
	public ResponseEntity<?> execute() throws ResponseStatusException {
    	String rsin = this.getZaakService().getRSIN(this.zdsDocument.stuurgegevens.zender.organisatie);
    	var authorization = this.getZaakService().zgwClient.getAuthorization(rsin);    	
    	
		var zdsZakLv01 = (ZdsZakLv01) this.getZdsDocument();
		var replicator = new Replicator(this);
		var legacyresponse = replicator.proxy();
		replicator.replicateZaak(authorization, zdsZakLv01.gelijk.identificatie);
		return legacyresponse;
	}
}
