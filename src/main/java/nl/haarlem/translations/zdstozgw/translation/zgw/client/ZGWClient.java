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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.haarlem.translations.zdstozgw.translation.zds.model.Endpoint;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsStuurgegevens;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.*;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import nl.haarlem.translations.zdstozgw.config.StufHeader;
import nl.haarlem.translations.zdstozgw.config.model.Configuration;
import nl.haarlem.translations.zdstozgw.config.model.Organisatie;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.debug.Debugger;
import nl.haarlem.translations.zdstozgw.utils.StringUtils;

import org.apache.tomcat.util.json.JSONParser;
import org.springframework.web.client.RestTemplate;
//import org.json.simple.JSONObject;

@Service
public class ZGWClient {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Debugger debug = Debugger.getDebugger(MethodHandles.lookup().lookupClass());

	@Value("${zgw.registry.zaken.url}")
	private @Getter String zakenUrl;
	@Value("${zgw.registry.zaken.jwt.url:#{null}}")
	private String zakenUrlJwtUrl;
	@Value("${zgw.registry.zaken.jwt.issuer}")
	private String zakenUrlJwtIssuer;
	@Value("${zgw.registry.zaken.jwt.secret}")
	private String zakenUrlJwtSecret;	
	
	@Value("${zgw.registry.documenten.url}")
	private @Getter String documentenUrl;
	@Value("${zgw.registry.documenten.jwt.url:#{null}}")
	private String documentenUrlJwtUrl;
	@Value("${zgw.registry.documenten.jwt.issuer}")
	private String documentenUrlJwtIssuer;
	@Value("${zgw.registry.documenten.jwt.secret}")
	private String documentenUrlJwtSecret;	
	
	@Value("${zgw.registry.catalogi.url}")
	private @Getter String catalogiUrl;
	@Value("${zgw.registry.catalogi.jwt.url:#{null}}")
	private String catalogiUrlJwtUrl;
	@Value("${zgw.registry.catalogi.jwt.issuer}")
	private String catalogiUrlJwtIssuer;
	@Value("${zgw.registry.catalogi.jwt.secret}")
	private String catalogiUrlJwtSecret;	
	
	@Value("${zgw.registry.besluiten.url}")
	private @Getter String besluitenUrl;
	@Value("${zgw.registry.besluiten.jwt.url:#{null}}")
	private String besluitenUrlJwtUrl;
	@Value("${zgw.registry.besluiten.jwt.issuer}")
	private String besluitenUrlJwtIssuer;
	@Value("${zgw.registry.besluiten.jwt.secret}")
	private String besluitenUrlJwtSecret;

	@Value("${zgw.endpoint.catalogus:/api/v1/catalogussen}")
	private @Getter String endpointCatalogus;
	
	@Value("${zgw.endpoint.roltype:/api/v1/roltypen}")
	private @Getter String endpointRolType;

	@Value("${zgw.endpoint.rol:/api/v1/rollen}")
	private @Getter String endpointRol;

	@Value("${zgw.endpoint.zaaktype:/api/v1/zaaktypen}")
	private @Getter String endpointZaaktype;

	@Value("${zgw.endpoint.status:/api/v1/statussen}")
	private @Getter String endpointStatus;

	@Value("${zgw.endpoint.resultaat:/api/v1/resultaten}")
	private @Getter String endpointResultaat;

	@Value("${zgw.endpoint.statustype:/api/v1/statustypen}")
	private @Getter String endpointStatustype;

	@Value("${zgw.endpoint.resultaattype:/api/v1/resultaattypen}")
	private @Getter String endpointResultaattype;

	@Value("${zgw.endpoint.zaakinformatieobject:/api/v1/zaakinformatieobjecten}")
	private @Getter String endpointZaakinformatieobject;

	@Value("${zgw.endpoint.enkelvoudiginformatieobject:/api/v1/enkelvoudiginformatieobjecten}")
	private @Getter String endpointEnkelvoudiginformatieobject;

	@Value("${zgw.endpoint.objectinformatieobject:/api/v1/objectinformatieobjecten}")
	private @Getter String endpointObjectinformatieobject;

	@Value("${zgw.endpoint.zaak:/api/v1/zaken}")
	private @Getter String endpointZaak;

	@Value("${zgw.endpoint.informatieobjecttype:/api/v1/informatieobjecttypen}")
	private @Getter String endpointInformatieobjecttype;

	@Value("${nl.haarlem.translations.zdstozgw.additional-call-to-retrieve-related-object-informatie-objecten-for-caching:true}")
	public Boolean additionalCallToRetrieveRelatedObjectInformatieObjectenForCaching;

	@Autowired
	RestTemplateService restTemplateService;
	
	
//	public ZgwAuthorization getAuthorization(String url, String rsin) {
//		ZgwAuthorization authorization = getAuthorization(url);
//		var catalogus = 
//		//authorization.setCatalogusUrl(catalogus.url);
//		//authorization.setCatalogusRsin(rsin);
//		return authorization;
//	}	
	
