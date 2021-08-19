package nl.haarlem.translations.zdstozgw.config;

import java.io.InputStream;
import java.util.Properties;

public class ApplicationInformation {
	public String name;
	public String version;

	public static ApplicationInformation getApplicationInformation() {
	    try {
	        InputStream is = ApplicationInformation.class.getResourceAsStream("/META-INF/maven/nl.haarlem.translations/zds-to-zgw/pom.properties");
	        Properties p = new Properties();
	        p.load(is);
            var ai = new ApplicationInformation();
            ai.name = p.getProperty("m2e.projectName");
            ai.version = p.getProperty("version");
	        is.close();
	        return ai;
	    } catch (Exception e) { }

        Package aPackage = ApplicationInformation.class.getPackage();
        if (aPackage != null) {
        	var ai = new ApplicationInformation();
            ai.name = aPackage.getImplementationTitle();
            if (ai.name == null) {
            	ai.name = aPackage.getSpecificationTitle();
            }
        	ai.version = aPackage.getImplementationVersion();
            if (ai.version == null) {
            	ai.version = aPackage.getSpecificationVersion();
            }
            if(ai.name != null && ai.version != null) {
            	return ai;
            }
        }
	    //throw new RuntimeException("Version information could not be retrieved!");
    	var ai = new ApplicationInformation();
    	ai.name = "undefined";
    	ai.version = "undefined";
    	return ai;
    }	
}
