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

import lombok.Data;

@Data
public class Translation {
	@Expose
	public String translation;
	@Expose
	public String path;
	@Expose
	public String soapaction;
	@Expose
	public String applicatie;
	@Expose
	public String implementation;
	@Expose
	public String legacyservice;
	@Expose
	public List<KeyValuePair> parameters;
	
	public String getParameterValue(String key) {
		for(KeyValuePair kvp : parameters) {
			if(kvp.key.equals(key)) {
				return kvp.value;
			}
		}
		return null;
	}
}