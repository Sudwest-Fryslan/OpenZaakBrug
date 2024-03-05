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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import nl.haarlem.translations.zdstozgw.config.ConfigService;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.haarlem.translations.zdstozgw.translation.zds.services.ZaakService;

@Component
public class ConverterFactory {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ConfigService configService;
	private final ZaakService zaakService;

	private Object requestHandlerFactory;

	@Autowired
	public ConverterFactory(ConfigService configService, ZaakService zaakService) {
		this.configService = configService;
		this.zaakService = zaakService;
	}

	public Converter getConverter(RequestResponseCycle session) throws ResponseStatusException {
		Translation translation = this.configService.getTranslationByPathAndSoapAction(session.getClientUrl(),
				session.getClientSoapAction());

		if (translation == null) {
			log.error("Could not load a convertor for path: '" + session.getClientUrl() + "' with soapaction: '"
					+ session.getClientSoapAction() + "'");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Could not load a convertor for path: '" + session.getClientUrl() + "' with soapaction: '"
							+ session.getClientSoapAction() + "'\navailable services:"
							+ this.configService.getConfiguration().getTranslationsString());
		}
		String classname = translation.implementation;
		session.setConverterImplementation(classname);
		try {
			Class<?> c = Class.forName(classname);
			java.lang.reflect.Constructor<?> ctor = c.getConstructor(RequestResponseCycle.class, Translation.class,
					ZaakService.class);
			Object object = ctor.newInstance(new Object[] { session, translation, this.zaakService });

			var converter = (Converter) object;
			return converter;
		} catch (Exception e) {
			log.error("error loading class:" + classname, e);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error loading class:" + classname, e);
		}
	}
}
