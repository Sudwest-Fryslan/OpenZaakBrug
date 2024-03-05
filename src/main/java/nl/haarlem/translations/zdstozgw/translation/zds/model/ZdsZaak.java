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

import static nl.haarlem.translations.zdstozgw.translation.zds.model.namespace.Namespace.ZKN;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ZdsZaak extends ZdsZaakIdentificatie {
//	@XmlAttribute(namespace = STUF)
//	public String entiteittype;

//	@XmlAttribute(namespace = STUF)
//	public String scope;

	@XmlElement(namespace = ZKN)
	public String omschrijving;

	@XmlElement(namespace = ZKN)
	public String toelichting;

	@XmlElement(namespace = ZKN)
	public List<ZdsKenmerk> kenmerk;

	@XmlElement(namespace = ZKN)
	public ZdsAnderZaakObject anderZaakObject;

	@XmlElement(namespace = ZKN)
	public ZdsResultaat resultaat;

	@XmlElement(namespace = ZKN)
	public String startdatum;

	@XmlElement(namespace = ZKN)
	public String registratiedatum;

	@XmlElement(namespace = ZKN)
	public String publicatiedatum;

	@XmlElement(namespace = ZKN)
	public String einddatumGepland;

	@XmlElement(namespace = ZKN)
	public String uiterlijkeEinddatum;

	@XmlElement(namespace = ZKN)
	public String einddatum;

	@XmlElement(namespace = ZKN)
	public ZdsOpschorting opschorting;

	@XmlElement(namespace = ZKN)
	public ZdsVerlenging verlenging;

	@XmlElement(namespace = ZKN)
	public String betalingsIndicatie;

	@XmlElement(namespace = ZKN)
	public String laatsteBetaaldatum;

	@XmlElement(namespace = ZKN)
	public String archiefnominatie;

	@XmlElement(namespace = ZKN)
	public String datumVernietigingDossier;

	@XmlElement(namespace = ZKN)
	public String zaakniveau;

	@XmlElement(namespace = ZKN)
	public String deelzakenIdicatie;

	@XmlElement(namespace = ZKN)
	public ZdsVan isVan;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftBetrekkingOp;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsBelanghebbende;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsGemachtigde;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsInitiator;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsUitvoerende;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsVerantwoordelijke;

	@XmlElement(namespace = ZKN)
	public ZdsRol heeftAlsOverigBetrokkene;

	@XmlElement(namespace = ZKN)
	public List<ZdsHeeftGerelateerde> heeftAlsDeelzaak;

	@XmlElement(namespace = ZKN)
	public ZdsHeeftGerelateerde heeftAlsHoofdzaak;

	@XmlElement(namespace = ZKN)
	public List<ZdsHeeftGerelateerde> heeftBetrekkingOpAndere;

	@XmlElement(namespace = ZKN)
	public List<ZdsHeeft> heeft;

	@XmlElement(namespace = ZKN)
	public ZdsHeeftRelevant heeftRelevant;
}
