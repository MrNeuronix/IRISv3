package ru.iris;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.iris.commons.bus.config.ReactorConfig;
import ru.iris.commons.config.JpaConfig;
import ru.iris.commons.service.Service;
import ru.iris.commons.service.Speak;

@SpringBootApplication
public class CoreApplication {

	@Autowired
	private Speak speak;

	@Autowired
	@Qualifier("zwave")
	private Service zwave;

	public static void main(String[] args) {

		SpringApplication.run(new Class<?>[] {
				CoreApplication.class,
				JpaConfig.class,
				ReactorConfig.class
		}, args);
	}
}
