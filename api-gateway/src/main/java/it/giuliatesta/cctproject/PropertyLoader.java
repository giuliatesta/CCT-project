package it.giuliatesta.cctproject;

import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
    private static final Properties configs = new Properties();

    private static final PropertyLoader instance = new PropertyLoader();

    static {
        instance.load();
    }

    static PropertyLoader getInstance() {
        return instance;
    }

    private void load() {
        try {
            InputStream input = PropertyLoader.class.getClassLoader().getResourceAsStream("cct-config.properties");
            configs.load(input);
        } catch (Exception e) {
            System.out.println("[Property Loader] Error while loading microservices properties");
            e.printStackTrace();

        }
    }

    public String get(String config) {
        return configs.getProperty(config);
    }
}
