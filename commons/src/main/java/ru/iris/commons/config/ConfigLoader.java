package ru.iris.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.EventBus;
import ru.iris.commons.database.dao.ConfigDAO;
import ru.iris.models.database.Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfigLoader {

    private static Map<String, String> propertyMap = new ConcurrentHashMap<>();
    private final ConfigDAO configDAO;
    private final EventBus r;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ConfigLoader(EventBus r, ConfigDAO configDAO) {
        this.r = r;
        this.configDAO = configDAO;

        loadPropertiesFromDatabase();
    }

    /**
     * Loads properties from config directory by given name.
     *
     * @return true if load successfully.
     */
    public boolean loadPropertiesFormCfgDirectory(String name) {
        logger.debug("Start loading {} property from config directory", name);
        final Properties properties = new Properties();
        try (final InputStream stream = new FileInputStream("./config/" + name + ".properties")) {
            properties.load(stream);

            for (String key : properties.stringPropertyNames()) {
                set(key, properties.getProperty(key));
            }
            logger.debug("Properties {} loaded. Inserted {} keys", name, properties.size());
            stream.close();
        } catch (IOException | NullPointerException ex) {
            logger.error("Failed to load property {} from config directory: {}. Trying to load defaults from classpath...", name, ex.getLocalizedMessage());

            if (!loadPropertiesFormClasspath(name))
                return false;
        }

        return true;
    }

    /**
     * Loads properties from classpath by given name.
     *
     * @return true if load successfully.
     */
    public boolean loadPropertiesFormClasspath(String name) {
        logger.debug("Start loading {} property from classpath", name);
        final Properties properties = new Properties();
        try (final InputStream stream = getClass().getResourceAsStream(name)) {
            properties.load(stream);

            for (String key : properties.stringPropertyNames()) {
                set(key, properties.getProperty(key));
            }
            logger.debug("Properties {} loaded. Inserted {} keys", name, properties.size());
            stream.close();
        } catch (IOException | NullPointerException ex) {
            logger.error("Failed to load property {} from classpath: {}", name, ex.getLocalizedMessage());
            return false;
        }

        return true;
    }

    /**
     * Loads properties from database.
     *
     * @return true if load successfully.
     */
    private boolean loadPropertiesFromDatabase() {
        List<Config> dbcfg = (List<Config>) configDAO.findAll();

        for (Config line : dbcfg) {
            logger.info("Loading config for " + line.getParam());
            set(line.getParam(), line.getValue());
        }

        return true;
    }

    /**
     * @return the configuration property value.
     */
    public String get(String key) {
        if (propertyMap.get(key) != null) {
            return propertyMap.get(key);
        }

        logger.error("Configuration key = " + key + " not found!");
        return null;
    }

    public synchronized void set(String key, String value) {
        propertyMap.put(key, value);
        logger.info("Configuration key " + key + " = " + value);
    }
}
