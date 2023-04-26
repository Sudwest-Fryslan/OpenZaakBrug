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
import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsGerelateerde extends ZdsObject {

	@XmlAttribute(namespace = STUF)
	public String entiteittype;

	@XmlElement(namespace = ZKN)
	public String identificatie;

	@XmlAttribute(namespace = STUF)
	public String verwerkingssoort;

	@XmlElement(namespace = ZKN, name = "zkt.code")
	public String zktCode;

	@XmlElement(namespace = ZKN, name = "zkt.omschrijving")
	public String zktOmschrijving;

	@XmlElement(namespace = ZKN)
	public String omschrijving;

	@XmlElement(namespace = ZKN)
	public String code;

	@XmlElement(namespace = ZKN)
	public String ingangsdatumObject;

	@XmlElement(namespace = ZKN)
	public String volgnummer;

	@XmlElement(namespace = ZKN)
	public ZdsMedewerker medewerker;

	@XmlElement(namespace = ZKN)
	public ZdsNatuurlijkPersoon natuurlijkPersoon;

	@XmlElement(namespace = ZKN)
	public ZdsNietNatuurlijkPersoon nietNatuurlijkPersoon;

	@XmlElement(namespace = ZKN)
	public ZdsVestiging vestiging;

	@XmlElement(namespace = ZKN)
    public ZdsAoaAdres adres;
}
