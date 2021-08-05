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
package nl.haarlem.translations.zdstozgw.debug;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import nl.haarlem.translations.zdstozgw.config.SpringContext;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestHandler;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;
import nl.nn.testtool.ExternalConnectionCode;
import nl.nn.testtool.ExternalConnectionCodeThrowsException;
import nl.nn.testtool.TestTool;

/**
 * @author Jaco de Groot
 */
public class Debugger {
	private static Map<Class<?>, Debugger> debuggers = new HashMap<Class<?>, Debugger>();
	private TestTool testTool;
	private String sourceClassName;

	private Debugger(TestTool testTool, String sourceClassName) {
		this.testTool = testTool;
		this.sourceClassName = sourceClassName;
	}

	public static synchronized Debugger getDebugger(Class<?> clazz) {
		Debugger debugger = debuggers.get(clazz);
		if (debugger == null) {
			debugger = new Debugger(SpringContext.getBean(TestTool.class), clazz.getCanonicalName());
			debuggers.put(clazz, debugger);
		}
		return debugger;
	}

	public Boolean isReportGeneratorEnabled() {
		return testTool.isReportGeneratorEnabled();
	}

	public void startpoint(String name) {
		startpoint(name, null);
	}

	public <T> T startpoint(String name, T message) {
		return testTool.startpoint(getReferentienummer(), sourceClassName, name, message);
	}

	public <T> T endpoint(String name, T message) {
		return testTool.endpoint(getReferentienummer(), sourceClassName, name, message);
	}

	public <T> T endpoint(String name, ExternalConnectionCode externalConnectionCode) {
		return testTool.endpoint(getReferentienummer(), sourceClassName, name, externalConnectionCode);
	}

	public <T, E extends Exception> T endpoint(String name,
			ExternalConnectionCodeThrowsException externalConnectionCodeThrowsException, E throwsException) throws E {
		return testTool.endpoint(getReferentienummer(), sourceClassName, name, externalConnectionCodeThrowsException,
				throwsException);
	}

	public <T> T inputpoint(String name, T message) {
		return testTool.inputpoint(getReferentienummer(), sourceClassName, name, message);
	}

	public <T> T outputpoint(String name, T message) {
		return testTool.outputpoint(getReferentienummer(), sourceClassName, name, message);
	}

	public <T, E extends Exception> T outputpoint(String name,
			ExternalConnectionCodeThrowsException externalConnectionCodeThrowsException, E throwsException) throws E {
		return testTool.outputpoint(getReferentienummer(), sourceClassName, name,
				externalConnectionCodeThrowsException, throwsException);
	}

	public <T> T infopoint(String name, T message) {
		return testTool.infopoint(getReferentienummer(), sourceClassName, name, message);
	}

	public <T> T abortpoint(String name, T message) {
		return testTool.abortpoint(getReferentienummer(), sourceClassName, name, message);
	}

	public void close() {
		testTool.close(getReferentienummer());
	}

	private static String getReferentienummer() {
		return (String)RequestContextHolder.getRequestAttributes()
				.getAttribute("referentienummer", RequestAttributes.SCOPE_REQUEST);
	}

	public void startpoint(RequestResponseCycle session) {
		this.startpoint(session.getReportName(), session.getClientRequestBody());
		this.inputpoint("modus", session.getModus());
		this.inputpoint("version", session.getVersion());
		this.inputpoint("protocol", session.getProtocol());
		this.inputpoint("endpoint", session.getEndpoint());
		this.inputpoint("soapAction", session.getClientSoapAction());
		this.infopoint("referentienummer", session.getReferentienummer());
	}

	public void infopoint(Converter converter, RequestHandler handler, String path) {
		this.infopoint("converter", converter.getClass().getCanonicalName());
		this.infopoint("handler", handler.getClass().getCanonicalName());
		this.infopoint("path", path);
	}

	public void endpoint(RequestResponseCycle session, ResponseEntity<?> response) {
		this.outputpoint("statusCode", response.getStatusCodeValue());
		this.outputpoint("kenmerk", session.getKenmerk());

		var message = "Soapaction: " + session.getClientSoapAction() + " took " + session.getDurationInMilliseconds() + " milliseconds";
		this.infopoint("Total duration", message);
		this.endpoint(session.getReportName(), response.getBody().toString());

		if(response.getStatusCode() == HttpStatus.OK) {
			this.endpoint(session.getReportName(), response.getBody().toString());
		}
		else {
			this.abortpoint(session.getReportName(), response.getBody().toString());
		}
	}
}
