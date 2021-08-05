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
package nl.haarlem.translations.zdstozgw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.haarlem.translations.zdstozgw.translation.zgw.client.JWTService;

@RestController
public class JWTController {

	@Autowired
	private JWTService jwtService;

	@Value("${nl.haarlem.translations.zdstozgw.enableJWTEntpoint}")
	private boolean enabled;

	@GetMapping("/jwt")
	public String JWT() {
		String jwt = "This endpoint is dissabled. Enable by setting: nl.haarlem.transtlations.zdstozgw.enableJWTEntpoint = true";
		if (this.enabled) {
			jwt = this.jwtService.getJWT();
		}
		return jwt;
	}

}
