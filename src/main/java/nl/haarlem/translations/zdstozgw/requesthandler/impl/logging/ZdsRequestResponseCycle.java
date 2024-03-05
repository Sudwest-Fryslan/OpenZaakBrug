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
package nl.haarlem.translations.zdstozgw.requesthandler.impl.logging;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.http.ResponseEntity;

import lombok.Data;

@Data
@Entity
@Table(indexes = @Index(columnList = "referentienummer"))
public class ZdsRequestResponseCycle {
	@Id
	@GeneratedValue
	private long id;
	private String referentienummer;

	private LocalDateTime startdatetime;
	private LocalDateTime stopdatetime;
	private Long durationInMilliseconds;

	private String zdsUrl;
	private String zdsSoapAction;

	@Column(columnDefinition = "TEXT", name = "zds_request_body")
	private String zdsShortenedRequestBody;
	private Integer zdsRequestSize;

	private int zdsResponseCode;

	@Column(columnDefinition = "TEXT", name = "zds_response_body")
	private String zdsShortenedResponseBody;
	private Integer zdsResponseSize;

	public ZdsRequestResponseCycle() {
		startdatetime = LocalDateTime.now();
	};

	public ZdsRequestResponseCycle(String zdsUrl, String zdsSoapAction, String zdsRequestBody,
			String referentienummer) {
		this.zdsUrl = zdsUrl;
		this.zdsSoapAction = zdsSoapAction;

		this.zdsRequestSize = zdsRequestBody.length();
		this.zdsShortenedRequestBody = zdsRequestBody;// StringUtils.shortenLongString(zdsRequestBody,
														// StringUtils.MAX_MESSAGE_SIZE);

		this.referentienummer = referentienummer;
		startdatetime = LocalDateTime.now();
	}

	public long getDurationInMilliseconds() {
		var milliseconds = Duration.between(startdatetime, LocalDateTime.now()).toMillis();
		return milliseconds;
	}

	public void setResponse(ResponseEntity<?> response) {
		this.zdsResponseCode = response.getStatusCodeValue();

		var message = response.getBody().toString();
		this.zdsResponseSize = message.length();
		this.zdsShortenedResponseBody = message;// StringUtils.shortenLongString(message, StringUtils.MAX_MESSAGE_SIZE);

		this.stopdatetime = LocalDateTime.now();
		this.durationInMilliseconds = Duration.between(startdatetime, stopdatetime).toMillis();
	}
}