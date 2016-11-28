package org.bbop.termgenie.services.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by nathandunn on 11/22/16.
 */
public class ConfigurationHandler {

    private static ConfigurationHandler configurationHandler ;
    private Properties properties = new Properties();

    private ConfigurationHandler(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("termgenie.properties");
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigurationHandler getConfigurationHandler() {
        if(configurationHandler==null) configurationHandler = new ConfigurationHandler();
        return configurationHandler;
    }

    public String getValue(String key){
        return properties.getProperty(key);
    }
}
