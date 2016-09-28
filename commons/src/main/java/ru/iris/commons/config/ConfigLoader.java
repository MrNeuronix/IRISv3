package ru.iris.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.bus.EventBus;
import ru.iris.commons.database.dao.ConfigDAO;
import ru.iris.commons.database.model.Config;

import java.util.List;
import java.util.Map;

@Component
@Scope("singleton")
public class ConfigLoader {

	private final IrisConfig irisConfig;
	private final ConfigDAO configDAO;
	private final EventBus r;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static Map<String, String> propertyMap = null;

	@Autowired
	public ConfigLoader(EventBus r, IrisConfig irisConfig, ConfigDAO configDAO)
	{
		this.r = r;
		this.irisConfig = irisConfig;
		this.configDAO = configDAO;

			if (propertyMap != null)
			{
				return;
			}
			propertyMap = irisConfig.getConfig();

			logger.info("Loaded " + propertyMap.size() + " properties from file");

			loadPropertiesFromDatabase();
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
			propertyMap.put(line.getParam(), line.getValue());
		}

		return true;
	}

	/**
	 *
	 * @return the configuration property value.
	 */
	public String get(String key)
	{
		if (propertyMap.get(key) != null)
		{
			return propertyMap.get(key);
		}

		logger.error("Configuration key = " + key + " not found!");
		return null;
	}

	public void set(String key, String value) {
		propertyMap.put(key, value);
		logger.info("Configuration key " + key + " = " + value);
	}
}
