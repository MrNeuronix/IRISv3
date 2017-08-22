package ru.iris;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import ru.iris.commons.config.JpaConfig;
import ru.iris.commons.config.ReactorConfig;
import ru.iris.commons.service.ProtocolService;
import ru.iris.commons.service.Service;
import ru.iris.commons.service.Speak;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Component
@Slf4j
public class CoreApplication {

    @Autowired(required = false)
    private Speak speak;
    @Autowired(required = false)
    @Qualifier("events")
    private Service events;
    @Autowired(required = false)
    @Qualifier("zwave")
    private ProtocolService zwave;
    @Autowired(required = false)
    @Qualifier("nooliterx")
    private ProtocolService nooliteRx;
    @Autowired(required = false)
    @Qualifier("noolitetx")
    private ProtocolService nooliteTx;
    @Autowired(required = false)
    @Qualifier("xiaomi")
    private ProtocolService xiaomi;
    @Autowired(required = false)
    @Qualifier("httpapi")
    private ProtocolService httpapi;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(new Class<?>[]{
                CoreApplication.class,
                JpaConfig.class,
                ReactorConfig.class
        }, args);

    }

    @PostConstruct
    private void init() throws Exception {
        if (speak != null)
            speak.run();
        if (events != null)
            events.run();
        if (zwave != null)
            zwave.run();
        if (nooliteRx != null)
            nooliteRx.run();
        if (nooliteTx != null)
            nooliteTx.run();
        if (xiaomi != null)
            xiaomi.run();
        if (httpapi != null)
            httpapi.run();
    }
}
