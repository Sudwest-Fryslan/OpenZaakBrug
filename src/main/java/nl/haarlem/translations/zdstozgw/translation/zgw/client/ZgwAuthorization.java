package nl.haarlem.translations.zdstozgw.translation.zgw.client;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.google.gson.annotations.Expose;

import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwBetrokkeneIdentificatie;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwCatalogus;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwDrcExpand;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZrcExpand;
import lombok.Data;


public class ZgwAuthorization {
	
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	
	private ZgwCatalogus catalogus;
	
	@Data
	public class ZgwJwtTokenEntry {		
		
		public  String jwtUrl;
		public  String jwtIssuer;
		public  String jwtSecret;
		public 	String jwtClientIds;
		public  String authorization;
		
		public ZgwJwtTokenEntry(String jwtUrl, String jwtIssuer, String jwtSecret,  String jwtClientIds, String authorization) {
			this.jwtUrl = jwtUrl;
			this.jwtIssuer = jwtIssuer;
			this.jwtSecret = jwtSecret;
			this.jwtClientIds = jwtClientIds;
			this.authorization = authorization;
		}
		
		private String version;

		public void setVersion(String version) {
			this.version = version;			
		}

		public String getVersion() {
			return this.version;
		}
	}
	
	public Map<String, ZgwJwtTokenEntry> authorizations = new LinkedHashMap<>();		
	public void AddZgwAuthorization(String url, String jwtUrl, String jwtIssuer, String jwtSecret, String jwtClientIds) {		
		// do we already have an endpoint for this jwt-token?
		for(ZgwJwtTokenEntry entry : this.authorizations.values()) {			
			if (entry.jwtUrl == null) {
				if(jwtUrl != null) continue;
			}
			else {
				if(!entry.jwtUrl.equals(jwtUrl)) {
					continue;
				}
			}
			if(!entry.jwtIssuer.equals(jwtIssuer)) {
				continue;
			}
			if(!entry.jwtSecret.equals(jwtSecret)) {
				continue;
			}
			// bestond al, geven we terug
			log.debug("For url: " + url + " using: '" + entry.getAuthorization() +  "' from previous found entry");
			this.authorizations.put(url, entry);
			return;
		}
		
		// not already known, new jwt-token needed for this endpoint
		if(jwtUrl != null && jwtUrl.trim().length() > 0) {
			var authorizationRequestHeaders = new HttpHeaders();
	        String json =  "{\n" +
	            "    \"clientIds\": [\n" +
	            "        \"" + jwtClientIds + "\"\n" +
	            "    ],\n" +
	            "    \"secret\": \"" + jwtSecret +  "\",\n" +
	            "    \"label\": \"" + jwtIssuer +  "\",\n" +
	            "    \"heeftAlleAutorisaties\": \"true\",\n" +
	            "    \"autorisaties\": []\n" +
	            "}";
	
	        final RestTemplate restTemplate = new RestTemplate();
	        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
	        final HttpClient httpClient = HttpClientBuilder.create()
	            .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods, needed because VNG-cloud redirects with 307 -> 308 -> 200
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
	
	        log.debug("For url: " + url + " using: '" + zgwJwtTokenEntry.getAuthorization() +  "' from url:'" + jwtUrl + "' with requestjson:\n" + json);
	        zgwJwtTokenEntry.jwtUrl = jwtUrl;
	        zgwJwtTokenEntry.jwtIssuer = jwtIssuer;
	        zgwJwtTokenEntry.jwtSecret = jwtSecret;
	        this.authorizations.put(url, zgwJwtTokenEntry);
		}
		else {
			var token = JWTService.getJWT(jwtIssuer, jwtSecret);
			var zgwJwtTokenEntry = new ZgwJwtTokenEntry(jwtUrl, jwtIssuer, jwtSecret, jwtClientIds, "Bearer " + token);
			log.debug("For url: " + url + " using: '" + zgwJwtTokenEntry.getAuthorization() +  "' without using a jwtUrl");
			this.authorizations.put(url, zgwJwtTokenEntry);
		}
	}	
	
	
	public String getAuthorizationToken(String url) {
		for(String baseurl: this.authorizations.keySet()) {
			if(url.startsWith(baseurl)) {
				return authorizations.get(baseurl).getAuthorization();
			}
		}
		throw new ConverterException("No authorization defined for the url: " + url); 
	}

	public void setVersion(String url, ResponseEntity<String> responseEntity) {
        List<String> apiVersionHeader = responseEntity.getHeaders().get("API-version");
        String version = responseEntity.getHeaders().get("API-version").get(0);
        log.info(version);		
		
		for(String baseurl: this.authorizations.keySet()) {
			if(url.startsWith(baseurl)) {
				authorizations.get(baseurl).setVersion(version);
			}
		}
		throw new ConverterException("No authorization defined for the url: " + url); 
	}

