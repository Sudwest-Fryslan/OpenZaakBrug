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
package nl.haarlem.translations.zdstozgw.translation.zgw.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ZgwZaakPut {

	@SerializedName("identificatie")
	@Expose
	public String identificatie;
	@SerializedName("bronorganisatie")
	@Expose
	public String bronorganisatie;
	@SerializedName("omschrijving")
	@Expose
	public String omschrijving;
	@SerializedName("toelichting")
	@Expose
	public String toelichting;
	@SerializedName("zaaktype")
	@Expose
	public String zaaktype;
	@SerializedName("registratiedatum")
	@Expose
	public String registratiedatum;
	@SerializedName("verantwoordelijkeOrganisatie")
	@Expose
	public String verantwoordelijkeOrganisatie;
	@SerializedName("startdatum")
	@Expose
	public String startdatum;
	@SerializedName("einddatumGepland")
	@Expose
	public String einddatumGepland;
	@SerializedName("uiterlijkeEinddatumAfdoening")
	@Expose
	public String uiterlijkeEinddatumAfdoening;
	@SerializedName("publicatiedatum")
	@Expose
	public String publicatiedatum;
	@SerializedName("communicatiekanaal")
	@Expose
	public String communicatiekanaal;
	@SerializedName("productenOfDiensten")
	@Expose
	public List<Object> productenOfDiensten = null;
	@SerializedName("vertrouwelijkheidaanduiding")
	@Expose
	public String vertrouwelijkheidaanduiding;
	@SerializedName("betalingsindicatie")
	@Expose
	public String betalingsindicatie;
//    @SerializedName("laatsteBetaaldatum")
//    @Expose
//    public String laatsteBetaaldatum = null;
	@SerializedName("zaakgeometrie")
	@Expose
	public String zaakgeometrie;
	@SerializedName("verlenging")
	@Expose
	public ZgwVerlenging verlenging = null;
	@SerializedName("opschorting")
	@Expose
	public ZgwOpschorting opschorting = null;
	@SerializedName("selectielijstklasse")
	@Expose
	public String selectielijstklasse;
	@SerializedName("hoofdzaak")
	@Expose
	public String hoofdzaak  = null;
	@SerializedName("relevanteAndereZaken")
	@Expose
	public List<ZgwAndereZaak> relevanteAndereZaken = null;
	@SerializedName("kenmerken")
	@Expose
	public List<ZgwKenmerk> kenmerk = null;
	@SerializedName("archiefnominatie")
	@Expose
	public String archiefnominatie  = null;
	@SerializedName("archiefstatus")
	@Expose
	public String archiefstatus;
	@SerializedName("archiefactiedatum")
	@Expose
	public String archiefactiedatum  = null;

	public static ZgwZaakPut merge(ZgwZaak original, ZgwZaakPut changes) {
		var result = new ZgwZaakPut();
		result.identificatie = changes.identificatie != null ? changes.identificatie : original.identificatie;
		result.bronorganisatie = changes.bronorganisatie != null ? changes.bronorganisatie : original.bronorganisatie;
		result.toelichting = changes.toelichting != null ? changes.toelichting : original.toelichting;
		result.zaaktype = changes.zaaktype != null ? changes.zaaktype : original.zaaktype;
		result.registratiedatum = changes.registratiedatum != null ? changes.registratiedatum
				: original.registratiedatum;
		result.verantwoordelijkeOrganisatie = changes.verantwoordelijkeOrganisatie != null
				? changes.verantwoordelijkeOrganisatie
				: original.verantwoordelijkeOrganisatie;
		result.startdatum = changes.startdatum != null ? changes.startdatum : original.startdatum;
		result.einddatumGepland = changes.einddatumGepland != null ? changes.einddatumGepland
				: original.einddatumGepland;
		result.uiterlijkeEinddatumAfdoening = changes.uiterlijkeEinddatumAfdoening != null
				? changes.uiterlijkeEinddatumAfdoening
				: original.uiterlijkeEinddatumAfdoening;
		result.publicatiedatum = changes.publicatiedatum != null ? changes.publicatiedatum : original.publicatiedatum;
		result.communicatiekanaal = changes.communicatiekanaal != null ? changes.communicatiekanaal
				: original.communicatiekanaal;
		result.productenOfDiensten = changes.productenOfDiensten != null ? changes.productenOfDiensten
				: original.productenOfDiensten;
		result.vertrouwelijkheidaanduiding = changes.vertrouwelijkheidaanduiding != null
				? changes.vertrouwelijkheidaanduiding
				: original.vertrouwelijkheidaanduiding;
		result.betalingsindicatie = changes.betalingsindicatie != null ? changes.betalingsindicatie
				: original.betalingsindicatie;
//		result.laatsteBetaaldatum  = changes.laatsteBetaaldatum!= null ? changes.laatsteBetaaldatum : original.laatsteBetaaldatum;
		result.zaakgeometrie = changes.zaakgeometrie != null ? changes.zaakgeometrie : original.zaakgeometrie;
		result.verlenging = changes.verlenging != null ? changes.verlenging : original.verlenging;
		result.opschorting = changes.opschorting != null ? changes.opschorting : original.opschorting;
		result.selectielijstklasse = changes.selectielijstklasse != null ? changes.selectielijstklasse
				: original.selectielijstklasse;
		result.hoofdzaak = changes.hoofdzaak != null ? changes.hoofdzaak : original.hoofdzaak;
		result.relevanteAndereZaken = changes.relevanteAndereZaken != null ? changes.relevanteAndereZaken
				: original.relevanteAndereZaken;
		result.kenmerk = changes.kenmerk != null ? changes.kenmerk : original.kenmerk;
		result.archiefnominatie = changes.archiefnominatie != null ? changes.archiefnominatie
				: original.archiefnominatie;
		result.archiefstatus = changes.archiefstatus != null ? changes.archiefstatus : original.archiefstatus;
		result.archiefactiedatum = changes.archiefactiedatum != null ? changes.archiefactiedatum
				: original.archiefactiedatum;
		return result;
	}
}
