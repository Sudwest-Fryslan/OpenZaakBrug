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
package nl.haarlem.translations.zdstozgw.translation.zds.client;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.requesthandler.impl.logging.ZdsRequestResponseCycle;
import nl.haarlem.translations.zdstozgw.requesthandler.impl.logging.ZdsRequestResponseCycleRepository;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsObject;
import nl.haarlem.translations.zdstozgw.utils.XmlUtils;

@Service
public class ZDSClient {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

	private ZdsRequestResponseCycleRepository repository;

	public ZDSClient(ZdsRequestResponseCycleRepository zdsRequestResponseCycleRepository) {
		this.repository = zdsRequestResponseCycleRepository;
	}

	public ResponseEntity<?> post(String referentienummer, String zdsUrl, String zdsSoapAction, ZdsObject zdsRequest) {
		var request = XmlUtils.getSOAPMessageFromObject(zdsRequest);
		return post(referentienummer, zdsUrl, zdsSoapAction, request);
	}

	public ResponseEntity<?> post(String referentienummer, String zdsUrl, String zdsSoapAction, String zdsRequestBody) {
		log.info("Performing ZDS request to: '" + zdsUrl + "' for soapaction:" + zdsSoapAction);
		log.debug("Requestbody:\n" + zdsRequestBody);
		var method = new PostMethod(zdsUrl);
		try {
			long startTime = System.currentTimeMillis();
			method.setRequestHeader("SOAPAction", zdsSoapAction);
			method.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
			StringRequestEntity requestEntity = new org.apache.commons.httpclient.methods.StringRequestEntity(
					zdsRequestBody, "text/xml", "utf-8");
			method.setRequestEntity(requestEntity);
			var httpclient = new org.apache.commons.httpclient.HttpClient();

			//String referentienummer = (String) RequestContextHolder.getRequestAttributes().getAttribute("referentienummer", RequestAttributes.SCOPE_REQUEST);

            ZdsRequestResponseCycle zdsRequestResponseCycle = new ZdsRequestResponseCycle(zdsUrl, zdsSoapAction, zdsRequestBody, referentienummer);
            this.repository.save(zdsRequestResponseCycle);

			String debugName = "ZDSClient POST";
			debug.startpoint(debugName, zdsRequestBody);
			debug.infopoint("url", zdsUrl);
			int responsecode = (Integer) debug.outputpoint("statusCode", () -> {
				return httpclient.executeMethod(method);
			}, (IOException)null);
			String zdsResponseBody = (String) debug.endpoint(debugName, () -> {
					return method.getResponseBodyAsString();
			}, (IOException)null);

			ResponseEntity<?> result = new ResponseEntity<>(zdsResponseBody, HttpStatus.valueOf(responsecode));
			zdsRequestResponseCycle.setResponse(result);
			this.repository.save(zdsRequestResponseCycle);

			if(responsecode != 200) {
				String message = "Error: responsecode #" + responsecode + " (not 200) while requesting url:" + zdsUrl + " with soapaction: " + zdsSoapAction;
				debugName = "Invalid response code";
				debug.startpoint(debugName, responsecode);
				debug.abortpoint(debugName, message);
				throw new ConverterException(message);
			}
			long endTime = System.currentTimeMillis();
			var duration = endTime - startTime;
			var message = "Soapaction: " + zdsSoapAction + " took " + duration + " milliseconds";
			log.info(message);
			debug.infopoint("Duration", message);
			return result;
		} catch (IOException ce) {
			throw new ConverterException(
					"Error: " + ce.toString() + " requesting url:" + zdsUrl + " with soapaction: " + zdsSoapAction, ce);
		} catch (java.lang.IllegalArgumentException iae) {
			throw new ConverterException(
					"Error " + iae.toString() + " requesting url:" + zdsUrl + " with soapaction: " + zdsSoapAction,
					iae);
		} finally {
			// Release current connection to the connection pool once you are done
			method.releaseConnection();
		}
	}
}
