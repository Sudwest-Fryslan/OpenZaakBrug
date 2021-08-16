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
package nl.haarlem.translations.zdstozgw.controller;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import nl.haarlem.translations.zdstozgw.config.ApplicationInformation;
import nl.haarlem.translations.zdstozgw.config.ConfigService;
import nl.haarlem.translations.zdstozgw.converter.ConverterFactory;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestHandlerFactory;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;

@RestController
public class SoapController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

	private final ConverterFactory converterFactory;
	private final ConfigService configService;
	private final RequestHandlerFactory requestHandlerFactory;
    private final ApplicationInformation applicationInformation;

	@Autowired
	public SoapController(ConverterFactory converterFactory, ConfigService configService,
                          RequestHandlerFactory requestHandlerFactory, ApplicationInformation applicationInformation) {
		this.converterFactory = converterFactory;
		this.configService = configService;
		this.requestHandlerFactory = requestHandlerFactory;
        this.applicationInformation = applicationInformation;
    }


    /**
     * Does not handle any requests, returns a list of available endpoints
     *
     * @return List of available endpoints
     */
	@GetMapping(path = { "/" }, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> HandleRequest() {
		var response = "=== Open Zaakbrug ===\n\n";
		response += "Application name:\t\t" + applicationInformation.getName() + "\n";
		response += "Application version:\t\t" + applicationInformation.getVersion() + "\n\n";

		response += "Supported translations:" + this.configService.getConfiguration().getTranslationsString();
		response += "\n\nDebugging:\n\t(not-persistent) request-log can be found at path './debug/'\n\tpersistent (error-)log in de database";

		return new  ResponseEntity<>(response, HttpStatus.OK);
	}

    /**
     * Receives the SOAP requests. Based on the configuration and path variables, the correct translation implementation is used.
     *
     * @param modus
     * @param version
     * @param protocol
     * @param endpoint
     * @param soapAction
     * @param body
     * @return ZDS response
     */
	@PostMapping(path = { "/{modus}/{version}/{protocol}/{endpoint}" },
                    consumes = MediaType.TEXT_XML_VALUE, produces = MediaType.TEXT_XML_VALUE)
	public ResponseEntity<?> HandleRequest(
			@PathVariable String modus, @PathVariable String version, @PathVariable String protocol,
			@PathVariable String endpoint, @RequestHeader(name = "SOAPAction", required = true) String soapAction,
			@RequestBody String body, String referentienummer) {

		// used by the ladybug-tests
		if (referentienummer == null)  referentienummer = "ozb-" + java.util.UUID.randomUUID().toString();
		var path = modus + "/" + version + "/" + protocol + "/" + endpoint;
		log.info("Processing request for path: /" + path + "/ with soapaction: " + soapAction + " with referentienummer:" + referentienummer);


		var session = new RequestResponseCycle(modus, version, protocol, endpoint, path, soapAction.replace("\"", ""), body, referentienummer);
		RequestContextHolder.getRequestAttributes().setAttribute("referentienummer", referentienummer, RequestAttributes.SCOPE_REQUEST);
		debug.startpoint(session);

		ResponseEntity<?> response;
		try {
			var converter = this.converterFactory.getConverter(session);
			var handler = this.requestHandlerFactory.getRequestHandler(converter);
			handler.save(session);

			debug.infopoint(converter, handler, path);
			response = handler.execute();
			debug.endpoint(session, response);

			session.setResponse(response);
			handler.save(session);
		} catch(Throwable t) {
			debug.abortpoint(session.getReportName(), t.toString());
			throw t;
		} finally {
			debug.close();
		}
		return response;
	}
}
