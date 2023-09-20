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

import static java.time.ZonedDateTime.now;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.hmac.HMACSigner;

@Service
public class JWTService {	
	@Value("${openzaak.jwt.url}")
	private String jwturl;

	@Value("${openzaak.jwt.issuer}")
	private String issuer;	
	
	@Value("${openzaak.jwt.secret}")
	private String secret;

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	public String getJWT() {
		try {			
	        URL obj = new URL(jwturl);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

	        con.setRequestMethod("POST");
	        con.setRequestProperty("Content-Type", "application/json");
	        con.setDoOutput(true);

	        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			//String json = "{\"clientIds'\":[\"" + issuer +  "\"],\"secret\": \"" + issuer + "\",\"label\": \"user\"heeftAlleAutorisaties': 'true','autorisaties': []}";
	        String json =  "{\"clientIds\": [\"" + issuer + "\"],\"secret\": \"" + secret + "\" ,\"label\": \"user\",\"heeftAlleAutorisaties\": \"true\",\"autorisaties\": []}";
	        log.info("Sending to: " + jwturl + " the following payload:"  + json);
	        wr.writeBytes(json);
	        wr.flush();
	        wr.close();

	        int responseCode = con.getResponseCode();
	        if(responseCode != 200) {
		        log.info("Response Code: " + responseCode + " Errormessage:" + con.getResponseMessage());
	        	throw new RuntimeException("Error retrieving token:" + con.getResponseMessage());
	        }

	        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String inputLine;
	        StringBuffer response = new StringBuffer();

	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        in.close();

		    log.info("Retrieved jwt-token:'" + response.toString() + "' from: " + jwturl);
		    return response.toString();
		}
		catch(Exception ex) {
			String message = "Could not get an jwt token from:" + jwturl;
			log.warn(message, ex);			
			throw new RuntimeException(message);
		}
	}
}
