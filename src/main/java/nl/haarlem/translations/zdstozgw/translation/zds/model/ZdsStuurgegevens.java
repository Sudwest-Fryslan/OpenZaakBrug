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

import lombok.Data;
import nl.haarlem.translations.zdstozgw.utils.StufUtils;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsStuurgegevens extends ZdsObject {

	@XmlElement(namespace = STUF)
	public String berichtcode;

	@XmlElement(namespace = STUF)
	public Endpoint zender;

	@XmlElement(namespace = STUF)
	public Endpoint ontvanger;

	@XmlElement(namespace = STUF)
	public String referentienummer;

	@XmlElement(namespace = STUF)
	public String tijdstipBericht;

	@XmlElement(namespace = STUF)
	public String crossRefnummer;

	@XmlElement(namespace = STUF)
	public String functie;

	@XmlElement(namespace = STUF)
	public String entiteittype;

	public ZdsStuurgegevens() {
	}

	public ZdsStuurgegevens(ZdsStuurgegevens stuurgegevens, String referentienummer) {
		this.zender = new Endpoint(stuurgegevens.ontvanger);
		this.ontvanger = new Endpoint(stuurgegevens.zender);
		this.referentienummer = referentienummer;
		this.crossRefnummer = stuurgegevens.referentienummer;
		this.tijdstipBericht = StufUtils.getStufDateTime();
	}
}