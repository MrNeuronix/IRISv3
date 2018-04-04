package ru.iris.facade.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.models.bus.service.ServiceEvent;
import ru.iris.models.status.ErrorStatus;
import ru.iris.models.status.OkStatus;

import java.util.Properties;

@RestController
@Profile("facade")
@Slf4j
public class SystemFacade {

    private final Properties properties = new Properties();
    @Autowired
    private DeviceRegistry registry;
    @Autowired
    private EventBus r;
    @Value("${git.commit.message.short}")
    private String commitMessage;
    @Value("${git.branch}")
    private String branch;
    @Value("${git.commit.id.describe-short}")
    private String commitId;
    @Value("${git.build.user.name}")
    private String username;
    @Value("${git.build.time}")
    private String buildtime;
    @Value("${git.build.version}")
    private String version;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig
                = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }

    @RequestMapping(value = "/api/system/info", method = RequestMethod.GET)
    public Object getSystemInfo() {
        properties.put("commitMessage", commitMessage);
        properties.put("branch", branch);
        properties.put("commitId", commitId);
        properties.put("buildUsername", username);
        properties.put("buildTime", buildtime);
        properties.put("version", version);
        return properties;
    }

    @RequestMapping(value = "/api/system/scripts/{state}", method = RequestMethod.GET)
    public Object systemRestart(@PathVariable String state) {
        switch (state.toLowerCase()) {
            case "on":
                r.notify("command.service", Event.wrap(ServiceEvent.builder()
                        .label("ServiceOn")
                        .identifier("event")
                        .build())
                );
                break;
            case "off":
                r.notify("command.service", Event.wrap(ServiceEvent.builder()
                        .label("ServiceOff")
                        .identifier("event")
                        .build())
                );
                break;
            default:
                return new ErrorStatus("Arguments unknown");
        }

        return new OkStatus("Script engine is " + state.toUpperCase());
    }
}
