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

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import nl.haarlem.translations.zdstozgw.config.SpringContext;
import nl.haarlem.translations.zdstozgw.controller.SoapController;
import nl.nn.testtool.Checkpoint;
import nl.nn.testtool.Report;
import nl.nn.testtool.SecurityContext;
import nl.nn.testtool.run.ReportRunner;

/**
 * @author Jaco de Groot
 */
@Component
public class Rerunner implements nl.nn.testtool.Rerunner {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	@Transactional
	public String rerun(String correlationId, Report originalReport, SecurityContext securityContext,
			ReportRunner reportRunner) {
		// Get bean here instead of specifying the bean in the constructor (with @Lazy)
		// and have it auto wired to
		// prevent cycle of bean dependencies between bean rerunner and bean testTool
		// that sometimes occurs when running
		// with java -jar (problem has not been observed with mvn spring-boot:run)
		SoapController soapController = SpringContext.getBean(SoapController.class);
		String errorMessage = null;
		String modus = null;
		String version = null;
		String protocol = null;
		String endpoint = null;
		String soapAction = null;
		String body = null;
		List<Checkpoint> checkpoints = originalReport.getCheckpoints();
		if (checkpoints.size() > 5) {
			if ("modus".equals(checkpoints.get(1).getName())) {
				modus = checkpoints.get(1).getMessage();
				if ("version".equals(checkpoints.get(2).getName())) {
					version = checkpoints.get(2).getMessage();
					if ("protocol".equals(checkpoints.get(3).getName())) {
						protocol = checkpoints.get(3).getMessage();
						if ("endpoint".equals(checkpoints.get(4).getName())) {
							endpoint = checkpoints.get(4).getMessage();
							if ("soapAction".equals(checkpoints.get(5).getName())) {
								soapAction = checkpoints.get(5).getMessage();
								body = checkpoints.get(0).getMessage();
								try {
									RequestContextHolder.setRequestAttributes(new MockRequestAttributes());
									soapController.HandleRequest(modus, version, protocol, endpoint, soapAction, body,
											correlationId);
								} catch (Throwable t) {
									// Exceptions can be legitimate when testing error handling with Ladybug
									log.debug(errorMessage, t);
								}
							} else {
								errorMessage = "Checkpoint soapAction not found in original report at position 5";
							}
						} else {
							errorMessage = "Checkpoint endpoint not found in original report at position 4";
						}
					} else {
						errorMessage = "Checkpoint protocol not found in original report at position 3";
					}
				} else {
					errorMessage = "Checkpoint version not found in original report at position 2";
				}
			} else {
				errorMessage = "Checkpoint modus not found in original report at position 1";
			}
		} else {
			errorMessage = "Original report only has " + checkpoints.size()
					+ " checkpoints, hence cannot read modus, version, protocol, endpoint and soapAction from it";
		}
		return errorMessage;
	}

}
