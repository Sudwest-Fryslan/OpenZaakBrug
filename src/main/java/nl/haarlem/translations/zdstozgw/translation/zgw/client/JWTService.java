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

import java.time.ZoneOffset;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.hmac.HMACSigner;

public class JWTService {

	public static String getJWT(String issuer, String secret) {
		Signer signer = HMACSigner.newSHA256Signer(secret);

		io.fusionauth.jwt.domain.JWT jwt = new io.fusionauth.jwt.domain.JWT().setIssuer(issuer)
				.setIssuedAt(now(ZoneOffset.UTC)).addClaim("client_id", issuer).addClaim("user_id", issuer)
				.addClaim("user_representation", issuer).setExpiration(now(ZoneOffset.UTC).plusMinutes(10));

		return io.fusionauth.jwt.domain.JWT.getEncoder().encode(jwt, signer);
	}
}