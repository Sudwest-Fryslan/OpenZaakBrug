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

@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsIsRelevantVoor extends ZdsObject {

	@XmlAttribute(namespace = STUF)
	public String entiteittype = "EDCZAK";

	@XmlElement(namespace = ZKN)
	public ZdsGerelateerde gerelateerde;

	@XmlElement(namespace = ZKN, name = "dct.omschrijving")
	public String volgnummer;

	@XmlElement(namespace = ZKN, name = "stt.omschrijving")
	public String omschrijving;

	@XmlElement(namespace = ZKN, name = "sta.datumStatusGezet")
	public String datumStatusGezet;

}
