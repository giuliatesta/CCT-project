package it.giuliatesta.cctproject;

import java.io.InputStream;
import java.util.Properties;

public class CctConfiguration {
    private static final Properties configs = new Properties();

    private static final CctConfiguration instance = new CctConfiguration();

    static {
        instance.load();
    }

    static CctConfiguration getInstance() {
        return instance;
    }

    private void load() {
        try {
            InputStream input = CctConfiguration.class.getClassLoader().getResourceAsStream("cct-config.properties");
            configs.load(input);
        } catch (Exception e) {
            System.out.println("Error while loading microservices properties");
            e.printStackTrace();

        }
    }

    public String get(String config) {
        return configs.getProperty(config);
    }
}
