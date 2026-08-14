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

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Bewaart, puur in het geheugen van deze OpenZaakbrug-instantie, bij welke ZGW-pagina een StUF-ZKN
 * vervolgvraag voor geefZaakdetails-bij-bsn moet verdergaan. StUF's indicatorVervolgvraag is enkel een
 * boolean (geen volgnummer/offset in het protocol zelf, geverifieerd in stuf0301.xsd), dus deze state is
 * onvermijdelijk - bewust zo klein mogelijk gehouden en losgekoppeld van de audit-log
 * (RequestResponseCycle blijft puur een log, geen functionele afhankelijkheid hierop).
 *
 * Beperking, bewust geaccepteerd: alleen betrouwbaar binnen één draaiende instantie. Bij meerdere
 * OpenZaakbrug-replica's kan een vervolgvraag op een andere instantie belanden en valt terug op pagina 1 -
 * geen crash, nette degradatie (zie ZaakService.getZaakDetailsByBsn).
 */
@Component
public class ZakLv01PagingCache {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long ENTRY_TTL_MILLIS = 60 * 60 * 1000L; // 1 uur

	private static class PagingKey {
		final String zenderOrganisatie;
		final String zenderApplicatie;
		final String bsn;

		PagingKey(String zenderOrganisatie, String zenderApplicatie, String bsn) {
			this.zenderOrganisatie = zenderOrganisatie;
			this.zenderApplicatie = zenderApplicatie;
			this.bsn = bsn;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof PagingKey)) return false;
			PagingKey other = (PagingKey) o;
			return Objects.equals(zenderOrganisatie, other.zenderOrganisatie)
					&& Objects.equals(zenderApplicatie, other.zenderApplicatie)
					&& Objects.equals(bsn, other.bsn);
		}

		@Override
		public int hashCode() {
			return Objects.hash(zenderOrganisatie, zenderApplicatie, bsn);
		}

		@Override
		public String toString() {
			return zenderOrganisatie + "/" + zenderApplicatie + "/" + bsn;
		}
	}

	private static class PagingEntry {
		final int nextZgwPage;
		final long timestamp;

		PagingEntry(int nextZgwPage, long timestamp) {
			this.nextZgwPage = nextZgwPage;
			this.timestamp = timestamp;
		}
	}

	private final ConcurrentHashMap<PagingKey, PagingEntry> cache = new ConcurrentHashMap<>();

	/**
	 * @return de ZGW-pagina waar de vorige vraag stopte, of null als er geen (nog geldige) entry is - in
	 *         dat geval start de aanroeper gewoon opnieuw vanaf pagina 1, geen foutmelding.
	 */
	public Integer getNextPage(String zenderOrganisatie, String zenderApplicatie, String bsn) {
		var entry = cache.get(new PagingKey(zenderOrganisatie, zenderApplicatie, bsn));
		if (entry == null || isExpired(entry)) {
			return null;
		}
		return entry.nextZgwPage;
	}

	public void putNextPage(String zenderOrganisatie, String zenderApplicatie, String bsn, int nextZgwPage) {
		cache.put(new PagingKey(zenderOrganisatie, zenderApplicatie, bsn), new PagingEntry(nextZgwPage, System.currentTimeMillis()));
	}

	public void remove(String zenderOrganisatie, String zenderApplicatie, String bsn) {
		cache.remove(new PagingKey(zenderOrganisatie, zenderApplicatie, bsn));
	}

	private boolean isExpired(PagingEntry entry) {
		return System.currentTimeMillis() - entry.timestamp > ENTRY_TTL_MILLIS;
	}

	@Scheduled(fixedDelay = 10 * 60 * 1000L)
	public void cleanupExpiredEntries() {
		var before = cache.size();
		cache.entrySet().removeIf(e -> isExpired(e.getValue()));
		var removed = before - cache.size();
		if (removed > 0) {
			log.debug("ZakLv01PagingCache: " + removed + " verlopen entries opgeruimd (" + cache.size() + " resterend)");
		}
	}
}
