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
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import nl.haarlem.translations.zdstozgw.config.ConfigService;
import nl.haarlem.translations.zdstozgw.converter.ConverterFactory;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestHandlerFactory;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;

@Controller
public class SoapController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

	private final ConverterFactory converterFactory;
	private final ConfigService configService;
	private final RequestHandlerFactory requestHandlerFactory;
	private final BuildProperties buildProperties;

	@Autowired
	private org.springframework.core.env.Environment enviroment;	
	
	@Autowired
	public SoapController(ConverterFactory converterFactory, ConfigService configService,
                          RequestHandlerFactory requestHandlerFactory, BuildProperties buildProperties) {
		this.converterFactory = converterFactory;
		this.configService = configService;
		this.requestHandlerFactory = requestHandlerFactory;
        this.buildProperties = buildProperties;
    }


    /**
     * Give some basic information about the application
     */
	@RequestMapping("/")
    public String index(Model model ) {
		model.addAttribute("applicationname", buildProperties.getName());
        model.addAttribute("applicationversion", buildProperties.getVersion());
        model.addAttribute("applicationtime", buildProperties.getTime());
        model.addAttribute("translations", this.configService.getConfiguration().getTranslations());
        model.addAttribute("ladybugpath", "./debug");
        if("org.h2.Driver".equals(enviroment.getProperty("spring.datasource.driverClassName"))) {
        	if(enviroment.getProperty("spring.h2.console.path") != null) {
        		model.addAttribute("databasepath", enviroment.getProperty("spring.h2.console.path"));        		
        	}
        	else {
        		model.addAttribute("databasepath", "./h2-console");
        	}
        }
        else if ("org.postgresql.Driver".equals(enviroment.getProperty("spring.datasource.driverClassName")))  {
    		model.addAttribute("databasepath", "http://127.0.0.1:64386/browser/");        	
        }
        else {
        	// unknown
        	model.addAttribute("databasepath", "/");
        }
        return "index";
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
		var msg = "Processing: " + referentienummer + " with path: /" + path + "/ and soapaction: " + soapAction; 
		log.info(msg);


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
			log.warn("Unhandled exception while processing:" + msg);
			throw t;
		} finally {
			debug.close();
		}
		log.info("Finished: "+ referentienummer + " with:" + response.getStatusCode());
		return response;
	}
}
