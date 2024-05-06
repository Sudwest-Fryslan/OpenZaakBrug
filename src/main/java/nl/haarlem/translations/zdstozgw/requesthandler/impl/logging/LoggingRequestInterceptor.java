package nl.haarlem.translations.zdstozgw.requesthandler.impl.logging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import nl.haarlem.translations.zdstozgw.config.SpringContext;

public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		RequestResponseCycleService requestResponseCycleService = SpringContext.getBean(RequestResponseCycleService.class);
		ZgwRequestResponseCycle zgwRequestResponseCycle = new ZgwRequestResponseCycle();
		addRequestToDatabase(requestResponseCycleService, zgwRequestResponseCycle, request, body);
		ClientHttpResponse response = execution.execute(request, body);
		addResponseToDatabase(requestResponseCycleService, zgwRequestResponseCycle, response);
		return response;
	}

	private void addRequestToDatabase(RequestResponseCycleService requestResponseCycleService,
			ZgwRequestResponseCycle zgwRequestResponseCycle, HttpRequest request, byte[] body
			) throws UnsupportedEncodingException {
		String referentienummer = (String) RequestContextHolder.getRequestAttributes().getAttribute("referentienummer",
				RequestAttributes.SCOPE_REQUEST);
		zgwRequestResponseCycle.setRequest(referentienummer, request, body);
//		requestResponseCycleService.add(zgwRequestResponseCycle);
	}

	private void addResponseToDatabase(RequestResponseCycleService requestResponseCycleService,
			ZgwRequestResponseCycle zgwRequestResponseCycle, ClientHttpResponse response
			) throws IOException {
		zgwRequestResponseCycle.setResponse(response);
		requestResponseCycleService.add(zgwRequestResponseCycle);
	}
}