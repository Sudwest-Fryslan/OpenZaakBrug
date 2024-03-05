package nl.haarlem.translations.zdstozgw.translation.zgw.client;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwCatalogus;

public class ZgwAuthorization {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private ZgwCatalogus catalogus;

	@Data
	public class ZgwJwtTokenEntry {

		public String jwtUrl;
		public String jwtIssuer;
		public String jwtSecret;
		public String authorization;

		public ZgwJwtTokenEntry(String jwtUrl, String jwtIssuer, String jwtSecret, String authorization) {
			this.jwtUrl = jwtUrl;
			this.jwtIssuer = jwtIssuer;
			this.jwtSecret = jwtSecret;
			this.authorization = authorization;
		}
	}

	public Map<String, ZgwJwtTokenEntry> authorizations = new LinkedHashMap<>();

	public void AddZgwAuthorization(String url, String jwtUrl, String jwtIssuer, String jwtSecret) {
		// do we already have an endpoint for this jwt-token?
		for (ZgwJwtTokenEntry entry : this.authorizations.values()) {
			if (entry.jwtUrl == null) {
				if (jwtUrl != null)
					continue;
			} else {
				if (!entry.jwtUrl.equals(jwtUrl)) {
					continue;
				}
			}
			if (!entry.jwtIssuer.equals(jwtIssuer)) {
				continue;
			}
			if (!entry.jwtSecret.equals(jwtSecret)) {
				continue;
			}
			// bestond al, geven we terug
			log.debug("For url: " + url + " using: '" + entry.getAuthorization() + "' from previous found entry");
			this.authorizations.put(url, entry);
			return;
		}

		// not already known, new jwt-token needed for this endpoint
		if (jwtUrl != null && jwtUrl.trim().length() > 0) {
			var authorizationRequestHeaders = new HttpHeaders();
			String json = "{\n" + "    \"clientIds\": [\n" + "        \"test_user\"\n" + "    ],\n"
					+ "    \"secret\": \"" + jwtSecret + "\",\n" + "    \"label\": \"" + jwtIssuer + "\",\n"
					+ "    \"heeftAlleAutorisaties\": \"true\",\n" + "    \"autorisaties\": []\n" + "}";

			final RestTemplate restTemplate = new RestTemplate();
			final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			final HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()) // adds
																													// HTTP
																													// REDIRECT
																													// support
																													// to
																													// GET
																													// and
																													// POST
																													// methods,
																													// needed
																													// because
																													// VNG-cloud
																													// redirects
																													// with
																													// 307
																													// ->
																													// 308
																													// ->
																													// 200
					.build();
			factory.setHttpClient(httpClient);
			restTemplate.setRequestFactory(factory);

			authorizationRequestHeaders.setContentType(MediaType.APPLICATION_JSON);
			var accept = new ArrayList<MediaType>();
			accept.add(MediaType.APPLICATION_JSON);
			authorizationRequestHeaders.setAccept(accept);

			HttpEntity<String> entity = new HttpEntity<String>(json, authorizationRequestHeaders);
			ResponseEntity<String> bearerResponse = restTemplate.postForEntity(jwtUrl, entity, String.class);
			Gson gson = new Gson();
			var zgwJwtTokenEntry = gson.fromJson(bearerResponse.getBody(), ZgwJwtTokenEntry.class);

			log.debug("For url: " + url + " using: '" + zgwJwtTokenEntry.getAuthorization() + "' from url:'" + jwtUrl
					+ "' with requestjson:\n" + json);
			zgwJwtTokenEntry.jwtUrl = jwtUrl;
			zgwJwtTokenEntry.jwtIssuer = jwtIssuer;
			zgwJwtTokenEntry.jwtSecret = jwtSecret;
			this.authorizations.put(url, zgwJwtTokenEntry);
		} else {
			var token = JWTService.getJWT(jwtIssuer, jwtSecret);
			var zgwJwtTokenEntry = new ZgwJwtTokenEntry(jwtUrl, jwtIssuer, jwtSecret, "Bearer " + token);
			log.debug(
					"For url: " + url + " using: '" + zgwJwtTokenEntry.getAuthorization() + "' without using a jwtUrl");
			this.authorizations.put(url, zgwJwtTokenEntry);
		}
	}

	public String getAuthorizationToken(String url) {
		for (String baseurl : this.authorizations.keySet()) {
			if (url.startsWith(baseurl)) {
				return authorizations.get(baseurl).getAuthorization();
			}
		}
		throw new ConverterException("No authorization defined for the url: " + url);
	}

	public void setCatalogus(ZgwCatalogus catalogus) {
		this.catalogus = catalogus;
	}

	public String getCatalogusRsin() {
		return this.catalogus.getRsin();
	}

	public String getCatalogusUrl() {
		return this.catalogus.getUrl();
	}
}
