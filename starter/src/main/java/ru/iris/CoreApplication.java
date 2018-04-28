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

@SpringBootApplication
@Component
@Slf4j
public class CoreApplication {

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        CoreApplication application = new CoreApplication();
        application.start(args);
    }

    private void start(String[] args) {
        SpringApplication.run(new Class<?>[]{
                CoreApplication.class,
                JpaConfig.class,
                ReactorConfig.class,
                SchedulerConfig.class
        }, args);

        context.getBeansWithAnnotation(RunOnStartup.class).forEach((name, bean) -> {
            RunnableService service = (RunnableService) bean;

            if (service != null) {
                try {
                    service.run();
                } catch (Exception e) {
                    logger.error("Error while starting up service {}", name, e);
                }
            }
        });
    }
}
