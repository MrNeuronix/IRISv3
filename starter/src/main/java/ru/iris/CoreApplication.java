package ru.iris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import ru.iris.commons.config.JpaConfig;
import ru.iris.commons.config.ReactorConfig;
import ru.iris.commons.service.Service;
import ru.iris.commons.service.Speak;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableAsync
@Component
public class CoreApplication {

	@Autowired
	private CoreApplication core;

	@Autowired(required = false)
	private Speak speak;

	@Autowired(required = false)
	@Qualifier("zwave")
	private Service zwave;

	private static final Logger logger = LoggerFactory.getLogger(CoreApplication.class);

	public static void main(String[] args) throws Exception {

		ConfigurableApplicationContext context = SpringApplication.run(new Class<?>[] {
				CoreApplication.class,
				JpaConfig.class,
				ReactorConfig.class
		}, args);

	}

	@PostConstruct
	private void init() throws Exception {
		if(speak != null)
			speak.run();
		if(zwave != null)
			zwave.run();
	}
}
