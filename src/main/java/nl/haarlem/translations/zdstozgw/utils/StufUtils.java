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
package nl.haarlem.translations.zdstozgw.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StufUtils {

	public static String getStufDateTime() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMddHHmmss");
		return now.format(formatter);
	}

	public static String getStufDateFromDateString(String dateString) {
		if (dateString == null) {
			return null;
		}
		var year = dateString.substring(0, 4);
		var month = dateString.substring(5, 7);
		var day = dateString.substring(8, 10);
		return year + month + day;
	}
}
