package ru.iris;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.iris.commons.bus.config.ReactorConfig;
import ru.iris.commons.config.JpaConfig;
import ru.iris.commons.service.Service;

@SpringBootApplication
public class CoreApplication {

	@Autowired
	private Service speakController;

	public static void main(String[] args) {

		SpringApplication.run(new Class<?>[] {
				CoreApplication.class,
				JpaConfig.class,
				ReactorConfig.class
		}, args);
	}
}
