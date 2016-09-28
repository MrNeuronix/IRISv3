package ru.iris.commons.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "iris")
public class IrisConfig {

	private final Map<String, String> config = new HashMap<>();

	public Map<String, String> getConfig() {
		return config;
	}

}
