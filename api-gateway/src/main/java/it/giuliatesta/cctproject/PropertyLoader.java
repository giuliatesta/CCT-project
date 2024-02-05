package it.giuliatesta.cctproject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class PropertyLoader {
    private static final Properties configs = new Properties();
    private final ArrayList<Route> routes = new ArrayList<>();

    private static final PropertyLoader instance = new PropertyLoader();

    public static PropertyLoader getInstance() {
        // if it's the first time, properties needs to be loaded
        if (configs.isEmpty()) {
            instance.load();
        }
        return instance;
    }

    private void load() {
        try {
            InputStream input = PropertyLoader.class.getClassLoader().getResourceAsStream("cct-config.properties");
            configs.load(input);
            castIntoRoutes();
            System.out.println("[PropertyLoader] loaded properties in 'cct-config.properties'");
        } catch (Exception e) {
            System.out.println("[Property Loader] Error while loading microservices properties");
            e.printStackTrace();

        }
    }

    public String get(String config) {
        var property = configs.getProperty(config);
        if (property != null) {
            property.trim();
        }
        return property;
    }

    // routes are defined in properies with three values
    // each key is defined as route.i.sourcePath
    // reads in configs until no other routes are defined
    private void castIntoRoutes() {
        int i = 0;
        boolean stop = false;
        do {
            String sourcePath = get("route." + i + ".sourcePath");
            String destinationPath = get("route." + i + ".destinationPath");
            String destinationHost = get("route." + i + ".destinationHost");
            if (sourcePath == null || destinationPath == null || destinationHost == null) {
                stop = true;
            } else {
                var route = new Route(sourcePath, destinationPath, destinationHost);
                routes.add(route);
                i++;
            }
        } while (!stop);
    }

    public Route getRoute(String sourcePath) {
        return routes.stream()
                .filter(route -> route.sourcePath.equals(sourcePath))
                .findFirst()
                .orElse(null);
    }

    public String getHosts(String host) {
        // remove the prefix "/" to search the service
        if (host.startsWith("/")) {
            host.substring(1);
        }
        return get("service." + host);
    }
}
