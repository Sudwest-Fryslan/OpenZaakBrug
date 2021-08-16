package nl.haarlem.translations.zdstozgw.config;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Properties;

@Data
@Component
public class ApplicationInformation {

    @Value("${info.app.name}")
    private String name;

    @Value("${info.app.version}")
	public String version;

}
