package ru.iris;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.iris.commons.annotations.RunOnStartup;
import ru.iris.commons.config.JpaConfig;
import ru.iris.commons.config.ReactorConfig;
import ru.iris.commons.config.SchedulerConfig;
import ru.iris.commons.service.RunnableService;

import java.util.Map;

@SpringBootApplication
@Component
@Slf4j
public class CoreApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(new Class<?>[]{
                CoreApplication.class,
                JpaConfig.class,
                ReactorConfig.class,
                SchedulerConfig.class
        }, args);

        Map<String, Object> mapOfBeans = context.getBeansWithAnnotation(RunOnStartup.class);

        mapOfBeans
                .entrySet()
                .stream()
                .filter(entry -> !mapOfBeans.containsKey("scopedTarget."+entry.getKey()))
                .forEach(entry -> {
                    String name = entry.getKey();
                    RunnableService service = (RunnableService) entry.getValue();

                    if (service != null) {
                        try {
                            logger.info("Starting up {}", name);
                            service.run();
                        } catch (Exception e) {
                            logger.error("Error while starting up service {}", name, e);
                        }
                    }
                });
    }
}
