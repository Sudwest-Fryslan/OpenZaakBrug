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
package nl.haarlem.translations.zdstozgw.config.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Configuration {
	@Expose
	public String requestHandlerImplementation = null;
	@Expose
	public List<Organisatie> organisaties = null;
	@Expose
	public ZgwRolOmschrijving zgwRolOmschrijving = null;
	@Expose
	public List<ZaaktypeMetCoalesceResultaat> beeindigZaakWanneerEinddatum = null;	
	@Expose
	public List<ZaaktypeMetCoalesceResultaat> einddatumEnResultaatWanneerLastStatus = null;	
	@Expose
	public List<TranslateVerblijfsadresForZaaktype> translateVerblijfsadresForZaaktype = null;
	@Expose
	public Replication replication = null;
	@Expose
	public List<Translation> translations = null;
	
	public String getTranslationsString() {
		String combinations = "";
		for (Translation t : this.getTranslations()) {
			combinations += "\n\tpath: './" + t.getPath() + "' soapaction: '" + t.getSoapaction() + "'";
		}
		return combinations;
	}
}