	public String getVersion(String url) {
		for(String baseurl: this.authorizations.keySet()) {
			if(url.startsWith(baseurl)) {
				return authorizations.get(baseurl).getVersion();
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

	
	public class LRUCache<K, V> extends LinkedHashMap<K, V> {
	    private final int capacity;

	    public LRUCache(int capacity) {
	        // Pass true for accessOrder to the superclass to achieve LRU order.
	        // The default initial capacity (16) and default load factor (0.75) are used.
	        super(16, 0.75f, true);
	        this.capacity = capacity;
	    }

	    public boolean hasCapacity() {
	    	return this.capacity > super.size();
	    }
	    
	    @Override
	    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	        // Remove the eldest entry if the size exceeds the capacity of the cache.
	        return size() > capacity;
	    }
	    
	    @Override
	    public String toString() {
	        StringBuilder sb = new StringBuilder();
	        sb.append(String.format("\n\t\t[CACHE-LRU Cachecapacity=%d, currentSize=%d]\n", capacity, size()));
	        for (Map.Entry<K, V> entry : entrySet()) {
	            // sb.append(String.format("\t[%s] %s %s\n", entry.getKey(), entry.getValue().getClass().getSimpleName(), entry.getValue()));
	        	sb.append(String.format("\t\t\t%s\t%s\n", entry.getKey(), entry.getValue().getClass().getSimpleName()));
	        }
	        return sb.toString();
	    }
	}	
	private LRUCache<String, ZgwObject> cache = new LRUCache<>(1024);
	
	public void cacheAdd(List<? extends ZgwObject> zgwObjecten) {
	    for (ZgwObject zgwObject : zgwObjecten) {
	    	cacheAdd(zgwObject);
		}
	}		
	
	public void cacheAdd(ZgwObject zgwObject) {
		log.debug("\n\t\t[ADD-CACHE-ZGWOBJECT-ADD] '" + zgwObject.getClass().getSimpleName() + "' : " + getUuid(zgwObject.url));		
		
		if(!cache.hasCapacity()) {
			log.warn("Volle zgwobject-cache!(" + cache.size() + " objects)");
		}
		
	    // Directly cache the passed object
        cache.put(getUuid(zgwObject.url), zgwObject);
        // Recursively cache its members
        cacheMembers(zgwObject);
        
        log.debug(cache.toString());
    }
	
    private void cacheMembers(Object member)  {
         for (Field field : member.getClass().getDeclaredFields()) {
            field.setAccessible(true); // Make private fields accessible
            try {
	            Object fieldValue = field.get(member);
	            if(fieldValue == null) continue;
	
	            if (ZgwZrcExpand.class.isAssignableFrom(field.getType())) {
	            	cacheMembers(fieldValue);
	            }
	            else if (ZgwDrcExpand.class.isAssignableFrom(field.getType())) {
	            	cacheMembers(fieldValue);
	            }	            
	            else if (ZgwObject.class.isAssignableFrom(field.getType())) {
	                ZgwObject zgwObject = (ZgwObject) fieldValue;
	                log.debug("\n\t\t[MEMBER-CACHE-ZGWOBJECT-ADD] '" + field.getName() + "' : " + getUuid(zgwObject.url));
	                cache.put(getUuid(zgwObject.url), zgwObject);
	                cacheMembers(zgwObject);
	            } else if (Collection.class.isAssignableFrom(field.getType())) {
	                // If the field is a collection, process each element
	                Collection<?> collection = (Collection<?>) fieldValue;
	                for (Object item : collection) {
	                    if (item instanceof ZgwObject) {
	                        ZgwObject zgwObject = (ZgwObject) item;
	                        log.debug("\n\t\t[MEMBER-CACHE-COLLECTION-ADD] '" + field.getName() + "' : " + zgwObject.uuid);
	                        cache.put(getUuid(zgwObject.url), zgwObject);
	                        cacheMembers(zgwObject);
	                    }
	                }
	            } else if (field.getType().isArray()) {
	            	// If the field is an array, process each element
	            	int length = Array.getLength(fieldValue);
	            	for (int i = 0; i < length; i++) {
	            		Object item = Array.get(fieldValue, i);
	            		if (item instanceof ZgwObject) {
	            			ZgwObject zgwObject = (ZgwObject) item;
	            			log.debug("\n\t\t[MEMBER-CACHE-ARRAY-ADD] '" + field.getName() + "' : " + getUuid(zgwObject.url));
	            			cache.put(getUuid(zgwObject.url), zgwObject);
	            			cacheMembers(zgwObject);
	            		}
	            	}
	            }
	            else if (ZgwBetrokkeneIdentificatie.class.isAssignableFrom(field.getType())) {
	            	// datacontainers
	            }
	            	            
	            else if (field.getType().equals(java.lang.String.class) || field.getType().equals(java.util.Date.class) || field.getType().getName().equals("int")|| field.getType().getName().equals("boolean")) {
	            	// basictypes: no caching needed
	            }
	            else {
	            	log.debug("\n\t\t[MEMBER-CACHE-IGNORE] '" + field.getName() + "' : " + field.getType().getName());
	            }
            }
            catch(Exception e) {
            	log.warn("Error while adding object to cache:" + member, e);
            }
        }
    }	
    
    // Not everything has an uuid,
    // Everything does have an url
    public String getUuid(String url) {
    	if(url == null) {
    		return null;
    	}
		return url.substring(url.length() - 36);    	
    }
    
	public ZgwObject cacheGet(String url) {
		// also accept urls
		var uuid = getUuid(url);
		if(cache.get(uuid) != null) {
			log.debug("\n\t\t[GET-CACHE-FOUND]" + uuid + " (url: " + cache.get(uuid).url + " java-type:" + cache.get(uuid).getClass().getName() + ")");			
		}
		else {
			log.debug("\n\t\t[GET-CACHE-MISS]" + uuid);			
		}
		return cache.get(uuid);
	}
}