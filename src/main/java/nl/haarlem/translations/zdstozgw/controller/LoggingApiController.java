package nl.haarlem.translations.zdstozgw.controller;

import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;

import nl.haarlem.translations.zdstozgw.requesthandler.impl.logging.RequestResponseCycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
public class LoggingApiController {

    private final RequestResponseCycleService requestResponseCycleService;

    public LoggingApiController(RequestResponseCycleService reqestResponseCycleService) {
        this.requestResponseCycleService = reqestResponseCycleService;
    }

    @GetMapping("/api/logs")
    public List<RequestResponseCycle> getLogs(@RequestParam String id) {
        return requestResponseCycleService.getById(id);
    }
}
