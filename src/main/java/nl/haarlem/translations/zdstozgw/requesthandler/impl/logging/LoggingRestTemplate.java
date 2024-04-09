package nl.haarlem.translations.zdstozgw.requesthandler.impl.logging;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class LoggingRestTemplate implements RestTemplateCustomizer {

	public LoggingRestTemplate() {
	}

	@Override
	public void customize(RestTemplate restTemplate) {
		restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(this.getAllCertsTrustingRequestFactory()));
		restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
	}

	private HttpComponentsClientHttpRequestFactory getAllCertsTrustingRequestFactory() {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = null;
		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
					.build();
		} catch (Exception ex) {
		}

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return requestFactory;
	}
}
