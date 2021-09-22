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
package nl.haarlem.translations.zdstozgw.translation.zgw.client;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.QueryResult;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwInformatieObjectType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwLock;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwResultaat;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwResultaatType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwRol;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwRolType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatus;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatusType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakPatch;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakPut;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakType;
import nl.haarlem.translations.zdstozgw.utils.StringUtils;

@Service
public class ZGWClient {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

	@Value("${openzaak.baseUrl}")
	private String baseUrl;

	@Value("${zgw.endpoint.roltype:/catalogi/api/v1/roltypen}")
	private String endpointRolType;

	@Value("${zgw.endpoint.rol:/zaken/api/v1/rollen}")
	private String endpointRol;

	@Value("${zgw.endpoint.zaaktype:/catalogi/api/v1/zaaktypen}")
	private String endpointZaaktype;

	@Value("${zgw.endpoint.status:/zaken/api/v1/statussen}")
	private String endpointStatus;

	@Value("${zgw.endpoint.resultaat:/zaken/api/v1/resultaten}")
	private String endpointResultaat;

	@Value("${zgw.endpoint.statustype:/catalogi/api/v1/statustypen}")
	private String endpointStatustype;

	@Value("${zgw.endpoint.resultaattype:/catalogi/api/v1/resultaattypen}")
	private String endpointResultaattype;

	@Value("${zgw.endpoint.zaakinformatieobject:/zaken/api/v1/zaakinformatieobjecten}")
	private String endpointZaakinformatieobject;

	@Value("${zgw.endpoint.enkelvoudiginformatieobject:/documenten/api/v1/enkelvoudiginformatieobjecten}")
	private String endpointEnkelvoudiginformatieobject;

	@Value("${zgw.endpoint.zaak:/zaken/api/v1/zaken}")
	private String endpointZaak;

	@Value("${zgw.endpoint.informatieobjecttype:/catalogi/api/v1/informatieobjecttypen}")
	private String endpointInformatieobjecttype;

	@Autowired
	RestTemplateService restTemplateService;

