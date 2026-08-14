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

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsParametersMetSortering extends ZdsParameters{

	@XmlElement(namespace = STUF, nillable = true)
	public String sortering;

	// vraag-veld: StUF default "15" (stuf0301.xsd ParametersVraag) - JAXB materialiseert xsd:default niet
	// vanzelf in een kaal veld, dus een ontbrekende/lege waarde wordt door de aanroeper zelf als 15
	// behandeld i.p.v. hier.
	@XmlElement(namespace = STUF, nillable = true)
	public String maximumAantal;

	public ZdsParametersMetSortering() {
	}

	public ZdsParametersMetSortering(ZdsParametersMetSortering zdsParameters) {
		this.sortering = zdsParameters.sortering;
		this.indicatorVervolgvraag = zdsParameters.indicatorVervolgvraag;
		this.maximumAantal = zdsParameters.maximumAantal;
	}
}