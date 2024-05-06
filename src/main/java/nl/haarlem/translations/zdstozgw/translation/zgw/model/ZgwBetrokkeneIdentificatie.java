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
public class ZgwBetrokkeneIdentificatie  {
	// natuurlijk_persoon
	@SerializedName("inpBsn")
	@Expose
	public String inpBsn;

	@SerializedName("anpIdentificatie")
	@Expose
	public String anpIdentificatie;

	@SerializedName("inpA_nummer")
	@Expose
	public String inpA;

	@SerializedName("geslachtsnaam")
	@Expose
	public String geslachtsnaam;

	@SerializedName("achternaam")
	@Expose
	public String achternaam;

	@SerializedName("voorvoegselGeslachtsnaam")
	@Expose
	public String voorvoegselGeslachtsnaam;

	@SerializedName("voorletters")
	@Expose
	public String voorletters;

	@SerializedName("voornamen")
	@Expose
	public String voornamen;

	@SerializedName("geslachtsaanduiding")
	@Expose
	public String geslachtsaanduiding;

	@SerializedName("geboortedatum")
	@Expose
	public String geboortedatum;

	@SerializedName("verblijfsadres")
	@Expose
	public ZgwAdres verblijfsadres;

	@SerializedName("sub")
	@Expose
	public String sub;

	@Expose
	public String isGehuisvestIn;

	@Expose
	public String naam;

	// niet_natuurlijk_persoon
	@Expose
	public String innNnpId;

	@Expose
	public String annIdentificatie;

	@Expose
	public String statutaireNaam;

	@Expose
	public String innRechtsvorm;

	@Expose
	public String bezoekadres;
	
	@Expose
	public ZgwAdresBuitenland subVerblijfBuitenland;	

	// vestiging
	@Expose
	public String vestigingsNummer;

	@Expose
	public String[] handelsnaam;

	//public ZgwAdres verblijfsadres;
	//public ZgwAdresBuitenland subVerblijfBuitenland;	
	
	// organisatorische_eenheid
	@Expose	
	public String identificatie;	
	//public String naam;		
	//public String isGehuisvestIn;	
	
	// medewerker
	//public String identificatie;
	//public String achternaam;	
	//public String voorletters;
	@Expose		
	public String voorvoegselAchternaam;
		
}