	public ZgwAuthorization getAuthorization(Configuration config, ZdsStuurgegevens stuurgegevens) {
		String rsin = StufHeader.getRSIN(config, stuurgegevens);
		String applicatie =  StufHeader.getApplicatie(stuurgegevens);
		String gebruiker =  StufHeader.getGebruiker(stuurgegevens);
		ZgwAuthorization authorization = new ZgwAuthorization();
		
		authorization.AddZgwAuthorization(zakenUrl, zakenUrlJwtUrl, zakenUrlJwtIssuer, zakenUrlJwtSecret, gebruiker);
		authorization.AddZgwAuthorization(documentenUrl, documentenUrlJwtUrl, documentenUrlJwtIssuer, documentenUrlJwtSecret, gebruiker);
		authorization.AddZgwAuthorization(catalogiUrl, catalogiUrlJwtUrl, catalogiUrlJwtIssuer, catalogiUrlJwtSecret, gebruiker);
		authorization.AddZgwAuthorization(besluitenUrl, besluitenUrlJwtUrl, besluitenUrlJwtIssuer, besluitenUrlJwtSecret, gebruiker);		
		
		ZgwCatalogus catalogus = getCatalogusByRsin(authorization, rsin);
		if(catalogus == null) {
			throw new ConverterException("Catalogus voor rsin: " + rsin + " kon niet worden gevonden");
		}
		authorization.setCatalogus(catalogus);
		return 	authorization;
	}
	
	private HttpHeaders getHeaders(ZgwAuthorization authorization, String url) {
		var headers = new HttpHeaders();
		headers.set("Authorization", authorization.getAuthorizationToken(url));		
		headers.set("Accept-Crs", "EPSG:4326");
		headers.set("Content-Crs", "EPSG:4326");
		headers.set("Content-Type", "application/json");

		return headers;
	}

	private String post(ZgwAuthorization authorization, String url, String json) {
		String debugName = "ZGWClient POST";
		json = debug.startpoint(debugName, json);
		url = debug.inputpoint("url", url);
		HttpEntity<String> entity = new HttpEntity<String>(json, this.getHeaders(authorization, url));
		log.debug("\n\tPOST: " + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n\tJson: " + json);
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
			var details = "--------------POST:\n" + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n" + StringUtils.shortenLongString(json, StringUtils.MAX_ERROR_SIZE) + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);			
			log.warn("POST naar ZgwRegistry: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("POST naar ZgwRegistry: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("POST naar ZgwRegistry: " + url + " niet geslaagd", rae);
			throw new ConverterException("POST naar ZgwRegistry: " + url + " niet geslaagd", rae);
		}
	}

	private String getUrlWithParameters(String url, Map<String, String> parameters) {
		for (String key : parameters.keySet()) {
			url += !url.contains("?") ? "?" + key + "=" + parameters.get(key) : "&" + key + "=" + parameters.get(key);
		}
		return url;
	}
		
	private String get(ZgwAuthorization authorization, String url, Map<String, String> parameters) {
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
		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders(authorization, url));
		log.debug("\n\tGET: " + url + "\n\tHeaders:" + entity.getHeaders().toString());
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
			var details = "--------------GET:\n" + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("GET naar ZgwRegistry: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("GET naar ZgwRegistry: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("GET naar ZgwRegistry: " + url + " niet geslaagd", rae);
			throw new ConverterException("GET naar ZgwRegistry: " + url + " niet geslaagd", rae);
		}
	}

