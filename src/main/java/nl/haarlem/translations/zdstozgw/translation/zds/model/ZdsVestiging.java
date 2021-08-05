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

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.BG;
import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.STUF;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsVestiging extends ZdsObject {

	@XmlAttribute(namespace = STUF)
	public String entiteittype;

	@XmlElement(namespace = BG)
	public String vestigingsNummer;

	@XmlElement(namespace = BG)
	public String handelsnaam;

	@XmlElement(namespace = BG)
	public VerblijfsAdres verblijfsAdres;

	@Data
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class VerblijfsAdres {
		@XmlElement(namespace = BG, name = "wpl.woonplaatsNaam")
		public String woonplaatsNaam;

		@XmlElement(namespace = BG, name = "gor.openbareRuimteNaam")
		public String openbareRuimteNaam;

		@XmlElement(namespace = BG, name = "gor.straatnaam")
		public String straatNaam;

		@XmlElement(namespace = BG, name = "aoa.postcode")
		public String postcode;

		@XmlElement(namespace = BG, name = "aoa.huisnummer")
		public String huisnummer;

		@XmlElement(namespace = BG, name = "aoa.huisletter")
		public String huisletter;

		@XmlElement(namespace = BG, name = "aoa.huisnummertoevoeging")
		public String huisnummertoevoeging;
	}
}
