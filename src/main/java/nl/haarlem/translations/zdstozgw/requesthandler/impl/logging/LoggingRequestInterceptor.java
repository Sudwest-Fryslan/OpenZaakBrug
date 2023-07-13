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

	private RequestResponseCycleService requestResponseCycleService;
	private ZgwRequestResponseCycle currentInterimRequestResponseCycle;
//	private String referentienummer;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		this.requestResponseCycleService = SpringContext.getBean(RequestResponseCycleService.class);
		addRequestToDatabase(request, body);
		ClientHttpResponse response = execution.execute(request, body);
		addResponseToDatabase(response);
		return response;
	}

	private void addRequestToDatabase(HttpRequest request, byte[] body) throws UnsupportedEncodingException {
		String referentienummer = (String) RequestContextHolder.getRequestAttributes().getAttribute("referentienummer",
				RequestAttributes.SCOPE_REQUEST);
		this.currentInterimRequestResponseCycle = new ZgwRequestResponseCycle(referentienummer, request, body);
		//this.requestResponseCycleService.add(this.currentInterimRequestResponseCycle);
	}

	private void addResponseToDatabase(ClientHttpResponse response) throws IOException {
		// Added to prevent us from:
		//		org.springframework.orm.jpa.JpaSystemException: identifier of an instance of 
		//		nl.haarlem.translations.zdstozgw.requesthandler.impl.logging.ZgwRequestResponseCycle 
		//		was altered from 133274 to 133275; nested exception is org.hibernate.HibernateException: 
		//		identifier of an instance of nl.haarlem.translations.zdstozgw.requesthandler.impl.logging.ZgwRequestResponseCycle 
		//		was altered from 133274 to 133275
		//ZgwRequestResponseCycle existingRecordRef = this.requestResponseCycleService
		//		.getZgwRequestResponseCycleRepository()
		//		.findById(this.currentInterimRequestResponseCycle.getId())
		//		.orElse(this.currentInterimRequestResponseCycle);
		currentInterimRequestResponseCycle.setResponse(response);
		this.requestResponseCycleService.add(currentInterimRequestResponseCycle);
	}
}