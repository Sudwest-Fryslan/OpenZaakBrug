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

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@Slf4j
public class ConverterException extends RuntimeException {

	public String details;

	public ConverterException(String omschrijving) {
		super(omschrijving);
	}

	public ConverterException(String omschrijving, String details) {
		super(omschrijving);
		this.details = details;
	}

	
	public ConverterException(String omschrijving, Throwable cause) {
		super(omschrijving, cause);
		log.error(cause.getStackTrace().toString());
	}

	public ConverterException(String omschrijving, String details, Throwable cause) {
		super(omschrijving, cause);
		log.error(cause.getStackTrace().toString());
		this.details = details;
	}
}