	public String getBas64Inhoud(ZgwAuthorization authorization, String url) {
		String debugName = "ZGWClient GET(BASE64)";
		debug.startpoint(debugName);
		url = debug.inputpoint("url", url);
		HttpEntity entity = new HttpEntity(this.getHeaders(authorization, url));
		log.debug("\n\tGET(BASE64): " + url + "\n\tHeaders:" + entity.getHeaders().toString() );
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
			log.debug("BASE64 INHOUD DOWNLOADED:" + (data == null ? "[null], is ZgwRegistry dms-broken?" : data.length + " bytes"));
			return java.util.Base64.getEncoder().encodeToString(data);

		} catch (HttpStatusCodeException hsce) {
			var response = hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}",
					"\"\n}");
			var details = "--------------GET:\n" + url + "\n\tHeaders:" + entity.getHeaders().toString() +  "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("GET(BASE64) naar ZgwRegistry: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("GET(BASE64) naar ZgwRegistry: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("GET(BASE64) naar ZgwRegistry: " + url + " niet geslaagd", rae);
			throw new ConverterException("GET(BASE64) naar ZgwRegistry: " + url + " niet geslaagd", rae);
		}
	}
	
	
	private String delete(ZgwAuthorization authorization, String url) {
		String debugName = "ZGWClient DELETE";
		debug.startpoint(debugName);
		url = debug.inputpoint("url", url);
		HttpEntity<String> entity = new HttpEntity<String>(this.getHeaders(authorization, url));
		log.debug("\n\tDELETE: " + url + "\n\tHeaders:" + entity.getHeaders().toString());	
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
			var details = "--------------DELETE:\n" + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("DELETE naar ZgwRegistry: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("DELETE naar ZgwRegistry: " + url + " gaf foutmelding:" + hsce.toString(),
					details, hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("DELETE naar ZgwRegistry: " + url + " niet geslaagd", rae);
			throw new ConverterException("DELETE naar ZgwRegistry: " + url + " niet geslaagd", rae);
		}
	}

	private String put(ZgwAuthorization authorization, String url, String json) {
		String debugName = "ZGWClient PUT";
		json = debug.startpoint(debugName, json);
		url = debug.inputpoint("url", url);
		HttpEntity<String> entity = new HttpEntity<String>(json, this.getHeaders(authorization, url));
		log.debug("\n\tPUT: " + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n\tJson: " + json);
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
			var details = "--------------PUT:\n" + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n" + StringUtils.shortenLongString(json, StringUtils.MAX_ERROR_SIZE) + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("PUT naar ZgwRegistry: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("PUT naar ZgwRegistry: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("PUT naar ZgwRegistry: " + url + " niet geslaagd", rae);
			throw new ConverterException("PUT naar ZgwRegistry: " + url + " niet geslaagd", rae);
		}
	}


	private String patch(ZgwAuthorization authorization, String url, String json) {
		String debugName = "ZGWClient PATCH";
		json = debug.startpoint(debugName, json);
		url = debug.inputpoint("url", url);
		HttpEntity<String> entity = new HttpEntity<String>(json, this.getHeaders(authorization, url));
		log.debug("\n\tPATCH: " + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n\tJson: " + json);
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
			var details = "--------------PATCH:\n" + url + "\n\tHeaders:" + entity.getHeaders().toString() + "\n" + StringUtils.shortenLongString(json, StringUtils.MAX_ERROR_SIZE) + "\n--------------RESPONSE:\n" + StringUtils.shortenLongString(response, StringUtils.MAX_ERROR_SIZE);
			log.warn("PATCH naar ZgwRegistry: " + url + " gaf foutmelding:\n" + details, hsce);
			throw new ConverterException("PATCH naar ZgwRegistry: " + url + " gaf foutmelding:" + hsce.toString(), details,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			log.warn("PATCH naar ZgwRegistry: " + url + " niet geslaagd", rae);
			throw new ConverterException("PATCH naar ZgwRegistry: " + url + " niet geslaagd", rae);
		}
	}

	public ZgwEnkelvoudigInformatieObject getZgwEnkelvoudigInformatieObject(ZgwAuthorization authorization, Map<String, String> parameters) {
		ZgwEnkelvoudigInformatieObject result = null;
		var documentJson = get(authorization, this.documentenUrl + this.endpointEnkelvoudiginformatieobject, parameters);
		Type type = new TypeToken<QueryResult<ZgwEnkelvoudigInformatieObject>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwEnkelvoudigInformatieObject> queryResult = gson.fromJson(documentJson, type);

		if (queryResult.getResults() != null && queryResult.getResults().size() == 1) {
			return queryResult.getResults().get(0);
		}		
		return null;		
	}
	
	public ZgwEnkelvoudigInformatieObject getZgwEnkelvoudigInformatieObjectByIdentiticatie(ZgwAuthorization authorization, String documentIdentificatie) {
		log.debug("get zaakdocument #" + documentIdentificatie);

		if(documentIdentificatie == null || documentIdentificatie.length() == 0) {
			throw new ConverterException("getZgwEnkelvoudigInformatieObjectByIdentiticatie without an identificatie");
		}

		Map<String, String> parameters = new HashMap();		
		parameters.put("bronorganisatie", authorization.getCatalogusRsin());
		parameters.put("identificatie", documentIdentificatie);
		parameters.put("expand", getEnkelvoudigInformatieObjectenExpandParameterValue());						
		
		ZgwEnkelvoudigInformatieObject zgwDocument = this.getZgwEnkelvoudigInformatieObject(authorization, parameters);
		authorization.cacheAdd(zgwDocument);
		return zgwDocument;
	}
	
	public ZgwEnkelvoudigInformatieObject getZgwEnkelvoudigInformatieObjectByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwEnkelvoudigInformatieObject) cachedObject;		

		Map<String, String> parameters = new HashMap();
		parameters.put("expand", getEnkelvoudigInformatieObjectenExpandParameterValue());
		var zaakInformatieObjectJson = get(authorization, url, parameters);
		Gson gson = new Gson();
		var result = gson.fromJson(zaakInformatieObjectJson, ZgwEnkelvoudigInformatieObject.class);
		if(result == null) {
			throw new ConverterException("ZaakDocument met url:" + url + " niet gevonden!");
		}
		
		authorization.cacheAdd(result);
		return result;
	}
		
	public ZgwRolType getRolTypeByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwRolType) cachedObject;
		
		if(authorization.cacheGet(url) != null) return (ZgwRolType) authorization.cacheGet(url);
		
		var rolTypeJson = get(authorization, url, null);
		Gson gson = new Gson();
		ZgwRolType result = gson.fromJson(rolTypeJson, ZgwRolType.class);
		if(result == null) {
			throw new ConverterException("Roltype met url:" + url + " niet gevonden!");
		}
		authorization.cacheAdd(result);
		return result;
	}

	public List<ZgwZaak> getZaken(ZgwAuthorization authorization, Map<String, String> parameters) {
		var zaakTypeJson = get(authorization, this.zakenUrl + this.endpointZaak, parameters);
		Type type = new TypeToken<QueryResult<ZgwZaak>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwZaak> queryResult = gson.fromJson(zaakTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwZaak>();
		}
		return queryResult.getResults();
	}	
	
	public ZgwZaak getZaakByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwZaak) cachedObject;
		
		Map<String, String> parameters = new HashMap();
		parameters.put("expand", getZakenExpandParameterValue());		
		var zaakJson = get(authorization, url, null);
		Gson gson = new Gson();
		ZgwZaak result = gson.fromJson(zaakJson, ZgwZaak.class);
		if(result == null) {
			throw new ConverterException("Zaak met url:" + url + " niet gevonden!");
		}
		
		authorization.cacheAdd(result);
		return result;
	}


	public ZgwCatalogus getCatalogus(ZgwAuthorization authorization, Map<String, String> parameters) {
		ZgwCatalogus result = null;
		var catalogusJson = get(authorization, this.catalogiUrl + this.endpointCatalogus, parameters);
		Type type = new TypeToken<QueryResult<ZgwCatalogus>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwCatalogus> queryResult = gson.fromJson(catalogusJson, type);
		if (queryResult.getResults() != null &&  queryResult.getResults().size() == 1) {
			result = queryResult.getResults().get(0);
		}
		return result;
	}
	
	public ZgwCatalogus getCatalogusByRsin(ZgwAuthorization authorization, String rsin) {
		if(rsin == null || rsin.length() == 0) {
			throw new ConverterException("getCatalogusByRsin without an rsin");
		}

		Map<String, String> parameters = new HashMap();
		parameters.put("rsin", rsin);
		ZgwCatalogus zgwCatalogus = this.getCatalogus(authorization, parameters);

		return zgwCatalogus;
	}
	
	public ZgwZaak getZaak(ZgwAuthorization authorization, Map<String, String> parameters) {
		ZgwZaak result = null;
		var zaakJson = get(authorization, this.zakenUrl + this.endpointZaak, parameters);
		Type type = new TypeToken<QueryResult<ZgwZaak>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwZaak> queryResult = gson.fromJson(zaakJson, type);
		if (queryResult.getResults() != null &&  queryResult.getResults().size() == 1) {
			result = queryResult.getResults().get(0);
		}
		return result;
	}

	public ZgwZaak addZaak(ZgwAuthorization authorization, ZgwZaak zgwZaak) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwZaak);
		String response = this.post(authorization, this.zakenUrl + this.endpointZaak, json);
		return gson.fromJson(response, ZgwZaak.class);
	}


	public void patchZaak(ZgwAuthorization authorization, String zaakUuid, ZgwZaakPut zaak) {
		Gson gson = new Gson();
		String json = gson.toJson(zaak);
		this.patch(authorization, this.zakenUrl + this.endpointZaak + "/" + zaakUuid, json);
	}
	public ZgwRol addZgwRol(ZgwAuthorization authorization, ZgwRol zgwRol) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwRol);
		String response = this.post(authorization, this.zakenUrl + this.endpointRol, json);
		return gson.fromJson(response, ZgwRol.class);
	}

	public ZgwEnkelvoudigInformatieObject addZaakDocument(ZgwAuthorization authorization, 
			ZgwEnkelvoudigInformatieObjectPost zgwEnkelvoudigInformatieObjectPost) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String json = gson.toJson(zgwEnkelvoudigInformatieObjectPost);
		String response = this.post(authorization, this.documentenUrl + this.endpointEnkelvoudiginformatieobject, json);
		return gson.fromJson(response, ZgwEnkelvoudigInformatieObject.class);
	}

	public ZgwZaakInformatieObject addDocumentToZaak(ZgwAuthorization authorization, ZgwZaakInformatieObject zgwZaakInformatieObject) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwZaakInformatieObject);
		String response = this.post(authorization, this.zakenUrl + this.endpointZaakinformatieobject, json);
		return gson.fromJson(response, ZgwZaakInformatieObject.class);
	}

	public List<ZgwZaakInformatieObject> getZgwZaakInformatieObjects(ZgwAuthorization authorization, Map<String, String> parameters) {
		// Fetch EnkelvoudigInformatieObjects
		var zaakInformatieObjectJson = get(authorization, this.zakenUrl + this.endpointZaakinformatieobject, parameters);

		Gson gson = new Gson();
		Type documentList = new TypeToken<ArrayList<ZgwZaakInformatieObject>>() {
		}.getType();
		return gson.fromJson(zaakInformatieObjectJson, documentList);
	}

	public List<ZgwStatusType> getStatusTypes(ZgwAuthorization authorization, Map<String, String> parameters) {
		var statusTypeJson = get(authorization, this.catalogiUrl + this.endpointStatustype, parameters);
		Type type = new TypeToken<QueryResult<ZgwStatusType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwStatusType> queryResult = gson.fromJson(statusTypeJson, type);
		if(queryResult.getResults() == null) {
			return new ArrayList<ZgwStatusType>();
		}
		return queryResult.getResults();
	}

	public List<ZgwResultaatType> getResultaatTypes(ZgwAuthorization authorization, Map<String, String> parameters) {
		var restulaatTypeJson = get(authorization, this.catalogiUrl + this.endpointResultaattype, parameters);
		Type type = new TypeToken<QueryResult<ZgwResultaatType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwResultaatType> queryResult = gson.fromJson(restulaatTypeJson, type);
		if(queryResult.getResults() == null) {
			return new ArrayList<ZgwResultaatType>();
		}
		return queryResult.getResults();
	}

	public List<ZgwResultaat> getResultaten(ZgwAuthorization authorization, Map<String, String> parameters) {
		var restulaatJson = get(authorization, this.zakenUrl + this.endpointResultaat, parameters);
		Type type = new TypeToken<QueryResult<ZgwResultaat>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwResultaat> queryResult = gson.fromJson(restulaatJson, type);
		if(queryResult.getResults() == null) {
			return new ArrayList<ZgwResultaat>();
		}
		return queryResult.getResults();
	}


	public List<ZgwStatus> getStatussen(ZgwAuthorization authorization, Map<String, String> parameters) {
		var statusTypeJson = get(authorization, this.zakenUrl + this.endpointStatus, parameters);
		Type type = new TypeToken<QueryResult<ZgwStatus>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwStatus> queryResult = gson.fromJson(statusTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwStatus>();
		}
		return queryResult.getResults();
	}

	public <T> T getResource(ZgwAuthorization authorization, String url, Class<T> resourceType) {		
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null)  return resourceType.cast(authorization.cacheGet(url));		
		
		Gson gson = new Gson();
		String response = get(authorization, url, null);
		var result = gson.fromJson(response, resourceType);
		authorization.cacheAdd((ZgwObject)result);
		return result; 
	}

	public ZgwStatus addZaakStatus(ZgwAuthorization authorization, ZgwStatus zgwSatus) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwSatus);
		String response = this.post(authorization, this.zakenUrl + this.endpointStatus, json);
		return gson.fromJson(response, ZgwStatus.class);
	}

	public ZgwResultaat addZaakResultaat(ZgwAuthorization authorization, ZgwResultaat zgwResultaat) {
		Gson gson = new Gson();
		String json = gson.toJson(zgwResultaat);
		String response = this.post(authorization, this.zakenUrl + this.endpointResultaat, json);
		return gson.fromJson(response, ZgwResultaat.class);
	}

	public List<ZgwZaakType> getZaakTypes(ZgwAuthorization authorization, Map<String, String> parameters) {
		var zaakTypeJson = get(authorization, this.catalogiUrl + this.endpointZaaktype, parameters);
		Type type = new TypeToken<QueryResult<ZgwZaakType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwZaakType> queryResult = gson.fromJson(zaakTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwZaakType>();
		}
		return queryResult.getResults();
	}

	public ZgwZaakType getZaakTypeByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwZaakType) cachedObject;
		
		var zaakTypeJson = get(authorization, url, null);
		Gson gson = new Gson();
		ZgwZaakType result = gson.fromJson(zaakTypeJson, ZgwZaakType.class);
		authorization.cacheAdd(result);
		return result;
	}


	public ZgwZaakType getZaakTypeByZaak(ZgwAuthorization authorization, ZgwZaak zgwZaak) {
		return getZaakTypeByUrl(authorization, zgwZaak.getZaaktype());
	}

	public List<ZgwRol> getRollen(ZgwAuthorization authorization, Map<String, String> parameters) {
		var zaakTypeJson = get(authorization, this.zakenUrl + this.endpointRol, parameters);
		Type type = new TypeToken<QueryResult<ZgwRol>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwRol> queryResult = gson.fromJson(zaakTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwRol>();
		}
		return queryResult.getResults();
	}

	public List<ZgwRolType> getRolTypen(ZgwAuthorization authorization, Map<String, String> parameters) {
		var rolTypeJson = get(authorization, this.catalogiUrl + this.endpointRolType, parameters);
		Type type = new TypeToken<QueryResult<ZgwRolType>>() {
		}.getType();
		Gson gson = new Gson();
		QueryResult<ZgwRolType> queryResult = gson.fromJson(rolTypeJson, type);
		if(queryResult == null) {
			return new ArrayList<ZgwRolType>();
		}
		return queryResult.getResults();
	}

