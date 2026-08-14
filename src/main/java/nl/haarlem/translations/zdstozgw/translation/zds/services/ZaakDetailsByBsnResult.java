/*
 * Copyright 2020-2021, 2026 The Open Zaakbrug Contributors
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
package nl.haarlem.translations.zdstozgw.translation.zds.services;

import java.util.List;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsZaak;

@Data
public class ZaakDetailsByBsnResult {
	public final List<ZdsZaak> zaken;
	public final boolean heeftVervolg;
	public final int aantalVoorkomens;
}
