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
package nl.haarlem.translations.zdstozgw.requesthandler;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;

import javax.xml.soap.SOAPConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.config.ConfigService;
import nl.haarlem.translations.zdstozgw.config.model.Configuration;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsDetailsXML;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsFo03;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

@Data
public abstract class RequestHandler {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Converter converter;
	protected ConfigService configService;

	public RequestHandler(Converter converter, ConfigService configService) {
		this.converter = converter;
		this.configService = configService;
	}

	protected String getStacktrace(Exception ex) {
		var swriter = new java.io.StringWriter();
		var pwriter = new PrintWriter(swriter);
		ex.printStackTrace(pwriter);
		var stacktrace = swriter.toString();

		return stacktrace;
	}

	protected ZdsFo03 getErrorZdsDocument(Exception ex, Converter convertor) {
		log.warn("request for path: /" + this.converter.getSession().getClientUrl() + "/ with soapaction: "
				+ this.converter.getSession().getClientSoapAction(), ex);

		var fo03 = this.converter.getZdsDocument() != null ? new ZdsFo03(this.converter.getZdsDocument().stuurgegevens,
				convertor.getSession().getReferentienummer()) : new ZdsFo03();
		fo03.body = new ZdsFo03.Body();
		fo03.body.code = "StUF058";
		fo03.body.plek = "server";
		var omschrijving = ex.toString();
		// max 200 chars
		if (omschrijving.length() > 200) {
			omschrijving = omschrijving.substring(omschrijving.length() - 200);
		}
		fo03.body.omschrijving = omschrijving;
		if (ex instanceof ConverterException) {
			var ce = (ConverterException) ex;
			fo03.body.details = ce.details;
		} else {
			fo03.body.details = getStacktrace(ex);
		}
		// maxlength
		if (fo03.body.details != null && fo03.body.details.length() >= 1000) {

			fo03.body.details = fo03.body.details.substring(0, 1000);
		}
		fo03.body.detailsXML = new ZdsDetailsXML();
		// TODO: put the xml in DetailsXml, without escaping
		fo03.body.detailsXML.todo = this.converter.getSession().getClientRequestBody();
		return fo03;
	}

	public ResponseEntity<?> execute() {
		log.debug("Executing request with handler: " + this.getClass().getCanonicalName() + " and converter: "
				+ this.converter.getClass().getCanonicalName());
		Configuration configuration = this.configService.getConfiguration();

		try {
			this.converter.load();
			var response = this.converter.execute();

			return response;
		} catch (Exception ex) {
			log.warn("Exception handling request with handler: " + this.getClass().getCanonicalName()
					+ " and converter: " + this.converter.getClass().getCanonicalName(), ex);
			var fo03 = getErrorZdsDocument(ex, this.getConverter());
			var responseBody = XmlUtils.getSOAPFaultMessageFromObject(SOAPConstants.SOAP_RECEIVER_FAULT, ex.toString(),
					fo03);
			var response = new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
			this.getConverter().getSession().setStackTrace(getStacktrace(ex));
			return response;
		}
	}

	public abstract void save(RequestResponseCycle session);
}