//	public ZgwRolType getRolTypeByZaaktypeAndOmschrijving(ZgwAuthorization authorization, ZgwZaakType zgwZaakType, String omschrijving) {
//		for (String found : zgwZaakType.roltypen) {
//			ZgwRolType roltype = getRolTypeByUrl(authorization, found);
//			if (roltype.omschrijving.equals(omschrijving)) {
//				return roltype;
//			}
//		}
//		return null;
//	}

	public ZgwRolType getRolTypeByZaaktypeAndOmschrijving(ZgwAuthorization authorization, ZgwZaakType zgwZaakType, String omschrijving) {		
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zgwZaakType.url);
		List<ZgwRolType> roltypen = getRolTypen(authorization, parameters);
		for (ZgwRolType roltype : roltypen) {
			if (roltype.omschrijving.equals(omschrijving)) {
				return roltype;
			}
		}
		return null;
	}
	
	
	public void updateZaak(ZgwAuthorization authorization, String zaakUuid, ZgwZaakPut zaak) {
		Gson gson = new Gson();
		String json = gson.toJson(zaak);
		this.put(authorization, this.zakenUrl + this.endpointZaak + "/" + zaakUuid, json);
	}

	public void deleteRol(ZgwAuthorization authorization, String uuid) {
		if (uuid == null) {
			throw new ConverterException("rol uuid may not be null");
		}
		delete(authorization, this.zakenUrl + this.endpointRol + "/" + uuid);
	}

	public void deleteZaakResultaat(ZgwAuthorization authorization, String uuid) {
		if (uuid == null) {
			throw new ConverterException("zaakresultaat uuid may not be null");
		}
		delete(authorization, this.zakenUrl + this.endpointResultaat + "/" + uuid);
	}


	public List<ZgwZaakInformatieObject> getZaakInformatieObjectenByZaak(ZgwAuthorization authorization, String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);
		return this.getZgwZaakInformatieObjects(authorization, parameters);
	}
	
	public ZgwZaakInformatieObject getZaakInformatieObjectByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwZaakInformatieObject) cachedObject;
		
		var zgwZaakInformatieObjectJson = get(authorization, url, null);
		Gson gson = new Gson();
		ZgwZaakInformatieObject result = gson.fromJson(zgwZaakInformatieObjectJson, ZgwZaakInformatieObject.class);
		if(result == null) {
			throw new ConverterException("ZgwZaakInformatieObject met url:" + url + " niet gevonden!");
		}
		authorization.cacheAdd(result);
		return result;
	}
	
	private String getEnkelvoudigInformatieObjectenExpandParameterValue() {
		var expand = new ArrayList<String>();
		expand.add("informatieobjecttype");
		return String.join(",", expand);
	}
	
	
	private String getZakenExpandParameterValue() {
		var expand = new ArrayList<String>();
		expand.add("zaaktype");
		expand.add("eigenschappen");
		expand.add("eigenschappen.eigenschap");
		expand.add("status");
		expand.add("status.statustype");
		expand.add("resultaat");
		expand.add("resultaat.resultaattype");
		expand.add("rollen");
		expand.add("rollen.roltype");
		expand.add("zaakinformatieobjecten");
		expand.add("zaakobjecten");
		expand.add("hoofdzaak");	
		expand.add("hoofdzaak.zaaktype");		
		expand.add("hoofdzaak.status");
		expand.add("hoofdzaak.status.statustype");
		expand.add("hoofdzaak.resultaat");
		expand.add("hoofdzaak.resultaat.resultaattype");
		expand.add("hoofdzaak.rollen");
		expand.add("hoofdzaak.rollen.roltype");
		expand.add("hoofdzaak.zaakinformatieobjecten");
		expand.add("hoofdzaak.zaakobjecten");
		expand.add("deelzaken");	
		expand.add("deelzaken.zaaktype");		
		expand.add("deelzaken.status");
		expand.add("deelzaken.status.statustype");
		expand.add("deelzaken.resultaat");
		expand.add("deelzaken.resultaat.resultaattype");
		expand.add("deelzaken.rollen");
		expand.add("deelzaken.rollen.roltype");
		expand.add("deelzaken.zaakinformatieobjecten");
		expand.add("deelzaken.zaakobjecten");
		return String.join(",", expand);
	}

	public List<ZgwZaak> getZakenByBsn(ZgwAuthorization authorization, String bsn) {
		if(bsn == null || bsn.length() == 0) {
			throw new ConverterException("getZaakByIdentificatie without an identificatie");
		}

		Map<String, String> parameters = new HashMap();		
		parameters.put("bronorganisatie", authorization.getCatalogusRsin());
		parameters.put("rol__betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", bsn);		
		parameters.put("expand", getZakenExpandParameterValue());	

		List<ZgwZaak> zaken = this.getZaken(authorization, parameters);
		authorization.cacheAdd(zaken);
		return zaken;
	}	
	
	
	public ZgwZaak getZaakByIdentificatie(ZgwAuthorization authorization, String zaakIdentificatie) {
		if(zaakIdentificatie == null || zaakIdentificatie.length() == 0) {
			throw new ConverterException("getZaakByIdentificatie without an identificatie");
		}


		Map<String, String> parameters = new HashMap();		
		parameters.put("bronorganisatie", authorization.getCatalogusRsin());
		parameters.put("identificatie", zaakIdentificatie);
		parameters.put("expand", getZakenExpandParameterValue());				
		
		ZgwZaak zgwZaak = this.getZaak(authorization, parameters);		
		if (zgwZaak == null) {
			return null;
		}

		// When Verlenging/Opschorting not set, zgw returns object with empty values, in
		// stead of null.
		// This will cause issues when response of getzaakdetails is used for
		// updatezaak.
		if (zgwZaak.getVerlenging() != null && (zgwZaak.getVerlenging().getDuur() == null || zgwZaak.getVerlenging().getReden().equals(""))) {
			zgwZaak.setVerlenging(null);
		}
		if (zgwZaak.getOpschorting() != null && zgwZaak.getOpschorting().getReden().equals("")) {
			zgwZaak.setOpschorting(null);
		}
		
		authorization.cacheAdd(zgwZaak);
		
		return zgwZaak;
	}

	public ZgwZaakInformatieObject getZgwZaakInformatieObjectByEnkelvoudigInformatieObjectUrl(ZgwAuthorization authorization, String url) {
		Map<String, String> parameters = new HashMap();
		parameters.put("informatieobject", url);		
		var zaakinformatieobjecten = this.getZgwZaakInformatieObjects(authorization, parameters);		
		if(zaakinformatieobjecten.size() == 0) {
			throw new ConverterException("Geen zaakinformatieobject gevonden voor de url: '" + url + "'");
		}
		var result  = zaakinformatieobjecten.get(0);
		authorization.cacheAdd(result);
		return result;
	}

	public List<ZgwStatusType> getStatusTypesByZaakType(ZgwAuthorization authorization, ZgwZaakType zgwZaakType) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zgwZaakType.url);
		List<ZgwStatusType> statustypes = this.getStatusTypes(authorization, parameters);
		return statustypes;
	}


	public ZgwStatusType getLastStatusTypeByZaakType(ZgwAuthorization authorization, ZgwZaakType zgwZaakType) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zgwZaakType.url);
		for(ZgwStatusType statustype : this.getStatusTypes(authorization, parameters)) {
			if("true".equals(statustype.isEindstatus)) {
				return statustype;
			}
		}
		return null;
	}

	public ZgwStatusType getStatusTypeByZaakTypeAndOmschrijving(ZgwAuthorization authorization, ZgwZaakType zaakType, String statusOmschrijving, String verwachteVolgnummer) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zaakType.url);
		List<ZgwStatusType> statustypes = this.getStatusTypes(authorization, parameters);

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


	public ZgwResultaatType getResultaatTypeByZaakTypeAndOmschrijving(ZgwAuthorization authorization, ZgwZaakType zaakType, String resultaatOmschrijving) {
		var omschrijving = resultaatOmschrijving;
		if(omschrijving.length() > 20) {
			// maximum length of omschrijving is 20 characters
			omschrijving = omschrijving.substring(0, 20);
		}
		for (String found: zaakType.resultaattypen) {
			ZgwResultaatType resultaatType = getResultaatTypeByUrl(authorization, found);
			log.debug("opgehaald:" + resultaatType.omschrijving + " zoeken naar: " + omschrijving + "' (ingekort van: " + resultaatOmschrijving + ")");

			// in some applications, the omschrijving can not be as long as we want.....
			if (resultaatType.omschrijving.startsWith(omschrijving)) {
				log.debug("gevonden:" + resultaatType.omschrijving + " zoeken naar: " + omschrijving);
				return resultaatType;
			}
		}
		throw new ConverterException("zaakresultaat niet gevonden voor omschrijving: '" + resultaatOmschrijving + "'");
	}

	private ZgwResultaatType getResultaatTypeByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwResultaatType) cachedObject;
		
		var resultaatTypeJson = get(authorization, url, null);
		Gson gson = new Gson();
		ZgwResultaatType result = gson.fromJson(resultaatTypeJson, ZgwResultaatType.class);
		if(result == null) {
			throw new ConverterException("ZgwResultaatType met url:" + url + " niet gevonden!");
		}
		authorization.cacheAdd(result);
		return result;

	}

	public List<ZgwResultaat> getResultatenByZaakUrl(ZgwAuthorization authorization, String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);
		return this.getResultaten(authorization, parameters);
	}

	public List<ZgwRol> getRollenByZaakUrl(ZgwAuthorization authorization, String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);

		return this.getRollen(authorization, parameters);
	}	
	
	public ZgwRol getRolByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwRol) cachedObject;
		
		var zaakJson = get(authorization, url, null);
		Gson gson = new Gson();
		ZgwRol result = gson.fromJson(zaakJson, ZgwRol.class);
		if(result == null) {
			throw new ConverterException("Rol met url:" + url + " niet gevonden!");
		}
		
		authorization.cacheAdd(result);
		return result;
	}	
	

	public List<ZgwRol> getRollenByBsn(ZgwAuthorization authorization, String bsn) {
		Map<String, String> parameters = new HashMap();
		parameters.put("betrokkeneIdentificatie__natuurlijkPersoon__inpBsn", bsn);
		return this.getRollen(authorization, parameters);
	}
	

	public ZgwRol getRolByZaakUrlAndRolTypeUrl(ZgwAuthorization authorization, String zaakUrl, String rolTypeUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);
		parameters.put("roltype", rolTypeUrl);

		return this.getRollen(authorization, parameters).stream().findFirst().orElse(null);
	}

	public List<ZgwStatus> getStatussenByZaakUrl(ZgwAuthorization authorization, String zaakUrl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", zaakUrl);

		return this.getStatussen(authorization, parameters);
	}

	public ZgwZaakType getZgwZaakTypeByIdentificatie(ZgwAuthorization authorization, String identificatie) {
		if(identificatie == null || identificatie.length() == 0) {
			throw new ConverterException("getZgwZaakTypeByIdentificatie without an identificatie");
		}

		Map<String, String> parameters = new HashMap<>();
		parameters.put("catalogus", authorization.getCatalogusUrl());
		parameters.put("identificatie", identificatie);
		parameters.put("status", "definitief");
		var types = this.getZaakTypes(authorization, parameters);

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

	public ZgwInformatieObjectType getZgwInformatieObjectTypeByOmschrijving(ZgwAuthorization authorization, ZgwZaakType zaaktype, String omschrijving) {
		for (String found : zaaktype.informatieobjecttypen ) {
			ZgwInformatieObjectType ziot = getZgwInformatieObjectTypeByUrl(authorization, found);
			log.debug("gevonden ZgwInformatieObjectType met omschrijving: '" + ziot.omschrijving + "'");
			//if (omschrijving.equals(ziot.omschrijving)) {
			if (omschrijving.equalsIgnoreCase(ziot.omschrijving)) {
				return ziot;
			}
		}
		return null;
	}

	public ZgwInformatieObjectType getZgwInformatieObjectTypeByUrl(ZgwAuthorization authorization, String url) {
		var cachedObject = authorization.cacheGet(url);
		if (cachedObject != null) return (ZgwInformatieObjectType) cachedObject;		
		
		var documentType = get(authorization, url, null);
		Gson gson = new Gson();
		ZgwInformatieObjectType result = gson.fromJson(documentType, ZgwInformatieObjectType.class);
		return result;
	}

	public ZgwLock getZgwInformatieObjectLock(ZgwAuthorization authorization, ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject) {
		String json = "{ }";
		var lock = post(authorization, zgwEnkelvoudigInformatieObject.url + "/lock", json);
		Gson gson = new Gson();
		ZgwLock result = gson.fromJson(lock, ZgwLock.class);
		return result;
	}

	public void getZgwInformatieObjectUnLock(ZgwAuthorization authorization, ZgwEnkelvoudigInformatieObjectPut zgwEnkelvoudigInformatieObjectPut, ZgwLock zgwLock) {
			Gson gson = new Gson();
			String json = gson.toJson(zgwLock);
			var lock = post(authorization, zgwEnkelvoudigInformatieObjectPut.url + "/unlock", json);
			Object result = gson.fromJson(lock, Object.class);
			return;
	}

	public ZgwEnkelvoudigInformatieObject putZaakDocument(ZgwAuthorization authorization, ZgwEnkelvoudigInformatieObjectPut zgwEnkelvoudigInformatieObjectPut) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
		String json = gson.toJson(zgwEnkelvoudigInformatieObjectPut);
		String response = this.put(authorization, zgwEnkelvoudigInformatieObjectPut.url, json);
		return gson.fromJson(response, ZgwEnkelvoudigInformatieObject.class);
	}

	private void debugWarning(String message) {
		log.info("[processing warning] " + message);
		debug.infopoint("Warning", message);
	}

	public void addChildZaakToZaak(ZgwAuthorization authorization, ZgwZaak zgwZaak, ZgwZaakPatch zgwChildZaak) {
		zgwChildZaak.hoofdzaak = zgwZaak.url;
		if(zgwChildZaak.verlenging != null) {
			if(zgwChildZaak.verlenging.duur == null) {
				zgwChildZaak.verlenging = null;
			}
		}
		this.patchZaak(authorization, zgwChildZaak.uuid, zgwChildZaak);
	}

	public void addRelevanteAndereZaakToZaak(ZgwAuthorization authorization, ZgwZaakPatch zgwZaak,  ZgwZaak andereZaak, String aardRelatie) {
		if(zgwZaak.verlenging != null) {
			if(zgwZaak.verlenging.duur == null) {
				zgwZaak.verlenging = null;
			}
		}
		var relevanteAndereZaak = new ZgwAndereZaak();
		relevanteAndereZaak.url = andereZaak.url;
		// https://www.gemmaonline.nl/index.php/Imztc_2.2/doc/enumeration/aardrelatie
		// 	Moet dus zijn: bijdrage / onderwerp / vervolg
		relevanteAndereZaak.aardRelatie = aardRelatie;
		zgwZaak.relevanteAndereZaken.add(relevanteAndereZaak);
		this.patchZaak(authorization, zgwZaak.uuid, zgwZaak);
	}

	public List<ZgwObjectInformatieObject> getObjectInformatieObjectsByUrl(ZgwAuthorization authorization, Map<String, String> parameters) {
		// Fetch ObjectInformatieObject
		var objectInformatieObjectJson = get(authorization, this.documentenUrl + this.endpointObjectinformatieobject, parameters);

		Gson gson = new Gson();
		Type documentList = new TypeToken<ArrayList<ZgwObjectInformatieObject>>() {
		}.getType();
		return gson.fromJson(objectInformatieObjectJson, documentList);
	}

	public List<ZgwObjectInformatieObject> getObjectInformatieObjectsByUrl(ZgwAuthorization authorization, String objecturl) {
		Map<String, String> parameters = new HashMap();
		parameters.put("object", objecturl);
		return this.getObjectInformatieObjectsByUrl(authorization, parameters);
	}
}