	private String post(String url, String json) {
		String debugName = "ZGWClient POST";
		json = debug.startpoint(debugName, json);
		url = debug.inputpoint("url", url);
		log.debug("POST: " + url + ", json: " + json);
		HttpEntity<String> entity = new HttpEntity<String>(json, this.restTemplateService.getHeaders());
		try {
			long startTime = System.currentTimeMillis();
			long[] exchangeDuration = new long[2];
			String finalUrl = url;
			String zgwResponse = (String) debug.endpoint(debugName, () -> {
				exchangeDuration[0] = System.currentTimeMillis();
				String response = this.restTemplateService.getRestTemplate().postForObject(finalUrl, entity, String.class);
				exchangeDuration[1] = System.currentTimeMillis();
				return response;
			});
			long endTime = System.currentTimeMillis();
			var duration = endTime - startTime;
			var message = "POST to: " + url + " took " + duration + " milliseconds";
			log.debug(message);
			debug.infopoint("Duration", message);
			log.debug("POST response: " + zgwResponse);
			return zgwResponse;
		} catch (HttpStatusCodeException hsce) {
			if(json!=null) {
				json = json.replace("{", "{\n").replace("\",", "\",\n").replace("\"}", "\"\n}");
			}
			var response = hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}",
					"\"\n}");
			var details = "--------------POST:\n" + url + "\n" + StringUtils.shortenLongString(json, StringUtils.MAX_ERROR_SIZE) + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("POST naar OpenZaak: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("POST naar OpenZaak: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("POST naar OpenZaak: " + url + " niet geslaagd", rae);
			throw new ConverterException("POST naar OpenZaak: " + url + " niet geslaagd", rae);
		}
	}

	private String get(String url, Map<String, String> parameters) {
		if (parameters != null) {
			url = getUrlWithParameters(url, parameters);
		}
		String debugName = "ZGWClient GET";
		debug.startpoint(debugName);
		url = debug.inputpoint("url", url);
		if (parameters != null) {
			for (String key : parameters.keySet()) {
				parameters.put(key, debug.inputpoint("Parameter " + key, parameters.get(key)));
			}
		}
		log.debug("GET: " + url);
		HttpEntity entity = new HttpEntity(this.restTemplateService.getHeaders());
		try {
			long startTime = System.currentTimeMillis();
			long[] exchangeDuration = new long[2];
			String finalUrl = url;
			String zgwResponse = (String) debug.endpoint(debugName, () -> {
				exchangeDuration[0] = System.currentTimeMillis();
				ResponseEntity<String> response = this.restTemplateService.getRestTemplate().exchange(finalUrl,
						HttpMethod.GET, entity, String.class);
				exchangeDuration[1] = System.currentTimeMillis();
				return response.getBody();
			});
			long endTime = System.currentTimeMillis();
			var duration = endTime - startTime;
			var message = "GET to: " + url + " took " + duration + " milliseconds";
			log.debug(message);
			debug.infopoint("Duration", message);
			log.debug("GET response: " + zgwResponse);
			return zgwResponse;
		} catch (HttpStatusCodeException hsce) {
			var response = hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}",
					"\"\n}");
			var details = "--------------GET:\n" + url + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("GET naar OpenZaak: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("GET naar OpenZaak: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("GET naar OpenZaak: " + url + " niet geslaagd", rae);
			throw new ConverterException("GET naar OpenZaak: " + url + " niet geslaagd", rae);
		}
	}

	private String delete(String url) {
		String debugName = "ZGWClient DELETE";
		debug.startpoint(debugName);
		url = debug.inputpoint("url", url);
		log.debug("DELETE: " + url);
		HttpEntity entity = new HttpEntity(this.restTemplateService.getHeaders());
		try {
			long startTime = System.currentTimeMillis();
			long[] exchangeDuration = new long[2];
			String finalUrl = url;
			String zgwResponse = (String) debug.endpoint(debugName, () -> {
				exchangeDuration[0] = System.currentTimeMillis();
				ResponseEntity<String> response = this.restTemplateService.getRestTemplate().exchange(finalUrl,
						HttpMethod.DELETE, entity, String.class);
				exchangeDuration[1] = System.currentTimeMillis();
				return response.getBody();
			});
			long endTime = System.currentTimeMillis();
			var duration = endTime - startTime;
			var message = "DELETE to: " + url + " took " + duration + " milliseconds";
			log.debug(message);
			debug.infopoint("Duration", message);
			log.debug("DELETE response: " + zgwResponse);
			return zgwResponse;
		} catch (HttpStatusCodeException hsce) {
			var response = hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}",
					"\"\n}");
			var details = "--------------DELETE:\n" + url + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("DELETE naar OpenZaak: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("DELETE naar OpenZaak: " + url + " gaf foutmelding:" + hsce.toString(),
					details, hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("DELETE naar OpenZaak: " + url + " niet geslaagd", rae);
			throw new ConverterException("DELETE naar OpenZaak: " + url + " niet geslaagd", rae);
		}
	}

	private String put(String url, String json) {
		String debugName = "ZGWClient PUT";
		json = debug.startpoint(debugName, json);
		url = debug.inputpoint("url", url);
		log.debug("PUT: " + url + ", json: " + json);
		HttpEntity<String> entity = new HttpEntity<String>(json, this.restTemplateService.getHeaders());
		try {
			long startTime = System.currentTimeMillis();
			long[] exchangeDuration = new long[2];
			String finalUrl = url;
			String zgwResponse = (String) debug.endpoint(debugName, () -> {
				exchangeDuration[0] = System.currentTimeMillis();
				ResponseEntity<String> response = this.restTemplateService.getRestTemplate().exchange(finalUrl,
						HttpMethod.PUT, entity, String.class);
				exchangeDuration[1] = System.currentTimeMillis();
				return response.getBody();
			});
			long endTime = System.currentTimeMillis();
			var duration = endTime - startTime;
			var message = "PUT to: " + url + " took " + duration + " milliseconds";
			log.debug(message);
			debug.infopoint("Duration", message);
			log.debug("PUT response: " + zgwResponse);
			return zgwResponse;
		} catch (HttpStatusCodeException hsce) {
			json = json.replace("{", "{\n").replace("\",", "\",\n").replace("\"}", "\"\n}");
			var response = hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}",
					"\"\n}");
			var details = "--------------PUT:\n" + url + "\n" + StringUtils.shortenLongString(json, StringUtils.MAX_ERROR_SIZE) + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("PUT naar OpenZaak: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("PUT naar OpenZaak: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("PUT naar OpenZaak: " + url + " niet geslaagd", rae);
			throw new ConverterException("PUT naar OpenZaak: " + url + " niet geslaagd", rae);
		}
	}


	private String patch(String url, String json) {
		String debugName = "ZGWClient PATCH";
		json = debug.startpoint(debugName, json);
		url = debug.inputpoint("url", url);
		log.debug("PATCH: " + url + ", json: " + json);
		HttpEntity<String> entity = new HttpEntity<String>(json, this.restTemplateService.getHeaders());
		try {
			long startTime = System.currentTimeMillis();
			long[] exchangeDuration = new long[2];
			String finalUrl = url;
			String zgwResponse = (String) debug.endpoint(debugName, () -> {
				exchangeDuration[0] = System.currentTimeMillis();
				ResponseEntity<String> response = this.restTemplateService.getRestTemplate().exchange(finalUrl,
						HttpMethod.PATCH, entity, String.class);
				exchangeDuration[1] = System.currentTimeMillis();
				return response.getBody();
			});
			long endTime = System.currentTimeMillis();
			var duration = endTime - startTime;
			var message = "PATCH to: " + url + " took " + duration + " milliseconds";
			log.debug(message);
			debug.infopoint("Duration", message);
			log.debug("PATCH response: " + zgwResponse);
			return zgwResponse;
		} catch (HttpStatusCodeException hsce) {
			json = json.replace("{", "{\n").replace("\",", "\",\n").replace("\"}", "\"\n}");
			var response = hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}",
					"\"\n}");
			var details = "--------------PATCH:\n" + url + "\n" + StringUtils.shortenLongString(json, StringUtils.MAX_ERROR_SIZE) + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("PATCH naar OpenZaak: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("PATCH naar OpenZaak: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("PATCH naar OpenZaak: " + url + " niet geslaagd", rae);
			throw new ConverterException("PATCH naar OpenZaak: " + url + " niet geslaagd", rae);
		}
	}
		
	private String getUrlWithParameters(String url, Map<String, String> parameters) {
		for (String key : parameters.keySet()) {
			url += !url.contains("?") ? "?" + key + "=" + parameters.get(key) : "&" + key + "=" + parameters.get(key);
		}
		return url;
	}

	public ZgwEnkelvoudigInformatieObject getZgwEnkelvoudigInformatieObjectByIdentiticatie(String identificatie) {
		log.debug("get zaakdocument #" + identificatie);

		if(identificatie == null || identificatie.length() == 0) {
			throw new ConverterException("getZgwEnkelvoudigInformatieObjectByIdentiticatie without an identificatie");
		}

		var documentJson = get(
				this.baseUrl + this.endpointEnkelvoudiginformatieobject + "?identificatie=" + identificatie, null);
		Type type = new TypeToken<QueryResult<ZgwEnkelvoudigInformatieObject>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwEnkelvoudigInformatieObject> queryResult = gson.fromJson(documentJson, type);

		if (queryResult.getResults() != null && queryResult.getResults().size() == 1) {
			return queryResult.getResults().get(0);
		}
		log.debug("zaakdocument #" + identificatie + " not found!");
		return null;
	}

	public ZgwRolType getRolTypeByUrl(String url) {
		var rolTypeJson = get(url, null);
		Gson gson = new Gson();
		ZgwRolType result = gson.fromJson(rolTypeJson, ZgwRolType.class);
		if(result == null) {
			throw new ConverterException("Roltype met url:" + url + " niet gevonden!");
		}
		return result;
	}

	public ZgwZaak getZaakByUrl(String url) {
		var zaakJson = get(url, null);
		Gson gson = new Gson();
		ZgwZaak result = gson.fromJson(zaakJson, ZgwZaak.class);
		if(result == null) {
			throw new ConverterException("Zaak met url:" + url + " niet gevonden!");
		}
		return result;
	}

	public String getBas64Inhoud(String url) {
		String debugName = "ZGWClient GET(BASE64)";
		debug.startpoint(debugName);
		url = debug.inputpoint("url", url);
		log.debug("GET(BASE64): " + url);
		HttpEntity entity = new HttpEntity(this.restTemplateService.getHeaders());
		try {
			long startTime = System.currentTimeMillis();
			String finalUrl = url;
			byte[] data = (byte[]) debug.endpoint(debugName, () -> {
				return this.restTemplateService.getRestTemplate()
						.exchange(finalUrl, HttpMethod.GET, entity, byte[].class).getBody();
			});
			long endTime = System.currentTimeMillis();
			var duration = endTime - startTime;
			var message = "GET from: " + url + " took " + duration + " milliseconds";			
			log.debug("BASE64 INHOUD DOWNLOADED:" + (data == null ? "[null], is openzaak dms-broken?" : data.length + " bytes"));
			return java.util.Base64.getEncoder().encodeToString(data);

		} catch (HttpStatusCodeException hsce) {
			var response = hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}",
					"\"\n}");
			var details = "--------------GET:\n" + url + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("GET(BASE64) naar OpenZaak: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("GET(BASE64) naar OpenZaak: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("GET(BASE64) naar OpenZaak: " + url + " niet geslaagd", rae);
			throw new ConverterException("GET(BASE64) naar OpenZaak: " + url + " niet geslaagd", rae);
		}
	}

	public ZgwZaak getZaak(Map<String, String> parameters) {
		ZgwZaak result = null;
		var zaakJson = get(this.baseUrl + this.endpointZaak, parameters);
		Type type = new TypeToken<QueryResult<ZgwZaak>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwZaak> queryResult = gson.fromJson(zaakJson, type);
		if (queryResult.getResults() != null &&  queryResult.getResults().size() == 1) {
			result = queryResult.getResults().get(0);
		}
		return result;
	}

	public ZgwZaak addZaak(ZgwZaak zgwZaak) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwZaak);
		String response = this.post(this.baseUrl + this.endpointZaak, json);
		return gson.fromJson(response, ZgwZaak.class);
	}

	
	public void patchZaak(String zaakUuid, ZgwZaakPut zaak) {
		Gson gson = new Gson();
		String json = gson.toJson(zaak);
		this.patch(this.baseUrl + this.endpointZaak + "/" + zaakUuid, json);
	}
	public ZgwRol addZgwRol(ZgwRol zgwRol) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwRol);
		String response = this.post(this.baseUrl + this.endpointRol, json);
		return gson.fromJson(response, ZgwRol.class);
	}

	public ZgwEnkelvoudigInformatieObject addZaakDocument(
			ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwEnkelvoudigInformatieObject);
		String response = this.post(this.baseUrl + this.endpointEnkelvoudiginformatieobject, json);
		return gson.fromJson(response, ZgwEnkelvoudigInformatieObject.class);
	}

	public ZgwZaakInformatieObject addDocumentToZaak(ZgwZaakInformatieObject zgwZaakInformatieObject) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwZaakInformatieObject);
		String response = this.post(this.baseUrl + this.endpointZaakinformatieobject, json);
		return gson.fromJson(response, ZgwZaakInformatieObject.class);
	}

	public List<ZgwZaakInformatieObject> getZgwZaakInformatieObjects(Map<String, String> parameters) {
		// Fetch EnkelvoudigInformatieObjects
		var zaakInformatieObjectJson = get(this.baseUrl + this.endpointZaakinformatieobject, parameters);

		Gson gson = new Gson();
		Type documentList = new TypeToken<ArrayList<ZgwZaakInformatieObject>>() {
		}.getType();
		return gson.fromJson(zaakInformatieObjectJson, documentList);
	}

	public ZgwEnkelvoudigInformatieObject getZaakDocumentByUrl(String url) {
		var zaakInformatieObjectJson = get(url, null);
		Gson gson = new Gson();
		var result = gson.fromJson(zaakInformatieObjectJson, ZgwEnkelvoudigInformatieObject.class);
		if(result == null) {
			throw new ConverterException("ZaakDocument met url:" + url + " niet gevonden!");
		}
		return result;
	}

	public List<ZgwStatusType> getStatusTypes(Map<String, String> parameters) {
		var statusTypeJson = get(this.baseUrl + this.endpointStatustype, parameters);
		Type type = new TypeToken<QueryResult<ZgwStatusType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwStatusType> queryResult = gson.fromJson(statusTypeJson, type);
		if(queryResult.getResults() == null) {
			return new ArrayList<ZgwStatusType>();
		}
		return queryResult.getResults();
	}

	public List<ZgwResultaatType> getResultaatTypes(Map<String, String> parameters) {
		var restulaatTypeJson = get(this.baseUrl + this.endpointResultaattype, parameters);
		Type type = new TypeToken<QueryResult<ZgwResultaatType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwResultaatType> queryResult = gson.fromJson(restulaatTypeJson, type);
		if(queryResult.getResults() == null) {
			return new ArrayList<ZgwResultaatType>();
		}
		return queryResult.getResults();
	}

	public List<ZgwResultaat> getResultaten(Map<String, String> parameters) {
		var restulaatJson = get(this.baseUrl + this.endpointResultaat, parameters);
		Type type = new TypeToken<QueryResult<ZgwResultaat>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwResultaat> queryResult = gson.fromJson(restulaatJson, type);
		if(queryResult.getResults() == null) {
			return new ArrayList<ZgwResultaat>();
		}
		return queryResult.getResults();
	}


	public List<ZgwStatus> getStatussen(Map<String, String> parameters) {
		var statusTypeJson = get(this.baseUrl + this.endpointStatus, parameters);
		Type type = new TypeToken<QueryResult<ZgwStatus>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwStatus> queryResult = gson.fromJson(statusTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwStatus>();
		}
		return queryResult.getResults();
	}

	public <T> T getResource(String url, Class<T> resourceType) {
		Gson gson = new Gson();
		String response = get(url, null);
		return gson.fromJson(response, resourceType);
	}

	public ZgwStatus addZaakStatus(ZgwStatus zgwSatus) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwSatus);
		String response = this.post(this.baseUrl + this.endpointStatus, json);
		return gson.fromJson(response, ZgwStatus.class);
	}

	public ZgwResultaat addZaakResultaat(ZgwResultaat zgwResultaat) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwResultaat);
		String response = this.post(this.baseUrl + this.endpointResultaat, json);
		return gson.fromJson(response, ZgwResultaat.class);
	}

	public List<ZgwZaakType> getZaakTypes(Map<String, String> parameters) {
		var zaakTypeJson = get(this.baseUrl + this.endpointZaaktype, parameters);
		Type type = new TypeToken<QueryResult<ZgwZaakType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwZaakType> queryResult = gson.fromJson(zaakTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwZaakType>();
		}
		return queryResult.getResults();
	}

	public ZgwZaakType getZaakTypeByUrl(String url) {
		var zaakTypeJson = get(url, null);
		Gson gson = new Gson();
		ZgwZaakType result = gson.fromJson(zaakTypeJson, ZgwZaakType.class);
		return result;
	}


	public ZgwZaakType getZaakTypeByZaak(ZgwZaak zgwZaak) {
		return getZaakTypeByUrl(zgwZaak.getZaaktype());
	}

	public List<ZgwRol> getRollen(Map<String, String> parameters) {
		var zaakTypeJson = get(this.baseUrl + this.endpointRol, parameters);
		Type type = new TypeToken<QueryResult<ZgwRol>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwRol> queryResult = gson.fromJson(zaakTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwRol>();
		}
		return queryResult.getResults();
	}

	public List<ZgwRolType> getRolTypen(Map<String, String> parameters) {
		var rolTypeJson = get(this.baseUrl + this.endpointRolType, parameters);
		Type type = new TypeToken<QueryResult<ZgwRolType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwRolType> queryResult = gson.fromJson(rolTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwRolType>();
		}
		return queryResult.getResults();
	}

	public ZgwRolType getRolTypeByZaaktypeAndOmschrijving(ZgwZaakType zgwZaakType, String omschrijving) {
		for (String found : zgwZaakType.roltypen) {
			ZgwRolType roltype = getRolTypeByUrl(found);
			if (roltype.omschrijving.equals(omschrijving)) {
				return roltype;
			}
		}
		return null;
	}

	public void updateZaak(String zaakUuid, ZgwZaakPut zaak) {
		Gson gson = new Gson();
		String json = gson.toJson(zaak);
		this.put(this.baseUrl + this.endpointZaak + "/" + zaakUuid, json);
	}

	public void deleteRol(String uuid) {
		if (uuid == null) {
			throw new ConverterException("rol uuid may not be null");
		}
		delete(this.baseUrl + this.endpointRol + "/" + uuid);
	}

	public void deleteZaakResultaat(String uuid) {
		if (uuid == null) {
			throw new ConverterException("zaakresultaat uuid may not be null");
		}
		delete(this.baseUrl + this.endpointResultaat + "/" + uuid);
	}

	public List<ZgwZaakInformatieObject> getZaakInformatieObjectenByZaak(String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);
		return this.getZgwZaakInformatieObjects(parameters);
	}

	public ZgwZaak getZaakByIdentificatie(String zaakIdentificatie) {
		if(zaakIdentificatie == null || zaakIdentificatie.length() == 0) {
			throw new ConverterException("getZaakByIdentificatie without an identificatie");
		}


		Map<String, String> parameters = new HashMap();
		parameters.put("identificatie", zaakIdentificatie);

		ZgwZaak zgwZaak = this.getZaak(parameters);

		if (zgwZaak == null) {
			return null;
		}

		// When Verlenging/Opschorting not set, zgw returns object with empty values, in
		// stead of null.
		// This will cause issues when response of getzaakdetails is used for
		// updatezaak.
		if (zgwZaak.getVerlenging().getDuur() == null || zgwZaak.getVerlenging().getReden().equals("")) {
			zgwZaak.setVerlenging(null);
		}
		if (zgwZaak.getOpschorting().getReden().equals("")) {
			zgwZaak.setOpschorting(null);
		}
		return zgwZaak;
	}

	public ZgwZaakInformatieObject getZgwZaakInformatieObjectByEnkelvoudigInformatieObjectUrl(String url) {
		Map<String, String> parameters = new HashMap();
		parameters.put("informatieobject", url);
		var zaakinformatieobjecten = this.getZgwZaakInformatieObjects(parameters);
		if(zaakinformatieobjecten.size() == 0) {
			throw new ConverterException("Geen zaakinformatieobject gevonden voor de url: '" + url + "'");
		}
		return zaakinformatieobjecten.get(0);
	}

	public List<ZgwStatusType> getStatusTypesByZaakType(ZgwZaakType zgwZaakType) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zgwZaakType.url);
		List<ZgwStatusType> statustypes = this.getStatusTypes(parameters);
		return statustypes;
	}
	
	
	public ZgwStatusType getLastStatusTypeByZaakType(ZgwZaakType zgwZaakType) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zgwZaakType.url);
		for(ZgwStatusType statustype : this.getStatusTypes(parameters)) {
			if("true".equals(statustype.isEindstatus)) {
				return statustype;
			}			
		}		
		return null;
	}	

	public ZgwStatusType getStatusTypeByZaakTypeAndOmschrijving(ZgwZaakType zaakType, String statusOmschrijving, String verwachteVolgnummer) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zaakType.url);
		List<ZgwStatusType> statustypes = this.getStatusTypes(parameters);

		for (ZgwStatusType statustype : statustypes) {
			log.debug("opgehaald:" + statustype.omschrijving + " zoeken naar: " + statusOmschrijving);
			if (statustype.omschrijving.startsWith(statusOmschrijving)) {
				try {
					if (statustype.volgnummer != Integer.valueOf(verwachteVolgnummer)) {
						debugWarning("Zaakstatus verschil in zgw-statustype met omschrijving: " + statustype.omschrijving
								+ " met volgnummer #" + statustype.volgnummer + " en het meegestuurde omschrijving:'" + statusOmschrijving + "' volgnummer: '"
								+ Integer.valueOf(verwachteVolgnummer) + "'");
					}
				} catch (java.lang.NumberFormatException nft) {
					debugWarning("Zaakstatus verschil in zgw-statustype met omschrijving: " + statustype.omschrijving
							+ " ongeldig volnummer: '" + verwachteVolgnummer + "'");
				}
				log.debug("gevonden:" + statustype.omschrijving + " zoeken naar: " + statusOmschrijving);
				return statustype;
			}
		}
		throw new ConverterException("zaakstatus niet gevonden voor omschrijving: '" + statusOmschrijving + "'");
	}


	public ZgwResultaatType getResultaatTypeByZaakTypeAndOmschrijving(ZgwZaakType zaakType, String resultaatOmschrijving) {
		var omschrijving = resultaatOmschrijving;
		if(omschrijving.length() > 20) {
			// maximum length of openzaak is 20 characters
			omschrijving = omschrijving.substring(0, 20);
		}
		for (String found: zaakType.resultaattypen) {
			ZgwResultaatType resultaatType = getResultaatTypeByUrl(found);
			log.debug("opgehaald:" + resultaatType.omschrijving + " zoeken naar: " + omschrijving + "' (ingekort van: " + resultaatOmschrijving + ")");

			// in some applications, the omschrijving can not be as long as we want.....
			if (resultaatType.omschrijving.startsWith(omschrijving)) {
				log.debug("gevonden:" + resultaatType.omschrijving + " zoeken naar: " + omschrijving);
				return resultaatType;
			}
		}
		throw new ConverterException("zaakresultaat niet gevonden voor omschrijving: '" + resultaatOmschrijving + "'");
	}

	private ZgwResultaatType getResultaatTypeByUrl(String url) {
		var resultaatTypeJson = get(url, null);
		Gson gson = new Gson();
		ZgwResultaatType result = gson.fromJson(resultaatTypeJson, ZgwResultaatType.class);
		if(result == null) {
			throw new ConverterException("ZgwResultaatType met url:" + url + " niet gevonden!");
		}
		return result;

	}

	public List<ZgwResultaat> getResultatenByZaakUrl(String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);
		return this.getResultaten(parameters);
	}

	public List<ZgwRol> getRollenByZaakUrl(String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);

		return this.getRollen(parameters);
	}

	public List<ZgwRol> getRollenByBsn(String bsn) {
		Map<String, String> parameters = new HashMap();
		parameters.put("betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", bsn);
		return this.getRollen(parameters);
	}

	public ZgwRol getRolByZaakUrlAndRolTypeUrl(String zaakUrl, String rolTypeUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);
		parameters.put("roltype", rolTypeUrl);

		return this.getRollen(parameters).stream().findFirst().orElse(null);
	}

	public List<ZgwStatus> getStatussenByZaakUrl(String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);

		return this.getStatussen(parameters);
	}

	public ZgwZaakType getZgwZaakTypeByIdentificatie(String identificatie) {
		if(identificatie == null || identificatie.length() == 0) {
			throw new ConverterException("getZgwZaakTypeByIdentificatie without an identificatie");
		}

		Map<String, String> parameters = new HashMap<>();
		parameters.put("identificatie", identificatie);
		parameters.put("status", "definitief");
		var types = this.getZaakTypes(parameters);

		var now = new Date();
		var active = new ArrayList<ZgwZaakType>();
		for(ZgwZaakType zaaktype : types) {
			if(zaaktype.beginGeldigheid.before(now)){
				if(zaaktype.eindeGeldigheid == null || zaaktype.eindeGeldigheid.after(now)){
					active.add(zaaktype);
				}
				else {
					debugWarning("zaaktype met identificatie: '" + identificatie + "' heeft een versie die al beeindigd is:" + zaaktype.beginGeldigheid + " (" + zaaktype.url + ")");
				}
			}
			else {
				debugWarning("zaaktype met identificatie: '" + identificatie + "' heeft een versie die nog moet beginnen:" + zaaktype.beginGeldigheid + " (" + zaaktype.url + ")");
			}
		}
		if (active.size() == 1) {
			return active.get(0);
		}
		else if (active.size() > 1) {
			throw new ConverterException("meerdere active zaaktype versies gevonden met de identificatie: '" + identificatie + "'");
		}
		else {
			return null;
		}
	}

	public ZgwInformatieObjectType getZgwInformatieObjectTypeByOmschrijving(ZgwZaakType zaaktype, String omschrijving) {
		for (String found : zaaktype.informatieobjecttypen ) {
			ZgwInformatieObjectType ziot = getZgwInformatieObjectTypeByUrl(found);
			log.debug("gevonden ZgwInformatieObjectType met omschrijving: '" + ziot.omschrijving + "'");
			//if (omschrijving.equals(ziot.omschrijving)) {
			if (omschrijving.equalsIgnoreCase(ziot.omschrijving)) {
				return ziot;
			}
		}
		return null;
	}

	public ZgwInformatieObjectType getZgwInformatieObjectTypeByUrl(String url) {
		var documentType = get(url, null);
		Gson gson = new Gson();
		ZgwInformatieObjectType result = gson.fromJson(documentType, ZgwInformatieObjectType.class);
		return result;
	}

	public ZgwLock getZgwInformatieObjectLock(ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject) {
		var lock = post(zgwEnkelvoudigInformatieObject.url + "/lock", null);
		Gson gson = new Gson();
		ZgwLock result = gson.fromJson(lock, ZgwLock.class);
		return result;
	}

	public void getZgwInformatieObjectUnLock(ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject, ZgwLock zgwLock) {
			Gson gson = new Gson();
			String json = gson.toJson(zgwLock);
			var lock = post(zgwEnkelvoudigInformatieObject.url + "/unlock", json);
			Object result = gson.fromJson(lock, Object.class);
			return;
	}

	public ZgwEnkelvoudigInformatieObject putZaakDocument(ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwEnkelvoudigInformatieObject);
		String response = this.put(zgwEnkelvoudigInformatieObject.url, json);
		return gson.fromJson(response, ZgwEnkelvoudigInformatieObject.class);
	}

	private void debugWarning(String message) {
		log.info("[processing warning] " + message);
		debug.infopoint("Warning", message);
	}

	public void addChildZaakToZaak(ZgwZaak zgwZaak, ZgwZaakPatch zgwChildZaak) {
		zgwChildZaak.hoofdzaak = zgwZaak.url;
		if(zgwChildZaak.verlenging != null) {
			if(zgwChildZaak.verlenging.duur == null) {
				zgwChildZaak.verlenging = null;
			}
		}
		this.patchZaak(zgwChildZaak.uuid, zgwChildZaak);
	}
}
