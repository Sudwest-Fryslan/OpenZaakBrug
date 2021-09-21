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
package nl.haarlem.translations.zdstozgw.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.config.model.BeeindigZaakWanneerEinddatum;
import nl.haarlem.translations.zdstozgw.config.model.Configuration;
import nl.haarlem.translations.zdstozgw.config.model.Organisatie;
import nl.haarlem.translations.zdstozgw.config.model.Translation;

@Service
@Data
public class ConfigService {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Configuration configuration;

	public ConfigService(@Value("${config.json.location:config.json}") String configPath) throws Exception {
		var cpr = new ClassPathResource(configPath);

		try(InputStream configStream = cpr.getInputStream()){
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(configStream));
			Gson gson = new Gson();
			this.configuration = gson.fromJson(bufferedReader, Configuration.class);
		}

		validateConfiguration();
		log.debug("ConfigService succesfully loaded");
	}

	private void validateConfiguration() throws Exception {
		log.debug("validateConfiguration");
		log.debug("requestHandlerImplementation:" + this.configuration.getRequestHandlerImplementation());

		this.configuration.getOrganisaties().size();
		log.debug("=== organisaties ===");
		for (Organisatie organisatie : this.configuration.getOrganisaties()) {
			log.debug("gemeentenaam:" + organisatie.getGemeenteNaam());
			log.debug("gemeentecode:" + organisatie.getGemeenteCode());
			log.debug("rsin:" + organisatie.getRSIN());
		}

		var rolomschrijving = this.configuration.getZgwRolOmschrijving();
		log.debug("=== rol omschrijvingen ===");
		log.debug("heeftBetrekkingOp:" + rolomschrijving.getHeeftBetrekkingOp());
		log.debug("heeftAlsBelanghebbende:" + rolomschrijving.getHeeftAlsBelanghebbende());
		log.debug("heeftAlsInitiator:" + rolomschrijving.getHeeftAlsInitiator());
		log.debug("heeftAlsUitvoerende:" + rolomschrijving.getHeeftAlsUitvoerende());
		log.debug("heeftAlsVerantwoordelijke:" + rolomschrijving.getHeeftAlsVerantwoordelijke());
		log.debug("heeftAlsGemachtigde:" + rolomschrijving.getHeeftAlsGemachtigde());
		log.debug("heeftAlsOverigeBetrokkene:" + rolomschrijving.getHeeftAlsOverigBetrokkene());

		this.configuration.getBeeindigZaakWanneerEinddatum().size();
		log.debug("=== beeindigZaakWanneerEinddatum ===");
		for (BeeindigZaakWanneerEinddatum beeindigZaakWanneerEinddatum : this.configuration.getBeeindigZaakWanneerEinddatum()) {
			log.debug("zaakType:" + beeindigZaakWanneerEinddatum.getZaakType());
			log.debug("coalesceStatus:" + beeindigZaakWanneerEinddatum.getCoalesceStatus());
			log.debug("coalesceResultaat:" + beeindigZaakWanneerEinddatum.getCoalesceResultaat());
		}
		
		var replicatie = this.configuration.getReplication();
		log.debug("=== replicatie ===");
		log.debug("geefZaakDetailsAction:" + replicatie.getGeefZaakdetails().getSoapaction());
		log.debug("geefZaakDetailsUrl:" + replicatie.getGeefZaakdetails().getUrl());
		log.debug("geefLijstZaakdocumentenAction:" + replicatie.getGeefLijstZaakdocumenten().getSoapaction());
		log.debug("geefLijstZaakdocumentenUrl:" + replicatie.getGeefLijstZaakdocumenten().getUrl());
		log.debug("geefZaakDocumentLezenAction:" + replicatie.getGeefZaakdocumentLezen().getSoapaction());
		log.debug("geefZaakDocumentLezenUrl:" + replicatie.getGeefZaakdocumentLezen().getUrl());

		this.configuration.getTranslations().size();
		log.debug("=== translaties ===");
		for (Translation translation : this.configuration.getTranslations()) {
			log.debug("translation:" + translation.getTranslation());
			log.debug("path:" + translation.getPath());
			log.debug("soapAction:" + translation.getSoapaction());
			log.debug("template:" + translation.getTemplate());
			log.debug("implementation:" + translation.getImplementation());
			log.debug("legacyservice:" + translation.getLegacyservice());
		}
	}

	public Translation getTranslationByPathAndSoapAction(String path, String soapAction) {
		log.debug("searching first translaton for : /" + path + "/ with soapaction: " + soapAction);
		for (Translation translation : this.configuration.getTranslations()) {
			log.debug("\t checking path '" + translation.getPath() + "' with action: '" + translation.getSoapaction()
					+ "'");
			if (path.equals(translation.getPath()) && soapAction.equals(translation.getSoapaction())) {
				return translation;
			}
		}
		return null;
	}
}
