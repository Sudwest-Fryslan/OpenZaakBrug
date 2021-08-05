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
package nl.haarlem.translations.zdstozgw.translation.zds.model;

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement(name = "Fo03Bericht", namespace = STUF)
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsFo03 extends ZdsStufDocument {

	@XmlElement(namespace = STUF)
	public Body body;

	public ZdsFo03() {
	}

	public ZdsFo03(ZdsStuurgegevens stuurgegevens, String referentienummer) {
		this.stuurgegevens = new ZdsStuurgegevens(stuurgegevens, referentienummer);
		this.stuurgegevens.crossRefnummer = stuurgegevens.referentienummer;
		this.stuurgegevens.berichtcode = "Fo03";
	}

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Body {
		@XmlElement(namespace = STUF)
		public String code;
		@XmlElement(namespace = STUF)
		public String plek;
		@XmlElement(namespace = STUF)
		public String omschrijving;
		@XmlElement(namespace = STUF)
		public String details;
		@XmlElement(namespace = STUF)
		public ZdsDetailsXML detailsXML;

		public Body() {
		}
	}
}
