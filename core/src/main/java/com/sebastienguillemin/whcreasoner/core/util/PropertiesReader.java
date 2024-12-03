package com.sebastienguillemin.whcreasoner.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Class to read the properties from the "properties.yml" file.
 * 
 * THIS CLASS IS A SINGLETON.
 */
public class PropertiesReader {
    private static PropertiesReader propertiesReader;

    public static PropertiesReader getInstance() {

        if (PropertiesReader.propertiesReader == null)
            PropertiesReader.propertiesReader = new PropertiesReader();

        return PropertiesReader.propertiesReader;
    }

    private HashMap<String, Object> properties;

    private PropertiesReader() {
        try {
            this.loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @SuppressWarnings("unchecked")
    public String getPropertyValue(String propertyName) {
        String parts[] = propertyName.split("\\.");

        String value = "";
        Map<String, Object> nextNode = this.properties;
        Object next;
        for (int i = 0; i < parts.length; i ++) {
            next = nextNode.get(parts[i]);

            if (next == null) {
                return "";
            }
            else if (next instanceof Map)
                nextNode = (Map<String, Object>) next;
            else if (next instanceof String ) {
                if (i < parts.length - 1)
                    return "";
    
                value = (String) next;
            } else if (next instanceof Boolean)
                return ((boolean) next) ? "true" : "false";
        }

        return value;
    }

    public boolean getPropertyValueBoolean(String propertyName) {
        return Boolean.valueOf(this.getPropertyValue(propertyName));
    }

    private void loadProperties() throws IOException {
        Logger.logInfo("Loading properties.");
        
        try (InputStream in = new FileInputStream(new File(System.getProperty("user.dir") + "/core/properties.yml"))) {
            Yaml yaml = new Yaml();
            this.properties = yaml.load(in);
        }
        
        Logger.logInfo("Properties loaded.");
    }
}
