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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ZgwEnkelvoudigInformatieObject {

	@SerializedName("url")
	@Expose
	public String url;

	@SerializedName("identificatie")
	@Expose
	public String identificatie;

	@SerializedName("bronorganisatie")
	@Expose
	public String bronorganisatie;

	@SerializedName("creatiedatum")
	@Expose
	public String creatiedatum;

	@SerializedName("titel")
	@Expose
	public String titel;

	@SerializedName("vertrouwelijkheidaanduiding")
	@Expose
	public String vertrouwelijkheidaanduiding;

	@SerializedName("auteur")
	@Expose
	public String auteur;

	@SerializedName("formaat")
	@Expose
	public String formaat;

	@SerializedName("taal")
	@Expose
	public String taal;

	@SerializedName("bestandsnaam")
	@Expose
	public String bestandsnaam;

	@SerializedName("informatieobjecttype")
	@Expose
	public String informatieobjecttype;

	@SerializedName("status")
	@Expose
	public String status;

	@SerializedName("versie")
	@Expose
	public String versie;

	@SerializedName("ontvangstdatum")
	@Expose
	public String ontvangstdatum;

	@SerializedName("verzenddatum")
	@Expose
	public String verzenddatum;

	@SerializedName("indicatieGebruiksrecht")
	@Expose
	public String indicatieGebruiksrecht;

	@SerializedName("beschrijving")
	@Expose
	public String beschrijving;

	@SerializedName("inhoud")
	@Expose
	public String inhoud;

	@SerializedName("lock")
	@Expose
	public String lock;

	@SerializedName("locked")
	@Expose
	public boolean locked;

    @SerializedName("bestandsomvang")
    @Expose
    public Long bestandsomvang;
}
