package org.threadmonitoring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.threadmonitoring.model.Config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

public class Configuration {

    public static List<String> monitoredPackages;

    public static void readConfiguration() {
        URI uri;
        try {
            uri = Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String jarDir = new File(uri).getParent();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            monitoredPackages = mapper.readValue(Paths.get(jarDir + "/../configuration/conf.yml").toFile(), Config.class).monitored;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
