package nl.haarlem.translations.zdstozgw.controller;

import nl.haarlem.translations.zdstozgw.config.ApplicationInformation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationInformationController {

    private final ApplicationInformation applicationInformation;

    public ApplicationInformationController(ApplicationInformation applicationInformation) {
        this.applicationInformation = applicationInformation;
    }

    @GetMapping("/info")
    public ApplicationInformation getApplicationInformation(){
        return applicationInformation;
    }
}
