/*
 * Copyright 2020-2023 The Open Zaakbrug Contributors
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import lombok.Data;

@Data
@Entity
@Table(indexes = @Index(columnList = "referentienummer"))
public class ZgwRequestResponseCycle {
	@Id
	@GeneratedValue
	private long id;
	private String referentienummer;

	private LocalDateTime startdatetime;
	private LocalDateTime stopdatetime;
	private Long durationInMilliseconds;

	private String zgwMethod;
	@Column(columnDefinition = "TEXT")
	private String zgwUrl;
	@Column(columnDefinition = "TEXT", name = "zgw_request_body")
	private String zgwShortenedRequestBody;
	private Integer zgwRequestSize;

	private int zgwResponseCode;
	@Column(columnDefinition = "TEXT", name = "zgw_response_body")
	private String zgwShortenedResponseBody;
	private Integer zgwResponseSize;

	public ZgwRequestResponseCycle() {
		startdatetime = LocalDateTime.now();
	};

	public void setRequest(String referentienummer, HttpRequest request, byte[] body)
			throws UnsupportedEncodingException {
		this.referentienummer = referentienummer;
		this.zgwMethod = request.getMethodValue();
		this.zgwUrl = request.getURI().toString();

		var message = new String(body, "UTF-8");
		this.zgwRequestSize = message.length();
		this.zgwShortenedRequestBody = message;

		startdatetime = LocalDateTime.now();
	}

	public long getDurationInMilliseconds() {
		var milliseconds = Duration.between(startdatetime, LocalDateTime.now()).toMillis();
		return milliseconds;
	}

	public void setResponse(ClientHttpResponse response) throws UnsupportedEncodingException, IOException {
		this.zgwResponseCode = response.getStatusCode().value();

		StringBuilder inputStringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
		String line = bufferedReader.readLine();
		while (line != null) {
			line = line.replaceAll("\u0000", "");
			inputStringBuilder.append(line);
			inputStringBuilder.append('\n');
			line = bufferedReader.readLine();
		}
		var message = inputStringBuilder.toString();
		this.zgwResponseSize = message.length();
		this.zgwShortenedResponseBody = message;

		this.stopdatetime = LocalDateTime.now();
		this.durationInMilliseconds = Duration.between(startdatetime, stopdatetime).toMillis();
	}
}