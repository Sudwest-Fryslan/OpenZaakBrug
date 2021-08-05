package nl.haarlem.translations.zdstozgw.requesthandler.impl.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;

@Service
@Data
public class RequestResponseCycleService {

	private final RequestResponseCycleRepository requestResponseCycleRepository;
	private final ZdsRequestResponseCycleRepository zdsRequestResponseCycleRepository;

	@Autowired
	public RequestResponseCycleService(RequestResponseCycleRepository requestResponseCycleRepository,
			ZdsRequestResponseCycleRepository zdsRequestResponseCycleRepository) {
		this.requestResponseCycleRepository = requestResponseCycleRepository;
		this.zdsRequestResponseCycleRepository = zdsRequestResponseCycleRepository;
	}

	public RequestResponseCycle save(RequestResponseCycle requestResponseCycle) {
		return this.requestResponseCycleRepository.save(requestResponseCycle);
	}

	public ZdsRequestResponseCycle add(ZdsRequestResponseCycle interimRequestResponseCycle) {
		return this.zdsRequestResponseCycleRepository.save(interimRequestResponseCycle);
	}
}
