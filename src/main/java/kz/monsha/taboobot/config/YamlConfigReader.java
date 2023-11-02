package kz.monsha.taboobot.config;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.InputStream;
import java.util.Map;

public class YamlConfigReader {

    private Map<String, Object> properties;

    public YamlConfigReader(String fileName) {
        Yaml yaml = new Yaml(new Constructor(Map.class));
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }
            properties = yaml.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file", e);
        }



    }

    public Object getValue(String key) {
        return properties.get(key);
    }

    // You can add more methods to navigate through the nested structures
    // as per your requirement
}
