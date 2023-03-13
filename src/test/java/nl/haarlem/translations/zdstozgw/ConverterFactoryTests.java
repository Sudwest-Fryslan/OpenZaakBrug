package nl.haarlem.translations.zdstozgw;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import nl.haarlem.translations.zdstozgw.config.ConfigService;
import nl.haarlem.translations.zdstozgw.config.model.Translation;
import nl.haarlem.translations.zdstozgw.converter.Converter;
import nl.haarlem.translations.zdstozgw.converter.ConverterFactory;
import nl.haarlem.translations.zdstozgw.requesthandler.RequestResponseCycle;

@RunWith(SpringRunner.class)
public class ConverterFactoryTests {

    ConverterFactory converterFactory;

    @Mock
    ConfigService configService;

    @Before
    public void setup() {
        this.converterFactory = new ConverterFactory(configService, null);
    }

    @Ignore
    @Test
    public void getConverter_shouldInitiateCorrectConverter() throws Exception {
        //assign
        String soapAction = "http://www.egem.nl/StUF/sector/zkn/0310/voegZaakdocumentToe_Lk01";
        String implementation = "nl.haarlem.translations.zdstozgw.converter.impl.VoegZaakdocumentToeConverter";
        String content = IOUtils.toString(getClass()
                .getClassLoader()
                .getResourceAsStream("zds1.1/VoegZaakDocumentToe"), "UTF-8");

        Translation translation = new Translation()
                .setSoapaction(soapAction)
                .setImplementation(implementation);
        doReturn(translation).when(configService).getTranslationByPathAndSoapAction(any(), any());

        //act
        var context = new RequestResponseCycle("", "", "", "", "", soapAction, content, null);
        Converter converter = converterFactory.getConverter(context);

        //assert
        Assert.assertTrue(converter != null);
        Assert.assertEquals(implementation, converter.getClass().getCanonicalName());
    }

}
