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

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ZgwZaakType {

	@SerializedName("omschrijving")
	@Expose
	public String omschrijving;
	@SerializedName("identificatie")
	@Expose
	public String identificatie;
	@SerializedName("url")
	@Expose
	public String url;
	@SerializedName("beginGeldigheid")
	@Expose
	public Date beginGeldigheid;
	@SerializedName("eindeGeldigheid")
	@Expose
	public Date eindeGeldigheid;

	@SerializedName("statustypen")
	@Expose
	public List<String> statustypen = null;
	@SerializedName("resultaattypen")
	@Expose
	public List<String> resultaattypen = null;
	@SerializedName("informatieobjecttypen")
	@Expose
	public List<String> informatieobjecttypen = null;
	@SerializedName("roltypen")
	@Expose
	public List<String> roltypen = null;
}
